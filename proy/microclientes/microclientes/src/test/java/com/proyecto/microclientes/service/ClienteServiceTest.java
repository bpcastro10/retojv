package com.proyecto.microclientes.service;

import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

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
    @DisplayName("Debería listar todos los clientes correctamente")
    void testListar() {
        // Given
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

        List<Cliente> clientesEsperados = Arrays.asList(cliente, cliente2);
        when(clienteRepository.findAll()).thenReturn(clientesEsperados);

        // When
        List<Cliente> resultado = clienteService.listar();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(clientesEsperados, resultado);
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería buscar cliente por ID correctamente")
    void testBuscarPorId() {
        // Given
        String clienteId = "CLI001";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // When
        Cliente resultado = clienteService.buscarPorId(clienteId);

        // Then
        assertNotNull(resultado);
        assertEquals(cliente, resultado);
        assertEquals(clienteId, resultado.getClienteid());
        verify(clienteRepository, times(1)).findById(clienteId);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando cliente no existe")
    void testBuscarPorIdClienteNoExiste() {
        // Given
        String clienteId = "CLI999";
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.buscarPorId(clienteId);
        });

        assertEquals("Cliente no encontrado con ID: " + clienteId, exception.getMessage());
        verify(clienteRepository, times(1)).findById(clienteId);
    }

    @Test
    @DisplayName("Debería guardar cliente correctamente")
    void testGuardar() {
        // Given
        Cliente clienteNuevo = new Cliente();
        clienteNuevo.setClienteid("CLI003");
        clienteNuevo.setIdentificacion("11223344");
        clienteNuevo.setNombre("Carlos López");
        clienteNuevo.setGenero("M");
        clienteNuevo.setEdad(35);
        clienteNuevo.setDireccion("Plaza Mayor 789");
        clienteNuevo.setTelefono("555-9012");
        clienteNuevo.setContrasena("password789");
        clienteNuevo.setEstado("ACTIVO");

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteNuevo);

        // When
        Cliente resultado = clienteService.guardar(clienteNuevo);

        // Then
        assertNotNull(resultado);
        assertEquals(clienteNuevo, resultado);
        assertEquals("CLI003", resultado.getClienteid());
        assertEquals("Carlos López", resultado.getNombre());
        verify(clienteRepository, times(1)).save(clienteNuevo);
    }

    @Test
    @DisplayName("Debería actualizar cliente existente correctamente")
    void testActualizarCliente() {
        // Given
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setClienteid("CLI001");
        clienteActualizado.setIdentificacion("12345678");
        clienteActualizado.setNombre("Juan Pérez Actualizado");
        clienteActualizado.setGenero("M");
        clienteActualizado.setEdad(31);
        clienteActualizado.setDireccion("Nueva Dirección 456");
        clienteActualizado.setTelefono("555-9999");
        clienteActualizado.setContrasena("nuevaPassword");
        clienteActualizado.setEstado("ACTIVO");

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        // When
        Cliente resultado = clienteService.guardar(clienteActualizado);

        // Then
        assertNotNull(resultado);
        assertEquals(clienteActualizado, resultado);
        assertEquals("Juan Pérez Actualizado", resultado.getNombre());
        assertEquals(31, resultado.getEdad());
        verify(clienteRepository, times(1)).save(clienteActualizado);
    }

    @Test
    @DisplayName("Debería eliminar cliente correctamente")
    void testEliminar() {
        // Given
        String clienteId = "CLI001";
        doNothing().when(clienteRepository).deleteById(clienteId);

        // When
        clienteService.eliminar(clienteId);

        // Then
        verify(clienteRepository, times(1)).deleteById(clienteId);
    }

    @Test
    @DisplayName("Debería manejar cliente con campos nulos")
    void testClienteConCamposNulos() {
        // Given
        Cliente clienteConNulos = new Cliente();
        clienteConNulos.setClienteid("CLI004");
        // Los demás campos se mantienen nulos

        when(clienteRepository.save(clienteConNulos)).thenReturn(clienteConNulos);

        // When
        Cliente resultado = clienteService.guardar(clienteConNulos);

        // Then
        assertNotNull(resultado);
        assertEquals("CLI004", resultado.getClienteid());
        assertNull(resultado.getNombre());
        assertNull(resultado.getIdentificacion());
        verify(clienteRepository, times(1)).save(clienteConNulos);
    }
} 