package com.proyecto.microclientes.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Entidad Persona")
class PersonaTest {

    private Persona persona;

    @BeforeEach
    void setUp() {
        persona = new Persona();
    }

    @Test
    @DisplayName("Debería establecer y obtener identificacion correctamente")
    void testIdentificacion() {
        // Arrange
        String identificacion = "12345678";
        
        // Act
        persona.setIdentificacion(identificacion);
        
        // Assert
        assertEquals(identificacion, persona.getIdentificacion());
    }

    @Test
    @DisplayName("Debería establecer y obtener nombre correctamente")
    void testNombre() {
        // Arrange
        String nombre = "María García";
        
        // Act
        persona.setNombre(nombre);
        
        // Assert
        assertEquals(nombre, persona.getNombre());
    }

    @Test
    @DisplayName("Debería establecer y obtener genero correctamente")
    void testGenero() {
        // Arrange
        String genero = "F";
        
        // Act
        persona.setGenero(genero);
        
        // Assert
        assertEquals(genero, persona.getGenero());
    }

    @Test
    @DisplayName("Debería establecer y obtener edad correctamente")
    void testEdad() {
        // Arrange
        Integer edad = 25;
        
        // Act
        persona.setEdad(edad);
        
        // Assert
        assertEquals(edad, persona.getEdad());
    }

    @Test
    @DisplayName("Debería establecer y obtener direccion correctamente")
    void testDireccion() {
        // Arrange
        String direccion = "Avenida Central 456";
        
        // Act
        persona.setDireccion(direccion);
        
        // Assert
        assertEquals(direccion, persona.getDireccion());
    }

    @Test
    @DisplayName("Debería establecer y obtener telefono correctamente")
    void testTelefono() {
        // Arrange
        String telefono = "555-9876";
        
        // Act
        persona.setTelefono(telefono);
        
        // Assert
        assertEquals(telefono, persona.getTelefono());
    }

    @Test
    @DisplayName("Debería manejar valores nulos correctamente")
    void testValoresNulos() {
        // Act & Assert
        assertNull(persona.getIdentificacion());
        assertNull(persona.getNombre());
        assertNull(persona.getGenero());
        assertNull(persona.getEdad());
        assertNull(persona.getDireccion());
        assertNull(persona.getTelefono());
    }

    @Test
    @DisplayName("Debería permitir actualizar todos los campos")
    void testActualizacionCompleta() {
        // Arrange
        String identificacion = "87654321";
        String nombre = "Carlos López";
        String genero = "M";
        Integer edad = 35;
        String direccion = "Calle Secundaria 789";
        String telefono = "555-1111";
        
        // Act
        persona.setIdentificacion(identificacion);
        persona.setNombre(nombre);
        persona.setGenero(genero);
        persona.setEdad(edad);
        persona.setDireccion(direccion);
        persona.setTelefono(telefono);
        
        // Assert
        assertEquals(identificacion, persona.getIdentificacion());
        assertEquals(nombre, persona.getNombre());
        assertEquals(genero, persona.getGenero());
        assertEquals(edad, persona.getEdad());
        assertEquals(direccion, persona.getDireccion());
        assertEquals(telefono, persona.getTelefono());
    }

    @Test
    @DisplayName("Debería manejar edad cero correctamente")
    void testEdadCero() {
        // Arrange
        Integer edad = 0;
        
        // Act
        persona.setEdad(edad);
        
        // Assert
        assertEquals(edad, persona.getEdad());
    }

    @Test
    @DisplayName("Debería manejar edad negativa correctamente")
    void testEdadNegativa() {
        // Arrange
        Integer edad = -5;
        
        // Act
        persona.setEdad(edad);
        
        // Assert
        assertEquals(edad, persona.getEdad());
    }
} 