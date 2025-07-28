package com.proyecto.microcuentas.controller;

import com.proyecto.microcuentas.dto.MovimientoDTO;
import com.proyecto.microcuentas.dto.CuentaDTO;
import com.proyecto.microcuentas.dto.ClienteDTO;
import com.proyecto.microcuentas.service.MovimientoService;
import com.proyecto.microcuentas.service.CuentaService;
import com.proyecto.microcuentas.client.ClienteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.modelmapper.ModelMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

@RestController
@RequestMapping("/reportes")
public class ReporteController {
    @Autowired
    private MovimientoService movimientoService;
    
    @Autowired
    private CuentaService cuentaService;
    
    @Autowired
    private ClienteClient clienteClient;
    
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/estado-cuenta/{numeroCuenta}")
    public ResponseEntity<?> estadoCuenta(@PathVariable String numeroCuenta) {
        try {
            var cuenta = cuentaService.obtenerCuentaPorId(numeroCuenta)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
            
            var movimientos = movimientoService.reportePorCliente(numeroCuenta);
            
            // Obtener información del cliente
            ClienteDTO cliente = clienteClient.obtenerClientePorCuenta(numeroCuenta);
            
            var reporte = new EstadoCuentaReporte();
            reporte.setCuenta(modelMapper.map(cuenta, CuentaDTO.class));
            reporte.setCliente(cliente);
            reporte.setMovimientos(movimientos.stream()
                .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
                .collect(Collectors.toList()));
            
            return ResponseEntity.ok(reporte);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al generar el estado de cuenta: " + e.getMessage());
        }
    }

    @GetMapping("/movimientos")
    public ResponseEntity<?> reporteMovimientos(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin) {
        try {
            if (fechaInicio.isAfter(fechaFin)) {
                return ResponseEntity.badRequest().body("La fecha de inicio debe ser anterior a la fecha fin");
            }

            var movimientos = movimientoService.reportePorFecha(fechaInicio, fechaFin);
            var movimientosDTO = movimientos.stream()
                .map(mov -> {
                    MovimientoDTO dto = modelMapper.map(mov, MovimientoDTO.class);
                    try {
                        ClienteDTO cliente = clienteClient.obtenerClientePorCuenta(mov.getNumeroCuenta());
                        dto.setCliente(cliente);
                    } catch (Exception e) {
                        // Si no se puede obtener el cliente, continuamos sin esa información
                        dto.setCliente(null);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(movimientosDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al generar el reporte de movimientos: " + e.getMessage());
        }
    }
}

@Data
class EstadoCuentaReporte {
    private CuentaDTO cuenta;
    private ClienteDTO cliente;
    private List<MovimientoDTO> movimientos;
} 