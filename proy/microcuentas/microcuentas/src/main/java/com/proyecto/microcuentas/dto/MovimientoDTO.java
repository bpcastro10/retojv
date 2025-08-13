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
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoDTO {
    
    private Long id;
    
    @NotNull(message = "La fecha es obligatoria")
    private LocalDateTime fecha;
    
    @NotBlank(message = "El tipo de movimiento es obligatorio")
    @Pattern(regexp = "^(DEBITO|CREDITO)$", message = "El tipo de movimiento debe ser DEBITO o CREDITO")
    private String tipoMovimiento;
    
    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El valor debe ser mayor a 0")
    private BigDecimal valor;
    
    @NotNull(message = "El saldo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo no puede ser negativo")
    private BigDecimal saldo;
    
    // El número de cuenta se obtiene de la relación con Cuenta, no se valida aquí
    private String numeroCuenta;
    
    private ClienteDTO cliente;
} 