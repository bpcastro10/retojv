package com.proyecto.microclientes.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.microclientes.dto.ClienteDTO;
import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.repository.ClienteRepository;
import com.proyecto.microclientes.service.ClienteService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("Pruebas de Comunicación entre Microservicios")
class ComunicacionMicroserviciosTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/clientes";
        
        // Limpiar la base de datos antes de cada prueba
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("Debería permitir buscar cliente por ID desde otro microservicio")
    void testBuscarClientePorId() {
        // Given - Crear un cliente
        Cliente cliente = crearClienteValido();
        clienteService.guardar(cliente);

        // When - Buscar cliente por ID
        ResponseEntity<ClienteDTO> response = restTemplate.getForEntity(
            baseUrl + "/" + cliente.getClienteid(), 
            ClienteDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(cliente.getClienteid(), response.getBody().getClienteid());
        assertEquals(cliente.getNombre(), response.getBody().getNombre());
        assertEquals(cliente.getIdentificacion(), response.getBody().getIdentificacion());
    }

    @Test
    @DisplayName("Debería permitir buscar cliente por identificación desde otro microservicio")
    void testBuscarClientePorIdentificacion() {
        // Given - Crear un cliente
        Cliente cliente = crearClienteValido();
        clienteService.guardar(cliente);

        // When - Buscar cliente por identificación
        ResponseEntity<ClienteDTO> response = restTemplate.getForEntity(
            baseUrl + "/identificacion/" + cliente.getIdentificacion(), 
            ClienteDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(cliente.getClienteid(), response.getBody().getClienteid());
        assertEquals(cliente.getNombre(), response.getBody().getNombre());
        assertEquals(cliente.getIdentificacion(), response.getBody().getIdentificacion());
    }

    @Test
    @DisplayName("Debería retornar 404 cuando cliente no existe")
    void testClienteNoExiste() {
        // When - Buscar cliente que no existe
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/CLI999", 
            String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Cliente no encontrado"));
    }

    @Test
    @DisplayName("Debería retornar 404 cuando identificación no existe")
    void testIdentificacionNoExiste() {
        // When - Buscar cliente por identificación que no existe
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/identificacion/99999999", 
            String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Cliente no encontrado"));
    }

    @Test
    @DisplayName("Debería listar todos los clientes correctamente")
    void testListarClientes() {
        // Given - Crear múltiples clientes
        Cliente cliente1 = crearClienteValido();
        cliente1.setClienteid("CLI001");
        cliente1.setIdentificacion("12345678");
        clienteService.guardar(cliente1);

        Cliente cliente2 = crearClienteValido();
        cliente2.setClienteid("CLI002");
        cliente2.setIdentificacion("87654321");
        clienteService.guardar(cliente2);

        // When - Listar todos los clientes
        ResponseEntity<ClienteDTO[]> response = restTemplate.getForEntity(
            baseUrl, 
            ClienteDTO[].class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
        
        // Verificar que ambos clientes están en la lista
        boolean cliente1Encontrado = false;
        boolean cliente2Encontrado = false;
        
        for (ClienteDTO cliente : response.getBody()) {
            if ("CLI001".equals(cliente.getClienteid())) {
                cliente1Encontrado = true;
            }
            if ("CLI002".equals(cliente.getClienteid())) {
                cliente2Encontrado = true;
            }
        }
        
        assertTrue(cliente1Encontrado);
        assertTrue(cliente2Encontrado);
    }

    private Cliente crearClienteValido() {
        Cliente cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setIdentificacion("12345678");
        cliente.setNombre("Juan Pérez");
        cliente.setGenero("M");
        cliente.setEdad(30);
        cliente.setDireccion("Calle Principal 123");
        cliente.setTelefono("555-1234");
        cliente.setContrasena("Password123!");
        cliente.setEstado("ACTIVO");
        return cliente;
    }
} 