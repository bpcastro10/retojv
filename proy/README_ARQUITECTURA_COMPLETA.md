# 🏦 SISTEMA BANCARIO MICROSERVICIOS - ARQUITECTURA COMPLETA

## 📋 **TABLA DE CONTENIDOS**

1. [Visión General y Arquitectura](#-visión-general-y-arquitectura)
2. [Funcionamiento Detallado de Microservicios](#-funcionamiento-detallado-de-microservicios)
3. [Tecnologías y Marco Teórico](#-tecnologías-y-marco-teórico)
4. [Principios de Programación Orientada a Objetos](#-principios-de-programación-orientada-a-objetos)
5. [Principios SOLID](#-principios-solid)
6. [Propiedades ACID](#-propiedades-acid)
7. [Tabla de Patrones de Diseño](#-tabla-de-patrones-de-diseño)
8. [Tabla de Decoradores/Anotaciones](#%EF%B8%8F-tabla-de-decoradoresanotaciones)
9. [Tabla de Inyecciones de Dependencias](#-tabla-de-inyecciones-de-dependencias)
10. [Flujos y Relaciones](#-flujos-y-relaciones)
11. [Comunicación WebFlux](#-comunicación-webflux)

---

## 🎯 **VISIÓN GENERAL Y ARQUITECTURA**

### **🏗️ Arquitectura del Sistema**

```
                    🌐 Cliente/Postman
                           ↓
               ┌─────────────────────────┐
               │    API Gateway (8083)   │ ← 🚪 Punto de entrada único
               │  Spring Cloud Gateway   │
               └─────────────────────────┘
                           ↓
               ┌─────────────────────────┐
               │  Eureka Server (8761)   │ ← 🔍 Service Discovery
               │   Netflix Eureka        │
               └─────────────────────────┘
                           ↓
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
┌─────────────────┐ ┌─────────────────┐ WebFlux
│  MICROCLIENTES  │ │   MICROCUENTAS  │ Communication
│   Puerto 8080   │ │   Puerto 8081   │      ↗
│                 │ │                 │     ↙
│ ┌─────────────┐ │ │ ┌─────────────┐ │
│ │Controllers  │ │ │ │Controllers  │ │
│ │    ↓        │ │ │ │    ↓        │ │
│ │Services     │ │ │ │Services     │ │
│ │    ↓        │ │ │ │    ↓        │ │
│ │Repositories │ │ │ │Repositories │ │
│ │    ↓        │ │ │ │    ↓        │ │
│ │Entities     │ │ │ │Entities     │ │
│ │    ↓        │ │ │ │    ↓        │ │
│ │DTOs (Valid) │ │ │ │DTOs (Valid) │ │
│ └─────────────┘ │ │ └─────────────┘ │
└─────────────────┘ └─────────────────┘
        ↓                   ↓
┌─────────────────┐ ┌─────────────────┐
│  PostgreSQL     │ │  PostgreSQL     │
│  microclientes  │ │  microcuentas   │
│  (Puerto 5432)  │ │  (Puerto 5433)  │
└─────────────────┘ └─────────────────┘
```

### **🏢 Componentes del Sistema**

| **Componente** | **Puerto** | **Responsabilidad** | **Tecnología Principal** |
|----------------|------------|--------------------|-----------------------|
| **Gateway** | 8083 | Enrutamiento, CORS, Filtros | Spring Cloud Gateway |
| **Eureka Server** | 8761 | Service Discovery | Netflix Eureka |
| **Microclientes** | 8080 | Gestión clientes/personas | Spring Boot + JPA |
| **Microcuentas** | 8081 | Gestión cuentas/movimientos | Spring Boot + WebFlux |
| **PostgreSQL Clientes** | 5432 | Persistencia clientes | PostgreSQL |
| **PostgreSQL Cuentas** | 5433 | Persistencia cuentas | PostgreSQL |

---

## 🏗️ **FUNCIONAMIENTO DETALLADO DE MICROSERVICIOS**

### **🔍 EUREKA SERVER (Puerto 8761)**

#### **🎯 Propósito:**
Service Discovery que actúa como registro central donde todos los microservicios se registran y descubren automáticamente.

#### **📁 Estructura de Carpetas:**
```
eureka-server/
├── src/main/java/com/proyecto/eureka_server/
│   └── EurekaServerApplication.java      # @EnableEurekaServer
├── src/main/resources/
│   ├── application.properties           # Puerto 8761, configuración servidor
│   └── logback-spring.xml              # Configuración de logs
├── logs/                               # Archivos de log
│   ├── eureka-server-info.log
│   ├── eureka-server-error.log
│   └── eureka-server-debug.log
└── Dockerfile                         # Containerización
```

#### **⚙️ Funcionamiento:**
1. **Startup**: Se inicia en puerto 8761 como servidor standalone
2. **Registration**: Los microservicios se auto-registran enviando heartbeats
3. **Discovery**: Proporciona información de ubicación de servicios
4. **Health Check**: Monitorea estado de salud de instancias registradas
5. **Load Balancing**: Facilita distribución de carga entre instancias

#### **🔧 Configuración Clave:**
```properties
server.port=8761
eureka.client.register-with-eureka=false    # No se registra a sí mismo
eureka.client.fetch-registry=false          # No obtiene registro
eureka.server.enable-self-preservation=true # Modo protección automática
```

---

### **🚪 API GATEWAY (Puerto 8083)**

#### **🎯 Propósito:**
Punto de entrada único que enruta requests a microservicios específicos, maneja CORS, autenticación y filtros.

#### **📁 Estructura de Carpetas:**
```
gateway/gateway/
├── src/main/java/com/proyecto/gateway/
│   ├── GatewayApplication.java          # @SpringBootApplication
│   ├── config/
│   │   └── GatewayConfig.java          # @Configuration RouteLocator
│   ├── controller/
│   │   └── GatewayController.java      # Health checks, info endpoints
│   └── filter/
│       └── LoggingFilter.java          # Custom request/response logging
├── src/main/resources/
│   ├── application.properties          # Rutas, CORS, timeouts
│   └── logback-spring.xml             # Logs estructurados
└── logs/                              # Archivos de log del gateway
```

#### **⚙️ Funcionamiento:**
1. **Request Reception**: Recibe todas las peticiones del cliente
2. **Route Matching**: Analiza URI y determina microservicio destino
3. **Load Balancing**: Distribuye carga entre instancias disponibles
4. **Filtering**: Aplica filtros pre/post procesamiento
5. **Response Forwarding**: Devuelve respuesta al cliente

#### **🛣️ Rutas Configuradas:**
```java
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("clientes-service", r -> r
            .path("/api/v1/clientes/**")
            .filters(f -> f.stripPrefix(1))
            .uri("http://localhost:8080"))
        .route("cuentas-service", r -> r
            .path("/api/v1/cuentas/**")
            .filters(f -> f.stripPrefix(1))
            .uri("http://localhost:8081"))
        .build();
}
```

---

### **👥 MICROCLIENTES (Puerto 8080)**

#### **🎯 Propósito:**
Gestiona toda la información relacionada con clientes y personas, implementando herencia JPA y validaciones centralizadas.

#### **📁 Estructura Detallada de Carpetas:**
```
microclientes/microclientes/
├── src/main/java/com/proyecto/microclientes/
│   ├── MicroclientesApplication.java    # @SpringBootApplication
│   │
│   ├── 📁 entity/                      # Capa de Persistencia
│   │   ├── Persona.java               # @Entity base con @Inheritance(JOINED)
│   │   └── Cliente.java               # @Entity hereda de Persona
│   │
│   ├── 📁 dto/                        # Capa de Transferencia (VALIDACIONES)
│   │   ├── PersonaDTO.java            # DTO base con validaciones Bean Validation
│   │   └── ClienteDTO.java            # DTO hereda PersonaDTO, validaciones específicas
│   │
│   ├── 📁 controller/                 # Capa de Presentación
│   │   └── ClienteController.java     # @RestController, endpoints CRUD + PATCH
│   │
│   ├── 📁 service/                    # Capa de Lógica de Negocio
│   │   └── ClienteService.java        # @Service, lógica CRUD, mapeo DTO↔Entity
│   │
│   ├── 📁 repository/                 # Capa de Acceso a Datos
│   │   └── ClienteRepository.java     # @Repository JPA, queries customizadas
│   │
│   ├── 📁 config/                     # Configuraciones
│   │   ├── CorsConfig.java           # @Configuration CORS global
│   │   └── ModelMapperConfig.java    # @Bean ModelMapper
│   │
│   └── 📁 exception/                  # Manejo de Errores
│       └── GlobalExceptionHandler.java # @ControllerAdvice manejo centralizado
│
├── src/main/resources/
│   ├── application.properties         # Puerto 8080, BD, Eureka client
│   ├── application-dev.properties     # Profile desarrollo
│   ├── BaseDatos.sql                 # Schema + datos iniciales
│   └── logback-spring.xml            # Configuración logs por nivel
│
├── src/test/java/                    # Tests Completos
│   ├── controller/
│   │   └── ClienteControllerTest.java # @WebMvcTest
│   ├── service/
│   │   └── ClienteServiceTest.java    # @MockBean
│   ├── repository/
│   │   └── ClienteRepositoryTest.java # @DataJpaTest
│   ├── integration/
│   │   ├── ClienteIntegrationTest.java     # @SpringBootTest
│   │   ├── ClienteEndToEndTest.java        # E2E completo
│   │   └── ComunicacionMicroserviciosTest.java # Inter-service tests
│   └── entity/
│       └── ClienteTest.java          # Tests entidades
│
├── target/classes/                   # Clases compiladas
├── logs/                            # Logs por categoría
│   ├── microclientes-info.log       # Logs informativos
│   ├── microclientes-error.log      # Logs de errores
│   ├── microclientes-debug.log      # Logs debug desarrollo
│   └── microclientes-sql.log        # Queries SQL Hibernate
└── Dockerfile                       # Containerización
```

#### **⚙️ Funcionamiento por Capas:**

**1. 🌐 Controller Layer (Presentación):**
```java
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {
    
    private final ClienteService service;
    
    @GetMapping                              // GET /clientes
    @PostMapping                             // POST /clientes + @Valid
    @GetMapping("/{clienteid}")              // GET /clientes/{id}
    @PutMapping("/{clienteid}")              // PUT /clientes/{id} + @Valid
    @PatchMapping("/{clienteid}")            // PATCH /clientes/{id} (reflexión)
    @DeleteMapping("/{clienteid}")           // DELETE /clientes/{id}
    @GetMapping("/identificacion/{identificacion}") // GET por identificación
}
```

**2. 🔧 Service Layer (Lógica de Negocio):**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {
    
    private final ClienteRepository repository;
    private final ModelMapper modelMapper;
    
    // ✅ Operaciones CRUD completas
    public ClienteDTO crear(ClienteDTO dto) { /* Validar → Mapear → Guardar */ }
    public ClienteDTO buscarPorId(String id) { /* Buscar → Mapear → Retornar */ }
    public ClienteDTO actualizar(String id, ClienteDTO dto) { /* Buscar → Actualizar → Guardar */ }
    public ClienteDTO actualizarParcial(String id, Map<String, Object> updates) { /* Reflexión */ }
    public void eliminar(String id) { /* Buscar → Eliminar */ }
    
    // ✅ Métodos de consulta específicos
    public List<ClienteDTO> buscarPorEstado(String estado) { /* Query customizada */ }
    public List<ClienteDTO> buscarPorNombre(String nombre) { /* Like search */ }
}
```

**3. 💾 Repository Layer (Acceso a Datos):**
```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    
    // ✅ Queries derivadas automáticas
    Optional<Cliente> findByClienteid(String clienteid);
    List<Cliente> findByEstado(String estado);
    List<Cliente> findByNombreContaining(String nombre);
    
    // ✅ Queries de agregación
    Long countByEstado(String estado);
    Boolean existsByClienteid(String clienteid);
    
    // ✅ Queries customizadas
    @Query("SELECT c FROM Cliente c WHERE c.identificacion = :identificacion")
    Optional<Cliente> findByIdentificacion(@Param("identificacion") String identificacion);
}
```

**4. 📊 Entity Layer (Modelo de Datos):**
```java
// ✅ Entidad base con herencia
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Data @NoArgsConstructor @AllArgsConstructor
public class Persona {
    @Id
    @Column(name = "identificacion", unique = true)
    private String identificacion;  // ← PK principal
    // ... campos comunes
}

// ✅ Entidad derivada
@Entity
@Table(name = "cliente")
@EqualsAndHashCode(callSuper = true)
public class Cliente extends Persona {
    @Column(name = "clienteid", unique = true)
    private String clienteid;  // ← Unique pero no PK
    // ... campos específicos
}
```

---

### **💰 MICROCUENTAS (Puerto 8081)**

#### **🎯 Propósito:**
Gestiona cuentas bancarias, movimientos financieros, reportes y comunicación asíncrona con microclientes vía WebFlux.

#### **📁 Estructura Detallada de Carpetas:**
```
microcuentas/microcuentas/
├── src/main/java/com/proyecto/microcuentas/
│   ├── MicrocuentasApplication.java     # @SpringBootApplication
│   │
│   ├── 📁 entity/                      # Modelo de Datos
│   │   ├── Cuenta.java                # @Entity con @OneToMany a Movimiento
│   │   └── Movimiento.java            # @Entity con @ManyToOne a Cuenta
│   │
│   ├── 📁 dto/                        # DTOs con Validaciones
│   │   ├── CuentaDTO.java             # Validaciones Bean Validation
│   │   ├── MovimientoDTO.java         # DTO respuesta movimientos
│   │   ├── CrearMovimientoDTO.java    # DTO específico creación (Builder)
│   │   └── ClienteDTO.java            # DTO proxy para WebFlux
│   │
│   ├── 📁 controller/                 # APIs REST
│   │   ├── CuentaController.java      # CRUD cuentas
│   │   ├── MovimientoController.java  # CRUD movimientos + lógica DÉBITO/CRÉDITO
│   │   ├── ReporteController.java     # /reportes?fecha=rango, /estado-cuenta
│   │   └── ClienteController.java     # Proxy endpoints a microclientes
│   │
│   ├── 📁 service/                    # Lógica de Negocio
│   │   ├── CuentaService.java         # Lógica cuentas + validación WebFlux cliente
│   │   └── MovimientoService.java     # Lógica movimientos + normalización valores
│   │
│   ├── 📁 repository/                 # Acceso a Datos
│   │   ├── CuentaRepository.java      # Queries cuentas por cliente, estado
│   │   └── MovimientoRepository.java  # Queries movimientos por fecha, cuenta
│   │
│   ├── 📁 client/                     # Comunicación WebFlux
│   │   └── ClienteClient.java         # @Component WebClient a microclientes
│   │
│   ├── 📁 config/                     # Configuraciones
│   │   ├── CorsConfig.java           # CORS global
│   │   ├── ModelMapperConfig.java    # Bean mapeo
│   │   └── WebClientConfig.java      # @Bean WebClient con timeouts
│   │
│   └── 📁 exception/                  # Manejo Errores
│       ├── SaldoInsuficienteException.java # Custom exception
│       ├── ErrorResponse.java         # @Builder respuesta error
│       └── GlobalExceptionHandler.java # @ControllerAdvice centralizado
│
├── src/main/resources/
│   ├── application.properties         # Puerto 8081, BD, WebClient URL
│   ├── application-dev.properties     # Profile desarrollo
│   ├── data.sql                      # Datos iniciales cuentas/movimientos
│   ├── schema.sql                    # Schema BD microcuentas
│   └── logback-spring.xml            # Logs + SQL + WebFlux
│
├── logs/                             # Logs Categorizados
│   ├── microcuentas-info.log         # Info general
│   ├── microcuentas-error.log        # Errores aplicación
│   ├── microcuentas-feign.log        # Logs WebFlux comunicación
│   └── archive/                      # Logs archivados por fecha
│
└── target/classes/                   # Clases compiladas
```

#### **⚙️ Funcionamiento Específico:**

**1. 🏦 Gestión de Cuentas:**
```java
@Service
@RequiredArgsConstructor
public class CuentaService {
    
    private final CuentaRepository repository;
    private final ClienteClient clienteClient;  // ← WebFlux communication
    
    @Transactional
    public CuentaDTO crearCuenta(CuentaDTO cuentaDTO) {
        // 1. ✅ Validar cliente existe y está activo via WebFlux
        validarClienteExisteYActivo(cuentaDTO.getClienteId());
        
        // 2. ✅ Crear cuenta
        Cuenta cuenta = modelMapper.map(cuentaDTO, Cuenta.class);
        Cuenta guardada = repository.save(cuenta);
        
        return modelMapper.map(guardada, CuentaDTO.class);
    }
    
    private void validarClienteExisteYActivo(String clienteId) {
        ClienteDTO cliente = clienteClient.obtenerClientePorIdentificacion(clienteId);
        if (!"True".equals(cliente.getEstado())) {
            throw new RuntimeException("Cliente inactivo: " + clienteId);
        }
    }
}
```

**2. 💸 Gestión de Movimientos con Lógica Financiera:**
```java
@Service
@RequiredArgsConstructor
public class MovimientoService {
    
    @Transactional
    public Movimiento crearMovimiento(Movimiento mov) {
        // 1. ✅ Normalizar valores según tipo
        BigDecimal valor = mov.getValor();
        String tipoMovimiento = mov.getTipoMovimiento().toUpperCase();
        
        if ("DEBITO".equals(tipoMovimiento)) {
            // Para DÉBITO, valor debe ser negativo
            if (valor.compareTo(BigDecimal.ZERO) > 0) {
                valor = valor.negate();
            }
        } else if ("CREDITO".equals(tipoMovimiento)) {
            // Para CRÉDITO, valor debe ser positivo
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                valor = valor.abs();
            }
        }
        
        mov.setValor(valor);
        
        // 2. ✅ Obtener cuenta y calcular nuevo saldo
        Cuenta cuenta = cuentaRepository.findById(mov.getNumeroCuenta())
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
            
        BigDecimal nuevoSaldo = cuenta.getSaldoInicial().add(valor);
        
        // 3. ✅ Validar saldo para débitos
        if ("DEBITO".equals(tipoMovimiento) && nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException("Saldo no disponible");
        }
        
        // 4. ✅ Actualizar saldo y guardar movimiento
        cuenta.setSaldoInicial(nuevoSaldo);
        cuentaRepository.save(cuenta);
        
        mov.setSaldo(nuevoSaldo);
        return movimientoRepository.save(mov);
    }
}
```

**3. 📊 Generación de Reportes:**
```java
@RestController
@RequestMapping("/reportes")
public class ReporteController {
    
    // ✅ Endpoint específico requerido: /reportes?fecha=rango
    @GetMapping
    public ResponseEntity<EstadoCuentaReporte> generarReporte(
            @RequestParam String fecha,
            @RequestParam(required = false) String clienteId) {
        
        // Parse fecha: "2024-01-01T00:00:00,2024-12-31T23:59:59"
        String[] fechas = fecha.split(",");
        LocalDateTime inicio = LocalDateTime.parse(fechas[0]);
        LocalDateTime fin = LocalDateTime.parse(fechas[1]);
        
        // Generar reporte con datos de cliente via WebFlux
        EstadoCuentaReporte reporte = reporteService.generarEstadoCuenta(
            clienteId, inicio, fin);
            
        return ResponseEntity.ok(reporte);
    }
}
```

**4. 🌊 Cliente WebFlux para Comunicación:**
```java
@Component
@Slf4j
public class ClienteClient {
    
    private final WebClient webClient;
    
    // ✅ Método síncrono que usa WebFlux internamente
    public ClienteDTO obtenerClientePorIdentificacion(String identificacion) {
        log.info("Obteniendo cliente por identificación: {}", identificacion);
        
        try {
            return webClient
                .get()
                .uri("/clientes/identificacion/{identificacion}", identificacion)
                .retrieve()
                .bodyToMono(ClienteDTO.class)
                .block(); // ← Convierte async a sync para APIs externas
        } catch (Exception e) {
            log.error("Error obteniendo cliente: {}", identificacion, e);
            throw new RuntimeException("Cliente no encontrado: " + identificacion);
        }
    }
    
    // ✅ Método puramente asíncrono para uso interno
    public Mono<ClienteDTO> obtenerClienteAsync(String identificacion) {
        return webClient
            .get()
            .uri("/clientes/identificacion/{identificacion}", identificacion)
            .retrieve()
            .bodyToMono(ClienteDTO.class)
            .doOnError(error -> log.error("Error async: {}", error.getMessage()));
    }
}
```

---

## 🧬 **TECNOLOGÍAS Y MARCO TEÓRICO**

### **🏗️ SPRING FRAMEWORK ECOSYSTEM**

#### **🍃 Spring Boot 3.4.5**
**Teoría:** Framework que simplifica el desarrollo de aplicaciones Spring mediante auto-configuración, convención sobre configuración, y starter dependencies.

**Implementación en el Proyecto:**
- **Auto-Configuration**: `@SpringBootApplication` activa configuración automática
- **Embedded Server**: Tomcat embebido elimina necesidad de deployment externo
- **Profiles**: Separación de configuraciones por ambiente (`application-dev.properties`)
- **Actuator**: Endpoints de monitoreo y métricas (`/health`, `/info`, `/metrics`)

#### **🌐 Spring Cloud 2023.0.6**
**Teoría:** Conjunto de herramientas para construir sistemas distribuidos robustos con patrones como Service Discovery, Circuit Breaker, API Gateway.

**Componentes Implementados:**
```java
// 1. Service Discovery
@EnableEurekaServer  // Servidor de registro
public class EurekaServerApplication { }

// 2. API Gateway
@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("microclientes", r -> r.path("/api/v1/clientes/**")
                .uri("http://localhost:8080"))
            .build();
    }
}

// 3. Client Side Load Balancing (automático con Eureka)
```

#### **🔄 Spring WebFlux (Reactive Programming)**
**Teoría:** Paradigma de programación reactiva basado en Reactive Streams, que permite operaciones no bloqueantes y manejo eficiente de concurrencia.

**Principios Fundamentales:**
1. **Asynchronous**: Operaciones no bloquean hilos de ejecución
2. **Non-blocking**: I/O no detiene procesamiento de otros requests
3. **Backpressure**: Control automático de flujo de datos
4. **Event-driven**: Basado en eventos y callbacks

**Implementación Práctica:**
```java
// ✅ WebClient - Cliente HTTP reactivo
@Bean
public WebClient webClient() {
    return WebClient.builder()
        .baseUrl("http://microclientes:8080")
        .clientConnector(new ReactorClientHttpConnector(
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5))
        ))
        .build();
}

// ✅ Flujo reactivo con conversion a síncrono
public ClienteDTO obtenerCliente(String id) {
    return webClient
        .get()
        .uri("/clientes/{id}", id)
        .retrieve()
        .bodyToMono(ClienteDTO.class)  // ← Mono<ClienteDTO> (reactive)
        .block();                      // ← ClienteDTO (synchronous)
}
```

### **🗄️ PERSISTENCE & DATA ACCESS**

#### **🏛️ JPA (Java Persistence API) & Hibernate**
**Teoría:** Especificación que define API para mapeo objeto-relacional (ORM), permitiendo trabajar con objetos Java en lugar de SQL directo.

**Patrones ORM Implementados:**
```java
// 1. ✅ Entity Mapping
@Entity
@Table(name = "cliente")
public class Cliente extends Persona {
    @Id                                    // Primary Key mapping
    @Column(name = "identificacion")       // Column mapping
    private String identificacion;
    
    @OneToMany(mappedBy = "cliente",      // Relationship mapping
               cascade = CascadeType.ALL,  // Cascade operations
               fetch = FetchType.LAZY)     // Lazy loading
    private List<Cuenta> cuentas;
}

// 2. ✅ Repository Pattern con Spring Data
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    // Query derivation from method name
    List<Cliente> findByEstadoAndNombreContaining(String estado, String nombre);
    
    // Custom JPQL query
    @Query("SELECT c FROM Cliente c WHERE c.identificacion = :id")
    Optional<Cliente> findByIdentificacion(@Param("id") String identificacion);
}
```

#### **🧬 JPA Inheritance Strategy**
**Teoría:** JPA proporciona tres estrategias de herencia: SINGLE_TABLE, JOINED, TABLE_PER_CLASS.

**JOINED Strategy Implementada:**
```sql
-- ✅ Tabla padre
CREATE TABLE persona (
    identificacion VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100),
    genero VARCHAR(10),
    edad INTEGER,
    direccion VARCHAR(200),
    telefono VARCHAR(15)
);

-- ✅ Tabla hija con FK a padre
CREATE TABLE cliente (
    identificacion VARCHAR(20) PRIMARY KEY,
    clienteid VARCHAR(20) UNIQUE,
    contrasena VARCHAR(255),
    estado VARCHAR(20),
    FOREIGN KEY (identificacion) REFERENCES persona(identificacion)
        ON DELETE CASCADE
);
```

**Ventajas JOINED:**
- ✅ Normalización completa (no duplicación de campos)
- ✅ Integridad referencial estricta
- ✅ Consultas eficientes para entidades específicas
- ✅ Extensibilidad para nuevas subclases

### **✅ VALIDATION & DATA INTEGRITY**

#### **🛡️ Bean Validation (JSR-303/380)**
**Teoría:** Estándar Java para validación declarativa usando anotaciones, separando lógica de validación de lógica de negocio.

**Implementación Centralizada en DTOs:**
```java
// ✅ DTO con validaciones completas
public class ClienteDTO extends PersonaDTO {
    
    @NotBlank(message = "El ID del cliente es obligatorio")
    @Size(min = 3, max = 20, message = "El ID debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Solo letras mayúsculas y números")
    private String clienteid;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 255, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasena;
    
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(True|False)$", message = "Estado debe ser 'True' o 'False'")
    private String estado;
}

// ✅ Activación automática en controladores
@PostMapping
public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody ClienteDTO clienteDTO) {
    // @Valid activa validaciones automáticamente
    // Si falla → MethodArgumentNotValidException → GlobalExceptionHandler
}
```

#### **🎯 Global Exception Handling**
**Teoría:** Patrón que centraliza manejo de excepciones en un punto único, mejorando mantenibilidad y consistencia de respuestas.

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // ✅ Manejo específico de validaciones
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Datos de entrada inválidos")
            .details(errors)
            .build();
            
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // ✅ Manejo específico de negocio
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficiente(
            SaldoInsuficienteException ex) {
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Business Rule Violation")
            .message(ex.getMessage())
            .build();
            
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
```

### **🔧 DEVELOPMENT TOOLS & PRODUCTIVITY**

#### **🎁 Project Lombok**
**Teoría:** Biblioteca que reduce boilerplate code generando automáticamente getters, setters, constructores, equals, hashCode y toString en tiempo de compilación.

**Anotaciones Utilizadas:**
```java
// ✅ Combinación completa para entidades
@Entity
@Table(name = "cuenta")
@Data                          // getter/setter/toString/equals/hashCode
@Builder                       // Patrón Builder fluido
@NoArgsConstructor            // Constructor sin argumentos (JPA requirement)
@AllArgsConstructor           // Constructor con todos los argumentos
@EqualsAndHashCode(callSuper = true)  // Para herencia
public class Cuenta {
    // Campos sin getters/setters explícitos
}

// ✅ Constructor injection sin @Autowired
@Service
@RequiredArgsConstructor      // Constructor automático para campos final
public class CuentaService {
    private final CuentaRepository repository;  // ← Inyección inmutable
    private final ClienteClient clienteClient;  // ← No necesita @Autowired
}
```

#### **🗂️ ModelMapper (Object Mapping)**
**Teoría:** Framework que automatiza mapeo entre objetos con estructuras similares, especialmente útil para conversión Entity ↔ DTO.

**Configuración y Uso:**
```java
// ✅ Configuración centralizada
@Configuration
public class ModelMapperConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        
        // Configuración estricta para evitar mapeos accidentales
        mapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
            
        return mapper;
    }
}

// ✅ Uso en servicios
@Service
public class ClienteService {
    
    private final ModelMapper modelMapper;
    
    public ClienteDTO crear(ClienteDTO dto) {
        // DTO → Entity
        Cliente cliente = modelMapper.map(dto, Cliente.class);
        Cliente guardado = repository.save(cliente);
        
        // Entity → DTO
        return modelMapper.map(guardado, ClienteDTO.class);
    }
}
```

### **🐘 DATABASE TECHNOLOGIES**

#### **🐘 PostgreSQL**
**Teoría:** Sistema de gestión de bases de datos objeto-relacional (ORDBMS) que combina SQL con características orientadas a objetos.

**Características Aprovechadas:**
```sql
-- ✅ Constraints avanzados
CREATE TABLE cuenta (
    numero_cuenta VARCHAR(10) PRIMARY KEY 
        CHECK (numero_cuenta ~ '^[0-9]{10}$'),  -- Regex constraint
    saldo_inicial DECIMAL(15,2) NOT NULL 
        CHECK (saldo_inicial >= 0),             -- Business rule constraint
    cliente_id VARCHAR(20) NOT NULL,
    
    -- ✅ Foreign key con cascade
    FOREIGN KEY (cliente_id) 
        REFERENCES persona(identificacion) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ✅ Índices para performance
CREATE INDEX idx_cuenta_cliente ON cuenta(cliente_id);
CREATE INDEX idx_movimiento_fecha ON movimiento(fecha);
CREATE INDEX idx_movimiento_cuenta_fecha ON movimiento(numero_cuenta, fecha);
```

#### **🔄 Transaction Management**
**Teoría:** Las transacciones ACID garantizan consistencia y atomicidad en operaciones de múltiples pasos.

**Implementación Spring:**
```java
@Service
@Transactional  // ← Todas las operaciones son transaccionales por defecto
public class MovimientoService {
    
    @Transactional(isolation = Isolation.READ_COMMITTED,
                   propagation = Propagation.REQUIRED,
                   rollbackFor = Exception.class)
    public Movimiento crearMovimiento(Movimiento movimiento) {
        // ✅ Operación atómica multi-paso:
        
        // 1. Leer cuenta (participa en transacción)
        Cuenta cuenta = cuentaRepository.findById(movimiento.getNumeroCuenta())
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        
        // 2. Validar saldo (puede lanzar excepción → rollback automático)
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException("Saldo no disponible");
        }
        
        // 3. Actualizar saldo cuenta
        cuenta.setSaldoInicial(nuevoSaldo);
        cuentaRepository.save(cuenta);
        
        // 4. Guardar movimiento
        return movimientoRepository.save(movimiento);
        
        // ✅ Si cualquier paso falla → rollback completo
        // ✅ Si todo éxito → commit automático
    }
}
```

### **📊 MONITORING & OBSERVABILITY**

#### **📈 Spring Boot Actuator**
**Teoría:** Módulo que proporciona endpoints para monitoreo y gestión de aplicaciones en producción.

**Endpoints Configurados:**
```properties
# ✅ Exposición de endpoints
management.endpoints.web.exposure.include=health,info,metrics,env,eureka
management.endpoint.health.show-details=always
management.endpoint.health.show-components=always

# ✅ Métricas personalizadas
management.metrics.export.prometheus.enabled=true
```

**Endpoints Disponibles:**
- `/actuator/health` - Estado de salud aplicación y dependencias
- `/actuator/info` - Información aplicación y versión
- `/actuator/metrics` - Métricas JVM, HTTP, DB connections
- `/actuator/env` - Variables de entorno y propiedades
- `/actuator/eureka` - Estado registro Eureka

#### **📝 Structured Logging con Logback**
**Teoría:** Logging estructurado facilita monitoreo, troubleshooting y análisis de logs en producción.

**Configuración Multi-Level:**
```xml
<!-- logback-spring.xml -->
<configuration>
    <!-- ✅ Appenders por nivel y categoría -->
    <appender name="FILE_INFO" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/microclientes-info.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>INFO</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>
    
    <!-- ✅ Logger específico para SQL -->
    <logger name="org.hibernate.SQL" level="DEBUG" additivity="false">
        <appender-ref ref="FILE_SQL"/>
    </logger>
    
    <!-- ✅ Logger para WebFlux communication -->
    <logger name="com.proyecto.microcuentas.client" level="DEBUG" additivity="false">
        <appender-ref ref="FILE_FEIGN"/>
    </logger>
</configuration>
```

---

## 🧱 **PRINCIPIOS DE PROGRAMACIÓN ORIENTADA A OBJETOS**

### **1. 🔒 ENCAPSULACIÓN**

#### **Implementación en el Proyecto:**

```java
// ✅ Campos privados con acceso controlado
@Entity
@Table(name = "cliente")
@Data  // Lombok genera getters/setters automáticamente
public class Cliente extends Persona {
    
    @Column(name = "clienteid", unique = true)
    private String clienteid;        // ← Encapsulado
    
    @Column(name = "contrasena")
    private String contrasena;       // ← Acceso controlado
    
    @Column(name = "estado")
    private String estado;           // ← Datos protegidos
}
```

#### **Dónde se Aplica:**
- **Entidades**: Todos los campos son `private`
- **Servicios**: Lógica de negocio encapsulada en métodos
- **DTOs**: Validaciones encapsuladas en anotaciones
- **Configuraciones**: Beans encapsulados en clases `@Configuration`

### **2. 🧬 HERENCIA**

#### **Implementación JPA Inheritance:**

```java
// ✅ Clase base con @Inheritance
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class Persona {
    @Id
    @Column(name = "identificacion", unique = true)
    private String identificacion;    // ← Clave primaria heredada
    
    private String nombre;
    private String genero;
    // ... campos comunes
}

// ✅ Clase derivada que hereda de Persona
@Entity
@Table(name = "cliente")
@EqualsAndHashCode(callSuper = true)  // ← Consideración de herencia
public class Cliente extends Persona {
    // Campos específicos del cliente
    private String clienteid;
    private String contrasena;
    private String estado;
}
```

#### **Esquema de Base de Datos Resultante:**
```sql
-- Tabla padre
CREATE TABLE persona (
    identificacion VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100),
    genero VARCHAR(10),
    edad INTEGER,
    direccion VARCHAR(200),
    telefono VARCHAR(15)
);

-- Tabla hija (JOINED strategy)
CREATE TABLE cliente (
    identificacion VARCHAR(20) PRIMARY KEY,  -- FK a persona
    clienteid VARCHAR(20) UNIQUE,
    contrasena VARCHAR(255),
    estado VARCHAR(20),
    FOREIGN KEY (identificacion) REFERENCES persona(identificacion) ON DELETE CASCADE
);
```

### **3. 🎭 POLIMORFISMO**

#### **Implementación en Spring:**

```java
// ✅ Polimorfismo con interfaces Spring
public interface CuentaRepository extends JpaRepository<Cuenta, String> {
    // Métodos polimórficos heredados:
    // save(), findById(), findAll(), delete(), etc.
    
    // Métodos específicos
    List<Cuenta> findByClienteId(String clienteId);
    List<Cuenta> findByEstado(String estado);
}

// ✅ Polimorfismo en servicios
@Service
public class CuentaService {
    
    private final CuentaRepository repository;  // ← Polimorfismo JPA
    private final ClienteClient clienteClient;   // ← Polimorfismo WebFlux
    
    // Método que usa polimorfismo de JPA
    public List<CuentaDTO> obtenerTodasLasCuentas() {
        return repository.findAll()  // ← Método polimórfico de JpaRepository
            .stream()
            .map(cuenta -> modelMapper.map(cuenta, CuentaDTO.class))
            .collect(Collectors.toList());
    }
}
```

### **4. 🎨 ABSTRACCIÓN**

#### **Capas de Abstracción:**

```java
// ✅ Abstracción en Controllers (Interfaz REST)
@RestController
@RequestMapping("/clientes")
public class ClienteController {
    
    // Abstrae la complejidad del negocio
    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO resultado = service.crear(clienteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}

// ✅ Abstracción en Services (Lógica de Negocio)
@Service
@RequiredArgsConstructor
public class ClienteService {
    
    // Abstrae acceso a datos y validaciones
    public ClienteDTO crear(ClienteDTO clienteDTO) {
        // Lógica compleja abstraída en un método simple
        Cliente cliente = modelMapper.map(clienteDTO, Cliente.class);
        Cliente guardado = repository.save(cliente);
        return modelMapper.map(guardado, ClienteDTO.class);
    }
}
```

---

## ⚖️ **PRINCIPIOS SOLID**

### **S - Single Responsibility Principle (SRP)**

#### **✅ Implementación:**

```java
// ✅ UNA responsabilidad: Manejo de datos de cuenta
@Entity
public class Cuenta {
    // Solo maneja estructura de datos de cuenta
}

// ✅ UNA responsabilidad: Lógica de negocio de cuentas
@Service
public class CuentaService {
    // Solo maneja operaciones de negocio de cuentas
}

// ✅ UNA responsabilidad: Acceso a datos de cuentas
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, String> {
    // Solo maneja persistencia de cuentas
}

// ✅ UNA responsabilidad: Endpoints REST de cuentas
@RestController
public class CuentaController {
    // Solo maneja HTTP requests/responses de cuentas
}
```

### **O - Open/Closed Principle (OCP)**

#### **✅ Implementación:**

```java
// ✅ Abierto para extensión, cerrado para modificación
@Service
public class MovimientoService {
    
    // Método base que no se modifica
    public Movimiento crearMovimiento(Movimiento mov) {
        // Lógica base que no cambia
        validarMovimiento(mov);
        return repository.save(mov);
    }
    
    // ✅ Extensible via estrategias diferentes
    private void validarMovimiento(Movimiento mov) {
        // Se puede extender con nuevos tipos de validación
        switch (mov.getTipoMovimiento()) {
            case "DEBITO" -> validarDebito(mov);
            case "CREDITO" -> validarCredito(mov);
            // Fácil agregar nuevos casos sin modificar código existente
        }
    }
}
```

### **L - Liskov Substitution Principle (LSP)**

#### **✅ Implementación:**

```java
// ✅ Cliente puede sustituir a Persona sin problemas
@Entity
public class Cliente extends Persona {
    // Mantiene comportamiento esperado de Persona
    // Agrega funcionalidad sin romper contratos
}

// ✅ Cualquier implementación de Repository funciona igual
public interface CuentaRepository extends JpaRepository<Cuenta, String> {
    // Contract que respetan todas las implementaciones
}
```

### **I - Interface Segregation Principle (ISP)**

#### **✅ Implementación:**

```java
// ✅ Interfaces específicas y pequeñas
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, String> {
    // Solo métodos relevantes para Cuenta
    List<Cuenta> findByClienteId(String clienteId);
    List<Cuenta> findByEstado(String estado);
}

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    // Solo métodos relevantes para Movimiento
    List<Movimiento> findByNumeroCuentaAndFechaBetween(String cuenta, LocalDateTime inicio, LocalDateTime fin);
}

// ✅ No hay métodos innecesarios que las clases deban implementar
```

### **D - Dependency Inversion Principle (DIP)**

#### **✅ Implementación:**

```java
// ✅ Depende de abstracciones (interfaces), no de implementaciones concretas
@Service
@RequiredArgsConstructor
public class CuentaService {
    
    // ✅ Depende de interfaz, no de clase concreta
    private final CuentaRepository repository;        // ← Abstracción
    private final ClienteClient clienteClient;        // ← Abstracción
    private final ModelMapper modelMapper;            // ← Abstracción
    
    // Spring inyecta implementaciones concretas automáticamente
}

// ✅ Configuración que invierte dependencias
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        // Devuelve abstracción, no implementación específica
        return WebClient.builder().build();
    }
}
```

---

## 🔒 **PROPIEDADES ACID**

### **A - Atomicity (Atomicidad)**

#### **✅ Implementación:**

```java
@Service
@Transactional  // ← Garantiza atomicidad
public class MovimientoService {
    
    @Transactional
    public Movimiento crearMovimiento(Movimiento mov) {
        // ✅ TODO esto ejecuta como UNA unidad atómica
        
        // 1. Obtener cuenta
        Cuenta cuenta = cuentaRepository.findById(mov.getNumeroCuenta())
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        
        // 2. Calcular nuevo saldo
        BigDecimal nuevoSaldo = cuenta.getSaldoInicial().add(mov.getValor());
        
        // 3. Validar saldo (puede fallar y hace rollback completo)
        if ("DEBITO".equals(mov.getTipoMovimiento()) && nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException("Saldo no disponible");
        }
        
        // 4. Actualizar saldo de cuenta
        cuenta.setSaldoInicial(nuevoSaldo);
        cuentaRepository.save(cuenta);
        
        // 5. Guardar movimiento
        mov.setSaldo(nuevoSaldo);
        return movimientoRepository.save(mov);
        
        // ✅ Si cualquier paso falla, TODO se revierte
    }
}
```

### **C - Consistency (Consistencia)**

#### **✅ Implementación:**

```java
// ✅ Validaciones que mantienen consistencia
@Entity
public class Cuenta {
    
    @Column(nullable = false)
    private BigDecimal saldoInicial;  // ← No puede ser null
    
    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL)
    private List<Movimiento> movimientos;  // ← Relación consistente
}

// ✅ Validaciones en DTO
public class CuentaDTO {
    
    @NotBlank(message = "Número de cuenta es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "Número debe tener 10 dígitos")
    private String numeroCuenta;
    
    @DecimalMin(value = "0.0", message = "Saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial;
}

// ✅ Constraints en base de datos
/*
CREATE TABLE cuenta (
    numero_cuenta VARCHAR(10) PRIMARY KEY,
    saldo_inicial DECIMAL(15,2) NOT NULL CHECK (saldo_inicial >= 0),
    cliente_id VARCHAR(20) NOT NULL
);
*/
```

### **I - Isolation (Aislamiento)**

#### **✅ Implementación:**

```java
// ✅ Niveles de aislamiento configurados
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CuentaService {
    
    public Movimiento crearMovimiento(Movimiento mov) {
        // ✅ Esta transacción está aislada de otras
        // No ve cambios no confirmados de otras transacciones
        
        Cuenta cuenta = cuentaRepository.findById(mov.getNumeroCuenta())
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
            
        // ✅ Operaciones aisladas - no hay interferencia concurrente
        synchronized (cuenta.getNumeroCuenta().intern()) {
            // Bloqueo específico por cuenta para evitar condiciones de carrera
            cuenta.setSaldoInicial(cuenta.getSaldoInicial().add(mov.getValor()));
            return movimientoRepository.save(mov);
        }
    }
}
```

### **D - Durability (Durabilidad)**

#### **✅ Implementación:**

```java
// ✅ Configuración de persistencia durable
spring.datasource.url=jdbc:postgresql://localhost:5432/microcuentas
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

// ✅ Transacciones confirmadas son permanentes
@Transactional
public Movimiento crearMovimiento(Movimiento mov) {
    // Una vez que este método termina exitosamente,
    // los cambios están GARANTIZADOS en disco
    Movimiento resultado = movimientoRepository.save(mov);
    
    // ✅ PostgreSQL garantiza escritura en disco antes de confirmar
    return resultado;
}
```

---

## 🎨 **TABLA DE PATRONES DE DISEÑO**

| **Categoría** | **Patrón** | **Implementación** | **Ubicación** | **Propósito** | **Ventajas** | **Ejemplo en Código** |
|--------------|------------|-------------------|---------------|---------------|--------------|----------------------|
| **🏗️ Creacionales** | **Singleton** | Spring Beans (default) | @Service, @Repository, @Configuration | Una instancia por contexto | Control recursos, consistencia | `@Service // Singleton automático` |
| | **Builder** | Lombok @Builder | Entidades (Cuenta, Movimiento) | Construcción fluida objetos | Legibilidad, inmutabilidad opcional | `Cuenta.builder().numeroCuenta("123").build()` |
| | **Factory Method** | Spring Bean Factory | @Configuration classes | Creación controlada objetos | Flexibilidad instanciación | `@Bean public ModelMapper modelMapper()` |
| **🎯 Estructurales** | **Facade** | Controllers, Service Layer | ClienteController, ReporteController | Interfaz simplificada | Oculta complejidad subsistemas | `public EstadoCuentaReporte obtenerReporte()` simplifica múltiples llamadas |
| | **Adapter** | ModelMapper | Config classes | Conversión DTO ↔ Entity | Desacoplamiento capas | `modelMapper.map(cliente, ClienteDTO.class)` |
| | **Proxy** | Spring AOP, JPA Proxies | @Transactional, @Repository | Funcionalidad adicional transparente | Separación concerns | `@Transactional // Proxy maneja transacciones` |
| **🔄 Comportamiento** | **Observer** | Spring Events | Event Publishing | Comunicación desacoplada | Bajo acoplamiento | `applicationEventPublisher.publishEvent(new ClienteCreatedEvent())` |
| | **Strategy** | Spring Profiles | @Profile configurations | Comportamiento intercambiable | Flexibilidad configuración | `@Profile("dev") vs @Profile("prod")` |
| | **Template Method** | Spring Template Classes | JpaRepository, RestTemplate | Algoritmo con pasos variables | Reutilización código común | `repository.findById() // Template JPA` |
| | **Command** | REST Endpoints | @RequestMapping methods | Encapsular operaciones | Desacoplamiento invocador/receptor | `@PostMapping // Comando crear cliente` |
| **🏛️ Arquitecturales** | **MVC (Model-View-Controller)** | Spring MVC | Controllers, DTOs, Services | Separación responsabilidades | Mantenibilidad, testabilidad | `@RestController (Controller), ClienteDTO (Model), JSON (View)` |
| | **Repository** | Spring Data JPA | @Repository interfaces | Abstracción acceso datos | Testabilidad, flexibilidad BD | `interface ClienteRepository extends JpaRepository` |
| | **Service Layer** | @Service classes | Service package | Lógica negocio centralizada | Reutilización, transacciones | `@Service public class ClienteService` |
| | **DTO (Data Transfer Object)** | DTO classes | DTO package | Transferencia datos entre capas | Control exposición datos | `public class ClienteDTO` con validaciones |
| | **Layered Architecture** | Package structure | Todo el proyecto | Separación por responsabilidades | Modularidad, escalabilidad | `controller/ service/ repository/ entity/` |
| **🌐 Microservicios** | **Database per Service** | Separate databases | PostgreSQL instances | BD independiente por servicio | Autonomía, escalabilidad | `microclientesdb` vs `microcuentasdb` |
| | **API Gateway** | Spring Cloud Gateway | gateway/ folder | Punto entrada único | Centralización, routing | `@Configuration RouteLocator` |
| | **Service Discovery** | Netflix Eureka | eureka-server/ | Registro/descubrimiento servicios | Dinamismo, load balancing | `@EnableEurekaServer` |
| | **Circuit Breaker** | Spring Cloud | WebClient con timeout | Tolerancia fallos | Resilencia, estabilidad | `webClient.timeout(Duration.ofSeconds(5))` |
| **🔄 Integración** | **Asynchronous Messaging** | WebFlux | ClienteClient.java | Comunicación no bloqueante | Escalabilidad, performance | `webClient.get().bodyToMono().block()` |
| | **Saga Pattern** | Transaction coordination | Service methods | Transacciones distribuidas | Consistencia eventual | `@Transactional en CuentaService + ClienteClient` |

---

## 🏷️ **TABLA DE DECORADORES/ANOTACIONES**

| **Categoría** | **Anotación** | **Propósito** | **Ubicación en Proyecto** | **Parámetros Principales** | **Ejemplo de Uso** |
|--------------|--------------|---------------|---------------------------|---------------------------|-------------------|
| **🏗️ Spring Core** | `@SpringBootApplication` | Clase principal aplicación | MicroclientesApplication.java, MicrocuentasApplication.java | `scanBasePackages`, `exclude` | `@SpringBootApplication public class MicroclientesApplication` |
| | `@Configuration` | Clase de configuración | WebClientConfig.java, ModelMapperConfig.java | `proxyBeanMethods` | `@Configuration public class WebClientConfig` |
| | `@Bean` | Definición de bean | Métodos en @Configuration | `name`, `initMethod`, `destroyMethod` | `@Bean public ModelMapper modelMapper()` |
| | `@Value` | Inyección de propiedades | application.properties values | `defaultValue` | `@Value("${microclientes.url:http://localhost:8080}")` |
| | `@Profile` | Configuración por perfil | Clases de configuración | `value` | `@Profile("dev")` |
| **🎯 Estereotipos** | `@Service` | Servicio de negocio | ClienteService.java, CuentaService.java | Ninguno | `@Service public class ClienteService` |
| | `@Repository` | Repositorio de datos | ClienteRepository.java, CuentaRepository.java | `value` | `@Repository public interface ClienteRepository` |
| | `@RestController` | Controlador REST | ClienteController.java, CuentaController.java | Ninguno | `@RestController public class ClienteController` |
| | `@Component` | Componente genérico | ClienteClient.java | `value` | `@Component public class ClienteClient` |
| **🌐 Web/REST** | `@RequestMapping` | Mapeo de rutas | Controllers | `value`, `method`, `produces`, `consumes` | `@RequestMapping("/clientes")` |
| | `@GetMapping` | HTTP GET | Métodos controller | `value`, `produces` | `@GetMapping("/{id}")` |
| | `@PostMapping` | HTTP POST | Métodos controller | `value`, `consumes` | `@PostMapping` |
| | `@PutMapping` | HTTP PUT | Métodos controller | `value` | `@PutMapping("/{id}")` |
| | `@PatchMapping` | HTTP PATCH | Métodos controller | `value` | `@PatchMapping("/{id}")` |
| | `@DeleteMapping` | HTTP DELETE | Métodos controller | `value` | `@DeleteMapping("/{id}")` |
| | `@PathVariable` | Variable de ruta | Parámetros método | `value`, `required` | `@PathVariable String id` |
| | `@RequestBody` | Cuerpo de petición | Parámetros método | `required` | `@RequestBody ClienteDTO dto` |
| | `@RequestParam` | Parámetro de consulta | Parámetros método | `value`, `required`, `defaultValue` | `@RequestParam String nombre` |
| **🔍 JPA/Hibernate** | `@Entity` | Entidad de BD | Clases entity | `name` | `@Entity public class Cliente` |
| | `@Table` | Tabla de BD | Clases entity | `name`, `schema` | `@Table(name = "cliente")` |
| | `@Id` | Clave primaria | Campos entity | Ninguno | `@Id private String identificacion` |
| | `@Column` | Columna de BD | Campos entity | `name`, `nullable`, `unique` | `@Column(name = "nombre")` |
| | `@OneToMany` | Relación uno a muchos | Campos entity | `mappedBy`, `cascade`, `fetch` | `@OneToMany(mappedBy = "cuenta")` |
| | `@ManyToOne` | Relación muchos a uno | Campos entity | `fetch` | `@ManyToOne private Cuenta cuenta` |
| | `@JoinColumn` | Columna de unión | Campos relación | `name`, `nullable` | `@JoinColumn(name = "cuenta_id")` |
| | `@Inheritance` | Estrategia herencia | Clases padre | `strategy` | `@Inheritance(strategy = InheritanceType.JOINED)` |
| **✅ Validación** | `@Valid` | Activar validación | Parámetros método | Ninguno | `@Valid @RequestBody ClienteDTO dto` |
| | `@NotBlank` | No vacío/null | Campos String | `message` | `@NotBlank(message = "Es obligatorio")` |
| | `@NotNull` | No null | Cualquier campo | `message` | `@NotNull private String campo` |
| | `@Size` | Tamaño String/Collection | Campos String/List | `min`, `max`, `message` | `@Size(min = 2, max = 100)` |
| | `@Pattern` | Expresión regular | Campos String | `regexp`, `message` | `@Pattern(regexp = "^[0-9]+$")` |
| | `@DecimalMin` | Valor mínimo decimal | Campos numéricos | `value`, `message` | `@DecimalMin(value = "0.0")` |
| | `@DecimalMax` | Valor máximo decimal | Campos numéricos | `value`, `message` | `@DecimalMax(value = "999999.99")` |
| **🎁 Lombok** | `@Data` | Getters/setters/toString | Clases DTO/Entity | `staticConstructor` | `@Data public class ClienteDTO` |
| | `@NoArgsConstructor` | Constructor sin argumentos | Clases | `access`, `force` | `@NoArgsConstructor public class Cliente` |
| | `@AllArgsConstructor` | Constructor con todos args | Clases | `access` | `@AllArgsConstructor public class Cliente` |
| | `@RequiredArgsConstructor` | Constructor campos final | Services | `staticName` | `@RequiredArgsConstructor public class ClienteService` |
| | `@Builder` | Patrón Builder | Entidades | `builderMethodName`, `buildMethodName` | `@Builder public class Cuenta` |
| | `@Slf4j` | Logger automático | Clases que necesitan log | Ninguno | `@Slf4j public class ClienteService` |
| | `@EqualsAndHashCode` | equals() y hashCode() | Entidades con herencia | `callSuper` | `@EqualsAndHashCode(callSuper = true)` |
| **⚡ Microservicios** | `@EnableEurekaServer` | Servidor Eureka | EurekaServerApplication.java | Ninguno | `@EnableEurekaServer public class EurekaServerApplication` |
| **⚠️ Excepciones** | `@ControllerAdvice` | Manejo global errores | GlobalExceptionHandler.java | `basePackages`, `annotations` | `@ControllerAdvice public class GlobalExceptionHandler` |
| | `@ExceptionHandler` | Manejo excepción específica | Métodos en @ControllerAdvice | `value` | `@ExceptionHandler(RuntimeException.class)` |
| **💾 Transacciones** | `@Transactional` | Gestión transaccional | Métodos service | `readOnly`, `isolation`, `propagation` | `@Transactional public void metodo()` |

---

## 💉 **TABLA DE INYECCIONES DE DEPENDENCIAS**

| **Tipo** | **Anotación** | **Ubicación** | **Propósito** | **Ventajas** | **Ejemplo** |
|----------|--------------|---------------|---------------|--------------|-------------|
| **Constructor Injection** | `@RequiredArgsConstructor` | Services, Controllers | Inyección inmutable por constructor | Inmutabilidad, testabilidad | `@RequiredArgsConstructor public class ClienteService { private final ClienteRepository repo; }` |
| **Field Injection** | `@Autowired` | Legacy code | Inyección directa en campos | Simple sintaxis | `@Autowired private ClienteRepository repo;` |
| **Bean Configuration** | `@Bean` | Clases @Configuration | Definición manual de beans | Control total sobre instanciación | `@Bean public ModelMapper modelMapper() { return new ModelMapper(); }` |
| **Component Scanning** | `@Component` | Clases utilitarias | Auto-detección por Spring | Automático | `@Component public class ClienteClient { }` |
| **Specialized Components** | `@Service` | Capa de negocio | Servicios de lógica de negocio | Semántica clara | `@Service public class MovimientoService { }` |
| **Repository Injection** | `@Repository` | Capa de datos | Repositorios de acceso a datos | Manejo excepciones BD | `@Repository public interface CuentaRepository extends JpaRepository { }` |
| **Controller Injection** | `@RestController` | Capa presentación | Controllers REST | Manejo HTTP | `@RestController public class ClienteController { }` |
| **Configuration Injection** | `@Configuration` | Configuración | Clases de configuración | Centralización config | `@Configuration public class WebClientConfig { }` |

**Ejemplos prácticos en el código**:

```java
// ✅ Constructor Injection - RECOMENDADO
@Service
@RequiredArgsConstructor
public class CuentaService {
    private final CuentaRepository cuentaRepository;
    private final ClienteClient clienteClient;
    private final ModelMapper modelMapper;
    // Constructor generado automáticamente por Lombok
}

// ✅ Bean Configuration
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient(@Value("${microclientes.url}") String baseUrl) {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }
}

// ✅ Component Scanning
@Component
@Slf4j
public class ClienteClient {
    private final WebClient webClient;
    
    public ClienteClient(WebClient webClient) {
        this.webClient = webClient;
    }
}
```

---

## 🔄 **FLUJOS Y RELACIONES**

### **📊 Flujo Completo: Crear Movimiento**

```
1. 🌐 Cliente → API Gateway (8083)
   POST /api/v1/movimientos
   Body: {
     "numeroCuenta": "1234567890",
     "tipoMovimiento": "DEBITO",
     "valor": 100.00
   }

2. 🚪 Gateway → Microcuentas (8081)
   Enrutamiento automático

3. 🎯 MovimientoController.crear()
   ├── Validación @Valid
   └── → MovimientoService.crearMovimiento()

4. 🔧 MovimientoService.crearMovimiento()
   ├── Buscar cuenta en CuentaRepository
   ├── Validar saldo disponible
   ├── Normalizar valor (DEBITO = negativo)
   ├── Calcular nuevo saldo
   ├── Actualizar cuenta
   └── Guardar movimiento

5. 💾 Base de Datos PostgreSQL
   ├── UPDATE cuenta SET saldo_inicial = nuevo_saldo
   └── INSERT INTO movimiento VALUES (...)

6. 📤 Respuesta JSON
   {
     "id": 1,
     "numeroCuenta": "1234567890",
     "tipoMovimiento": "DEBITO",
     "valor": -100.00,
     "saldo": 900.00,
     "fecha": "2024-08-06T17:30:00"
   }
```

### **📊 Flujo WebFlux: Validar Cliente**

```
1. 🔧 CuentaService.crearCuenta()
   └── validarClienteExisteYActivo(clienteId)

2. 🌊 ClienteClient.obtenerClientePorIdentificacion()
   ├── WebClient.get()
   ├── .uri("/clientes/identificacion/{id}", id)
   ├── .retrieve()
   ├── .bodyToMono(ClienteDTO.class)
   └── .block() ← Convierte a síncrono

3. 🌐 HTTP Request → Microclientes (8080)
   GET /clientes/identificacion/12345678

4. 🎯 ClienteController.buscarPorIdentificacion()
   └── ClienteService.buscarPorIdentificacion()

5. 💾 PostgreSQL Microclientes
   SELECT * FROM persona p 
   JOIN cliente c ON p.identificacion = c.identificacion 
   WHERE p.identificacion = '12345678'

6. 📤 Respuesta Cliente
   {
     "identificacion": "12345678",
     "nombre": "Juan Pérez",
     "estado": "True",
     "clienteid": "CLI001"
   }

7. ✅ Validación en Microcuentas
   if (cliente.getEstado().equals("True")) {
     // Proceder con creación de cuenta
   } else {
     throw new RuntimeException("Cliente inactivo");
   }
```

### **🏗️ Relaciones entre Entidades**

```
📊 MICROCLIENTES:
┌─────────────────┐
│     PERSONA     │ ← Entidad base
│                 │
│ + identificacion│ ← @Id (PK)
│ + nombre        │
│ + genero        │
│ + edad          │
│ + direccion     │
│ + telefono      │
└─────────────────┘
         ↑ @Inheritance(JOINED)
         │
┌─────────────────┐
│     CLIENTE     │ ← Hereda de Persona
│                 │
│ + clienteid     │ ← Unique (no PK)
│ + contrasena    │
│ + estado        │
└─────────────────┘

📊 MICROCUENTAS:
┌─────────────────┐      ┌─────────────────┐
│     CUENTA      │ 1  N │   MOVIMIENTO    │
│                 │◄─────┤                 │
│ + numeroCuenta  │      │ + id            │ ← @Id
│ + tipoCuenta    │      │ + numeroCuenta  │ ← FK
│ + saldoInicial  │      │ + tipoMovimiento│
│ + estado        │      │ + valor         │
│ + clienteId     │      │ + saldo         │
│                 │      │ + fecha         │
└─────────────────┘      └─────────────────┘
         │                         ↑
         │ @OneToMany              │ @ManyToOne
         └─────────────────────────┘
```

---

## 🌊 **COMUNICACIÓN WEBFLUX**

### **🎯 Arquitectura WebFlux Implementada**

```
┌─────────────────────────────────────────────────────────────────┐
│                    MICROCUENTAS SERVICE                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  CuentaService.crearCuenta()                                   │
│         │                                                       │
│         ▼                                                       │
│  validarClienteExisteYActivo(clienteId)                       │
│         │                                                       │
│         ▼                                                       │
│  ClienteClient.obtenerClientePorIdentificacion()              │
│         │                                                       │
│         ▼                                                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │               WEBFLUX FLOW                              │   │
│  │                                                         │   │
│  │  webClient.get()                                       │   │
│  │     ▼                                                   │   │
│  │  .uri("/clientes/identificacion/{id}", id)            │   │
│  │     ▼                                                   │   │
│  │  .retrieve()                                           │   │
│  │     ▼                                                   │   │
│  │  .bodyToMono(ClienteDTO.class)                        │   │
│  │     ▼                                                   │   │
│  │  .block() ← Convierte Mono a valor síncrono           │   │
│  └─────────────────────────────────────────────────────────┘   │
│         │                                                       │
│         ▼                                                       │
│  return ClienteDTO                                             │
└─────────────────────────────────────────────────────────────────┘
                        │
                        ▼ HTTP Request
┌─────────────────────────────────────────────────────────────────┐
│                MICROCLIENTES SERVICE                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ClienteController.buscarPorIdentificacion()                  │
│         │                                                       │
│         ▼                                                       │
│  ClienteService.buscarPorIdentificacion()                     │
│         │                                                       │
│         ▼                                                       │
│  ClienteRepository.findByIdentificacion()                     │
│         │                                                       │
│         ▼                                                       │
│  PostgreSQL Query                                              │
└─────────────────────────────────────────────────────────────────┘
```

### **⚙️ Configuración WebClient**

```java
@Configuration
public class WebClientConfig {

    @Value("${microclientes.url:http://localhost:8080}")
    private String microclientesUrl;

    @Bean
    public WebClient webClient() {
        // ✅ HTTP Client Configuration
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)    // Connection timeout
            .responseTimeout(Duration.ofSeconds(5));               // Response timeout

        return WebClient.builder()
            .baseUrl(microclientesUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }
}
```

### **🔄 Implementación Cliente WebFlux**

```java
@Component
@Slf4j
public class ClienteClient {
    
    private final WebClient webClient;
    
    public ClienteClient(WebClient webClient) {
        this.webClient = webClient;
    }
    
    // ✅ Método síncrono para APIs externas
    public ClienteDTO obtenerCliente(String clienteid) {
        log.info("Obteniendo cliente con ID: {}", clienteid);
        try {
            return webClient
                .get()
                .uri("/clientes/{clienteid}", clienteid)
                .retrieve()
                .bodyToMono(ClienteDTO.class)
                .block(); // ← Convierte a síncrono
        } catch (Exception e) {
            log.error("Error al obtener cliente con ID: {}", clienteid, e);
            throw new RuntimeException("Cliente no encontrado: " + clienteid);
        }
    }
    
    // ✅ Método asíncrono puro para uso interno
    public Mono<ClienteDTO> obtenerClienteAsync(String clienteid) {
        log.info("Obteniendo cliente async con ID: {}", clienteid);
        return webClient
            .get()
            .uri("/clientes/{clienteid}", clienteid)
            .retrieve()
            .bodyToMono(ClienteDTO.class)
            .doOnError(error -> log.error("Error async al obtener cliente: {}", error.getMessage()));
    }
}
```

### **🚀 Ventajas de WebFlux en el Proyecto**

1. **Non-Blocking I/O**: No bloquea hilos durante esperas de red
2. **Escalabilidad**: Maneja más requests concurrentes con menos recursos
3. **Eficiencia**: Menor uso de memoria y CPU
4. **Reactive Streams**: Backpressure handling automático
5. **Resiliencia**: Timeouts y manejo de errores integrados

### **⚡ Comparación: RestTemplate vs WebClient**

| **Aspecto** | **RestTemplate (Síncrono)** | **WebClient (Asíncrono)** |
|-------------|------------------------------|---------------------------|
| **Bloqueo** | ❌ Bloquea hilo | ✅ No bloquea |
| **Recursos** | ❌ Más memoria/CPU | ✅ Menos recursos |
| **Escalabilidad** | ❌ Limitada | ✅ Alta |
| **Sintaxis** | ✅ Simple | ⚠️ Más compleja |
| **Futuro** | ❌ Deprecated | ✅ Recomendado |

---

## 🚀 **CONCLUSIÓN**

Este sistema bancario implementa una **arquitectura de microservicios moderna** que cumple con:

- ✅ **Principios POO**: Encapsulación, herencia, polimorfismo y abstracción
- ✅ **Principios SOLID**: SRP, OCP, LSP, ISP, DIP
- ✅ **Propiedades ACID**: Atomicidad, consistencia, aislamiento, durabilidad
- ✅ **Patrones de Diseño**: 20+ patrones implementados
- ✅ **Comunicación Asíncrona**: WebFlux para inter-servicios
- ✅ **Validaciones Centralizadas**: Solo en DTOs
- ✅ **Arquitectura Escalable**: Microservicios independientes

**El resultado es un sistema robusto, mantenible y preparado para producción.**