package com.proyecto.microclientes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persona_cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    @Column(name = "clienteid")
    @NotBlank(message = "El ID del cliente es obligatorio")
    @Size(min = 3, max = 20, message = "El ID del cliente debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "El ID del cliente debe contener solo letras mayúsculas y números")
    private String clienteid;
    
    @Column(name = "identificacion")
    @NotBlank(message = "La identificación es obligatoria")
    @Size(min = 8, max = 20, message = "La identificación debe tener entre 8 y 20 caracteres")
    @Pattern(regexp = "^[0-9]+$", message = "La identificación debe contener solo números")
    private String identificacion;
    
    @Column(name = "nombre")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre debe contener solo letras y espacios")
    private String nombre;
    
    @Column(name = "genero")
    @NotBlank(message = "El género es obligatorio")
    @Pattern(regexp = "^(M|F)$", message = "El género debe ser 'M' o 'F'")
    private String genero;
    
    @Column(name = "edad")
    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad debe ser mayor o igual a 0")
    @Max(value = 150, message = "La edad debe ser menor o igual a 150")
    private Integer edad;
    
    @Column(name = "direccion")
    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 100, message = "La dirección debe tener entre 5 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s,.-]+$", message = "La dirección contiene caracteres no válidos")
    private String direccion;
    
    @Column(name = "telefono")
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    @Pattern(regexp = "^[0-9\\-\\+\\(\\)\\s]+$", message = "El teléfono contiene caracteres no válidos")
    private String telefono;
    
    @Column(name = "contrasena")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
             message = "La contraseña debe contener al menos una letra minúscula, una mayúscula, un número y un carácter especial")
    private String contrasena;
    
    @Column(name = "estado")
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(ACTIVO|INACTIVO|SUSPENDIDO)$", message = "El estado debe ser 'ACTIVO', 'INACTIVO' o 'SUSPENDIDO'")
    private String estado;
} 