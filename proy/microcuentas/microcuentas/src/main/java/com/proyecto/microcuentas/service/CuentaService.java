package com.proyecto.microcuentas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.microcuentas.dto.CuentaDTO;
import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.repository.CuentaRepository;
import com.proyecto.microcuentas.validation.CuentaValidator;

@Service
public class CuentaService {
    @Autowired
    private CuentaRepository cuentaRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CuentaValidator cuentaValidator;

    public List<Cuenta> obtenerTodasLasCuentas() {
        return cuentaRepository.findAll();
    }

    public Optional<Cuenta> obtenerCuentaPorId(String numeroCuenta) {
        return cuentaRepository.findById(numeroCuenta);
    }

    @Transactional
    public Cuenta crearCuenta(Cuenta cuenta) {
        cuenta.setFechaCreacion(LocalDateTime.now());
        cuenta.setFechaActualizacion(LocalDateTime.now());
        cuentaValidator.validarCuenta(cuenta);
        return cuentaRepository.save(cuenta);
    }

    @Transactional
    public Cuenta actualizarCuenta(String numeroCuenta, Cuenta cuentaActualizada) {
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
        if (!cuentaRepository.existsById(numeroCuenta)) {
            throw new IllegalArgumentException("Cuenta no encontrada: " + numeroCuenta);
        }
        cuentaRepository.deleteById(numeroCuenta);
    }

    @Transactional
    public Cuenta actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo) {
        Optional<Cuenta> cuentaExistente = cuentaRepository.findById(numeroCuenta);
        if (cuentaExistente.isPresent()) {
            Cuenta cuenta = cuentaExistente.get();
            cuenta.setSaldoInicial(nuevoSaldo);
            cuenta.setFechaActualizacion(LocalDateTime.now());
            return cuentaRepository.save(cuenta);
        }
        throw new IllegalArgumentException("Cuenta no encontrada: " + numeroCuenta);
    }

    public CuentaDTO obtenerPorNumero(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findById(numeroCuenta)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        return modelMapper.map(cuenta, CuentaDTO.class);
    }

    public List<CuentaDTO> listarTodas() {
        return cuentaRepository.findAll().stream()
            .map(cuenta -> modelMapper.map(cuenta, CuentaDTO.class))
            .collect(Collectors.toList());
    }
} 