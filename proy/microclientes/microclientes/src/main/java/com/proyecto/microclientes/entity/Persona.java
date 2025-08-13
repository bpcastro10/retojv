package com.proyecto.microclientes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Persona {
    
    @Id
    @Column(name = "identificacion", unique = true)
    private String identificacion;
    
    @Column(name = "nombre")
    private String nombre;
    
    @Column(name = "genero")
    private String genero;
    
    @Column(name = "edad")
    private Integer edad;
    
    @Column(name = "direccion")
    private String direccion;
    
    @Column(name = "telefono")
    private String telefono;
}