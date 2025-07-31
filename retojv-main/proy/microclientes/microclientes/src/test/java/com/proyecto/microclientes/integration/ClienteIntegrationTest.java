package com.proyecto.microclientes.integration;

import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.repository.ClienteRepository;
import com.proyecto.microclientes.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Pruebas de Integración - Cliente")
class ClienteIntegrationTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos
        clienteRepository.deleteAll();

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
    @DisplayName("Debería guardar y recuperar cliente correctamente")
    void testGuardarYRecuperarCliente() {
        // Act
        Cliente clienteGuardado = clienteService.guardar(cliente);

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

        // Verificar que se puede recuperar
        Cliente clienteRecuperado = clienteService.buscarPorId("CLI001");
        assertEquals(clienteGuardado, clienteRecuperado);
    }

    @Test
    @DisplayName("Debería listar múltiples clientes correctamente")
    void testListarMultiplesClientes() {
        // Arrange
        clienteService.guardar(cliente);

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
        clienteService.guardar(cliente2);

        // Act
        List<Cliente> clientes = clienteService.listar();

        // Assert
        assertEquals(2, clientes.size());
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI001")));
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI002")));
        assertTrue(clientes.stream().anyMatch(c -> c.getIdentificacion().equals("12345678")));
        assertTrue(clientes.stream().anyMatch(c -> c.getIdentificacion().equals("87654321")));
    }

    @Test
    @DisplayName("Debería actualizar cliente existente correctamente")
    void testActualizarCliente() {
        // Arrange
        clienteService.guardar(cliente);

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
        Cliente resultado = clienteService.guardar(clienteActualizado);

        // Assert
        assertEquals("Juan Pérez Actualizado", resultado.getNombre());
        assertEquals(31, resultado.getEdad());
        assertEquals("nuevaPassword", resultado.getContrasena());
        assertEquals("INACTIVO", resultado.getEstado());

        // Verificar que se actualizó en la base de datos
        Cliente clienteVerificado = clienteService.buscarPorId("CLI001");
        assertEquals("Juan Pérez Actualizado", clienteVerificado.getNombre());
        assertEquals(31, clienteVerificado.getEdad());
    }

    @Test
    @DisplayName("Debería eliminar cliente correctamente")
    void testEliminarCliente() {
        // Arrange
        clienteService.guardar(cliente);

        // Act
        clienteService.eliminar("CLI001");

        // Assert
        assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId("CLI001");
        });
    }

    @Test
    @DisplayName("Debería manejar cliente con campos nulos")
    void testClienteConCamposNulos() {
        // Arrange
        Cliente clienteConNulos = new Cliente();
        clienteConNulos.setClienteid("CLI003");
        // Los demás campos se mantienen nulos

        // Act
        Cliente clienteGuardado = clienteService.guardar(clienteConNulos);

        // Assert
        assertNotNull(clienteGuardado);
        assertEquals("CLI003", clienteGuardado.getClienteid());
        assertNull(clienteGuardado.getNombre());
        assertNull(clienteGuardado.getIdentificacion());

        // Verificar que se guardó correctamente
        Cliente clienteVerificado = clienteService.buscarPorId("CLI003");
        assertNull(clienteVerificado.getNombre());
    }

    @Test
    @DisplayName("Debería lanzar excepción al buscar cliente inexistente")
    void testBuscarClienteInexistente() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId("CLI999");
        });

        assertEquals("Cliente no encontrado con ID: CLI999", exception.getMessage());
    }

    @Test
    @DisplayName("Debería manejar operaciones CRUD completas")
    void testOperacionesCRUDCompletas() {
        // Create
        Cliente clienteCreado = clienteService.guardar(cliente);
        assertNotNull(clienteCreado);
        assertEquals("CLI001", clienteCreado.getClienteid());

        // Read
        Cliente clienteLeido = clienteService.buscarPorId("CLI001");
        assertEquals(clienteCreado, clienteLeido);

        // Update
        clienteLeido.setNombre("Juan Pérez Modificado");
        clienteLeido.setEdad(32);
        Cliente clienteActualizado = clienteService.guardar(clienteLeido);
        assertEquals("Juan Pérez Modificado", clienteActualizado.getNombre());
        assertEquals(32, clienteActualizado.getEdad());

        // Delete
        clienteService.eliminar("CLI001");
        assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId("CLI001");
        });
    }

    @Test
    @DisplayName("Debería manejar múltiples clientes con diferentes datos")
    void testMultiplesClientesConDiferentesDatos() {
        // Arrange - Crear múltiples clientes
        for (int i = 1; i <= 5; i++) {
            Cliente clienteNuevo = new Cliente();
            clienteNuevo.setClienteid("CLI" + String.format("%03d", i));
            clienteNuevo.setIdentificacion("ID" + String.format("%08d", i));
            clienteNuevo.setNombre("Cliente " + i);
            clienteNuevo.setGenero(i % 2 == 0 ? "F" : "M");
            clienteNuevo.setEdad(20 + i);
            clienteNuevo.setDireccion("Dirección " + i);
            clienteNuevo.setTelefono("555-" + String.format("%04d", i));
            clienteNuevo.setContrasena("password" + i);
            clienteNuevo.setEstado("ACTIVO");

            clienteService.guardar(clienteNuevo);
        }

        // Act
        List<Cliente> clientes = clienteService.listar();

        // Assert
        assertEquals(5, clientes.size());
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI001")));
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI005")));
        assertTrue(clientes.stream().anyMatch(c -> c.getNombre().equals("Cliente 1")));
        assertTrue(clientes.stream().anyMatch(c -> c.getNombre().equals("Cliente 5")));
    }
} 