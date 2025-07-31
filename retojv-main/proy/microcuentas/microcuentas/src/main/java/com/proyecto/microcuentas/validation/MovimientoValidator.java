package com.proyecto.microcuentas.validation;

import com.proyecto.microcuentas.entity.Movimiento;
import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.repository.CuentaRepository;
import com.proyecto.microcuentas.exception.SaldoInsuficienteException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class MovimientoValidator {
    
    private final CuentaRepository cuentaRepository;

    public void validarMovimiento(Movimiento movimiento) {
        // Validar que la cuenta existe
        Cuenta cuenta = cuentaRepository.findById(movimiento.getCuenta().getNumeroCuenta())
            .orElseThrow(() -> new IllegalArgumentException("La cuenta no existe"));

        // Validar que la cuenta está activa
        if (!"ACTIVA".equals(cuenta.getEstado())) {
            throw new IllegalArgumentException("La cuenta no está activa");
        }

        // Validar saldo para débitos (regla de negocio específica)
        if ("DEBITO".equals(movimiento.getTipoMovimiento())) {
            BigDecimal nuevoSaldo = cuenta.getSaldoInicial().add(movimiento.getValor());
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                throw new SaldoInsuficienteException("No hay saldo suficiente para realizar el débito");
            }
        }
    }
} 