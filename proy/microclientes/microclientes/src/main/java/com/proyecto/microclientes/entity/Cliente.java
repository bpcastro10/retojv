package com.proyecto.microclientes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cliente")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends Persona {
    
    @Column(name = "clienteid", unique = true)
    private String clienteid;
    
    @Column(name = "contrasena")
    private String contrasena;
    
    @Column(name = "estado")
    private String estado;
} 