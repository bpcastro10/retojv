package com.proyecto.microcuentas.validation;

import com.proyecto.microcuentas.entity.Movimiento;
import com.proyecto.microcuentas.entity.Cuenta;
import com.proyecto.microcuentas.repository.CuentaRepository;
import com.proyecto.microcuentas.exception.SaldoInsuficienteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class MovimientoValidator {
    
    @Autowired
    private CuentaRepository cuentaRepository;

    public void validarMovimiento(Movimiento movimiento) {
        // Validar que la cuenta existe
        Cuenta cuenta = cuentaRepository.findById(movimiento.getCuenta().getNumeroCuenta())
            .orElseThrow(() -> new IllegalArgumentException("La cuenta no existe"));

        // Validar que la cuenta está activa
        if (!"ACTIVA".equals(cuenta.getEstado())) {
            throw new IllegalArgumentException("La cuenta no está activa");
        }

        // Validar el valor del movimiento
        if (movimiento.getValor() == null) {
            throw new IllegalArgumentException("El valor del movimiento no puede ser nulo");
        }

        // Validar saldo para retiros
        if (movimiento.getValor().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal nuevoSaldo = cuenta.getSaldoInicial().add(movimiento.getValor());
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                throw new SaldoInsuficienteException("No hay saldo suficiente para realizar la operación");
            }
        }

        // Validar tipo de movimiento
        if (movimiento.getTipoMovimiento() == null || movimiento.getTipoMovimiento().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento es requerido");
        }

        // Validar fecha
        if (movimiento.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del movimiento es requerida");
        }
    }
} 