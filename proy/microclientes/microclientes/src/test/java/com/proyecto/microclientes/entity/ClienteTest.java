package com.proyecto.microclientes.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Entidad Cliente")
class ClienteTest {

    private Cliente cliente;
    private Persona persona;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        persona = new Persona();
        
        // Configurar persona de prueba
        persona.setIdentificacion("12345678");
        persona.setNombre("Juan Pérez");
        persona.setGenero("M");
        persona.setEdad(30);
        persona.setDireccion("Calle Principal 123");
        persona.setTelefono("555-1234");
    }

    @Test
    @DisplayName("Debería establecer y obtener clienteid correctamente")
    void testClienteId() {
        // Arrange
        String clienteId = "CLI001";
        
        // Act
        cliente.setClienteid(clienteId);
        
        // Assert
        assertEquals(clienteId, cliente.getClienteid());
    }

    @Test
    @DisplayName("Debería establecer y obtener contrasena correctamente")
    void testContrasena() {
        // Arrange
        String contrasena = "password123";
        
        // Act
        cliente.setContrasena(contrasena);
        
        // Assert
        assertEquals(contrasena, cliente.getContrasena());
    }

    @Test
    @DisplayName("Debería establecer y obtener estado correctamente")
    void testEstado() {
        // Arrange
        String estado = "ACTIVO";
        
        // Act
        cliente.setEstado(estado);
        
        // Assert
        assertEquals(estado, cliente.getEstado());
    }

    @Test
    @DisplayName("Debería establecer y obtener persona correctamente")
    void testPersona() {
        // Act
        cliente.setPersona(persona);
        
        // Assert
        assertNotNull(cliente.getPersona());
        assertEquals(persona, cliente.getPersona());
        assertEquals("12345678", cliente.getPersona().getIdentificacion());
        assertEquals("Juan Pérez", cliente.getPersona().getNombre());
    }

    @Test
    @DisplayName("Debería manejar valores nulos correctamente")
    void testValoresNulos() {
        // Act & Assert
        assertNull(cliente.getClienteid());
        assertNull(cliente.getContrasena());
        assertNull(cliente.getEstado());
        assertNull(cliente.getPersona());
    }

    @Test
    @DisplayName("Debería permitir actualizar todos los campos")
    void testActualizacionCompleta() {
        // Arrange
        String clienteId = "CLI002";
        String contrasena = "nuevaPassword";
        String estado = "INACTIVO";
        
        // Act
        cliente.setClienteid(clienteId);
        cliente.setContrasena(contrasena);
        cliente.setEstado(estado);
        cliente.setPersona(persona);
        
        // Assert
        assertEquals(clienteId, cliente.getClienteid());
        assertEquals(contrasena, cliente.getContrasena());
        assertEquals(estado, cliente.getEstado());
        assertEquals(persona, cliente.getPersona());
    }
} 