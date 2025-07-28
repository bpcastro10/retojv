package com.proyecto.microcuentas.validation;

import com.proyecto.microcuentas.entity.Cuenta;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class CuentaValidator {

    public void validarCuenta(Cuenta cuenta) {
        // Validar número de cuenta
        if (cuenta.getNumeroCuenta() == null || cuenta.getNumeroCuenta().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cuenta es requerido");
        }

        // Validar tipo de cuenta
        if (cuenta.getTipoCuenta() == null || cuenta.getTipoCuenta().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de cuenta es requerido");
        }

        // Validar que el tipo de cuenta sea válido
        if (!"AHORROS".equals(cuenta.getTipoCuenta()) && !"CORRIENTE".equals(cuenta.getTipoCuenta())) {
            throw new IllegalArgumentException("El tipo de cuenta debe ser AHORROS o CORRIENTE");
        }

        // Validar saldo inicial
        if (cuenta.getSaldoInicial() == null) {
            throw new IllegalArgumentException("El saldo inicial es requerido");
        }

        if (cuenta.getSaldoInicial().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }

        // Validar estado
        if (cuenta.getEstado() == null || cuenta.getEstado().trim().isEmpty()) {
            throw new IllegalArgumentException("El estado de la cuenta es requerido");
        }

        // Validar que el estado sea válido
        if (!"ACTIVA".equals(cuenta.getEstado()) && !"INACTIVA".equals(cuenta.getEstado())) {
            throw new IllegalArgumentException("El estado debe ser ACTIVA o INACTIVA");
        }
    }
} 