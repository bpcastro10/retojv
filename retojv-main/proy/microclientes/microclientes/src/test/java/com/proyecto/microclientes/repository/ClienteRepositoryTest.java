package com.proyecto.microclientes.repository;

import com.proyecto.microclientes.entity.Cliente;
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

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        // Configurar cliente de prueba
        cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setIdentificacion("12345678");
        cliente.setNombre("Juan Pérez");
        cliente.setGenero("M");
        cliente.setEdad(30);
        cliente.setDireccion("Calle Principal 123");
        cliente.setTelefono("555-1234");
        cliente.setContrasena("password123");
        cliente.setEstado("ACTIVO");
    }

    @Test
    @DisplayName("Debería guardar cliente correctamente")
    void testGuardarCliente() {
        // Act
        Cliente clienteGuardado = clienteRepository.save(cliente);

        // Assert
        assertNotNull(clienteGuardado);
        assertEquals("CLI001", clienteGuardado.getClienteid());
        assertEquals("12345678", clienteGuardado.getIdentificacion());
        assertEquals("Juan Pérez", clienteGuardado.getNombre());
        assertEquals("M", clienteGuardado.getGenero());
        assertEquals(30, clienteGuardado.getEdad());
        assertEquals("Calle Principal 123", clienteGuardado.getDireccion());
        assertEquals("555-1234", clienteGuardado.getTelefono());
        assertEquals("password123", clienteGuardado.getContrasena());
        assertEquals("ACTIVO", clienteGuardado.getEstado());
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
        assertEquals("Juan Pérez", clienteEncontrado.get().getNombre());
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
        cliente2.setIdentificacion("87654321");
        cliente2.setNombre("María García");
        cliente2.setGenero("F");
        cliente2.setEdad(25);
        cliente2.setDireccion("Avenida Central 456");
        cliente2.setTelefono("555-5678");
        cliente2.setContrasena("password456");
        cliente2.setEstado("ACTIVO");

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
        clienteActualizado.setIdentificacion("12345678");
        clienteActualizado.setNombre("Juan Pérez Actualizado");
        clienteActualizado.setGenero("M");
        clienteActualizado.setEdad(31);
        clienteActualizado.setDireccion("Nueva Dirección 456");
        clienteActualizado.setTelefono("555-9999");
        clienteActualizado.setContrasena("nuevaPassword");
        clienteActualizado.setEstado("INACTIVO");

        // Act
        Cliente resultado = clienteRepository.save(clienteActualizado);

        // Assert
        assertEquals("Juan Pérez Actualizado", resultado.getNombre());
        assertEquals(31, resultado.getEdad());
        assertEquals("nuevaPassword", resultado.getContrasena());
        assertEquals("INACTIVO", resultado.getEstado());
        
        // Verificar que se actualizó en la base de datos
        Optional<Cliente> clienteEncontrado = clienteRepository.findById("CLI001");
        assertTrue(clienteEncontrado.isPresent());
        assertEquals("Juan Pérez Actualizado", clienteEncontrado.get().getNombre());
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
    @DisplayName("Debería manejar cliente con campos nulos")
    void testClienteConCamposNulos() {
        // Arrange
        Cliente clienteConNulos = new Cliente();
        clienteConNulos.setClienteid("CLI003");
        // Los demás campos se mantienen nulos

        // Act
        Cliente clienteGuardado = clienteRepository.save(clienteConNulos);

        // Assert
        assertNotNull(clienteGuardado);
        assertEquals("CLI003", clienteGuardado.getClienteid());
        assertNull(clienteGuardado.getNombre());
        assertNull(clienteGuardado.getIdentificacion());
        
        // Verificar que se guardó correctamente
        Optional<Cliente> clienteEncontrado = clienteRepository.findById("CLI003");
        assertTrue(clienteEncontrado.isPresent());
        assertNull(clienteEncontrado.get().getNombre());
    }

    @Test
    @DisplayName("Debería contar el número total de clientes")
    void testContarClientes() {
        // Arrange
        clienteRepository.save(cliente);
        
        Cliente cliente2 = new Cliente();
        cliente2.setClienteid("CLI002");
        cliente2.setIdentificacion("87654321");
        cliente2.setNombre("María García");
        cliente2.setGenero("F");
        cliente2.setEdad(25);
        cliente2.setDireccion("Avenida Central 456");
        cliente2.setTelefono("555-5678");
        cliente2.setContrasena("password456");
        cliente2.setEstado("ACTIVO");
        clienteRepository.save(cliente2);

        // Act
        long totalClientes = clienteRepository.count();

        // Assert
        assertEquals(2, totalClientes);
    }

    @Test
    @DisplayName("Debería manejar múltiples clientes con diferentes datos")
    void testMultiplesClientesConDiferentesDatos() {
        // Arrange
        Cliente cliente2 = new Cliente();
        cliente2.setClienteid("CLI002");
        cliente2.setIdentificacion("87654321");
        cliente2.setNombre("María García");
        cliente2.setGenero("F");
        cliente2.setEdad(25);
        cliente2.setDireccion("Avenida Central 456");
        cliente2.setTelefono("555-5678");
        cliente2.setContrasena("password456");
        cliente2.setEstado("ACTIVO");

        clienteRepository.save(cliente);
        clienteRepository.save(cliente2);

        // Act
        List<Cliente> clientes = clienteRepository.findAll();

        // Assert
        assertEquals(2, clientes.size());
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI001")));
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI002")));
        assertTrue(clientes.stream().anyMatch(c -> c.getIdentificacion().equals("12345678")));
        assertTrue(clientes.stream().anyMatch(c -> c.getIdentificacion().equals("87654321")));
    }
} 