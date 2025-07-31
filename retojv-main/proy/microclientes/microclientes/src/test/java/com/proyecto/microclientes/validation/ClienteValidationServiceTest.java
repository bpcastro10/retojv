package com.proyecto.microclientes.validation;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.proyecto.microclientes.entity.Cliente;

@DisplayName("Pruebas Unitarias - ClienteValidationService")
class ClienteValidationServiceTest {

    private ClienteValidationService validationService;
    private Cliente clienteValido;

    @BeforeEach
    void setUp() {
        validationService = new ClienteValidationService();
        
        // Configurar cliente válido
        clienteValido = new Cliente();
        clienteValido.setClienteid("CLI001");
        clienteValido.setIdentificacion("12345678");
        clienteValido.setNombre("Juan Pérez");
        clienteValido.setGenero("M");
        clienteValido.setEdad(30);
        clienteValido.setDireccion("Calle Principal 123");
        clienteValido.setTelefono("555-1234");
        clienteValido.setContrasena("Password123!");
        clienteValido.setEstado("ACTIVO");
    }

    @Test
    @DisplayName("Debería validar cliente con datos correctos")
    void testClienteValido() {
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        assertTrue(errors.isEmpty(), "No debería haber errores para un cliente válido");
    }

    @Test
    @DisplayName("Debería detectar edad menor de 18 años")
    void testEdadMenorDe18() {
        clienteValido.setEdad(17);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("edad"));
        assertEquals("El cliente debe ser mayor de edad (mínimo 18 años)", errors.get("edad"));
    }

    @Test
    @DisplayName("Debería detectar edad mayor de 120 años")
    void testEdadMayorDe120() {
        clienteValido.setEdad(121);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("edad"));
        assertEquals("La edad proporcionada no es realista (máximo 120 años)", errors.get("edad"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "123456789012345678901"})
    @DisplayName("Debería detectar identificación con formato incorrecto")
    void testIdentificacionFormatoIncorrecto(String identificacion) {
        clienteValido.setIdentificacion(identificacion);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("identificacion"));
        assertTrue(errors.get("identificacion").contains("debe contener solo números"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "12345678901234567890"})
    @DisplayName("Debería aceptar identificación con formato correcto")
    void testIdentificacionFormatoCorrecto(String identificacion) {
        clienteValido.setIdentificacion(identificacion);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertFalse(errors.containsKey("identificacion"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "123456789012345678901"})
    @DisplayName("Debería detectar teléfono con formato incorrecto")
    void testTelefonoFormatoIncorrecto(String telefono) {
        clienteValido.setTelefono(telefono);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("telefono"));
        assertTrue(errors.get("telefono").contains("formato del teléfono no es válido"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"555-1234", "(555) 123-4567", "+1-555-123-4567"})
    @DisplayName("Debería aceptar teléfono con formato correcto")
    void testTelefonoFormatoCorrecto(String telefono) {
        clienteValido.setTelefono(telefono);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertFalse(errors.containsKey("telefono"));
    }

    @Test
    @DisplayName("Debería detectar contraseña sin letra mayúscula")
    void testContrasenaSinMayuscula() {
        clienteValido.setContrasena("password123!");
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("contrasena"));
        assertTrue(errors.get("contrasena").contains("mayúscula"));
    }

    @Test
    @DisplayName("Debería detectar contraseña sin letra minúscula")
    void testContrasenaSinMinuscula() {
        clienteValido.setContrasena("PASSWORD123!");
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("contrasena"));
        assertTrue(errors.get("contrasena").contains("minúscula"));
    }

    @Test
    @DisplayName("Debería detectar contraseña sin número")
    void testContrasenaSinNumero() {
        clienteValido.setContrasena("Password!");
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("contrasena"));
        assertTrue(errors.get("contrasena").contains("número"));
    }

    @Test
    @DisplayName("Debería detectar contraseña sin carácter especial")
    void testContrasenaSinCaracterEspecial() {
        clienteValido.setContrasena("Password123");
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("contrasena"));
        assertTrue(errors.get("contrasena").contains("carácter especial"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVO", "INACTIVO", "SUSPENDIDO"})
    @DisplayName("Debería aceptar estados válidos")
    void testEstadosValidos(String estado) {
        clienteValido.setEstado(estado);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertFalse(errors.containsKey("estado"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"activo", "PENDIENTE", "BLOQUEADO", ""})
    @DisplayName("Debería detectar estados inválidos")
    void testEstadosInvalidos(String estado) {
        clienteValido.setEstado(estado);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("estado"));
        assertTrue(errors.get("estado").contains("debe ser 'ACTIVO', 'INACTIVO' o 'SUSPENDIDO'"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"M", "F"})
    @DisplayName("Debería aceptar géneros válidos")
    void testGenerosValidos(String genero) {
        clienteValido.setGenero(genero);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertFalse(errors.containsKey("genero"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"m", "f", "Masculino", "Femenino", ""})
    @DisplayName("Debería detectar géneros inválidos")
    void testGenerosInvalidos(String genero) {
        clienteValido.setGenero(genero);
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("genero"));
        assertTrue(errors.get("genero").contains("debe ser 'M' o 'F'"));
    }

    @Test
    @DisplayName("Debería detectar nombre con caracteres inválidos")
    void testNombreConCaracteresInvalidos() {
        clienteValido.setNombre("Juan123 Pérez");
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("nombre"));
        assertTrue(errors.get("nombre").contains("solo letras y espacios"));
    }

    @Test
    @DisplayName("Debería detectar nombre muy corto")
    void testNombreMuyCorto() {
        clienteValido.setNombre("J");
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("nombre"));
        assertTrue(errors.get("nombre").contains("al menos 2 caracteres"));
    }

    @Test
    @DisplayName("Debería detectar dirección muy corta")
    void testDireccionMuyCorta() {
        clienteValido.setDireccion("123");
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("direccion"));
        assertTrue(errors.get("direccion").contains("al menos 5 caracteres"));
    }

    @Test
    @DisplayName("Debería validar campos obligatorios correctamente")
    void testHasRequiredFields() {
        assertTrue(validationService.hasRequiredFields(clienteValido));
        
        // Probar con campos faltantes
        Cliente clienteIncompleto = new Cliente();
        clienteIncompleto.setClienteid("CLI001");
        clienteIncompleto.setNombre("Juan Pérez");
        // Faltan otros campos
        
        assertFalse(validationService.hasRequiredFields(clienteIncompleto));
    }

    @Test
    @DisplayName("Debería validar integridad referencial")
    void testValidateReferentialIntegrity() {
        Map<String, String> errors = validationService.validateReferentialIntegrity(clienteValido);
        assertTrue(errors.isEmpty());
        
        // Probar con ID inválido
        clienteValido.setClienteid("cli001"); // minúsculas
        errors = validationService.validateReferentialIntegrity(clienteValido);
        assertTrue(errors.containsKey("clienteid"));
    }

    @Test
    @DisplayName("Debería detectar múltiples errores simultáneamente")
    void testMultiplesErrores() {
        clienteValido.setEdad(15);
        clienteValido.setGenero("X");
        clienteValido.setEstado("PENDIENTE");
        clienteValido.setContrasena("weak");
        
        Map<String, String> errors = validationService.validateCliente(clienteValido);
        
        assertTrue(errors.containsKey("edad"));
        assertTrue(errors.containsKey("genero"));
        assertTrue(errors.containsKey("estado"));
        assertTrue(errors.containsKey("contrasena"));
        assertEquals(4, errors.size());
    }
} 