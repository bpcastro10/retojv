package com.proyecto.microclientes.service;

import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.entity.Persona;
import com.proyecto.microclientes.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

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

        // Configurar cliente de prueba
        cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setContrasena("password123");
        cliente.setEstado("ACTIVO");
        cliente.setPersona(persona);
    }

    @Test
    @DisplayName("Debería listar todos los clientes correctamente")
    void testListar() {
        // Arrange
        List<Cliente> clientesEsperados = Arrays.asList(cliente);
        when(clienteRepository.findAll()).thenReturn(clientesEsperados);

        // Act
        List<Cliente> resultado = clienteService.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(cliente, resultado.get(0));
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay clientes")
    void testListarVacio() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Cliente> resultado = clienteService.listar();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería buscar cliente por ID correctamente")
    void testBuscarPorId() {
        // Arrange
        String clienteId = "CLI001";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        Cliente resultado = clienteService.buscarPorId(clienteId);

        // Assert
        assertNotNull(resultado);
        assertEquals(cliente, resultado);
        assertEquals(clienteId, resultado.getClienteid());
        verify(clienteRepository, times(1)).findById(clienteId);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando cliente no existe")
    void testBuscarPorIdNoExiste() {
        // Arrange
        String clienteId = "CLI999";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId(clienteId);
        });
        verify(clienteRepository, times(1)).findById(clienteId);
    }

    @Test
    @DisplayName("Debería guardar cliente correctamente")
    void testGuardar() {
        // Arrange
        Cliente clienteNuevo = new Cliente();
        clienteNuevo.setClienteid("CLI002");
        clienteNuevo.setContrasena("nuevaPassword");
        clienteNuevo.setEstado("ACTIVO");
        clienteNuevo.setPersona(persona);

        when(clienteRepository.save(clienteNuevo)).thenReturn(clienteNuevo);

        // Act
        Cliente resultado = clienteService.guardar(clienteNuevo);

        // Assert
        assertNotNull(resultado);
        assertEquals(clienteNuevo, resultado);
        assertEquals("CLI002", resultado.getClienteid());
        verify(clienteRepository, times(1)).save(clienteNuevo);
    }

    @Test
    @DisplayName("Debería actualizar cliente existente correctamente")
    void testActualizarCliente() {
        // Arrange
        cliente.setContrasena("passwordActualizada");
        cliente.setEstado("INACTIVO");
        
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        // Act
        Cliente resultado = clienteService.guardar(cliente);

        // Assert
        assertNotNull(resultado);
        assertEquals("passwordActualizada", resultado.getContrasena());
        assertEquals("INACTIVO", resultado.getEstado());
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Debería eliminar cliente correctamente")
    void testEliminar() {
        // Arrange
        String clienteId = "CLI001";
        doNothing().when(clienteRepository).deleteById(clienteId);

        // Act
        clienteService.eliminar(clienteId);

        // Assert
        verify(clienteRepository, times(1)).deleteById(clienteId);
    }

    @Test
    @DisplayName("Debería manejar múltiples clientes en listar")
    void testListarMultiplesClientes() {
        // Arrange
        Cliente cliente2 = new Cliente();
        cliente2.setClienteid("CLI002");
        cliente2.setContrasena("password456");
        cliente2.setEstado("ACTIVO");
        cliente2.setPersona(persona);

        List<Cliente> clientesEsperados = Arrays.asList(cliente, cliente2);
        when(clienteRepository.findAll()).thenReturn(clientesEsperados);

        // Act
        List<Cliente> resultado = clienteService.listar();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("CLI001", resultado.get(0).getClienteid());
        assertEquals("CLI002", resultado.get(1).getClienteid());
        verify(clienteRepository, times(1)).findAll();
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

        when(clienteRepository.save(clienteSinPersona)).thenReturn(clienteSinPersona);

        // Act
        Cliente resultado = clienteService.guardar(clienteSinPersona);

        // Assert
        assertNotNull(resultado);
        assertNull(resultado.getPersona());
        verify(clienteRepository, times(1)).save(clienteSinPersona);
    }
} 