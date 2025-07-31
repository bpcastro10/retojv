package com.proyecto.microcuentas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.repository.CuentaRepository;
import com.proyecto.microcuentas.validation.CuentaValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuentaService {
    
    private final CuentaRepository cuentaRepository;
    private final CuentaValidator cuentaValidator;

    public List<Cuenta> obtenerTodasLasCuentas() {
        log.info("Obteniendo todas las cuentas");
        return cuentaRepository.findAll();
    }

    public Optional<Cuenta> obtenerCuentaPorId(String numeroCuenta) {
        log.info("Obteniendo cuenta con número: {}", numeroCuenta);
        return cuentaRepository.findById(numeroCuenta);
    }

    @Transactional
    public Cuenta crearCuenta(Cuenta cuenta) {
        log.info("Creando nueva cuenta: {}", cuenta.getNumeroCuenta());
        cuenta.setFechaCreacion(LocalDateTime.now());
        cuenta.setFechaActualizacion(LocalDateTime.now());
        cuentaValidator.validarCuenta(cuenta);
        return cuentaRepository.save(cuenta);
    }

    @Transactional
    public Cuenta actualizarCuenta(String numeroCuenta, Cuenta cuentaActualizada) {
        log.info("Actualizando cuenta: {}", numeroCuenta);
        Optional<Cuenta> cuentaExistente = cuentaRepository.findById(numeroCuenta);
        if (cuentaExistente.isPresent()) {
            Cuenta cuenta = cuentaExistente.get();
            cuenta.setTipoCuenta(cuentaActualizada.getTipoCuenta());
            cuenta.setSaldoInicial(cuentaActualizada.getSaldoInicial());
            cuenta.setEstado(cuentaActualizada.getEstado());
            cuenta.setFechaActualizacion(LocalDateTime.now());
            cuentaValidator.validarCuenta(cuenta);
            return cuentaRepository.save(cuenta);
        }
        throw new IllegalArgumentException("Cuenta no encontrada: " + numeroCuenta);
    }

    @Transactional
    public void eliminarCuenta(String numeroCuenta) {
        log.info("Eliminando cuenta: {}", numeroCuenta);
        if (!cuentaRepository.existsById(numeroCuenta)) {
            throw new IllegalArgumentException("Cuenta no encontrada: " + numeroCuenta);
        }
        cuentaRepository.deleteById(numeroCuenta);
    }

    @Transactional
    public Cuenta actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo) {
        log.info("Actualizando saldo de cuenta: {} a {}", numeroCuenta, nuevoSaldo);
        Optional<Cuenta> cuentaExistente = cuentaRepository.findById(numeroCuenta);
        if (cuentaExistente.isPresent()) {
            Cuenta cuenta = cuentaExistente.get();
            cuenta.setSaldoInicial(nuevoSaldo);
            cuenta.setFechaActualizacion(LocalDateTime.now());
            return cuentaRepository.save(cuenta);
        }
        throw new IllegalArgumentException("Cuenta no encontrada: " + numeroCuenta);
    }
} 