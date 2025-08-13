package com.proyecto.microclientes.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Pruebas Unitarias - Entidad Cliente")
class ClienteTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CLI001", "CLI002", "CLI003"})
    @DisplayName("Debería establecer y obtener clienteid correctamente")
    void testClienteid(String clienteid) {
        // Given & When
        cliente.setClienteid(clienteid);

        // Then
        assertEquals(clienteid, cliente.getClienteid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "87654321", "11223344"})
    @DisplayName("Debería establecer y obtener identificacion correctamente")
    void testIdentificacion(String identificacion) {
        // Given & When
        cliente.setIdentificacion(identificacion);

        // Then
        assertEquals(identificacion, cliente.getIdentificacion());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Juan Pérez", "María García", "Carlos López"})
    @DisplayName("Debería establecer y obtener nombre correctamente")
    void testNombre(String nombre) {
        // Given & When
        cliente.setNombre(nombre);

        // Then
        assertEquals(nombre, cliente.getNombre());
    }

    @ParameterizedTest
    @ValueSource(strings = {"M", "F"})
    @DisplayName("Debería establecer y obtener genero correctamente")
    void testGenero(String genero) {
        // Given & When
        cliente.setGenero(genero);

        // Then
        assertEquals(genero, cliente.getGenero());
    }

    @ParameterizedTest
    @ValueSource(ints = {18, 25, 35, 50, 65})
    @DisplayName("Debería establecer y obtener edad correctamente")
    void testEdad(int edad) {
        // Given & When
        cliente.setEdad(edad);

        // Then
        assertEquals(edad, cliente.getEdad());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Calle Principal 123", "Avenida Central 456", "Plaza Mayor 789"})
    @DisplayName("Debería establecer y obtener direccion correctamente")
    void testDireccion(String direccion) {
        // Given & When
        cliente.setDireccion(direccion);

        // Then
        assertEquals(direccion, cliente.getDireccion());
    }

    @ParameterizedTest
    @ValueSource(strings = {"555-1234", "555-5678", "555-9012"})
    @DisplayName("Debería establecer y obtener telefono correctamente")
    void testTelefono(String telefono) {
        // Given & When
        cliente.setTelefono(telefono);

        // Then
        assertEquals(telefono, cliente.getTelefono());
    }

    @ParameterizedTest
    @ValueSource(strings = {"password123", "secret456", "secure789"})
    @DisplayName("Debería establecer y obtener contrasena correctamente")
    void testContrasena(String contrasena) {
        // Given & When
        cliente.setContrasena(contrasena);

        // Then
        assertEquals(contrasena, cliente.getContrasena());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVO", "INACTIVO", "SUSPENDIDO"})
    @DisplayName("Debería establecer y obtener estado correctamente")
    void testEstado(String estado) {
        // Given & When
        cliente.setEstado(estado);

        // Then
        assertEquals(estado, cliente.getEstado());
    }

    @Test
    @DisplayName("Debería inicializar con valores nulos")
    void testInicializacionConValoresNulos() {
        // Then
        assertNull(cliente.getClienteid());
        assertNull(cliente.getIdentificacion());
        assertNull(cliente.getNombre());
        assertNull(cliente.getGenero());
        assertNull(cliente.getEdad());
        assertNull(cliente.getDireccion());
        assertNull(cliente.getTelefono());
        assertNull(cliente.getContrasena());
        assertNull(cliente.getEstado());
    }

    @Test
    @DisplayName("Debería establecer todos los campos correctamente")
    void testEstablecerTodosLosCampos() {
        // Given
        String clienteid = "CLI001";
        String identificacion = "12345678";
        String nombre = "Juan Pérez";
        String genero = "M";
        Integer edad = 30;
        String direccion = "Calle Principal 123";
        String telefono = "555-1234";
        String contrasena = "password123";
        String estado = "ACTIVO";

        // When
        cliente.setClienteid(clienteid);
        cliente.setIdentificacion(identificacion);
        cliente.setNombre(nombre);
        cliente.setGenero(genero);
        cliente.setEdad(edad);
        cliente.setDireccion(direccion);
        cliente.setTelefono(telefono);
        cliente.setContrasena(contrasena);
        cliente.setEstado(estado);

        // Then
        assertEquals(clienteid, cliente.getClienteid());
        assertEquals(identificacion, cliente.getIdentificacion());
        assertEquals(nombre, cliente.getNombre());
        assertEquals(genero, cliente.getGenero());
        assertEquals(edad, cliente.getEdad());
        assertEquals(direccion, cliente.getDireccion());
        assertEquals(telefono, cliente.getTelefono());
        assertEquals(contrasena, cliente.getContrasena());
        assertEquals(estado, cliente.getEstado());
    }

    @Test
    @DisplayName("Debería manejar edad nula correctamente")
    void testEdadNula() {
        // Given & When
        cliente.setEdad(null);

        // Then
        assertNull(cliente.getEdad());
    }

    @Test
    @DisplayName("Debería manejar edad cero correctamente")
    void testEdadCero() {
        // Given & When
        Integer edad = 0;
        cliente.setEdad(edad);

        // Then
        assertEquals(edad, cliente.getEdad());
    }
} 