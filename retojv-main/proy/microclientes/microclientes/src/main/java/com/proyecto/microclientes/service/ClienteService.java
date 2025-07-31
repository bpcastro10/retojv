package com.proyecto.microclientes.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.repository.ClienteRepository;
import com.proyecto.microclientes.validation.ClienteValidationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository repo;
    private final ClienteValidationService validationService;

    public List<Cliente> listar() { 
        return repo.findAll(); 
    }
    
    public Cliente buscarPorId(String clienteid) { 
        return repo.findById(clienteid)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteid)); 
    }
    
    public Cliente buscarPorIdentificacion(String identificacion) {
        return repo.findByIdentificacion(identificacion)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con identificación: " + identificacion));
    }
    
    public Cliente guardar(Cliente c) { 
        // Validación de datos antes de guardar
        validateClienteData(c);
        return repo.save(c); 
    }
    
    public void eliminar(String clienteid) { 
        repo.deleteById(clienteid); 
    }

    /**
     * Valida los datos del cliente antes de guardar
     * @param cliente El cliente a validar
     * @throws RuntimeException si hay errores de validación
     */
    private void validateClienteData(Cliente cliente) {
        // Validación de campos obligatorios
        if (!validationService.hasRequiredFields(cliente)) {
            throw new RuntimeException("Todos los campos son obligatorios");
        }

        // Validación de integridad de datos
        Map<String, String> validationErrors = validationService.validateCliente(cliente);
        if (!validationErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Errores de validación: ");
            validationErrors.forEach((field, error) -> 
                errorMessage.append(field).append(": ").append(error).append("; "));
            throw new RuntimeException(errorMessage.toString());
        }

        // Validación de integridad referencial
        Map<String, String> referentialErrors = validationService.validateReferentialIntegrity(cliente);
        if (!referentialErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Errores de integridad referencial: ");
            referentialErrors.forEach((field, error) -> 
                errorMessage.append(field).append(": ").append(error).append("; "));
            throw new RuntimeException(errorMessage.toString());
        }
    }
} 