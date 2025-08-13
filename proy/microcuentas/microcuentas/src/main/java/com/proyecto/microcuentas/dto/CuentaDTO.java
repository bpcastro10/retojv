package com.proyecto.microcuentas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaDTO {
    
    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El número de cuenta debe tener 10 dígitos")
    private String numeroCuenta;
    
    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Pattern(regexp = "^(AHORRO|CORRIENTE)$", message = "El tipo de cuenta debe ser AHORRO o CORRIENTE")
    private String tipoCuenta;
    
    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El saldo inicial debe ser mayor a 0")
    private BigDecimal saldoInicial;
    
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(ACTIVA|INACTIVA)$", message = "El estado debe ser ACTIVA o INACTIVA")
    private String estado;
    
    @NotBlank(message = "El ID del cliente es obligatorio")
    private String clienteId;
} 