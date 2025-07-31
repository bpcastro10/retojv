package com.proyecto.microclientes.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.proyecto.microclientes.entity.Cliente;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\-\\+\\(\\)\\s]{7,20}$");
    private static final Pattern ID_PATTERN = Pattern.compile("^[0-9]{8,20}$");

    /**
     * Valida la integridad de los datos del cliente
     * @param cliente El cliente a validar
     * @return Map con errores de validación (vacío si no hay errores)
     */
    public Map<String, String> validateCliente(Cliente cliente) {
        Map<String, String> errors = new HashMap<>();

        // Validación de edad
        if (cliente.getEdad() != null) {
            if (cliente.getEdad() < 18) {
                errors.put("edad", "El cliente debe ser mayor de edad (mínimo 18 años)");
            }
            if (cliente.getEdad() > 120) {
                errors.put("edad", "La edad proporcionada no es realista (máximo 120 años)");
            }
        }

        // Validación de identificación única
        if (cliente.getIdentificacion() != null) {
            if (!ID_PATTERN.matcher(cliente.getIdentificacion()).matches()) {
                errors.put("identificacion", "La identificación debe contener solo números y tener entre 8 y 20 dígitos");
            }
        }

        // Validación de teléfono
        if (cliente.getTelefono() != null) {
            if (!PHONE_PATTERN.matcher(cliente.getTelefono()).matches()) {
                errors.put("telefono", "El formato del teléfono no es válido");
            }
        }

        // Validación de contraseña
        if (cliente.getContrasena() != null) {
            if (cliente.getContrasena().length() < 8) {
                errors.put("contrasena", "La contraseña debe tener al menos 8 caracteres");
            }
            if (!cliente.getContrasena().matches(".*[A-Z].*")) {
                errors.put("contrasena", "La contraseña debe contener al menos una letra mayúscula");
            }
            if (!cliente.getContrasena().matches(".*[a-z].*")) {
                errors.put("contrasena", "La contraseña debe contener al menos una letra minúscula");
            }
            if (!cliente.getContrasena().matches(".*\\d.*")) {
                errors.put("contrasena", "La contraseña debe contener al menos un número");
            }
            if (!cliente.getContrasena().matches(".*[@$!%*?&].*")) {
                errors.put("contrasena", "La contraseña debe contener al menos un carácter especial (@$!%*?&)");
            }
        }

        // Validación de estado
        if (cliente.getEstado() != null) {
            if (!cliente.getEstado().matches("^(ACTIVO|INACTIVO|SUSPENDIDO)$")) {
                errors.put("estado", "El estado debe ser 'ACTIVO', 'INACTIVO' o 'SUSPENDIDO'");
            }
        }

        // Validación de género
        if (cliente.getGenero() != null) {
            if (!cliente.getGenero().matches("^(M|F)$")) {
                errors.put("genero", "El género debe ser 'M' o 'F'");
            }
        }

        // Validación de nombre
        if (cliente.getNombre() != null) {
            if (cliente.getNombre().trim().length() < 2) {
                errors.put("nombre", "El nombre debe tener al menos 2 caracteres");
            }
            if (!cliente.getNombre().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
                errors.put("nombre", "El nombre debe contener solo letras y espacios");
            }
        }

        // Validación de dirección
        if (cliente.getDireccion() != null) {
            if (cliente.getDireccion().trim().length() < 5) {
                errors.put("direccion", "La dirección debe tener al menos 5 caracteres");
            }
        }

        return errors;
    }

    /**
     * Valida que el cliente tenga todos los campos obligatorios
     * @param cliente El cliente a validar
     * @return true si todos los campos obligatorios están presentes
     */
    public boolean hasRequiredFields(Cliente cliente) {
        return cliente.getClienteid() != null && !cliente.getClienteid().trim().isEmpty() &&
               cliente.getIdentificacion() != null && !cliente.getIdentificacion().trim().isEmpty() &&
               cliente.getNombre() != null && !cliente.getNombre().trim().isEmpty() &&
               cliente.getGenero() != null && !cliente.getGenero().trim().isEmpty() &&
               cliente.getEdad() != null &&
               cliente.getDireccion() != null && !cliente.getDireccion().trim().isEmpty() &&
               cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty() &&
               cliente.getContrasena() != null && !cliente.getContrasena().trim().isEmpty() &&
               cliente.getEstado() != null && !cliente.getEstado().trim().isEmpty();
    }

    /**
     * Valida la integridad referencial del cliente
     * @param cliente El cliente a validar
     * @return Map con errores de validación (vacío si no hay errores)
     */
    public Map<String, String> validateReferentialIntegrity(Cliente cliente) {
        Map<String, String> errors = new HashMap<>();

        // Validación de que el ID del cliente sea único (esto se validaría contra la base de datos)
        if (cliente.getClienteid() != null) {
            // Aquí se podría agregar lógica para verificar si el ID ya existe en la base de datos
            // Por ahora solo validamos el formato
            if (!cliente.getClienteid().matches("^[A-Z0-9]{3,20}$")) {
                errors.put("clienteid", "El ID del cliente debe contener solo letras mayúsculas y números, entre 3 y 20 caracteres");
            }
        }

        return errors;
    }
} 