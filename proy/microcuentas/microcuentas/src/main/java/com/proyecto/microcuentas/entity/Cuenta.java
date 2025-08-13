package com.proyecto.microcuentas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuenta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    @Id
    @Column(name = "numero_cuenta", nullable = false, unique = true)
    private String numeroCuenta;

    @Column(name = "tipo_cuenta", nullable = false)
    private String tipoCuenta;

    @Column(name = "saldo_inicial", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoInicial;

    @Column(name = "estado", nullable = false)
    private String estado;
    
    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Movimiento> movimientos = new ArrayList<>();

    // Métodos helper para la relación bidireccional
    public void addMovimiento(Movimiento movimiento) {
        movimientos.add(movimiento);
        movimiento.setCuenta(this);
    }

    public void removeMovimiento(Movimiento movimiento) {
        movimientos.remove(movimiento);
        movimiento.setCuenta(null);
    }
} 