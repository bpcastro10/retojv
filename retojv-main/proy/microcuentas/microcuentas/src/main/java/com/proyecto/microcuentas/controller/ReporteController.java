package com.proyecto.microcuentas.controller;

import com.proyecto.microcuentas.dto.MovimientoDTO;
import com.proyecto.microcuentas.dto.CuentaDTO;
import com.proyecto.microcuentas.dto.ClienteDTO;
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
        
        // Por ahora, no obtenemos información del cliente ya que no hay relación directa
        // En el futuro, se podría agregar un campo clienteId en la entidad Cuenta
        ClienteDTO cliente = null;
        
        var reporte = new EstadoCuentaReporte();
        reporte.setCuenta(modelMapper.map(cuenta, CuentaDTO.class));
        reporte.setCliente(cliente);
        reporte.setMovimientos(movimientos.stream()
            .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
            .collect(Collectors.toList()));
        
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoDTO>> reporteMovimientos(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin) {
        log.info("Generando reporte de movimientos entre {} y {}", fechaInicio, fechaFin);
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha fin");
        }

        var movimientos = movimientoService.reportePorFecha(fechaInicio, fechaFin);
        var movimientosDTO = movimientos.stream()
            .map(mov -> {
                MovimientoDTO dto = modelMapper.map(mov, MovimientoDTO.class);
                // Por ahora, no obtenemos información del cliente
                dto.setCliente(null);
                return dto;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(movimientosDTO);
    }

    @GetMapping("/cliente/{identificacion}")
    public ResponseEntity<ClienteDTO> obtenerCliente(@PathVariable String identificacion) {
        log.info("Obteniendo información del cliente con identificación: {}", identificacion);
        
        try {
            ClienteDTO cliente = clienteClient.obtenerClientePorIdentificacion(identificacion);
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            log.error("Error al obtener cliente con identificación: {}", identificacion, e);
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