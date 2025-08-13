package com.proyecto.microclientes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO extends PersonaDTO {
    
    @NotBlank(message = "El ID del cliente es obligatorio")
    @Size(min = 3, max = 20, message = "El ID del cliente debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "El ID del cliente debe contener solo letras mayúsculas y números")
    private String clienteid;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
             message = "La contraseña debe contener al menos una letra minúscula, una mayúscula, un número y un carácter especial")
    private String contrasena;
    
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(ACTIVO|INACTIVO|SUSPENDIDO)$", message = "El estado debe ser 'ACTIVO', 'INACTIVO' o 'SUSPENDIDO'")
    private String estado;
} 