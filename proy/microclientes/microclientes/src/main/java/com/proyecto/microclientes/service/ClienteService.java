package com.proyecto.microclientes.service;

import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;

import org.springframework.stereotype.Service;

import com.proyecto.microclientes.dto.ClienteDTO;
import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.repository.ClienteRepository;

import org.modelmapper.ModelMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository repo;

    private final ModelMapper modelMapper;

    public List<Cliente> listar() { 
        return repo.findAll(); 
    }
    
    public Cliente buscarPorId(String clienteid) { 
        return repo.findByClienteid(clienteid)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteid)); 
    }
    
    public Cliente buscarPorIdentificacion(String identificacion) {
        return repo.findById(identificacion)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con identificación: " + identificacion));
    }
    
    public Cliente guardar(Cliente c) { 
        return repo.save(c); 
    }
    
    public void eliminar(String clienteid) {
        // Buscar cliente por clienteid para obtener la identificación
        Cliente cliente = repo.findByClienteid(clienteid)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteid));
        repo.deleteById(cliente.getIdentificacion()); 
    }

    /**
     * Actualiza parcialmente un cliente usando PATCH
     * @param clienteid ID del cliente a actualizar
     * @param updates Mapa con los campos a actualizar
     * @return ClienteDTO actualizado
     */
    public ClienteDTO actualizarParcial(String clienteid, Map<String, Object> updates) {
        Cliente clienteExistente = buscarPorId(clienteid);
        
        // Aplicar actualizaciones parciales usando reflexión
        updates.forEach((key, value) -> {
            try {
                Field field = getFieldFromClassHierarchy(Cliente.class, key);
                if (field != null) {
                    field.setAccessible(true);
                    
                    // Convertir el valor al tipo correcto del campo
                    Object convertedValue = convertValue(value, field.getType());
                    field.set(clienteExistente, convertedValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error al actualizar el campo: " + key, e);
            }
        });
        
        Cliente clienteActualizado = repo.save(clienteExistente);
        
        return modelMapper.map(clienteActualizado, ClienteDTO.class);
    }
    
    /**
     * Busca un campo en la jerarquía de clases (Cliente -> Persona)
     */
    private Field getFieldFromClassHierarchy(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
    
    /**
     * Convierte el valor al tipo correcto del campo
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        
        if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
            if (value instanceof Number number) {
                return number.intValue();
            }
            return Integer.valueOf(value.toString());
        }
        
        if (targetType.equals(String.class)) {
            return value.toString();
        }
        
        // Para otros tipos, retornar el valor tal como está
        return value;
    }


} 