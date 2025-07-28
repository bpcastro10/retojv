package com.proyecto.microclientes.integration;

import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.entity.Persona;
import com.proyecto.microclientes.repository.ClienteRepository;
import com.proyecto.microclientes.repository.PersonaRepository;
import com.proyecto.microclientes.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Pruebas de Integración - Flujo Completo Cliente")
class ClienteIntegrationTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    private Persona persona;

    @BeforeEach
    void setUp() {
        // Limpiar datos de pruebas anteriores
        clienteRepository.deleteAll();
        personaRepository.deleteAll();

        // Configurar persona de prueba
        persona = new Persona();
        persona.setIdentificacion("12345678");
        persona.setNombre("Juan Pérez");
        persona.setGenero("M");
        persona.setEdad(30);
        persona.setDireccion("Calle Principal 123");
        persona.setTelefono("555-1234");

        // Guardar la persona
        persona = personaRepository.save(persona);
    }

    @Test
    @DisplayName("Debería crear cliente completo a través del servicio")
    void testCrearClienteCompleto() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setContrasena("password123");
        cliente.setEstado("ACTIVO");
        cliente.setPersona(persona);

        // Act
        Cliente clienteGuardado = clienteService.guardar(cliente);

        // Assert
        assertNotNull(clienteGuardado);
        assertEquals("CLI001", clienteGuardado.getClienteid());
        assertEquals("password123", clienteGuardado.getContrasena());
        assertEquals("ACTIVO", clienteGuardado.getEstado());
        assertNotNull(clienteGuardado.getPersona());
        assertEquals("12345678", clienteGuardado.getPersona().getIdentificacion());
        assertEquals("Juan Pérez", clienteGuardado.getPersona().getNombre());
    }

    @Test
    @DisplayName("Debería listar todos los clientes a través del servicio")
    void testListarClientes() {
        // Arrange
        Cliente cliente1 = new Cliente();
        cliente1.setClienteid("CLI001");
        cliente1.setContrasena("password123");
        cliente1.setEstado("ACTIVO");
        cliente1.setPersona(persona);

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

        clienteService.guardar(cliente1);
        clienteService.guardar(cliente2);

        // Act
        List<Cliente> clientes = clienteService.listar();

        // Assert
        assertEquals(2, clientes.size());
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI001")));
        assertTrue(clientes.stream().anyMatch(c -> c.getClienteid().equals("CLI002")));
        assertTrue(clientes.stream().anyMatch(c -> c.getPersona().getIdentificacion().equals("12345678")));
        assertTrue(clientes.stream().anyMatch(c -> c.getPersona().getIdentificacion().equals("87654321")));
    }

    @Test
    @DisplayName("Debería buscar cliente por ID a través del servicio")
    void testBuscarClientePorId() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setContrasena("password123");
        cliente.setEstado("ACTIVO");
        cliente.setPersona(persona);

        clienteService.guardar(cliente);

        // Act
        Cliente clienteEncontrado = clienteService.buscarPorId("CLI001");

        // Assert
        assertNotNull(clienteEncontrado);
        assertEquals("CLI001", clienteEncontrado.getClienteid());
        assertEquals("password123", clienteEncontrado.getContrasena());
        assertEquals("ACTIVO", clienteEncontrado.getEstado());
        assertEquals("12345678", clienteEncontrado.getPersona().getIdentificacion());
    }

    @Test
    @DisplayName("Debería actualizar cliente a través del servicio")
    void testActualizarCliente() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setContrasena("password123");
        cliente.setEstado("ACTIVO");
        cliente.setPersona(persona);

        clienteService.guardar(cliente);

        // Actualizar cliente
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setClienteid("CLI001");
        clienteActualizado.setContrasena("nuevaPassword");
        clienteActualizado.setEstado("INACTIVO");
        clienteActualizado.setPersona(persona);

        // Act
        Cliente resultado = clienteService.guardar(clienteActualizado);

        // Assert
        assertEquals("nuevaPassword", resultado.getContrasena());
        assertEquals("INACTIVO", resultado.getEstado());

        // Verificar que se actualizó en la base de datos
        Cliente clienteVerificado = clienteService.buscarPorId("CLI001");
        assertEquals("nuevaPassword", clienteVerificado.getContrasena());
        assertEquals("INACTIVO", clienteVerificado.getEstado());
    }

    @Test
    @DisplayName("Debería eliminar cliente a través del servicio")
    void testEliminarCliente() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setContrasena("password123");
        cliente.setEstado("ACTIVO");
        cliente.setPersona(persona);

        clienteService.guardar(cliente);

        // Verificar que existe
        assertNotNull(clienteService.buscarPorId("CLI001"));

        // Act
        clienteService.eliminar("CLI001");

        // Assert
        assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId("CLI001");
        });
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
        Cliente clienteGuardado = clienteService.guardar(clienteSinPersona);

        // Assert
        assertNotNull(clienteGuardado);
        assertNull(clienteGuardado.getPersona());

        // Verificar que se guardó correctamente
        Cliente clienteVerificado = clienteService.buscarPorId("CLI003");
        assertNotNull(clienteVerificado);
        assertNull(clienteVerificado.getPersona());
    }

    @Test
    @DisplayName("Debería lanzar excepción al buscar cliente inexistente")
    void testBuscarClienteInexistente() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId("CLI999");
        });
    }

    @Test
    @DisplayName("Debería manejar flujo completo de CRUD")
    void testFlujoCompletoCRUD() {
        // CREATE
        Cliente cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setContrasena("password123");
        cliente.setEstado("ACTIVO");
        cliente.setPersona(persona);

        Cliente clienteCreado = clienteService.guardar(cliente);
        assertNotNull(clienteCreado);
        assertEquals("CLI001", clienteCreado.getClienteid());

        // READ
        Cliente clienteLeido = clienteService.buscarPorId("CLI001");
        assertNotNull(clienteLeido);
        assertEquals("password123", clienteLeido.getContrasena());

        // UPDATE
        clienteLeido.setContrasena("passwordActualizada");
        clienteLeido.setEstado("INACTIVO");
        Cliente clienteActualizado = clienteService.guardar(clienteLeido);
        assertEquals("passwordActualizada", clienteActualizado.getContrasena());
        assertEquals("INACTIVO", clienteActualizado.getEstado());

        // DELETE
        clienteService.eliminar("CLI001");
        assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId("CLI001");
        });
    }

    @Test
    @DisplayName("Debería manejar múltiples operaciones concurrentes")
    void testOperacionesConcurrentes() {
        // Crear múltiples clientes
        for (int i = 1; i <= 5; i++) {
            Persona personaNueva = new Persona();
            personaNueva.setIdentificacion("ID" + String.format("%08d", i));
            personaNueva.setNombre("Persona " + i);
            personaNueva.setGenero("M");
            personaNueva.setEdad(20 + i);
            personaNueva.setDireccion("Dirección " + i);
            personaNueva.setTelefono("555-" + String.format("%04d", i));
            personaNueva = personaRepository.save(personaNueva);

            Cliente cliente = new Cliente();
            cliente.setClienteid("CLI" + String.format("%03d", i));
            cliente.setContrasena("password" + i);
            cliente.setEstado("ACTIVO");
            cliente.setPersona(personaNueva);

            clienteService.guardar(cliente);
        }

        // Verificar que todos se guardaron
        List<Cliente> clientes = clienteService.listar();
        assertEquals(5, clientes.size());

        // Verificar que se pueden buscar individualmente
        for (int i = 1; i <= 5; i++) {
            Cliente cliente = clienteService.buscarPorId("CLI" + String.format("%03d", i));
            assertNotNull(cliente);
            assertEquals("password" + i, cliente.getContrasena());
        }
    }
} 