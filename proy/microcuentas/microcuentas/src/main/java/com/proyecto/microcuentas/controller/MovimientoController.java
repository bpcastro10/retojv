package com.proyecto.microcuentas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.microcuentas.dto.MovimientoDTO;
import com.proyecto.microcuentas.entity.Movimiento;
import com.proyecto.microcuentas.service.MovimientoService;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movimientos")
public class MovimientoController {
    
    @Autowired
    private MovimientoService movimientoService;
    
    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody MovimientoDTO movimientoDTO) {
        try {
            Movimiento movimiento = modelMapper.map(movimientoDTO, Movimiento.class);
            Movimiento movimientoCreado = movimientoService.crearMovimiento(movimiento);
            return ResponseEntity.ok(modelMapper.map(movimientoCreado, MovimientoDTO.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el movimiento: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            Movimiento movimiento = movimientoService.obtenerPorId(id);
            return ResponseEntity.ok(modelMapper.map(movimiento, MovimientoDTO.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener el movimiento: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<Movimiento> movimientos = movimientoService.obtenerTodos();
            List<MovimientoDTO> movimientosDTO = movimientos.stream()
                .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
                .collect(Collectors.toList());
            return ResponseEntity.ok(movimientosDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al listar los movimientos: " + e.getMessage());
        }
    }

    @GetMapping("/cuenta/{numeroCuenta}")
    public ResponseEntity<?> listarPorCuenta(@PathVariable String numeroCuenta) {
        try {
            List<Movimiento> movimientos = movimientoService.reportePorCliente(numeroCuenta);
            List<MovimientoDTO> movimientosDTO = movimientos.stream()
                .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
                .collect(Collectors.toList());
            return ResponseEntity.ok(movimientosDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al listar los movimientos de la cuenta: " + e.getMessage());
        }
    }

    @GetMapping("/reporte")
    public ResponseEntity<?> reportePorFecha(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin) {
        try {
            List<Movimiento> movimientos = movimientoService.reportePorFecha(fechaInicio, fechaFin);
            List<MovimientoDTO> movimientosDTO = movimientos.stream()
                .map(mov -> modelMapper.map(mov, MovimientoDTO.class))
                .collect(Collectors.toList());
            return ResponseEntity.ok(movimientosDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar el reporte: " + e.getMessage());
        }
    }
} 