package com.proyecto.microcuentas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.microcuentas.dto.CuentaDTO;
import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.service.CuentaService;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cuentas")
public class CuentaController {
    
    @Autowired
    private CuentaService cuentaService;
    
    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CuentaDTO cuentaDTO) {
        try {
            Cuenta cuenta = modelMapper.map(cuentaDTO, Cuenta.class);
            Cuenta cuentaCreada = cuentaService.crearCuenta(cuenta);
            return ResponseEntity.ok(modelMapper.map(cuentaCreada, CuentaDTO.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear la cuenta: " + e.getMessage());
        }
    }

    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<?> actualizar(
            @PathVariable String numeroCuenta,
            @RequestBody CuentaDTO cuentaDTO) {
        try {
            Cuenta cuenta = modelMapper.map(cuentaDTO, Cuenta.class);
            Cuenta cuentaActualizada = cuentaService.actualizarCuenta(numeroCuenta, cuenta);
            return ResponseEntity.ok(modelMapper.map(cuentaActualizada, CuentaDTO.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar la cuenta: " + e.getMessage());
        }
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<?> eliminar(@PathVariable String numeroCuenta) {
        try {
            cuentaService.eliminarCuenta(numeroCuenta);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar la cuenta: " + e.getMessage());
        }
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<?> obtener(@PathVariable String numeroCuenta) {
        try {
            return cuentaService.obtenerCuentaPorId(numeroCuenta)
                .map(cuenta -> ResponseEntity.ok(modelMapper.map(cuenta, CuentaDTO.class)))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener la cuenta: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<Cuenta> cuentas = cuentaService.obtenerTodasLasCuentas();
            List<CuentaDTO> cuentasDTO = cuentas.stream()
                .map(cuenta -> modelMapper.map(cuenta, CuentaDTO.class))
                .collect(Collectors.toList());
            return ResponseEntity.ok(cuentasDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al listar las cuentas: " + e.getMessage());
        }
    }
} 