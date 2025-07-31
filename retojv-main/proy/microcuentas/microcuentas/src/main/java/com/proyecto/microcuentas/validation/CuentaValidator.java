package com.proyecto.microcuentas.validation;

import com.proyecto.microcuentas.entity.Cuenta;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class CuentaValidator {

    public void validarCuenta(Cuenta cuenta) {
        // Validar que el tipo de cuenta sea válido (regla de negocio específica)
        if (!"AHORRO".equals(cuenta.getTipoCuenta()) && !"CORRIENTE".equals(cuenta.getTipoCuenta())) {
            throw new IllegalArgumentException("El tipo de cuenta debe ser AHORRO o CORRIENTE");
        }

        // Validar que el estado sea válido (regla de negocio específica)
        if (!"ACTIVA".equals(cuenta.getEstado()) && !"INACTIVA".equals(cuenta.getEstado())) {
            throw new IllegalArgumentException("El estado debe ser ACTIVA o INACTIVA");
        }

        // Validar saldo inicial no negativo (regla de negocio)
        if (cuenta.getSaldoInicial() != null && cuenta.getSaldoInicial().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }
    }
} 