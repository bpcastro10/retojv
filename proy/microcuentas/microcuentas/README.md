# Microservicio de Cuentas

## Descripción
Microservicio para la gestión de cuentas bancarias y movimientos financieros, implementado con Spring Boot 3.2.3 y Java 17.

## Características Implementadas

### ✅ Refactorización Completa con Lombok
- **Entidades**: Uso de anotaciones Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- **DTOs**: Implementación de Lombok con validaciones Bean Validation
- **Servicios**: Eliminación de `@Autowired` usando inyección por constructor con `@RequiredArgsConstructor`
- **Controladores**: Código limpio sin manejo manual de excepciones

### ✅ Relación One-to-Many Implementada
- **Cuenta → Movimiento**: Relación bidireccional con `@OneToMany` y `@ManyToOne`
- **Cascade**: Configuración de cascada para operaciones en cascada
- **Lazy Loading**: Optimización de consultas con `FetchType.LAZY`
- **Métodos Helper**: Métodos para gestión de la relación bidireccional

### ✅ Global Exception Handler Mejorado
- **Manejo de Validaciones**: Captura automática de errores de validación Bean Validation
- **Respuestas Estructuradas**: Clase `ErrorResponse` con timestamp, status, error y detalles
- **Logging**: Integración con SLF4J para logging estructurado
- **Tipos de Excepción**:
  - `SaldoInsuficienteException`
  - `IllegalArgumentException`
  - `MethodArgumentNotValidException`
  - Excepciones genéricas

### ✅ Validaciones Bean Validation
- **CuentaDTO**:
  - `@NotBlank` para campos obligatorios
  - `@Pattern` para validación de formato (número de cuenta, tipo de cuenta, estado)
  - `@DecimalMin` para validación de saldo mínimo
- **MovimientoDTO**:
  - `@NotNull` para campos obligatorios
  - `@Pattern` para tipo de movimiento
  - `@DecimalMin` para validación de valores monetarios

### ✅ Controladores Limpios
- **Eliminación de Try-Catch**: Manejo centralizado de excepciones
- **Tipado Fuerte**: Uso de tipos específicos en lugar de `ResponseEntity<?>`
- **Validación Automática**: Uso de `@Valid` para validación automática
- **Logging**: Logging estructurado en cada operación

### ✅ Inyección de Dependencias Moderna
- **Constructor Injection**: Uso de `@RequiredArgsConstructor` en lugar de `@Autowired`
- **Dependencias Finales**: Campos marcados como `final` para inmutabilidad
- **Mejor Testabilidad**: Facilita la creación de mocks en pruebas unitarias

## Estructura del Proyecto

```
src/main/java/com/proyecto/microcuentas/
├── entity/
│   ├── Cuenta.java          # Entidad con relación One-to-Many
│   └── Movimiento.java      # Entidad con relación Many-to-One
├── dto/
│   ├── CuentaDTO.java       # DTO con validaciones Bean Validation
│   ├── MovimientoDTO.java   # DTO con validaciones Bean Validation
│   └── ClienteDTO.java      # DTO para información de cliente
├── controller/
│   ├── CuentaController.java    # Controlador limpio sin try-catch
│   ├── MovimientoController.java # Controlador con validaciones
│   └── ReporteController.java    # Controlador de reportes
├── service/
│   ├── CuentaService.java       # Servicio con inyección por constructor
│   └── MovimientoService.java   # Servicio con logging
├── exception/
│   ├── GlobalExceptionHandler.java  # Handler centralizado
│   ├── ErrorResponse.java           # Respuesta de error estructurada
│   └── SaldoInsuficienteException.java
└── config/
    ├── ModelMapperConfig.java      # Configuración de mapeo
    ├── CorsConfig.java             # Configuración CORS
    └── FeignRequestInterceptor.java # Interceptor para Feign
```

## Tecnologías Utilizadas

- **Spring Boot 3.2.3**
- **Java 17**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **ModelMapper**
- **Bean Validation**
- **Spring Cloud OpenFeign**
- **Eureka Client**

## Endpoints Disponibles

### Cuentas
- `POST /cuentas` - Crear cuenta
- `PUT /cuentas/{numeroCuenta}` - Actualizar cuenta
- `DELETE /cuentas/{numeroCuenta}` - Eliminar cuenta
- `GET /cuentas/{numeroCuenta}` - Obtener cuenta
- `GET /cuentas` - Listar todas las cuentas

### Movimientos
- `POST /movimientos` - Crear movimiento
- `GET /movimientos/{id}` - Obtener movimiento
- `GET /movimientos` - Listar todos los movimientos
- `GET /movimientos/cuenta/{numeroCuenta}` - Movimientos por cuenta
- `GET /movimientos/reporte` - Reporte por fechas

### Reportes
- `GET /reportes/estado-cuenta/{numeroCuenta}` - Estado de cuenta
- `GET /reportes/movimientos` - Reporte de movimientos por fechas

## Validaciones Implementadas

### Cuenta
- Número de cuenta: 10 dígitos numéricos
- Tipo de cuenta: AHORRO o CORRIENTE
- Saldo inicial: Mayor a 0
- Estado: ACTIVA o INACTIVA

### Movimiento
- Tipo de movimiento: DEBITO o CREDITO
- Valor: Mayor a 0.01
- Saldo: No negativo
- Número de cuenta: 10 dígitos numéricos

## Ejecución

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar la aplicación
mvn spring-boot:run
```

## Docker

```bash
# Construir imagen
docker build -t microcuentas .

# Ejecutar contenedor
docker run -p 8080:8080 microcuentas
```

## Beneficios de la Refactorización

1. **Código más Limpio**: Eliminación de boilerplate con Lombok
2. **Mejor Mantenibilidad**: Estructura clara y consistente
3. **Validaciones Robustas**: Validación automática de datos de entrada
4. **Manejo de Errores Centralizado**: Respuestas de error consistentes
5. **Mejor Testabilidad**: Inyección por constructor facilita las pruebas
6. **Logging Estructurado**: Trazabilidad completa de operaciones
7. **Relaciones JPA Optimizadas**: Uso correcto de relaciones One-to-Many

