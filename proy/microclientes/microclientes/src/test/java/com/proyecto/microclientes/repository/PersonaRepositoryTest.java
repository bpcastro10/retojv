package com.proyecto.microclientes.repository;

import com.proyecto.microclientes.entity.Persona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Pruebas de Integración - PersonaRepository")
class PersonaRepositoryTest {

    @Autowired
    private PersonaRepository personaRepository;

    private Persona persona;

    @BeforeEach
    void setUp() {
        // Configurar persona de prueba
        persona = new Persona();
        persona.setIdentificacion("12345678");
        persona.setNombre("Juan Pérez");
        persona.setGenero("M");
        persona.setEdad(30);
        persona.setDireccion("Calle Principal 123");
        persona.setTelefono("555-1234");
    }

    @Test
    @DisplayName("Debería guardar persona correctamente")
    void testGuardarPersona() {
        // Act
        Persona personaGuardada = personaRepository.save(persona);

        // Assert
        assertNotNull(personaGuardada);
        assertEquals("12345678", personaGuardada.getIdentificacion());
        assertEquals("Juan Pérez", personaGuardada.getNombre());
        assertEquals("M", personaGuardada.getGenero());
        assertEquals(30, personaGuardada.getEdad());
        assertEquals("Calle Principal 123", personaGuardada.getDireccion());
        assertEquals("555-1234", personaGuardada.getTelefono());
    }

    @Test
    @DisplayName("Debería encontrar persona por ID")
    void testBuscarPorId() {
        // Arrange
        personaRepository.save(persona);

        // Act
        Optional<Persona> personaEncontrada = personaRepository.findById("12345678");

        // Assert
        assertTrue(personaEncontrada.isPresent());
        assertEquals("12345678", personaEncontrada.get().getIdentificacion());
        assertEquals("Juan Pérez", personaEncontrada.get().getNombre());
    }

    @Test
    @DisplayName("Debería retornar vacío cuando persona no existe")
    void testBuscarPorIdNoExiste() {
        // Act
        Optional<Persona> personaEncontrada = personaRepository.findById("99999999");

        // Assert
        assertFalse(personaEncontrada.isPresent());
    }

    @Test
    @DisplayName("Debería listar todas las personas")
    void testListarTodas() {
        // Arrange
        Persona persona2 = new Persona();
        persona2.setIdentificacion("87654321");
        persona2.setNombre("María García");
        persona2.setGenero("F");
        persona2.setEdad(25);
        persona2.setDireccion("Avenida Central 456");
        persona2.setTelefono("555-9876");

        personaRepository.save(persona);
        personaRepository.save(persona2);

        // Act
        List<Persona> personas = personaRepository.findAll();

        // Assert
        assertEquals(2, personas.size());
        assertTrue(personas.stream().anyMatch(p -> p.getIdentificacion().equals("12345678")));
        assertTrue(personas.stream().anyMatch(p -> p.getIdentificacion().equals("87654321")));
    }

    @Test
    @DisplayName("Debería actualizar persona existente")
    void testActualizarPersona() {
        // Arrange
        personaRepository.save(persona);
        
        Persona personaActualizada = new Persona();
        personaActualizada.setIdentificacion("12345678");
        personaActualizada.setNombre("Juan Carlos Pérez");
        personaActualizada.setGenero("M");
        personaActualizada.setEdad(31);
        personaActualizada.setDireccion("Nueva Dirección 789");
        personaActualizada.setTelefono("555-1111");

        // Act
        Persona resultado = personaRepository.save(personaActualizada);

        // Assert
        assertEquals("Juan Carlos Pérez", resultado.getNombre());
        assertEquals(31, resultado.getEdad());
        assertEquals("Nueva Dirección 789", resultado.getDireccion());
        
        // Verificar que se actualizó en la base de datos
        Optional<Persona> personaEncontrada = personaRepository.findById("12345678");
        assertTrue(personaEncontrada.isPresent());
        assertEquals("Juan Carlos Pérez", personaEncontrada.get().getNombre());
    }

    @Test
    @DisplayName("Debería eliminar persona correctamente")
    void testEliminarPersona() {
        // Arrange
        personaRepository.save(persona);

        // Act
        personaRepository.deleteById("12345678");

        // Assert
        Optional<Persona> personaEncontrada = personaRepository.findById("12345678");
        assertFalse(personaEncontrada.isPresent());
    }

    @Test
    @DisplayName("Debería contar el número total de personas")
    void testContarPersonas() {
        // Arrange
        personaRepository.save(persona);
        
        Persona persona2 = new Persona();
        persona2.setIdentificacion("87654321");
        persona2.setNombre("María García");
        persona2.setGenero("F");
        persona2.setEdad(25);
        persona2.setDireccion("Avenida Central 456");
        persona2.setTelefono("555-9876");
        personaRepository.save(persona2);

        // Act
        long totalPersonas = personaRepository.count();

        // Assert
        assertEquals(2, totalPersonas);
    }

    @Test
    @DisplayName("Debería manejar persona con edad cero")
    void testPersonaConEdadCero() {
        // Arrange
        persona.setEdad(0);
        
        // Act
        Persona personaGuardada = personaRepository.save(persona);

        // Assert
        assertEquals(0, personaGuardada.getEdad());
        
        // Verificar que se guardó correctamente
        Optional<Persona> personaEncontrada = personaRepository.findById("12345678");
        assertTrue(personaEncontrada.isPresent());
        assertEquals(0, personaEncontrada.get().getEdad());
    }

    @Test
    @DisplayName("Debería manejar persona con edad negativa")
    void testPersonaConEdadNegativa() {
        // Arrange
        persona.setEdad(-5);
        
        // Act
        Persona personaGuardada = personaRepository.save(persona);

        // Assert
        assertEquals(-5, personaGuardada.getEdad());
        
        // Verificar que se guardó correctamente
        Optional<Persona> personaEncontrada = personaRepository.findById("12345678");
        assertTrue(personaEncontrada.isPresent());
        assertEquals(-5, personaEncontrada.get().getEdad());
    }

    @Test
    @DisplayName("Debería manejar persona con valores nulos")
    void testPersonaConValoresNulos() {
        // Arrange
        Persona personaNula = new Persona();
        personaNula.setIdentificacion("99999999");
        // Los demás campos se dejan como null
        
        // Act
        Persona personaGuardada = personaRepository.save(personaNula);

        // Assert
        assertNotNull(personaGuardada);
        assertEquals("99999999", personaGuardada.getIdentificacion());
        assertNull(personaGuardada.getNombre());
        assertNull(personaGuardada.getGenero());
        assertNull(personaGuardada.getEdad());
        assertNull(personaGuardada.getDireccion());
        assertNull(personaGuardada.getTelefono());
    }
} 