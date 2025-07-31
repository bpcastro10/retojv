package com.proyecto.microclientes.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@DisplayName("Pruebas End-to-End - Cliente")
class ClienteEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/clientes";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Limpiar la base de datos antes de cada prueba
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("Debería crear, listar, buscar, actualizar y eliminar cliente correctamente")
    void testCRUDCompleto() {
        // 1. CREAR CLIENTE
        ClienteDTO clienteDTO = crearClienteDTOValido();
        HttpEntity<ClienteDTO> request = new HttpEntity<>(clienteDTO, headers);
        
        ResponseEntity<ClienteDTO> response = restTemplate.postForEntity(baseUrl, request, ClienteDTO.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CLI001", response.getBody().getClienteid());
        assertEquals("Juan Pérez", response.getBody().getNombre());
        
        // 2. LISTAR CLIENTES
        ResponseEntity<ClienteDTO[]> listResponse = restTemplate.getForEntity(baseUrl, ClienteDTO[].class);
        
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertEquals(1, listResponse.getBody().length);
        assertEquals("CLI001", listResponse.getBody()[0].getClienteid());
        
        // 3. BUSCAR CLIENTE POR ID
        ResponseEntity<ClienteDTO> getResponse = restTemplate.getForEntity(baseUrl + "/CLI001", ClienteDTO.class);
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals("CLI001", getResponse.getBody().getClienteid());
        assertEquals("Juan Pérez", getResponse.getBody().getNombre());
        
        // 4. ACTUALIZAR CLIENTE
        ClienteDTO clienteActualizado = crearClienteDTOValido();
        clienteActualizado.setNombre("Juan Pérez Actualizado");
        clienteActualizado.setEdad(31);
        clienteActualizado.setDireccion("Nueva Dirección 456");
        
        HttpEntity<ClienteDTO> updateRequest = new HttpEntity<>(clienteActualizado, headers);
        ResponseEntity<ClienteDTO> updateResponse = restTemplate.exchange(
            baseUrl + "/CLI001", 
            HttpMethod.PUT, 
            updateRequest, 
            ClienteDTO.class
        );
        
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("Juan Pérez Actualizado", updateResponse.getBody().getNombre());
        assertEquals(31, updateResponse.getBody().getEdad());
        
        // 5. VERIFICAR ACTUALIZACIÓN
        ResponseEntity<ClienteDTO> verifyResponse = restTemplate.getForEntity(baseUrl + "/CLI001", ClienteDTO.class);
        
        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
        assertEquals("Juan Pérez Actualizado", verifyResponse.getBody().getNombre());
        
        // 6. ELIMINAR CLIENTE
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            baseUrl + "/CLI001", 
            HttpMethod.DELETE, 
            null, 
            Void.class
        );
        
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
        
        // 7. VERIFICAR ELIMINACIÓN
        ResponseEntity<ClienteDTO[]> finalListResponse = restTemplate.getForEntity(baseUrl, ClienteDTO[].class);
        
        assertEquals(HttpStatus.OK, finalListResponse.getStatusCode());
        assertEquals(0, finalListResponse.getBody().length);
    }

    @Test
    @DisplayName("Debería rechazar cliente con datos inválidos en POST")
    void testRechazarClienteInvalidoEnPOST() {
        ClienteDTO clienteInvalido = crearClienteDTOInvalido();
        HttpEntity<ClienteDTO> request = new HttpEntity<>(clienteInvalido, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Validation Error"));
        assertTrue(response.getBody().contains("details"));
    }

    @Test
    @DisplayName("Debería rechazar cliente con datos inválidos en PUT")
    void testRechazarClienteInvalidoEnPUT() {
        // Primero crear un cliente válido
        ClienteDTO clienteValido = crearClienteDTOValido();
        HttpEntity<ClienteDTO> createRequest = new HttpEntity<>(clienteValido, headers);
        restTemplate.postForEntity(baseUrl, createRequest, ClienteDTO.class);
        
        // Luego intentar actualizar con datos inválidos
        ClienteDTO clienteInvalido = crearClienteDTOValido();
        clienteInvalido.setContrasena("weak"); // Contraseña débil
        clienteInvalido.setEdad(15); // Edad menor de 18
        
        HttpEntity<ClienteDTO> updateRequest = new HttpEntity<>(clienteInvalido, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/CLI001", 
            HttpMethod.PUT, 
            updateRequest, 
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Validation Error"));
    }

    @Test
    @DisplayName("Debería manejar cliente no encontrado")
    void testClienteNoEncontrado() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/CLI999", String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Cliente no encontrado"));
    }

    @Test
    @DisplayName("Debería manejar múltiples clientes correctamente")
    void testMultiplesClientes() {
        // Crear primer cliente
        ClienteDTO cliente1 = crearClienteDTOValido();
        cliente1.setClienteid("CLI001");
        cliente1.setNombre("Juan Pérez");
        
        HttpEntity<ClienteDTO> request1 = new HttpEntity<>(cliente1, headers);
        restTemplate.postForEntity(baseUrl, request1, ClienteDTO.class);
        
        // Crear segundo cliente
        ClienteDTO cliente2 = crearClienteDTOValido();
        cliente2.setClienteid("CLI002");
        cliente2.setNombre("María García");
        cliente2.setIdentificacion("87654321");
        cliente2.setGenero("F");
        cliente2.setEdad(25);
        
        HttpEntity<ClienteDTO> request2 = new HttpEntity<>(cliente2, headers);
        restTemplate.postForEntity(baseUrl, request2, ClienteDTO.class);
        
        // Listar todos los clientes
        ResponseEntity<ClienteDTO[]> response = restTemplate.getForEntity(baseUrl, ClienteDTO[].class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().length);
        
        // Verificar que ambos clientes están en la lista
        boolean cliente1Encontrado = false;
        boolean cliente2Encontrado = false;
        
        for (ClienteDTO cliente : response.getBody()) {
            if ("CLI001".equals(cliente.getClienteid()) && "Juan Pérez".equals(cliente.getNombre())) {
                cliente1Encontrado = true;
            }
            if ("CLI002".equals(cliente.getClienteid()) && "María García".equals(cliente.getNombre())) {
                cliente2Encontrado = true;
            }
        }
        
        assertTrue(cliente1Encontrado);
        assertTrue(cliente2Encontrado);
    }

    @Test
    @DisplayName("Debería validar todos los campos requeridos")
    void testValidarCamposRequeridos() {
        ClienteDTO clienteIncompleto = new ClienteDTO();
        // Solo establecer algunos campos, faltan otros
        clienteIncompleto.setClienteid("CLI001");
        clienteIncompleto.setNombre("Juan Pérez");
        
        HttpEntity<ClienteDTO> request = new HttpEntity<>(clienteIncompleto, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Validation Error"));
    }

    private ClienteDTO crearClienteDTOValido() {
        ClienteDTO cliente = new ClienteDTO();
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

    private ClienteDTO crearClienteDTOInvalido() {
        ClienteDTO cliente = new ClienteDTO();
        cliente.setClienteid("cli"); // ID muy corto
        cliente.setIdentificacion("123"); // Identificación muy corta
        cliente.setNombre("J"); // Nombre muy corto
        cliente.setGenero("X"); // Género inválido
        cliente.setEdad(15); // Edad menor de 18
        cliente.setDireccion("123"); // Dirección muy corta
        cliente.setTelefono("123"); // Teléfono muy corto
        cliente.setContrasena("weak"); // Contraseña débil
        cliente.setEstado("PENDIENTE"); // Estado inválido
        return cliente;
    }
} 