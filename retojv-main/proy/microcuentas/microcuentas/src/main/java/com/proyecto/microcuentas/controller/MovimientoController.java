package com.proyecto.microcuentas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import com.proyecto.microcuentas.dto.MovimientoDTO;

import com.proyecto.microcuentas.dto.CrearMovimientoDTO;
import com.proyecto.microcuentas.entity.Movimiento;
import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.service.MovimientoService;
import com.proyecto.microcuentas.service.CuentaService;
import org.modelmapper.ModelMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MovimientoController {
    
    private final MovimientoService movimientoService;
    private final CuentaService cuentaService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<MovimientoDTO> crear(@Valid @RequestBody CrearMovimientoDTO crearMovimientoDTO) {
        log.info("Creando nuevo movimiento para cuenta: {}", crearMovimientoDTO.getNumeroCuenta());
        
        // Obtener la cuenta
        Cuenta cuenta = cuentaService.obtenerCuentaPorId(crearMovimientoDTO.getNumeroCuenta())
            .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + crearMovimientoDTO.getNumeroCuenta()));
        
        // Crear el movimiento
        Movimiento movimiento = Movimiento.builder()
            .tipoMovimiento(crearMovimientoDTO.getTipoMovimiento())
            .valor(crearMovimientoDTO.getValor())
            .cuenta(cuenta)
            .build();
        
        Movimiento movimientoCreado = movimientoService.crearMovimiento(movimiento);
        return ResponseEntity.ok(modelMapper.map(movimientoCreado, MovimientoDTO.class));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoDTO> obtener(@PathVariable Long id) {
        log.info("Obteniendo movimiento con ID: {}", id);
        Movimiento movimiento = movimientoService.obtenerPorId(id);
        return ResponseEntity.ok(modelMapper.map(movimiento, MovimientoDTO.class));
    }

    @GetMapping
    public ResponseEntity<List<MovimientoDTO>> listar() {
        log.info("Listando todos los movimientos");
        List<Movimiento> movimientos = movimientoService.obtenerTodos();
        List<MovimientoDTO> movimientosDTO = movimientos.stream()
            .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
            .collect(Collectors.toList());
        return ResponseEntity.ok(movimientosDTO);
    }

    @GetMapping("/cuenta/{numeroCuenta}")
    public ResponseEntity<List<MovimientoDTO>> listarPorCuenta(@PathVariable String numeroCuenta) {
        log.info("Listando movimientos para cuenta: {}", numeroCuenta);
        List<Movimiento> movimientos = movimientoService.reportePorCliente(numeroCuenta);
        List<MovimientoDTO> movimientosDTO = movimientos.stream()
            .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
            .collect(Collectors.toList());
        return ResponseEntity.ok(movimientosDTO);
    }

    @GetMapping("/reporte")
    public ResponseEntity<List<MovimientoDTO>> reportePorFecha(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin) {
        log.info("Generando reporte de movimientos entre {} y {}", fechaInicio, fechaFin);
        List<Movimiento> movimientos = movimientoService.reportePorFecha(fechaInicio, fechaFin);
        List<MovimientoDTO> movimientosDTO = movimientos.stream()
            .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
            .collect(Collectors.toList());
        return ResponseEntity.ok(movimientosDTO);
    }
} 