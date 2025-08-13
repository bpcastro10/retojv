package com.proyecto.microcuentas.controller;

import com.proyecto.microcuentas.dto.MovimientoDTO;
import com.proyecto.microcuentas.dto.CuentaDTO;
import com.proyecto.microcuentas.dto.ClienteDTO;
import com.proyecto.microcuentas.entity.Movimiento;
import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.service.MovimientoService;
import com.proyecto.microcuentas.service.CuentaService;
import com.proyecto.microcuentas.client.ClienteClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;

import org.modelmapper.ModelMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReporteController {
    
    private final MovimientoService movimientoService;
    private final CuentaService cuentaService;
    private final ClienteClient clienteClient;
    private final ModelMapper modelMapper;

    @GetMapping("/estado-cuenta/{numeroCuenta}")
    public ResponseEntity<EstadoCuentaReporte> estadoCuenta(@PathVariable String numeroCuenta) {
        log.info("Generando estado de cuenta para: {}", numeroCuenta);
        
        var cuenta = cuentaService.obtenerCuentaPorId(numeroCuenta)
            .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        
        var movimientos = movimientoService.reportePorCliente(numeroCuenta);
        
        // Obtener información completa del cliente usando WebFlux
        ClienteDTO cliente = null;
        try {
            cliente = clienteClient.obtenerClientePorIdentificacion(cuenta.getClienteId());
            log.info("Información del cliente {} obtenida para estado de cuenta", 
                     cliente != null ? cliente.getNombre() : "null");
        } catch (Exception e) {
            log.error("Error al obtener cliente {} para estado de cuenta: {}", 
                      cuenta.getClienteId(), e.getMessage());
        }
        
        var reporte = new EstadoCuentaReporte();
        reporte.setCuenta(modelMapper.map(cuenta, CuentaDTO.class));
        reporte.setCliente(cliente);
        
        List<MovimientoDTO> movimientosDTO = movimientos.stream()
            .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
            .collect(Collectors.toList());
        reporte.setMovimientos(movimientosDTO);
        
        return ResponseEntity.ok(reporte);
    }

    /**
     * Endpoint de reportes según especificación exacta: /reportes?fecha=rango fechas
     * Formato fecha: 2024-01-01T00:00:00,2024-12-31T23:59:59
     */
    @GetMapping
    public ResponseEntity<List<EstadoCuentaReporte>> reportes(@RequestParam String fecha) {
        log.info("Generando reporte con rango de fechas: {}", fecha);
        
        // Parsear el rango de fechas
        String[] fechas = fecha.split(",");
        if (fechas.length != 2) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use: fechaInicio,fechaFin");
        }
        
        LocalDateTime fechaInicio = LocalDateTime.parse(fechas[0]);
        LocalDateTime fechaFin = LocalDateTime.parse(fechas[1]);
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha fin");
        }

        // Obtener movimientos en el rango de fechas
        var movimientos = movimientoService.reportePorFecha(fechaInicio, fechaFin);
        
        // Agrupar por cuenta y generar reportes de estado de cuenta
        var reportesPorCuenta = movimientos.stream()
            .collect(Collectors.groupingBy(mov -> mov.getCuenta().getNumeroCuenta()))
            .entrySet().stream()
            .map(entry -> {
                String numeroCuenta = entry.getKey();
                List<Movimiento> movimientosCuenta = entry.getValue();
                
                // Obtener información de la cuenta
                var cuenta = cuentaService.obtenerCuentaPorId(numeroCuenta)
                    .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
                
                // Obtener información completa del cliente usando WebFlux
                ClienteDTO cliente = null;
                try {
                    cliente = clienteClient.obtenerClientePorIdentificacion(cuenta.getClienteId());
                    log.info("Cliente {} obtenido para reporte de cuenta {}", 
                             cliente != null ? cliente.getNombre() : "null", numeroCuenta);
                } catch (Exception e) {
                    log.error("Error al obtener información del cliente {} para cuenta {}: {}", 
                              cuenta.getClienteId(), numeroCuenta, e.getMessage());
                    // Continúa sin cliente para no fallar el reporte completo
                }
                
                // Crear reporte de estado de cuenta
                var reporte = new EstadoCuentaReporte();
                reporte.setCuenta(modelMapper.map(cuenta, CuentaDTO.class));
                reporte.setCliente(cliente);
                
                List<MovimientoDTO> movimientosDTO = movimientosCuenta.stream()
                    .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
                    .collect(Collectors.toList());
                reporte.setMovimientos(movimientosDTO);
                
                return reporte;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(reportesPorCuenta);
    }
    
    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoDTO>> obtenerTodosLosMovimientos() {
        log.info("Obteniendo todos los movimientos");
        
        try {
            List<Movimiento> movimientos = movimientoService.obtenerTodos();
            
            List<MovimientoDTO> movimientosDTO = movimientos.stream()
                .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
                .collect(Collectors.toList());
            
            log.info("Se encontraron {} movimientos", movimientosDTO.size());
            return ResponseEntity.ok(movimientosDTO);
            
        } catch (Exception e) {
            log.error("Error al obtener movimientos: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/movimientos/cuenta/{numeroCuenta}")
    public ResponseEntity<List<MovimientoDTO>> obtenerMovimientosPorCuenta(@PathVariable String numeroCuenta) {
        log.info("Obteniendo movimientos para cuenta: {}", numeroCuenta);
        
        try {
            List<Movimiento> movimientos = movimientoService.reportePorCliente(numeroCuenta);
            
            List<MovimientoDTO> movimientosDTO = movimientos.stream()
                .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
                .collect(Collectors.toList());
            
            log.info("Se encontraron {} movimientos para la cuenta {}", movimientosDTO.size(), numeroCuenta);
            return ResponseEntity.ok(movimientosDTO);
            
        } catch (Exception e) {
            log.error("Error al obtener movimientos para cuenta {}: {}", numeroCuenta, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener todas las cuentas y movimientos de un cliente específico
     */
    @GetMapping("/cliente/{identificacion}/cuentas")
    public ResponseEntity<List<EstadoCuentaReporte>> reporteCompletoPorCliente(@PathVariable String identificacion) {
        log.info("Generando reporte completo para cliente: {}", identificacion);
        
        try {
            // 1. Validar que el cliente existe
            ClienteDTO cliente = clienteClient.obtenerClientePorIdentificacion(identificacion);
            if (cliente == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 2. Obtener todas las cuentas del cliente
            List<Cuenta> cuentasCliente = cuentaService.obtenerCuentasPorCliente(identificacion);
            
            if (cuentasCliente.isEmpty()) {
                log.info("Cliente {} no tiene cuentas registradas", identificacion);
                return ResponseEntity.ok(List.of());
            }
            
            // 3. Generar reporte para cada cuenta
            List<EstadoCuentaReporte> reportes = cuentasCliente.stream()
                .map(cuenta -> {
                    var movimientos = movimientoService.reportePorCliente(cuenta.getNumeroCuenta());
                    
                    var reporte = new EstadoCuentaReporte();
                    reporte.setCuenta(modelMapper.map(cuenta, CuentaDTO.class));
                    reporte.setCliente(cliente);
                    
                    List<MovimientoDTO> movimientosDTO = movimientos.stream()
                        .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
                        .collect(Collectors.toList());
                    reporte.setMovimientos(movimientosDTO);
                    
                    return reporte;
                })
                .collect(Collectors.toList());
            
            log.info("Reporte generado para cliente {}: {} cuentas encontradas", 
                     cliente.getNombre(), reportes.size());
            
            return ResponseEntity.ok(reportes);
            
        } catch (Exception e) {
            log.error("Error al generar reporte para cliente {}: {}", identificacion, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}

@Data
class EstadoCuentaReporte {
    private CuentaDTO cuenta;
    private ClienteDTO cliente;
    private List<MovimientoDTO> movimientos;
}