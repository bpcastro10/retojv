package com.proyecto.microclientes.repository;

import com.proyecto.microclientes.entity.Cliente;
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
@DisplayName("Pruebas de Integración - ClienteRepository")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    private Cliente cliente;
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

        // Guardar la persona primero
        persona = personaRepository.save(persona);

        // Configurar cliente de prueba
        cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setContrasena("password123");
        cliente.setEstado("ACTIVO");
        cliente.setPersona(persona);
    }

    @Test
    @DisplayName("Debería guardar cliente correctamente")
    void testGuardarCliente() {
        // Act
        Cliente clienteGuardado = clienteRepository.save(cliente);

        // Assert
        assertNotNull(clienteGuardado);
        assertEquals("CLI001", clienteGuardado.getClienteid());
        assertEquals("password123", clienteGuardado.getContrasena());
        assertEquals("ACTIVO", clienteGuardado.getEstado());
        assertNotNull(clienteGuardado.getPersona());
        assertEquals("12345678", clienteGuardado.getPersona().getIdentificacion());
    }

    @Test
    @DisplayName("Debería encontrar cliente por ID")
    void testBuscarPorId() {
        // Arrange
        clienteRepository.save(cliente);

        // Act
        Optional<Cliente> clienteEncontrado = clienteRepository.findById("CLI001");

        // Assert
        assertTrue(clienteEncontrado.isPresent());
        assertEquals("CLI001", clienteEncontrado.get().getClienteid());
        assertEquals("password123", clienteEncontrado.get().getContrasena());
    }

    @Test
    @DisplayName("Debería retornar vacío cuando cliente no existe")
    void testBuscarPorIdNoExiste() {
        // Act
        Optional<Cliente> clienteEncontrado = clienteRepository.findById("CLI999");

        // Assert
        assertFalse(clienteEncontrado.isPresent());
    }

    @Test
    @DisplayName("Debería listar todos los clientes")
    void testListarTodos() {
        // Arrange
        Cliente cliente2 = new Cliente();
        cliente2.setClienteid("CLI002");
        cliente2.setContrasena("password456");
        cliente2.setEstado("ACTIVO");
        cliente2.setPersona(persona);

        clienteRepository.save(cliente);
        clienteRepository.save(cliente2);

        // Act
        List<Cliente> clientes = clienteRepository.findAll();

        // Assert
        assertEquals(2, clientes.size());
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI001")));
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI002")));
    }

    @Test
    @DisplayName("Debería actualizar cliente existente")
    void testActualizarCliente() {
        // Arrange
        clienteRepository.save(cliente);
        
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setClienteid("CLI001");
        clienteActualizado.setContrasena("nuevaPassword");
        clienteActualizado.setEstado("INACTIVO");
        clienteActualizado.setPersona(persona);

        // Act
        Cliente resultado = clienteRepository.save(clienteActualizado);

        // Assert
        assertEquals("nuevaPassword", resultado.getContrasena());
        assertEquals("INACTIVO", resultado.getEstado());
        
        // Verificar que se actualizó en la base de datos
        Optional<Cliente> clienteEncontrado = clienteRepository.findById("CLI001");
        assertTrue(clienteEncontrado.isPresent());
        assertEquals("nuevaPassword", clienteEncontrado.get().getContrasena());
    }

    @Test
    @DisplayName("Debería eliminar cliente correctamente")
    void testEliminarCliente() {
        // Arrange
        clienteRepository.save(cliente);

        // Act
        clienteRepository.deleteById("CLI001");

        // Assert
        Optional<Cliente> clienteEncontrado = clienteRepository.findById("CLI001");
        assertFalse(clienteEncontrado.isPresent());
    }

    @Test
    @DisplayName("Debería manejar cliente con persona nula")
    void testClienteConPersonaNula() {
        // Arrange
        Cliente clienteSinPersona = new Cliente();
        clienteSinPersona.setClienteid("CLI003");
        clienteSinPersona.setContrasena("password789");
        clienteSinPersona.setEstado("ACTIVO");
        clienteSinPersona.setPersona(null);

        // Act
        Cliente clienteGuardado = clienteRepository.save(clienteSinPersona);

        // Assert
        assertNotNull(clienteGuardado);
        assertNull(clienteGuardado.getPersona());
        
        // Verificar que se guardó correctamente
        Optional<Cliente> clienteEncontrado = clienteRepository.findById("CLI003");
        assertTrue(clienteEncontrado.isPresent());
        assertNull(clienteEncontrado.get().getPersona());
    }

    @Test
    @DisplayName("Debería contar el número total de clientes")
    void testContarClientes() {
        // Arrange
        clienteRepository.save(cliente);
        
        Cliente cliente2 = new Cliente();
        cliente2.setClienteid("CLI002");
        cliente2.setContrasena("password456");
        cliente2.setEstado("ACTIVO");
        cliente2.setPersona(persona);
        clienteRepository.save(cliente2);

        // Act
        long totalClientes = clienteRepository.count();

        // Assert
        assertEquals(2, totalClientes);
    }

    @Test
    @DisplayName("Debería manejar múltiples clientes con diferentes personas")
    void testMultiplesClientesConDiferentesPersonas() {
        // Arrange
        Persona persona2 = new Persona();
        persona2.setIdentificacion("87654321");
        persona2.setNombre("María García");
        persona2.setGenero("F");
        persona2.setEdad(25);
        persona2.setDireccion("Avenida Central 456");
        persona2.setTelefono("555-9876");
        persona2 = personaRepository.save(persona2);

        Cliente cliente2 = new Cliente();
        cliente2.setClienteid("CLI002");
        cliente2.setContrasena("password456");
        cliente2.setEstado("ACTIVO");
        cliente2.setPersona(persona2);

        clienteRepository.save(cliente);
        clienteRepository.save(cliente2);

        // Act
        List<Cliente> clientes = clienteRepository.findAll();

        // Assert
        assertEquals(2, clientes.size());
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI001")));
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI002")));
        assertTrue(clientes.stream().anyMatch(c -> c.getPersona().getIdentificacion().equals("12345678")));
        assertTrue(clientes.stream().anyMatch(c -> c.getPersona().getIdentificacion().equals("87654321")));
    }
} 