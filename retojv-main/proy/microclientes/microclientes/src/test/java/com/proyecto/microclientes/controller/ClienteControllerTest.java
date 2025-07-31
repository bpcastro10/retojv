package com.proyecto.microclientes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.microclientes.dto.ClienteDTO;
import com.proyecto.microclientes.entity.Cliente;
import com.proyecto.microclientes.exception.GlobalExceptionHandler;
import com.proyecto.microclientes.service.ClienteService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de Integración - ClienteController")
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClienteController clienteController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Cliente cliente;
    private ClienteDTO clienteDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clienteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Configurar cliente de prueba
        cliente = new Cliente();
        cliente.setClienteid("CLI001");
        cliente.setIdentificacion("12345678");
        cliente.setNombre("Juan Pérez");
        cliente.setGenero("M");
        cliente.setEdad(30);
        cliente.setDireccion("Calle Principal 123");
        cliente.setTelefono("555-1234");
        cliente.setContrasena("Password123!");
        cliente.setEstado("ACTIVO");

        clienteDTO = new ClienteDTO();
        clienteDTO.setClienteid("CLI001");
        clienteDTO.setIdentificacion("12345678");
        clienteDTO.setNombre("Juan Pérez");
        clienteDTO.setGenero("M");
        clienteDTO.setEdad(30);
        clienteDTO.setDireccion("Calle Principal 123");
        clienteDTO.setTelefono("555-1234");
        clienteDTO.setContrasena("Password123!");
        clienteDTO.setEstado("ACTIVO");
    }

    @Test
    @DisplayName("GET /clientes - Debería listar todos los clientes correctamente")
    void testListarClientes() throws Exception {
        // Given
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteService.listar()).thenReturn(clientes);
        when(modelMapper.map(cliente, ClienteDTO.class)).thenReturn(clienteDTO);

        // When & Then
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].clienteid").value("CLI001"))
                .andExpect(jsonPath("$[0].nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("GET /clientes/{clienteid} - Debería buscar cliente por ID correctamente")
    void testBuscarClientePorId() throws Exception {
        // Given
        when(clienteService.buscarPorId("CLI001")).thenReturn(cliente);
        when(modelMapper.map(cliente, ClienteDTO.class)).thenReturn(clienteDTO);

        // When & Then
        mockMvc.perform(get("/clientes/CLI001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clienteid").value("CLI001"))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.identificacion").value("12345678"));
    }

    @Test
    @DisplayName("POST /clientes - Debería crear cliente correctamente")
    void testCrearCliente() throws Exception {
        // Given
        when(modelMapper.map(clienteDTO, Cliente.class)).thenReturn(cliente);
        when(clienteService.guardar(cliente)).thenReturn(cliente);
        when(modelMapper.map(cliente, ClienteDTO.class)).thenReturn(clienteDTO);

        // When & Then
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clienteid").value("CLI001"))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));
    }

    @Test
    @DisplayName("PUT /clientes/{clienteid} - Debería actualizar cliente correctamente")
    void testActualizarCliente() throws Exception {
        // Given
        ClienteDTO clienteActualizado = new ClienteDTO();
        clienteActualizado.setClienteid("CLI001");
        clienteActualizado.setIdentificacion("12345678");
        clienteActualizado.setNombre("Juan Pérez Actualizado");
        clienteActualizado.setGenero("M");
        clienteActualizado.setEdad(31);
        clienteActualizado.setDireccion("Nueva Dirección 456");
        clienteActualizado.setTelefono("555-9999");
        clienteActualizado.setContrasena("NewPassword123!");
        clienteActualizado.setEstado("ACTIVO");

        Cliente clienteActualizadoEntity = new Cliente();
        clienteActualizadoEntity.setClienteid("CLI001");
        clienteActualizadoEntity.setIdentificacion("12345678");
        clienteActualizadoEntity.setNombre("Juan Pérez Actualizado");
        clienteActualizadoEntity.setGenero("M");
        clienteActualizadoEntity.setEdad(31);
        clienteActualizadoEntity.setDireccion("Nueva Dirección 456");
        clienteActualizadoEntity.setTelefono("555-9999");
        clienteActualizadoEntity.setContrasena("NewPassword123!");
        clienteActualizadoEntity.setEstado("ACTIVO");

        when(modelMapper.map(clienteActualizado, Cliente.class)).thenReturn(clienteActualizadoEntity);
        when(clienteService.guardar(any(Cliente.class))).thenReturn(clienteActualizadoEntity);
        when(modelMapper.map(clienteActualizadoEntity, ClienteDTO.class)).thenReturn(clienteActualizado);

        // When & Then
        mockMvc.perform(put("/clientes/CLI001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteActualizado)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clienteid").value("CLI001"))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez Actualizado"))
                .andExpect(jsonPath("$.edad").value(31));
    }

    @Test
    @DisplayName("DELETE /clientes/{clienteid} - Debería eliminar cliente correctamente")
    void testEliminarCliente() throws Exception {
        // Given
        doNothing().when(clienteService).eliminar("CLI001");

        // When & Then
        mockMvc.perform(delete("/clientes/CLI001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /clientes - Debería rechazar cliente con datos inválidos")
    void testCrearClienteConDatosInvalidos() throws Exception {
        // Given
        ClienteDTO clienteInvalido = new ClienteDTO();
        clienteInvalido.setClienteid("cli"); // ID muy corto
        clienteInvalido.setIdentificacion("123"); // Identificación muy corta
        clienteInvalido.setNombre("J"); // Nombre muy corto
        clienteInvalido.setGenero("X"); // Género inválido
        clienteInvalido.setEdad(15); // Edad menor de 18
        clienteInvalido.setDireccion("123"); // Dirección muy corta
        clienteInvalido.setTelefono("123"); // Teléfono muy corto
        clienteInvalido.setContrasena("weak"); // Contraseña débil
        clienteInvalido.setEstado("PENDIENTE"); // Estado inválido

        // When & Then
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    @DisplayName("PUT /clientes/{clienteid} - Debería rechazar actualización con datos inválidos")
    void testActualizarClienteConDatosInvalidos() throws Exception {
        // Given
        ClienteDTO clienteInvalido = new ClienteDTO();
        clienteInvalido.setClienteid("CLI001");
        clienteInvalido.setIdentificacion("12345678");
        clienteInvalido.setNombre("Juan Pérez");
        clienteInvalido.setGenero("M");
        clienteInvalido.setEdad(30);
        clienteInvalido.setDireccion("Calle Principal 123");
        clienteInvalido.setTelefono("555-1234");
        clienteInvalido.setContrasena("weak"); // Contraseña débil
        clienteInvalido.setEstado("ACTIVO");

        // When & Then
        mockMvc.perform(put("/clientes/CLI001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    @DisplayName("POST /clientes - Debería aceptar cliente con datos válidos")
    void testCrearClienteConDatosValidos() throws Exception {
        // Given
        ClienteDTO clienteValido = new ClienteDTO();
        clienteValido.setClienteid("CLI002");
        clienteValido.setIdentificacion("87654321");
        clienteValido.setNombre("María García");
        clienteValido.setGenero("F");
        clienteValido.setEdad(25);
        clienteValido.setDireccion("Avenida Central 456");
        clienteValido.setTelefono("555-5678");
        clienteValido.setContrasena("SecurePass123!");
        clienteValido.setEstado("ACTIVO");

        Cliente clienteValidoEntity = new Cliente();
        clienteValidoEntity.setClienteid("CLI002");
        clienteValidoEntity.setIdentificacion("87654321");
        clienteValidoEntity.setNombre("María García");
        clienteValidoEntity.setGenero("F");
        clienteValidoEntity.setEdad(25);
        clienteValidoEntity.setDireccion("Avenida Central 456");
        clienteValidoEntity.setTelefono("555-5678");
        clienteValidoEntity.setContrasena("SecurePass123!");
        clienteValidoEntity.setEstado("ACTIVO");

        when(modelMapper.map(clienteValido, Cliente.class)).thenReturn(clienteValidoEntity);
        when(clienteService.guardar(clienteValidoEntity)).thenReturn(clienteValidoEntity);
        when(modelMapper.map(clienteValidoEntity, ClienteDTO.class)).thenReturn(clienteValido);

        // When & Then
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteValido)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clienteid").value("CLI002"))
                .andExpect(jsonPath("$.nombre").value("María García"))
                .andExpect(jsonPath("$.genero").value("F"))
                .andExpect(jsonPath("$.edad").value(25));
    }
} 