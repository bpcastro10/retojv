 package com.proyecto.microcuentas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import com.proyecto.microcuentas.dto.CuentaDTO;
import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.service.CuentaService;
import org.modelmapper.ModelMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CuentaController {
    
    private final CuentaService cuentaService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<CuentaDTO> crear(@Valid @RequestBody CuentaDTO cuentaDTO) {
        log.info("Creando nueva cuenta: {}", cuentaDTO.getNumeroCuenta());
        Cuenta cuenta = modelMapper.map(cuentaDTO, Cuenta.class);
        Cuenta cuentaCreada = cuentaService.crearCuenta(cuenta);
        return ResponseEntity.ok(modelMapper.map(cuentaCreada, CuentaDTO.class));
    }

    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaDTO> actualizar(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody CuentaDTO cuentaDTO) {
        log.info("Actualizando cuenta: {}", numeroCuenta);
        Cuenta cuenta = modelMapper.map(cuentaDTO, Cuenta.class);
        Cuenta cuentaActualizada = cuentaService.actualizarCuenta(numeroCuenta, cuenta);
        return ResponseEntity.ok(modelMapper.map(cuentaActualizada, CuentaDTO.class));
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> eliminar(@PathVariable String numeroCuenta) {
        log.info("Eliminando cuenta: {}", numeroCuenta);
        cuentaService.eliminarCuenta(numeroCuenta);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaDTO> obtener(@PathVariable String numeroCuenta) {
        log.info("Obteniendo cuenta: {}", numeroCuenta);
        return cuentaService.obtenerCuentaPorId(numeroCuenta)
            .map(cuenta -> ResponseEntity.ok(modelMapper.map(cuenta, CuentaDTO.class)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CuentaDTO>> listar() {
        log.info("Listando todas las cuentas");
        List<Cuenta> cuentas = cuentaService.obtenerTodasLasCuentas();
        List<CuentaDTO> cuentasDTO = cuentas.stream()
            .map(cuenta -> modelMapper.map(cuenta, CuentaDTO.class))
            .collect(Collectors.toList());
        return ResponseEntity.ok(cuentasDTO);
    }
} 