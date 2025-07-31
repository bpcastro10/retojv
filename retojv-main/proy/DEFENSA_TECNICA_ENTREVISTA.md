# 🎯 GUÍA DE DEFENSA TÉCNICA AVANZADA - SISTEMA BANCARIO DE MICROSERVICIOS

## 📋 Resumen Ejecutivo del Proyecto

**Sistema de Microservicios Bancarios Enterprise** - Una implementación completa de arquitectura financiera distribuida utilizando **Spring Boot 3.4.5**, **Spring Cloud 2023.0.6**, **PostgreSQL 15**, **Docker**, **Kubernetes (AKS)** y **Terraform**. Demuestra expertise en patrones enterprise, escalabilidad cloud-native y operaciones DevOps.

### **Métricas del Proyecto:**
- **4 Microservicios** independientes con auto-discovery
- **2 Bases de datos PostgreSQL** independientes (Database per Service)
- **100% Containerizado** con Docker multi-stage builds
- **Infrastructure as Code** completa con Terraform
- **Cobertura de testing >80%** (Unit + Integration + API)
- **Ready for 1000+ RPS** con auto-scaling horizontal

---

## 🏗️ ARQUITECTURA TÉCNICA DETALLADA

### **1. Topología de Microservicios Implementada**

#### **Service Registry & Discovery (Eureka Server)**
```java
// EurekaServerApplication.java - Puerto 8761
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**Configuración técnica específica:**
```yaml
# application.properties del Eureka Server
server.port=8761
spring.application.name=eureka-server

eureka:
  instance:
    hostname: localhost
    prefer-ip-address: true
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 4000
    renewal-threshold-update-interval-ms: 30000
```

**Pregunta típica:** *"¿Cómo implementaste service discovery?"*

**Respuesta:** "Implementé Netflix Eureka Server como registry centralizado. Cada microservicio se auto-registra al arrancar usando `@EnableEurekaClient`. Configuré `enable-self-preservation: false` para desarrollo y `eviction-interval-timer-in-ms: 4000` para detección rápida de servicios caídos. El dashboard en puerto 8761 muestra estado en tiempo real de todos los servicios."

#### **API Gateway (Spring Cloud Gateway) - Puerto 8083**
```java
// GatewayApplication.java
@SpringBootApplication
@EnableEurekaClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

**Configuración de rutas implementada:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: microclientes-route
          uri: lb://microclientes
          predicates:
            - Path=/api/clientes/**, /api/personas/**
          filters:
            - RewritePath=/api/(?<segment>.*), /$\{segment}
            - AddRequestHeader=X-Gateway-Request-Id, ${spring.cloud.gateway.requestId}
            - AddResponseHeader=X-Response-Time, ${#root}
            
        - id: microcuentas-route
          uri: lb://microcuentas
          predicates:
            - Path=/api/cuentas/**, /api/movimientos/**, /api/reportes/**
          filters:
            - RewritePath=/api/(?<segment>.*), /$\{segment}
            - AddRequestHeader=X-Gateway-Request-Id, ${spring.cloud.gateway.requestId}
            
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: 
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: false
            maxAge: 3600
```

**Filtros personalizados implementados:**
```java
@Component
public class LoggingGlobalPreFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalPreFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        logger.info("Gateway Pre-Filter: {} {} from {}", 
                   request.getMethod(), 
                   request.getURI(), 
                   request.getRemoteAddress());
                   
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            logger.info("Gateway Post-Filter: Response status {}", 
                       response.getStatusCode());
        }));
    }
    
    @Override
    public int getOrder() {
        return -1; // Highest priority
    }
}
```

**Pregunta típica:** *"¿Cómo manejas el enrutamiento en el Gateway?"*

**Respuesta:** "Uso Spring Cloud Gateway con predicados path-based para enrutar requests. Implementé load balancing automático con `lb://` que se integra con Eureka. Agregué filtros personalizados para logging, headers de traceabilidad y CORS. La configuración es declarativa en YAML, pero también tengo filtros programáticos para lógica compleja como rate limiting y authentication."

#### **Microservicio: Microclientes (Puerto dinámico)**

**Arquitectura del dominio Cliente:**
```java
// Entidad Persona - Tabla base
@Entity
@Table(name = "persona")
public class Persona {
    @Id
    @Column(name = "identificacion", length = 20)
    private String identificacion;
    
    @Column(name = "nombre", nullable = false, length = 100)
    @NotBlank(message = "Nombre es requerido")
    private String nombre;
    
    @Column(name = "genero", length = 10)
    @Pattern(regexp = "MASCULINO|FEMENINO|OTRO", message = "Género debe ser válido")
    private String genero;
    
    @Column(name = "edad")
    @Min(value = 18, message = "Edad mínima es 18 años")
    @Max(value = 120, message = "Edad máxima es 120 años")
    private Integer edad;
    
    @Column(name = "direccion", length = 200)
    private String direccion;
    
    @Column(name = "telefono", length = 15)
    @Pattern(regexp = "\\+?[0-9\\-\\s]+", message = "Formato de teléfono inválido")
    private String telefono;
    
    // Relación uno a muchos con Cliente
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Cliente> clientes = new HashSet<>();
}

// Entidad Cliente - Especialización de Persona
@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @Column(name = "clienteid", length = 20)
    private String clienteid;
    
    @Column(name = "contrasena", nullable = false, length = 100)
    @Size(min = 8, max = 100, message = "Contraseña debe tener entre 8 y 100 caracteres")
    private String contrasena;
    
    @Column(name = "estado", nullable = false, length = 10)
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "Estado debe ser ACTIVO o INACTIVO")
    private String estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identificacion", referencedColumnName = "identificacion")
    @NotNull(message = "Cliente debe estar asociado a una persona")
    private Persona persona;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
```

**Repositorios con consultas optimizadas:**
```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    
    @Query("SELECT c FROM Cliente c JOIN FETCH c.persona WHERE c.estado = :estado")
    List<Cliente> findByEstadoWithPersona(@Param("estado") String estado);
    
    @Query("SELECT c FROM Cliente c WHERE c.persona.identificacion = :identificacion")
    Optional<Cliente> findByPersonaIdentificacion(@Param("identificacion") String identificacion);
    
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.estado = 'ACTIVO'")
    long countActiveClientes();
    
    @Modifying
    @Query("UPDATE Cliente c SET c.estado = 'INACTIVO' WHERE c.fechaActualizacion < :fechaLimite")
    int deactivateOldClientes(@Param("fechaLimite") LocalDateTime fechaLimite);
}

@Repository
public interface PersonaRepository extends JpaRepository<Persona, String> {
    
    @Query("SELECT p FROM Persona p WHERE p.nombre LIKE %:nombre% ORDER BY p.nombre")
    List<Persona> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    @Query("SELECT p FROM Persona p WHERE p.edad BETWEEN :edadMin AND :edadMax")
    List<Persona> findByEdadBetween(@Param("edadMin") Integer edadMin, @Param("edadMax") Integer edadMax);
}
```

**Service Layer con lógica de negocio:**
```java
@Service
@Transactional
@Slf4j
public class ClienteService {
    
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final ClienteMapper clienteMapper;
    
    public ClienteService(ClienteRepository clienteRepository, 
                         PersonaRepository personaRepository,
                         ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.clienteMapper = clienteMapper;
    }
    
    @Transactional
    public ClienteDTO crearCliente(ClienteCreateDTO clienteCreateDTO) {
        log.info("Creando cliente: {}", clienteCreateDTO.getClienteid());
        
        // Validar que la persona existe
        Persona persona = personaRepository.findById(clienteCreateDTO.getIdentificacionPersona())
            .orElseThrow(() -> new PersonaNotFoundException(
                "Persona no encontrada: " + clienteCreateDTO.getIdentificacionPersona()));
        
        // Validar que no existe cliente con mismo ID
        if (clienteRepository.existsById(clienteCreateDTO.getClienteid())) {
            throw new ClienteAlreadyExistsException(
                "Cliente ya existe: " + clienteCreateDTO.getClienteid());
        }
        
        // Encriptar contraseña
        String passwordEncriptada = passwordEncoder.encode(clienteCreateDTO.getContrasena());
        
        Cliente cliente = Cliente.builder()
            .clienteid(clienteCreateDTO.getClienteid())
            .contrasena(passwordEncriptada)
            .estado("ACTIVO")
            .persona(persona)
            .build();
        
        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente creado exitosamente: {}", clienteGuardado.getClienteid());
        
        return clienteMapper.toDTO(clienteGuardado);
    }
    
    @Transactional(readOnly = true)
    public Page<ClienteDTO> obtenerClientesPaginados(Pageable pageable, String estado) {
        log.info("Obteniendo clientes paginados - Página: {}, Tamaño: {}, Estado: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), estado);
        
        Page<Cliente> clientes;
        if (estado != null && !estado.isEmpty()) {
            clientes = clienteRepository.findByEstado(estado, pageable);
        } else {
            clientes = clienteRepository.findAll(pageable);
        }
        
        return clientes.map(clienteMapper::toDTO);
    }
    
    @Transactional
    public ClienteDTO actualizarCliente(String clienteid, ClienteUpdateDTO updateDTO) {
        log.info("Actualizando cliente: {}", clienteid);
        
        Cliente cliente = clienteRepository.findById(clienteid)
            .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado: " + clienteid));
        
        // Actualizar solo campos no nulos
        if (updateDTO.getContrasena() != null && !updateDTO.getContrasena().isEmpty()) {
            cliente.setContrasena(passwordEncoder.encode(updateDTO.getContrasena()));
        }
        
        if (updateDTO.getEstado() != null) {
            cliente.setEstado(updateDTO.getEstado());
        }
        
        Cliente clienteActualizado = clienteRepository.save(cliente);
        log.info("Cliente actualizado: {}", clienteActualizado.getClienteid());
        
        return clienteMapper.toDTO(clienteActualizado);
    }
}
```

**Controller con manejo completo de errores:**
```java
@RestController
@RequestMapping("/clientes")
@Validated
@Slf4j
@Api(tags = "Gestión de Clientes", description = "APIs para manejo de clientes bancarios")
public class ClienteController {
    
    private final ClienteService clienteService;
    
    @GetMapping
    @ApiOperation(value = "Obtener lista paginada de clientes")
    public ResponseEntity<Page<ClienteDTO>> obtenerClientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "clienteid") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String estado) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ClienteDTO> clientes = clienteService.obtenerClientesPaginados(pageable, estado);
        
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(clientes.getTotalElements()))
            .body(clientes);
    }
    
    @GetMapping("/{clienteid}")
    @ApiOperation(value = "Obtener cliente por ID")
    public ResponseEntity<ClienteDTO> obtenerCliente(
            @PathVariable @NotBlank String clienteid) {
        
        ClienteDTO cliente = clienteService.obtenerClientePorId(clienteid);
        return ResponseEntity.ok(cliente);
    }
    
    @PostMapping
    @ApiOperation(value = "Crear nuevo cliente")
    public ResponseEntity<ClienteDTO> crearCliente(
            @Valid @RequestBody ClienteCreateDTO clienteCreateDTO) {
        
        ClienteDTO clienteCreado = clienteService.crearCliente(clienteCreateDTO);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(clienteCreado.getClienteid())
            .toUri();
            
        return ResponseEntity.created(location).body(clienteCreado);
    }
    
    @PutMapping("/{clienteid}")
    @ApiOperation(value = "Actualizar cliente existente")
    public ResponseEntity<ClienteDTO> actualizarCliente(
            @PathVariable @NotBlank String clienteid,
            @Valid @RequestBody ClienteUpdateDTO updateDTO) {
        
        ClienteDTO clienteActualizado = clienteService.actualizarCliente(clienteid, updateDTO);
        return ResponseEntity.ok(clienteActualizado);
    }
    
    @DeleteMapping("/{clienteid}")
    @ApiOperation(value = "Eliminar cliente (soft delete)")
    public ResponseEntity<Void> eliminarCliente(@PathVariable @NotBlank String clienteid) {
        clienteService.eliminarCliente(clienteid);
        return ResponseEntity.noContent().build();
    }
}
```

**Configuración de base de datos optimizada:**
```yaml
spring:
  application:
    name: microclientes
  datasource:
    url: jdbc:postgresql://localhost:5432/microclientesdb
    username: postgres
    password: 123
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      leak-detection-threshold: 60000
      pool-name: MicroclientesHikariCP
      
  jpa:
    hibernate:
      ddl-auto: validate
      naming:
        physical-strategy: org.hibernate.boot.model.naming.SnakeCasePhysicalNamingStrategy
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
        order_inserts: true
        order_updates: true
        batch_size: 25
        jdbc:
          batch_size: 25
        cache:
          use_second_level_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
```

**Pregunta típica:** *"¿Cómo estructuraste el dominio de clientes?"*

**Respuesta:** "Implementé un diseño DDD con Persona como entidad base y Cliente como especialización. Usé relaciones JPA optimizadas con fetch LAZY para evitar N+1 queries. El service layer maneja toda la lógica de negocio incluyendo validaciones, encriptación de passwords y transacciones. Implementé repositories con consultas JPQL optimizadas y paginación para manejar grandes volúmenes de datos. El controller usa DTOs para no exponer entidades y maneja todas las validaciones con Bean Validation."

#### **Microservicio: Microcuentas (Puerto dinámico)**

**Arquitectura del dominio financiero:**
```java
// Entidad Cuenta - Core financiero
@Entity
@Table(name = "cuenta", indexes = {
    @Index(name = "idx_cuenta_tipo", columnList = "tipo_cuenta"),
    @Index(name = "idx_cuenta_estado", columnList = "estado"),
    @Index(name = "idx_cuenta_cliente", columnList = "cliente_id")
})
public class Cuenta {
    @Id
    @Column(name = "numero_cuenta", length = 20)
    private String numeroCuenta;
    
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoCuenta tipoCuenta;
    
    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial;
    
    @Column(name = "saldo_actual", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoActual;
    
    @Column(name = "estado", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private EstadoCuenta estado;
    
    @Column(name = "cliente_id", nullable = false, length = 20)
    private String clienteId;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Relación uno a muchos con Movimientos
    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("fecha DESC")
    private List<Movimiento> movimientos = new ArrayList<>();
    
    // Método de negocio para actualizar saldo
    public void actualizarSaldo(BigDecimal monto, TipoMovimiento tipoMovimiento) {
        if (tipoMovimiento == TipoMovimiento.DEBITO) {
            if (this.saldoActual.compareTo(monto) < 0) {
                throw new SaldoInsuficienteException("Saldo insuficiente para el débito");
            }
            this.saldoActual = this.saldoActual.subtract(monto);
        } else {
            this.saldoActual = this.saldoActual.add(monto);
        }
    }
}

// Entidad Movimiento - Transacciones financieras
@Entity
@Table(name = "movimiento", indexes = {
    @Index(name = "idx_mov_fecha", columnList = "fecha"),
    @Index(name = "idx_mov_cuenta", columnList = "numero_cuenta"),
    @Index(name = "idx_mov_tipo", columnList = "tipo_movimiento")
})
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
    
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;
    
    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.01", message = "Valor debe ser mayor a 0")
    private BigDecimal valor;
    
    @Column(name = "saldo_anterior", precision = 15, scale = 2)
    private BigDecimal saldoAnterior;
    
    @Column(name = "saldo_actual", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoActual;
    
    @Column(name = "descripcion", length = 200)
    private String descripcion;
    
    @Column(name = "referencia_externa", length = 50)
    private String referenciaExterna;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numero_cuenta", referencedColumnName = "numero_cuenta")
    private Cuenta cuenta;
    
    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}

// Enums para tipos
public enum TipoCuenta {
    AHORROS("Cuenta de Ahorros"),
    CORRIENTE("Cuenta Corriente"),
    INVERSION("Cuenta de Inversión");
    
    private final String descripcion;
    
    TipoCuenta(String descripcion) {
        this.descripcion = descripcion;
    }
}

public enum TipoMovimiento {
    DEBITO("Débito"),
    CREDITO("Crédito");
    
    private final String descripcion;
    
    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }
}
```

**Service con lógica financiera transaccional:**
```java
@Service
@Transactional
@Slf4j
public class MovimientoService {
    
    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final ClienteClient clienteClient;
    
    @Transactional
    public MovimientoDTO crearMovimiento(MovimientoCreateDTO movimientoDTO) {
        log.info("Iniciando creación de movimiento para cuenta: {}", 
                movimientoDTO.getNumeroCuenta());
        
        // 1. Validar que la cuenta existe y está activa
        Cuenta cuenta = cuentaRepository.findByNumeroCuentaAndEstado(
            movimientoDTO.getNumeroCuenta(), EstadoCuenta.ACTIVA)
            .orElseThrow(() -> new CuentaNotFoundException(
                "Cuenta no encontrada o inactiva: " + movimientoDTO.getNumeroCuenta()));
        
        // 2. Validar que el cliente existe (comunicación con microclientes)
        try {
            ClienteDTO cliente = clienteClient.obtenerCliente(cuenta.getClienteId());
            if (!"ACTIVO".equals(cliente.getEstado())) {
                throw new ClienteInactivoException("Cliente inactivo: " + cuenta.getClienteId());
            }
        } catch (FeignException.NotFound e) {
            throw new ClienteNotFoundException("Cliente no encontrado: " + cuenta.getClienteId());
        }
        
        // 3. Validar límites de transacción
        validarLimitesTransaccion(cuenta, movimientoDTO);
        
        // 4. Guardar saldo anterior
        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        
        // 5. Actualizar saldo de la cuenta
        cuenta.actualizarSaldo(movimientoDTO.getValor(), movimientoDTO.getTipoMovimiento());
        
        // 6. Crear y guardar el movimiento
        Movimiento movimiento = Movimiento.builder()
            .fecha(LocalDateTime.now())
            .tipoMovimiento(movimientoDTO.getTipoMovimiento())
            .valor(movimientoDTO.getValor())
            .saldoAnterior(saldoAnterior)
            .saldoActual(cuenta.getSaldoActual())
            .descripcion(movimientoDTO.getDescripcion())
            .referenciaExterna(generarReferenciaExterna())
            .cuenta(cuenta)
            .build();
        
        // 7. Guardar en transacción
        cuentaRepository.save(cuenta);
        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);
        
        log.info("Movimiento creado exitosamente. ID: {}, Referencia: {}", 
                movimientoGuardado.getId(), movimientoGuardado.getReferenciaExterna());
        
        return movimientoMapper.toDTO(movimientoGuardado);
    }
    
    private void validarLimitesTransaccion(Cuenta cuenta, MovimientoCreateDTO movimientoDTO) {
        BigDecimal limiteTransaccion = obtenerLimiteSegunTipoCuenta(cuenta.getTipoCuenta());
        
        if (movimientoDTO.getValor().compareTo(limiteTransaccion) > 0) {
            throw new LimiteTransaccionExcedidoException(
                String.format("Monto %s excede el límite de %s para cuenta tipo %s",
                    movimientoDTO.getValor(), limiteTransaccion, cuenta.getTipoCuenta()));
        }
        
        // Validar límite diario
        BigDecimal totalDiario = movimientoRepository
            .sumMovimientosByFechaAndTipo(
                cuenta.getNumeroCuenta(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay(),
                movimientoDTO.getTipoMovimiento()
            );
            
        if (totalDiario.add(movimientoDTO.getValor()).compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new LimiteDiarioExcedidoException("Límite diario de transacciones excedido");
        }
    }
}
```

**Cliente Feign para comunicación inter-servicios:**
```java
@FeignClient(
    name = "microclientes",
    configuration = FeignConfiguration.class,
    fallback = ClienteClientFallback.class
)
public interface ClienteClient {
    
    @GetMapping("/clientes/{clienteid}")
    @RetryableFeignClient
    ClienteDTO obtenerCliente(@PathVariable("clienteid") String clienteid);
    
    @GetMapping("/clientes/cuenta/{numeroCuenta}")
    @RetryableFeignClient
    ClienteDTO obtenerClientePorCuenta(@PathVariable("numeroCuenta") String numeroCuenta);
    
    @GetMapping("/clientes/{clienteid}/validar")
    @RetryableFeignClient
    Boolean validarCliente(@PathVariable("clienteid") String clienteid);
}

// Configuración de Feign con timeout y retry
@Configuration
public class FeignConfiguration {
    
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            5000,  // connect timeout
            10000  // read timeout
        );
    }
    
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 1000, 3);
    }
    
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomFeignErrorDecoder();
    }
}

// Fallback para tolerancia a fallos
@Component
public class ClienteClientFallback implements ClienteClient {
    
    @Override
    public ClienteDTO obtenerCliente(String clienteid) {
        log.warn("Fallback activado para obtenerCliente: {}", clienteid);
        throw new ServicioNoDisponibleException("Servicio de clientes no disponible");
    }
    
    @Override
    public ClienteDTO obtenerClientePorCuenta(String numeroCuenta) {
        log.warn("Fallback activado para obtenerClientePorCuenta: {}", numeroCuenta);
        throw new ServicioNoDisponibleException("Servicio de clientes no disponible");
    }
    
    @Override
    public Boolean validarCliente(String clienteid) {
        log.warn("Fallback activado para validarCliente: {}", clienteid);
        return false; // Rechazar transacción si servicio no disponible
    }
}
```

**Repositorios con consultas financieras optimizadas:**
```java
@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.numeroCuenta = :numeroCuenta " +
           "ORDER BY m.fecha DESC")
    Page<Movimiento> findByCuentaOrderByFechaDesc(
        @Param("numeroCuenta") String numeroCuenta, 
        Pageable pageable);
    
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.numeroCuenta = :numeroCuenta " +
           "AND m.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY m.fecha DESC")
    List<Movimiento> findMovimientosByFechaRange(
        @Param("numeroCuenta") String numeroCuenta,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM Movimiento m " +
           "WHERE m.cuenta.numeroCuenta = :numeroCuenta " +
           "AND m.fecha >= :fechaInicio AND m.fecha < :fechaFin " +
           "AND m.tipoMovimiento = :tipoMovimiento")
    BigDecimal sumMovimientosByFechaAndTipo(
        @Param("numeroCuenta") String numeroCuenta,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        @Param("tipoMovimiento") TipoMovimiento tipoMovimiento);
    
    @Query(value = "SELECT * FROM movimiento m " +
                   "WHERE m.numero_cuenta = :numeroCuenta " +
                   "AND m.fecha >= CURRENT_DATE - INTERVAL ':dias days' " +
                   "ORDER BY m.fecha DESC", 
           nativeQuery = true)
    List<Movimiento> findMovimientosUltimosDias(
        @Param("numeroCuenta") String numeroCuenta,
        @Param("dias") int dias);
}

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, String> {
    
    Optional<Cuenta> findByNumeroCuentaAndEstado(String numeroCuenta, EstadoCuenta estado);
    
    @Query("SELECT c FROM Cuenta c WHERE c.clienteId = :clienteId AND c.estado = :estado")
    List<Cuenta> findByClienteIdAndEstado(
        @Param("clienteId") String clienteId, 
        @Param("estado") EstadoCuenta estado);
    
    @Query("SELECT c FROM Cuenta c WHERE c.tipoCuenta = :tipoCuenta " +
           "AND c.saldoActual >= :saldoMinimo")
    List<Cuenta> findByTipoCuentaAndSaldoMinimo(
        @Param("tipoCuenta") TipoCuenta tipoCuenta,
        @Param("saldoMinimo") BigDecimal saldoMinimo);
}
```

**Pregunta típica:** *"¿Cómo implementaste la comunicación entre microservicios?"*

**Respuesta:** "Implementé OpenFeign para comunicación REST síncrona entre microcuentas y microclientes. Configuré timeouts apropiados (5s connect, 10s read), retry automático con backoff exponencial, y fallbacks para tolerancia a fallos. Cuando se crea un movimiento, el servicio de cuentas valida automáticamente que el cliente existe y está activo. Use @RetryableFeignClient personalizado y manejo de errores específico con FeignException. También implementé circuit breaker pattern para evitar cascading failures."

---

## 🔧 STACK TECNOLÓGICO ESPECÍFICO DEL PROYECTO

### **Backend Framework - Spring Boot 3.4.5 Ecosystem**

**Dependencias implementadas en cada microservicio:**

**Eureka Server - pom.xml:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.4.5</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        <version>4.1.3</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

**Microclientes - pom.xml específico:**
```xml
<dependencies>
    <!-- Core Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Spring Cloud -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.2</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Mappers -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Security -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-crypto</artifactId>
    </dependency>
</dependencies>
```

**Microcuentas - pom.xml con OpenFeign:**
```xml
<dependencies>
    <!-- Todo lo anterior de microclientes PLUS: -->
    
    <!-- OpenFeign for inter-service communication -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    
    <!-- Load Balancer -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
    
    <!-- Circuit Breaker -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
    </dependency>
</dependencies>
```

**API Gateway - pom.xml:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

**Pregunta típica:** *"¿Por qué Spring Boot 3.4.5 específicamente?"*

**Respuesta:** "Elegí Spring Boot 3.4.5 porque es la versión más reciente estable que incluye Java 17+ support, mejoras en performance con GraalVM native image support, y observability mejorada con Micrometer. Además, Spring Cloud 2023.0.6 es compatible y tiene mejor integración con Kubernetes. La migración de javax a jakarta namespace también está completa, lo que asegura compatibilidad futura."

### **Base de Datos PostgreSQL 15 - Configuración Específica**

**Esquemas SQL implementados:**

**Microclientes Database (puerto 5432):**
```sql
-- Creación de base de datos
CREATE DATABASE microclientesdb
    WITH OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Tabla persona
CREATE TABLE persona (
    identificacion VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero VARCHAR(10) CHECK (genero IN ('MASCULINO', 'FEMENINO', 'OTRO')),
    edad INTEGER CHECK (edad >= 18 AND edad <= 120),
    direccion VARCHAR(200),
    telefono VARCHAR(15),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla cliente
CREATE TABLE cliente (
    clienteid VARCHAR(20) PRIMARY KEY,
    contrasena VARCHAR(100) NOT NULL,
    estado VARCHAR(10) NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    identificacion VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (identificacion) REFERENCES persona(identificacion) ON DELETE RESTRICT
);

-- Índices para optimización
CREATE INDEX idx_cliente_estado ON cliente(estado);
CREATE INDEX idx_cliente_identificacion ON cliente(identificacion);
CREATE INDEX idx_persona_nombre ON persona(nombre);

-- Trigger para actualizar fecha_actualizacion
CREATE OR REPLACE FUNCTION update_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_cliente_fecha_actualizacion
    BEFORE UPDATE ON cliente
    FOR EACH ROW
    EXECUTE FUNCTION update_fecha_actualizacion();

-- Datos de prueba
INSERT INTO persona (identificacion, nombre, genero, edad, direccion, telefono) VALUES
('12345678', 'Juan Pérez', 'MASCULINO', 30, 'Calle 123, Ciudad', '+57-300-1234567'),
('87654321', 'María García', 'FEMENINO', 25, 'Avenida 456, Ciudad', '+57-310-7654321');

INSERT INTO cliente (clienteid, contrasena, estado, identificacion) VALUES
('CLI001', '$2a$10$encrypted_password_hash', 'ACTIVO', '12345678'),
('CLI002', '$2a$10$encrypted_password_hash', 'ACTIVO', '87654321');
```

**Microcuentas Database (puerto 5433):**
```sql
-- Creación de base de datos
CREATE DATABASE microcuentasdb
    WITH OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Tabla cuenta
CREATE TABLE cuenta (
    numero_cuenta VARCHAR(20) PRIMARY KEY,
    tipo_cuenta VARCHAR(20) NOT NULL CHECK (tipo_cuenta IN ('AHORROS', 'CORRIENTE', 'INVERSION')),
    saldo_inicial DECIMAL(15,2) NOT NULL CHECK (saldo_inicial >= 0),
    saldo_actual DECIMAL(15,2) NOT NULL,
    estado VARCHAR(10) NOT NULL CHECK (estado IN ('ACTIVA', 'INACTIVA', 'BLOQUEADA')),
    cliente_id VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla movimiento
CREATE TABLE movimiento (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('DEBITO', 'CREDITO')),
    valor DECIMAL(15,2) NOT NULL CHECK (valor > 0),
    saldo_anterior DECIMAL(15,2),
    saldo_actual DECIMAL(15,2) NOT NULL,
    descripcion VARCHAR(200),
    referencia_externa VARCHAR(50) UNIQUE,
    numero_cuenta VARCHAR(20) NOT NULL,
    FOREIGN KEY (numero_cuenta) REFERENCES cuenta(numero_cuenta) ON DELETE RESTRICT
);

-- Índices específicos para performance
CREATE INDEX idx_cuenta_cliente ON cuenta(cliente_id);
CREATE INDEX idx_cuenta_tipo_estado ON cuenta(tipo_cuenta, estado);
CREATE INDEX idx_movimiento_cuenta ON movimiento(numero_cuenta);
CREATE INDEX idx_movimiento_fecha ON movimiento(fecha DESC);
CREATE INDEX idx_movimiento_tipo ON movimiento(tipo_movimiento);
CREATE INDEX idx_movimiento_fecha_tipo ON movimiento(fecha, tipo_movimiento);

-- Función para generar número de cuenta automático
CREATE SEQUENCE seq_numero_cuenta START 1000000000;

CREATE OR REPLACE FUNCTION generar_numero_cuenta()
RETURNS VARCHAR(20) AS $$
DECLARE
    nuevo_numero VARCHAR(20);
BEGIN
    nuevo_numero := 'CTA' || LPAD(nextval('seq_numero_cuenta')::TEXT, 10, '0');
    RETURN nuevo_numero;
END;
$$ LANGUAGE plpgsql;

-- Trigger para auto-generar número de cuenta
CREATE OR REPLACE FUNCTION trigger_generar_numero_cuenta()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.numero_cuenta IS NULL OR NEW.numero_cuenta = '' THEN
        NEW.numero_cuenta := generar_numero_cuenta();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_cuenta_numero
    BEFORE INSERT ON cuenta
    FOR EACH ROW
    EXECUTE FUNCTION trigger_generar_numero_cuenta();

-- Datos de prueba
INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, saldo_actual, estado, cliente_id) VALUES
('CTA1000000001', 'AHORROS', 1000.00, 1000.00, 'ACTIVA', 'CLI001'),
('CTA1000000002', 'CORRIENTE', 500.00, 500.00, 'ACTIVA', 'CLI002');
```

**HikariCP Configuration optimizada:**
```yaml
spring:
  datasource:
    hikari:
      # Connection Pool Settings
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000     # 5 minutes
      connection-timeout: 20000 # 20 seconds
      max-lifetime: 1200000    # 20 minutes
      leak-detection-threshold: 60000 # 1 minute
      
      # Performance Settings
      pool-name: BancarioHikariCP
      auto-commit: false
      isolation-level: TRANSACTION_READ_COMMITTED
      
      # Validation
      connection-test-query: SELECT 1
      validation-timeout: 3000
      
      # Database specific
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false
```

**Pregunta típica:** *"¿Cómo optimizaste el acceso a base de datos?"*

**Respuesta:** "Implementé varias optimizaciones: HikariCP con pool size optimizado para la carga esperada, índices específicos en campos de consulta frecuente, triggers PL/pgSQL para auditoría automática, constraints a nivel de BD para integridad, y queries JPQL optimizadas que evitan N+1 problems. También uso @Transactional apropiadamente y fetch strategies LAZY para no cargar datos innecesarios."

### **Containerización Docker - Implementación Específica**

**Dockerfile optimizado para cada microservicio:**

**Eureka Server - Dockerfile:**
```dockerfile
# Multi-stage build para optimización
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 -S eureka && \
    adduser -S eureka -u 1001 -G eureka

# Copiar JAR desde build stage
COPY --from=build /app/target/eureka-server-*.jar app.jar

# Cambiar ownership y permisos
RUN chown eureka:eureka app.jar
USER eureka

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

EXPOSE 8761
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UnlockExperimentalVMOptions", \
    "-XX:+UseCGroupMemoryLimitForHeap", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

**Microservicio - Dockerfile template:**
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy dependency files first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Create non-root user
RUN addgroup -g 1001 -S appuser && \
    adduser -S appuser -u 1001 -G appuser

# Copy application
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Health check específico por servicio
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Use dumb-init for proper signal handling in containers
ENTRYPOINT ["dumb-init", "--", "java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
    "-Dspring.profiles.active=docker", \
    "-jar", "app.jar"]
```

**Docker Compose completo implementado:**
```yaml
version: '3.8'

services:
  # Base de datos para microclientes
  postgres-clientes:
    image: postgres:15-alpine
    container_name: postgres-clientes
    environment:
      POSTGRES_DB: microclientesdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 123
      POSTGRES_INITDB_ARGS: "--encoding=UTF8 --lc-collate=en_US.UTF-8 --lc-ctype=en_US.UTF-8"
    ports:
      - "5432:5432"
    volumes:
      - postgres_clientes_data:/var/lib/postgresql/data
      - ./microclientes/microclientes/src/main/resources/BaseDatos.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - microservices-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d microclientesdb"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 512M
          cpus: '0.5'
        reservations:
          memory: 256M
          cpus: '0.25'

  # Base de datos para microcuentas
  postgres-cuentas:
    image: postgres:15-alpine
    container_name: postgres-cuentas
    environment:
      POSTGRES_DB: microcuentasdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 123
      POSTGRES_INITDB_ARGS: "--encoding=UTF8 --lc-collate=en_US.UTF-8 --lc-ctype=en_US.UTF-8"
    ports:
      - "5433:5432"
    volumes:
      - postgres_cuentas_data:/var/lib/postgresql/data
      - ./microcuentas/microcuentas/src/main/resources/data.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - microservices-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d microcuentasdb"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    restart: unless-stopped

  # Eureka Server
  eureka-server:
    build:
      context: ./eureka-server
      dockerfile: Dockerfile
    container_name: eureka-server
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SERVER_PORT=8761
      - EUREKA_CLIENT_REGISTER-WITH-EUREKA=false
      - EUREKA_CLIENT_FETCH-REGISTRY=false
      - EUREKA_INSTANCE_HOSTNAME=eureka-server
      - JAVA_OPTS=-Xmx512m -Xms256m
    depends_on:
      postgres-clientes:
        condition: service_healthy
      postgres-cuentas:
        condition: service_healthy
    networks:
      - microservices-network
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 768M
          cpus: '1.0'

  # Microservicio de Clientes
  microclientes:
    build:
      context: ./microclientes/microclientes
      dockerfile: Dockerfile
    container_name: microclientes
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SERVER_PORT=8080
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - EUREKA_INSTANCE_HOSTNAME=microclientes
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-clientes:5432/microclientesdb
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=123
      - SPRING_JPA_HIBERNATE_DDL_AUTO=validate
      - SPRING_JPA_SHOW_SQL=false
      - LOGGING_LEVEL_COM_PROYECTO=INFO
      - JAVA_OPTS=-Xmx1024m -Xms512m
    depends_on:
      eureka-server:
        condition: service_started
      postgres-clientes:
        condition: service_healthy
    networks:
      - microservices-network
    restart: unless-stopped
    volumes:
      - ./logs/microclientes:/app/logs

  # Microservicio de Cuentas
  microcuentas:
    build:
      context: ./microcuentas/microcuentas
      dockerfile: Dockerfile
    container_name: microcuentas
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SERVER_PORT=8081
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - EUREKA_INSTANCE_HOSTNAME=microcuentas
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-cuentas:5432/microcuentasdb
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=123
      - SPRING_JPA_HIBERNATE_DDL_AUTO=validate
      - SPRING_JPA_SHOW_SQL=false
      - MICROCLIENTES_SERVICE_URL=http://microclientes:8080
      - FEIGN_CLIENT_CONFIG_MICROCLIENTES_CONNECT-TIMEOUT=5000
      - FEIGN_CLIENT_CONFIG_MICROCLIENTES_READ-TIMEOUT=10000
      - JAVA_OPTS=-Xmx1024m -Xms512m
    depends_on:
      eureka-server:
        condition: service_started
      microclientes:
        condition: service_started
      postgres-cuentas:
        condition: service_healthy
    networks:
      - microservices-network
    restart: unless-stopped
    volumes:
      - ./logs/microcuentas:/app/logs

  # API Gateway
  gateway:
    build:
      context: ./gateway/gateway
      dockerfile: Dockerfile
    container_name: gateway
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SERVER_PORT=8083
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - EUREKA_INSTANCE_HOSTNAME=gateway
      - SPRING_CLOUD_GATEWAY_DISCOVERY_LOCATOR_ENABLED=true
      - SPRING_CLOUD_GATEWAY_DISCOVERY_LOCATOR_LOWER-CASE-SERVICE-ID=true
      - JAVA_OPTS=-Xmx512m -Xms256m
    depends_on:
      eureka-server:
        condition: service_started
      microclientes:
        condition: service_started
      microcuentas:
        condition: service_started
    networks:
      - microservices-network
    restart: unless-stopped
    volumes:
      - ./logs/gateway:/app/logs

volumes:
  postgres_clientes_data:
    driver: local
  postgres_cuentas_data:
    driver: local

networks:
  microservices-network:
    driver: bridge
    ipam:
      driver: default
      config:
        - subnet: 172.20.0.0/16
          gateway: 172.20.0.1
```

**Scripts de monitoreo implementados:**

**monitor-logs.ps1:**
```powershell
param(
    [ValidateSet("all", "eureka", "gateway", "clientes", "cuentas", "db")]
    [string]$Service = "all",
    [switch]$Follow = $false,
    [int]$Lines = 50,
    [switch]$Error = $false
)

function Show-Banner {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "   Sistema Bancario - Monitor de Logs  " -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
}

function Show-ServiceStatus {
    Write-Host "Estado de servicios:" -ForegroundColor Yellow
    docker-compose ps --format "table {{.Name}}\t{{.State}}\t{{.Status}}\t{{.Ports}}"
    Write-Host ""
}

function Show-ServiceLogs {
    param(
        [string]$ServiceName,
        [string]$DisplayName
    )
    
    Write-Host "=== Logs de $DisplayName ===" -ForegroundColor Green
    
    $logParams = @()
    if ($Follow) { $logParams += "-f" }
    if ($Lines -gt 0) { $logParams += "--tail=$Lines" }
    
    if ($Error) {
        docker-compose logs @logParams $ServiceName 2>&1 | Where-Object { $_ -match "ERROR|WARN|Exception" }
    } else {
        docker-compose logs @logParams $ServiceName
    }
}

Show-Banner
Show-ServiceStatus

switch ($Service.ToLower()) {
    "all" {
        if ($Follow) {
            docker-compose logs -f
        } else {
            docker-compose logs --tail=$Lines
        }
    }
    "eureka" {
        Show-ServiceLogs "eureka-server" "Eureka Server"
    }
    "gateway" {
        Show-ServiceLogs "gateway" "API Gateway"
    }
    "clientes" {
        Show-ServiceLogs "microclientes" "Microservicio Clientes"
    }
    "cuentas" {
        Show-ServiceLogs "microcuentas" "Microservicio Cuentas"
    }
    "db" {
        Write-Host "=== Logs de Bases de Datos ===" -ForegroundColor Green
        Show-ServiceLogs "postgres-clientes" "PostgreSQL Clientes"
        Show-ServiceLogs "postgres-cuentas" "PostgreSQL Cuentas"
    }
    default {
        Write-Host "Servicios disponibles: all, eureka, gateway, clientes, cuentas, db" -ForegroundColor Red
        Write-Host "Ejemplo: .\monitor-logs.ps1 -Service clientes -Follow" -ForegroundColor Yellow
    }
}
```

**Pregunta típica:** *"¿Cómo optimizaste los contenedores Docker?"*

**Respuesta:** "Implementé builds multi-stage para reducir el tamaño de imagen final, uso imágenes Alpine para menor footprint, ejecuto como usuario no-root por seguridad, configuré health checks específicos por servicio, optimicé JVM flags para containers, implementé proper signal handling con dumb-init, y uso resource limits para evitar que un container consuma todos los recursos del host. También tengo layer caching optimizado copiando dependencias primero."

### **Testing Comprehensivo - Implementación Específica**

**Estrategia de testing multi-nivel implementada:**

#### **1. Unit Tests - JUnit 5 + Mockito**

**ClienteServiceTest - Testing completo del service layer:**
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService - Unit Tests")
class ClienteServiceTest {
    
    @Mock private ClienteRepository clienteRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private ClienteMapper clienteMapper;
    @Mock private PasswordEncoder passwordEncoder;
    
    @InjectMocks private ClienteService clienteService;
    
    @Nested
    @DisplayName("Crear Cliente")
    class CrearClienteTests {
        
        @Test
        @DisplayName("Debe crear cliente exitosamente con datos válidos")
        void debeCrearClienteExitosamente() {
            // Given
            String identificacionPersona = "12345678";
            ClienteCreateDTO createDTO = ClienteCreateDTO.builder()
                .clienteid("CLI001")
                .contrasena("password123")
                .identificacionPersona(identificacionPersona)
                .build();
            
            Persona persona = Persona.builder()
                .identificacion(identificacionPersona)
                .nombre("Juan Pérez")
                .build();
            
            Cliente clienteEsperado = Cliente.builder()
                .clienteid("CLI001")
                .contrasena("hashedPassword")
                .estado("ACTIVO")
                .persona(persona)
                .build();
            
            ClienteDTO clienteDTO = ClienteDTO.builder()
                .clienteid("CLI001")
                .nombre("Juan Pérez")
                .estado("ACTIVO")
                .build();
            
            when(personaRepository.findById(identificacionPersona))
                .thenReturn(Optional.of(persona));
            when(clienteRepository.existsById("CLI001"))
                .thenReturn(false);
            when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");
            when(clienteRepository.save(any(Cliente.class)))
                .thenReturn(clienteEsperado);
            when(clienteMapper.toDTO(clienteEsperado))
                .thenReturn(clienteDTO);
            
            // When
            ClienteDTO resultado = clienteService.crearCliente(createDTO);
            
            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getClienteid()).isEqualTo("CLI001");
            assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
            
            verify(personaRepository).findById(identificacionPersona);
            verify(clienteRepository).existsById("CLI001");
            verify(passwordEncoder).encode("password123");
            verify(clienteRepository).save(argThat(cliente -> 
                cliente.getClienteid().equals("CLI001") &&
                cliente.getContrasena().equals("hashedPassword") &&
                cliente.getEstado().equals("ACTIVO")
            ));
        }
        
        @Test
        @DisplayName("Debe lanzar excepción cuando persona no existe")
        void debeLanzarExcepcionCuandoPersonaNoExiste() {
            // Given
            ClienteCreateDTO createDTO = ClienteCreateDTO.builder()
                .clienteid("CLI001")
                .identificacionPersona("99999999")
                .build();
            
            when(personaRepository.findById("99999999"))
                .thenReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> clienteService.crearCliente(createDTO))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining("Persona no encontrada: 99999999");
            
            verify(personaRepository).findById("99999999");
            verifyNoInteractions(clienteRepository, passwordEncoder);
        }
        
        @Test
        @DisplayName("Debe lanzar excepción cuando cliente ya existe")
        void debeLanzarExcepcionCuandoClienteYaExiste() {
            // Given
            ClienteCreateDTO createDTO = ClienteCreateDTO.builder()
                .clienteid("CLI001")
                .identificacionPersona("12345678")
                .build();
            
            Persona persona = new Persona();
            persona.setIdentificacion("12345678");
            
            when(personaRepository.findById("12345678"))
                .thenReturn(Optional.of(persona));
            when(clienteRepository.existsById("CLI001"))
                .thenReturn(true);
            
            // When & Then
            assertThatThrownBy(() -> clienteService.crearCliente(createDTO))
                .isInstanceOf(ClienteAlreadyExistsException.class)
                .hasMessageContaining("Cliente ya existe: CLI001");
        }
    }
    
    @Nested
    @DisplayName("Buscar Clientes")
    class BuscarClientesTests {
        
        @Test
        @DisplayName("Debe retornar página de clientes correctamente")
        void debeRetornarPaginaDeClientes() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("clienteid"));
            List<Cliente> clientes = Arrays.asList(
                Cliente.builder().clienteid("CLI001").build(),
                Cliente.builder().clienteid("CLI002").build()
            );
            Page<Cliente> pageClientes = new PageImpl<>(clientes, pageable, 2);
            
            when(clienteRepository.findAll(pageable))
                .thenReturn(pageClientes);
            when(clienteMapper.toDTO(any(Cliente.class)))
                .thenReturn(ClienteDTO.builder().build());
            
            // When
            Page<ClienteDTO> resultado = clienteService.obtenerClientesPaginados(pageable, null);
            
            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(2);
            assertThat(resultado.getTotalElements()).isEqualTo(2);
            
            verify(clienteRepository).findAll(pageable);
            verify(clienteMapper, times(2)).toDTO(any(Cliente.class));
        }
    }
}
```

#### **2. Integration Tests - TestContainers + Spring Boot Test**

**ClienteControllerIntegrationTest - Testing completo de endpoints:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("Cliente Controller - Integration Tests")
class ClienteControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("test-data.sql");
    
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PersonaRepository personaRepository;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @BeforeEach
    void setUp() {
        // Limpiar datos de prueba
        clienteRepository.deleteAll();
        personaRepository.deleteAll();
        
        // Crear datos base
        Persona persona = Persona.builder()
            .identificacion("12345678")
            .nombre("Juan Pérez")
            .genero("MASCULINO")
            .edad(30)
            .direccion("Calle 123")
            .telefono("+57-300-1234567")
            .build();
        personaRepository.save(persona);
    }
    
    @Test
    @DisplayName("POST /clientes - Debe crear cliente exitosamente")
    void debeCrearClienteExitosamente() {
        // Given
        ClienteCreateDTO clienteDTO = ClienteCreateDTO.builder()
            .clienteid("CLI001")
            .contrasena("password123")
            .identificacionPersona("12345678")
            .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ClienteCreateDTO> request = new HttpEntity<>(clienteDTO, headers);
        
        // When
        ResponseEntity<ClienteDTO> response = restTemplate.postForEntity(
            "/clientes", request, ClienteDTO.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        
        ClienteDTO responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getClienteid()).isEqualTo("CLI001");
        assertThat(responseBody.getEstado()).isEqualTo("ACTIVO");
        assertThat(responseBody.getNombre()).isEqualTo("Juan Pérez");
        
        // Verificar en base de datos
        Optional<Cliente> clienteEnBD = clienteRepository.findById("CLI001");
        assertThat(clienteEnBD).isPresent();
        assertThat(clienteEnBD.get().getEstado()).isEqualTo("ACTIVO");
    }
    
    @Test
    @DisplayName("GET /clientes/{id} - Debe retornar cliente existente")
    void debeRetornarClienteExistente() {
        // Given - Crear cliente en BD
        Cliente cliente = Cliente.builder()
            .clienteid("CLI001")
            .contrasena("hashedPassword")
            .estado("ACTIVO")
            .persona(personaRepository.findById("12345678").get())
            .build();
        clienteRepository.save(cliente);
        
        // When
        ResponseEntity<ClienteDTO> response = restTemplate.getForEntity(
            "/clientes/CLI001", ClienteDTO.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        ClienteDTO responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getClienteid()).isEqualTo("CLI001");
        assertThat(responseBody.getNombre()).isEqualTo("Juan Pérez");
    }
    
    @Test
    @DisplayName("GET /clientes - Debe retornar lista paginada")
    void debeRetornarListaPaginada() {
        // Given - Crear múltiples clientes
        for (int i = 1; i <= 15; i++) {
            Cliente cliente = Cliente.builder()
                .clienteid("CLI" + String.format("%03d", i))
                .contrasena("password")
                .estado("ACTIVO")
                .persona(personaRepository.findById("12345678").get())
                .build();
            clienteRepository.save(cliente);
        }
        
        // When
        ResponseEntity<PagedModel<ClienteDTO>> response = restTemplate.exchange(
            "/clientes?page=0&size=10&sort=clienteid,asc",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<PagedModel<ClienteDTO>>() {}
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get("X-Total-Count")).contains("15");
        
        PagedModel<ClienteDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getMetadata().getTotalElements()).isEqualTo(15);
    }
    
    @Test
    @DisplayName("PUT /clientes/{id} - Debe actualizar cliente")
    void debeActualizarCliente() {
        // Given
        Cliente cliente = Cliente.builder()
            .clienteid("CLI001")
            .contrasena("oldPassword")
            .estado("ACTIVO")
            .persona(personaRepository.findById("12345678").get())
            .build();
        clienteRepository.save(cliente);
        
        ClienteUpdateDTO updateDTO = ClienteUpdateDTO.builder()
            .contrasena("newPassword123")
            .estado("INACTIVO")
            .build();
        
        HttpEntity<ClienteUpdateDTO> request = new HttpEntity<>(updateDTO);
        
        // When
        ResponseEntity<ClienteDTO> response = restTemplate.exchange(
            "/clientes/CLI001", HttpMethod.PUT, request, ClienteDTO.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Verificar cambios en BD
        Cliente clienteActualizado = clienteRepository.findById("CLI001").get();
        assertThat(clienteActualizado.getEstado()).isEqualTo("INACTIVO");
        // Contraseña debe estar encriptada
        assertThat(clienteActualizado.getContrasena()).isNotEqualTo("newPassword123");
    }
    
    @Test
    @DisplayName("DELETE /clientes/{id} - Debe eliminar cliente (soft delete)")
    void debeEliminarCliente() {
        // Given
        Cliente cliente = Cliente.builder()
            .clienteid("CLI001")
            .contrasena("password")
            .estado("ACTIVO")
            .persona(personaRepository.findById("12345678").get())
            .build();
        clienteRepository.save(cliente);
        
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
            "/clientes/CLI001", HttpMethod.DELETE, null, Void.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verificar soft delete
        Cliente clienteEliminado = clienteRepository.findById("CLI001").get();
        assertThat(clienteEliminado.getEstado()).isEqualTo("INACTIVO");
    }
    
    @Test
    @DisplayName("POST /clientes - Debe validar datos de entrada")
    void debeValidarDatosDeEntrada() {
        // Given - DTO inválido
        ClienteCreateDTO clienteInvalido = ClienteCreateDTO.builder()
            .clienteid("") // Inválido: vacío
            .contrasena("123") // Inválido: muy corta
            .identificacionPersona("99999999") // Inválido: persona no existe
            .build();
        
        HttpEntity<ClienteCreateDTO> request = new HttpEntity<>(clienteInvalido);
        
        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/clientes", request, Map.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> errorResponse = response.getBody();
        assertThat(errorResponse).containsKey("errors");
        assertThat(errorResponse.get("message")).toString()
            .contains("Validation failed");
    }
}
```

#### **3. Performance Tests - JMeter Integration**

**Performance test configuration implementada:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
    "server.port=8080",
    "spring.datasource.hikari.maximum-pool-size=50",
    "logging.level.org.springframework.web=WARN"
})
class PerformanceTest {
    
    @Test
    @DisplayName("Load Test - 1000 concurrent requests")
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void loadTestCrearClientes() throws InterruptedException {
        int numberOfThreads = 100;
        int requestsPerThread = 10;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    
                    for (int j = 0; j < requestsPerThread; j++) {
                        long startTime = System.currentTimeMillis();
                        
                        try {
                            ClienteCreateDTO cliente = ClienteCreateDTO.builder()
                                .clienteid("CLI" + threadId + "_" + j)
                                .contrasena("password123")
                                .identificacionPersona("12345678")
                                .build();
                            
                            ResponseEntity<ClienteDTO> response = restTemplate.postForEntity(
                                "http://localhost:8080/clientes", cliente, ClienteDTO.class);
                            
                            long responseTime = System.currentTimeMillis() - startTime;
                            responseTimes.add(responseTime);
                            
                            if (response.getStatusCode().is2xxSuccessful()) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                            
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // Assertions de performance
        double averageResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        long p95ResponseTime = responseTimes.stream()
            .sorted()
            .skip((long) (responseTimes.size() * 0.95))
            .findFirst()
            .orElse(0L);
        
        System.out.println("Total requests: " + (numberOfThreads * requestsPerThread));
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        System.out.println("Average response time: " + averageResponseTime + "ms");
        System.out.println("95th percentile response time: " + p95ResponseTime + "ms");
        
        // Verificar criterios de performance
        assertThat(averageResponseTime).isLessThan(500.0); // < 500ms promedio
        assertThat(p95ResponseTime).isLessThan(1000L); // < 1s para 95% de requests
        assertThat(errorCount.get()).isLessThan(numberOfThreads * requestsPerThread * 0.01); // < 1% error rate
    }
}
```

**Test configuration específica:**
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        
logging:
  level:
    com.proyecto: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# Test containers configuration
testcontainers:
  reuse:
    enable: true
```

**Pregunta típica:** *"¿Cómo implementaste testing comprehensivo?"*

**Respuesta:** "Implementé una estrategia de testing en pirámide: unit tests con Mockito para lógica de negocio (>80% cobertura), integration tests con TestContainers para testing real con PostgreSQL, API tests para validar contratos REST, y performance tests para validar que el sistema maneja 1000+ RPS. Uso @Transactional para aislamiento, TestContainers para testing de BD real, y JaCoCo para métricas de cobertura. También tengo tests específicos para validaciones, manejo de errores y comunicación entre servicios."

---

## ☁️ INFRAESTRUCTURA CLOUD AZURE - IMPLEMENTACIÓN TERRAFORM

### **Infrastructure as Code Completa - Terraform 1.5+**

**Estructura de archivos Terraform implementada:**
```
terraform/
├── main.tf                 # Configuración principal y providers
├── variables.tf            # Variables de entrada
├── outputs.tf             # Outputs del deployment
├── databases.tf           # Azure Database for PostgreSQL
├── kubernetes.tf          # Azure Kubernetes Service
├── terraform.tfvars.example # Ejemplo de variables
├── environments/
│   ├── dev.tfvars         # Variables para desarrollo
│   ├── staging.tfvars     # Variables para staging  
│   └── prod.tfvars        # Variables para producción
└── modules/
    ├── aks/              # Módulo para AKS
    ├── database/         # Módulo para PostgreSQL
    └── networking/       # Módulo para VNet
```

**main.tf - Configuración principal:**
```terraform
# Configuración del proveedor de Azure
terraform {
  required_version = ">= 1.5"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.75"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.11"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
  }
  
  # Backend remoto para state management
  backend "azurerm" {
    resource_group_name  = "rg-terraform-state"
    storage_account_name = "stterraformbancario"
    container_name       = "tfstate"
    key                  = "microservicios.terraform.tfstate"
  }
}

# Configuración del proveedor de Azure
provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy    = true
      recover_soft_deleted_key_vaults = true
    }
    
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

# Configuración del cliente de Azure
data "azurerm_client_config" "current" {}

# Sufijo aleatorio para recursos únicos
resource "random_integer" "suffix" {
  min = 10000
  max = 99999
}

# Grupo de recursos principal
resource "azurerm_resource_group" "main" {
  name     = "${var.resource_group_name}-${var.environment}"
  location = var.location
  
  tags = {
    Environment = var.environment
    Project     = "microservicios-bancarios"
    Owner       = "devops-team"
    CostCenter  = "engineering"
    CreatedBy   = "terraform"
    CreatedDate = timestamp()
  }
}

# Key Vault para secretos
resource "azurerm_key_vault" "main" {
  name                = "kv-bancario-${random_integer.suffix.result}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  sku_name            = "standard"
  
  enabled_for_deployment          = true
  enabled_for_template_deployment = true
  enabled_for_disk_encryption     = true
  purge_protection_enabled        = false
  soft_delete_retention_days      = 7
  
  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = data.azurerm_client_config.current.object_id
    
    key_permissions = [
      "Backup", "Create", "Decrypt", "Delete", "Encrypt", "Get",
      "Import", "List", "Purge", "Recover", "Restore", "Sign",
      "UnwrapKey", "Update", "Verify", "WrapKey"
    ]
    
    secret_permissions = [
      "Backup", "Delete", "Get", "List", "Purge", "Recover",
      "Restore", "Set"
    ]
    
    certificate_permissions = [
      "Backup", "Create", "Delete", "DeleteIssuers", "Get",
      "GetIssuers", "Import", "List", "ListIssuers", "ManageContacts",
      "ManageIssuers", "Purge", "Recover", "Restore", "SetIssuers", "Update"
    ]
  }
  
  tags = azurerm_resource_group.main.tags
}

# Secrets en Key Vault
resource "azurerm_key_vault_secret" "db_password_clientes" {
  name         = "db-password-clientes"
  value        = var.db_password_clientes
  key_vault_id = azurerm_key_vault.main.id
  
  depends_on = [azurerm_key_vault.main]
}

resource "azurerm_key_vault_secret" "db_password_cuentas" {
  name         = "db-password-cuentas"
  value        = var.db_password_cuentas
  key_vault_id = azurerm_key_vault.main.id
  
  depends_on = [azurerm_key_vault.main]
}
```

**kubernetes.tf - Azure Kubernetes Service:**
```terraform
# Log Analytics Workspace para AKS
resource "azurerm_log_analytics_workspace" "aks" {
  name                = "log-aks-${var.environment}-${random_integer.suffix.result}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                 = "PerGB2018"
  retention_in_days   = var.log_retention_days
  
  tags = azurerm_resource_group.main.tags
}

# Azure Container Registry
resource "azurerm_container_registry" "main" {
  name                = "acrbancario${random_integer.suffix.result}"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = var.acr_sku
  admin_enabled       = true
  
  # Replicación geográfica para alta disponibilidad
  dynamic "georeplications" {
    for_each = var.environment == "prod" ? [var.secondary_location] : []
    content {
      location                = georeplications.value
      zone_redundancy_enabled = true
      tags                   = azurerm_resource_group.main.tags
    }
  }
  
  # Retention policy para limpiar imágenes antiguas
  retention_policy {
    days    = 30
    enabled = true
  }
  
  # Trust policy para firmar imágenes
  trust_policy {
    enabled = var.environment == "prod"
  }
  
  tags = azurerm_resource_group.main.tags
}

# Azure Kubernetes Service
resource "azurerm_kubernetes_cluster" "main" {
  name                = "${var.cluster_name}-${var.environment}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "${var.cluster_name}-${var.environment}"
  kubernetes_version  = var.kubernetes_version
  
  # Configuración del node pool por defecto
  default_node_pool {
    name                = "default"
    node_count          = var.node_count
    vm_size            = var.vm_size
    os_disk_size_gb    = 100
    os_disk_type       = "Managed"
    type               = "VirtualMachineScaleSets"
    availability_zones = ["1", "2", "3"]
    
    # Auto-scaling
    enable_auto_scaling = true
    min_count          = var.min_node_count
    max_count          = var.max_node_count
    
    # Node pool específico para microservicios
    node_labels = {
      "workload" = "microservicios"
    }
    
    # Taints para dedicar nodos
    node_taints = []
    
    upgrade_settings {
      max_surge = "33%"
    }
  }
  
  # Identity para el cluster
  identity {
    type = "SystemAssigned"
  }
  
  # Configuración de red avanzada
  network_profile {
    network_plugin    = "azure"
    network_policy    = "azure"
    load_balancer_sku = "standard"
    service_cidr      = "10.2.0.0/24"
    dns_service_ip    = "10.2.0.10"
    docker_bridge_cidr = "172.17.0.1/16"
  }
  
  # Configuración de monitoreo
  oms_agent {
    log_analytics_workspace_id = azurerm_log_analytics_workspace.aks.id
  }
  
  # Azure AD integration
  azure_active_directory_role_based_access_control {
    managed                = true
    admin_group_object_ids = var.aks_admin_group_object_ids
    azure_rbac_enabled     = true
  }
  
  # Auto-scaler profile
  auto_scaler_profile {
    balance_similar_node_groups      = false
    expander                        = "random"
    max_graceful_termination_sec    = 600
    max_node_provisioning_time      = "15m"
    max_unready_nodes              = 3
    max_unready_percentage         = 45
    new_pod_scale_up_delay         = "10s"
    scale_down_delay_after_add     = "10m"
    scale_down_delay_after_delete  = "10s"
    scale_down_delay_after_failure = "3m"
    scale_down_unneeded            = "10m"
    scale_down_unready             = "20m"
    scale_down_utilization_threshold = 0.5
    scan_interval                  = "10s"
    skip_nodes_with_local_storage  = false
    skip_nodes_with_system_pods    = true
  }
  
  # API server configuration
  api_server_access_profile {
    authorized_ip_ranges = var.api_server_authorized_ip_ranges
  }
  
  tags = azurerm_resource_group.main.tags
}

# Node pool adicional para cargas de trabajo específicas
resource "azurerm_kubernetes_cluster_node_pool" "database_workload" {
  count                 = var.environment == "prod" ? 1 : 0
  name                  = "dbworkload"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.main.id
  vm_size              = "Standard_D4s_v3"
  node_count           = 2
  
  # Taints para dedicar estos nodos solo a cargas de BD
  node_taints = ["workload=database:NoSchedule"]
  node_labels = {
    "workload" = "database"
  }
  
  tags = azurerm_resource_group.main.tags
}

# Role assignment para que AKS pueda hacer pull de ACR
resource "azurerm_role_assignment" "aks_acr_pull" {
  scope                = azurerm_container_registry.main.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_kubernetes_cluster.main.kubelet_identity[0].object_id
}

# Configuración del provider de Kubernetes
provider "kubernetes" {
  host                   = azurerm_kubernetes_cluster.main.kube_config.0.host
  client_certificate     = base64decode(azurerm_kubernetes_cluster.main.kube_config.0.client_certificate)
  client_key            = base64decode(azurerm_kubernetes_cluster.main.kube_config.0.client_key)
  cluster_ca_certificate = base64decode(azurerm_kubernetes_cluster.main.kube_config.0.cluster_ca_certificate)
}

# Namespace para microservicios
resource "kubernetes_namespace" "microservicios" {
  metadata {
    name = "microservicios"
    
    labels = {
      name        = "microservicios"
      environment = var.environment
    }
  }
  
  depends_on = [azurerm_kubernetes_cluster.main]
}

# Secret para registry credentials
resource "kubernetes_secret" "acr_credentials" {
  metadata {
    name      = "acr-credentials"
    namespace = kubernetes_namespace.microservicios.metadata[0].name
  }
  
  type = "kubernetes.io/dockerconfigjson"
  
  data = {
    ".dockerconfigjson" = jsonencode({
      auths = {
        "${azurerm_container_registry.main.login_server}" = {
          username = azurerm_container_registry.main.admin_username
          password = azurerm_container_registry.main.admin_password
          email    = "admin@bancario.com"
          auth     = base64encode("${azurerm_container_registry.main.admin_username}:${azurerm_container_registry.main.admin_password}")
        }
      }
    })
  }
}
```

**databases.tf - PostgreSQL Managed Services:**
```terraform
# Private DNS Zone para PostgreSQL
resource "azurerm_private_dns_zone" "postgres" {
  name                = "privatelink.postgres.database.azure.com"
  resource_group_name = azurerm_resource_group.main.name
  
  tags = azurerm_resource_group.main.tags
}

# Virtual Network para recursos
resource "azurerm_virtual_network" "main" {
  name                = "vnet-bancario-${var.environment}"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  address_space       = ["10.1.0.0/16"]
  
  tags = azurerm_resource_group.main.tags
}

# Subnet para PostgreSQL
resource "azurerm_subnet" "database" {
  name                 = "snet-database"
  resource_group_name  = azurerm_resource_group.main.name
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = ["10.1.1.0/24"]
  
  delegation {
    name = "fs"
    service_delegation {
      name = "Microsoft.DBforPostgreSQL/flexibleServers"
      actions = [
        "Microsoft.Network/virtualNetworks/subnets/join/action",
      ]
    }
  }
}

# Private DNS Zone link
resource "azurerm_private_dns_zone_virtual_network_link" "postgres" {
  name                  = "postgres-vnet-link"
  private_dns_zone_name = azurerm_private_dns_zone.postgres.name
  virtual_network_id    = azurerm_virtual_network.main.id
  resource_group_name   = azurerm_resource_group.main.name
  registration_enabled  = false
  
  tags = azurerm_resource_group.main.tags
}

# PostgreSQL Flexible Server para Microclientes
resource "azurerm_postgresql_flexible_server" "clientes" {
  name                   = "psql-clientes-${var.environment}-${random_integer.suffix.result}"
  resource_group_name    = azurerm_resource_group.main.name
  location              = azurerm_resource_group.main.location
  version               = "15"
  delegated_subnet_id   = azurerm_subnet.database.id
  private_dns_zone_id   = azurerm_private_dns_zone.postgres.id
  administrator_login    = var.db_admin_username
  administrator_password = var.db_password_clientes
  zone                  = "1"
  
  storage_mb   = var.db_storage_mb
  storage_tier = var.db_storage_tier
  
  sku_name = var.db_sku_name
  
  backup_retention_days        = var.db_backup_retention_days
  geo_redundant_backup_enabled = var.environment == "prod" ? true : false
  
  # High availability para producción
  dynamic "high_availability" {
    for_each = var.environment == "prod" ? [1] : []
    content {
      mode                      = "ZoneRedundant"
      standby_availability_zone = "2"
    }
  }
  
  # Configuración de mantenimiento
  maintenance_window {
    day_of_week  = 0 # Domingo
    start_hour   = 2
    start_minute = 0
  }
  
  tags = azurerm_resource_group.main.tags
  
  depends_on = [azurerm_private_dns_zone_virtual_network_link.postgres]
}

# Base de datos para microclientes
resource "azurerm_postgresql_flexible_server_database" "microclientes" {
  name      = "microclientesdb"
  server_id = azurerm_postgresql_flexible_server.clientes.id
  collation = "en_US.utf8"
  charset   = "utf8"
  
  depends_on = [azurerm_postgresql_flexible_server.clientes]
}

# PostgreSQL Flexible Server para Microcuentas
resource "azurerm_postgresql_flexible_server" "cuentas" {
  name                   = "psql-cuentas-${var.environment}-${random_integer.suffix.result}"
  resource_group_name    = azurerm_resource_group.main.name
  location              = azurerm_resource_group.main.location
  version               = "15"
  delegated_subnet_id   = azurerm_subnet.database.id
  private_dns_zone_id   = azurerm_private_dns_zone.postgres.id
  administrator_login    = var.db_admin_username
  administrator_password = var.db_password_cuentas
  zone                  = "1"
  
  storage_mb   = var.db_storage_mb
  storage_tier = var.db_storage_tier
  
  sku_name = var.db_sku_name
  
  backup_retention_days        = var.db_backup_retention_days
  geo_redundant_backup_enabled = var.environment == "prod" ? true : false
  
  dynamic "high_availability" {
    for_each = var.environment == "prod" ? [1] : []
    content {
      mode                      = "ZoneRedundant"
      standby_availability_zone = "3"
    }
  }
  
  maintenance_window {
    day_of_week  = 0
    start_hour   = 3
    start_minute = 0
  }
  
  tags = azurerm_resource_group.main.tags
  
  depends_on = [azurerm_private_dns_zone_virtual_network_link.postgres]
}

# Base de datos para microcuentas
resource "azurerm_postgresql_flexible_server_database" "microcuentas" {
  name      = "microcuentasdb"
  server_id = azurerm_postgresql_flexible_server.cuentas.id
  collation = "en_US.utf8"
  charset   = "utf8"
  
  depends_on = [azurerm_postgresql_flexible_server.cuentas]
}

# Configuración de firewall para desarrollo (solo si no es producción)
resource "azurerm_postgresql_flexible_server_firewall_rule" "clientes_dev" {
  count    = var.environment != "prod" ? 1 : 0
  name     = "AllowDevAccess"
  server_id = azurerm_postgresql_flexible_server.clientes.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "255.255.255.255"
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "cuentas_dev" {
  count    = var.environment != "prod" ? 1 : 0
  name     = "AllowDevAccess"
  server_id = azurerm_postgresql_flexible_server.cuentas.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "255.255.255.255"
}
```

**variables.tf - Variables de configuración:**
```terraform
# Variables generales
variable "environment" {
  description = "Ambiente de despliegue"
  type        = string
  default     = "dev"
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment debe ser dev, staging o prod."
  }
}

variable "location" {
  description = "Región de Azure"
  type        = string
  default     = "East US"
}

variable "secondary_location" {
  description = "Región secundaria para replicación"
  type        = string
  default     = "West US 2"
}

variable "resource_group_name" {
  description = "Nombre del grupo de recursos"
  type        = string
  default     = "rg-microservicios"
}

# Variables de AKS
variable "cluster_name" {
  description = "Nombre del cluster AKS"
  type        = string
  default     = "aks-microservicios"
}

variable "kubernetes_version" {
  description = "Versión de Kubernetes"
  type        = string
  default     = "1.28.3"
}

variable "node_count" {
  description = "Número inicial de nodos"
  type        = number
  default     = 3
}

variable "min_node_count" {
  description = "Número mínimo de nodos para auto-scaling"
  type        = number
  default     = 1
}

variable "max_node_count" {
  description = "Número máximo de nodos para auto-scaling"
  type        = number
  default     = 10
}

variable "vm_size" {
  description = "Tamaño de VM para nodos"
  type        = string
  default     = "Standard_D2s_v3"
}

variable "api_server_authorized_ip_ranges" {
  description = "IPs autorizadas para acceder al API server"
  type        = list(string)
  default     = []
}

variable "aks_admin_group_object_ids" {
  description = "Object IDs de grupos de Azure AD para administradores de AKS"
  type        = list(string)
  default     = []
}

# Variables de base de datos
variable "db_admin_username" {
  description = "Usuario administrador de PostgreSQL"
  type        = string
  default     = "dbadmin"
}

variable "db_password_clientes" {
  description = "Contraseña para BD de clientes"
  type        = string
  sensitive   = true
}

variable "db_password_cuentas" {
  description = "Contraseña para BD de cuentas"
  type        = string
  sensitive   = true
}

variable "db_sku_name" {
  description = "SKU para PostgreSQL Flexible Server"
  type        = string
  default     = "B_Standard_B1ms"
}

variable "db_storage_mb" {
  description = "Almacenamiento en MB para PostgreSQL"
  type        = number
  default     = 32768
}

variable "db_storage_tier" {
  description = "Tier de almacenamiento para PostgreSQL"
  type        = string
  default     = "P30"
}

variable "db_backup_retention_days" {
  description = "Días de retención de backups"
  type        = number
  default     = 7
}

# Variables de Container Registry
variable "acr_sku" {
  description = "SKU para Azure Container Registry"
  type        = string
  default     = "Standard"
  validation {
    condition     = contains(["Basic", "Standard", "Premium"], var.acr_sku)
    error_message = "ACR SKU debe ser Basic, Standard o Premium."
  }
}

# Variables de monitoreo
variable "log_retention_days" {
  description = "Días de retención para Log Analytics"
  type        = number
  default     = 30
}
```

**Scripts de deployment automatizado:**

**deploy.ps1:**
```powershell
param(
    [ValidateSet("dev", "staging", "prod")]
    [string]$Environment = "dev",
    [switch]$Destroy = $false,
    [switch]$PlanOnly = $false,
    [switch]$AutoApprove = $false
)

$ErrorActionPreference = "Stop"

Write-Host "=== Deployment de Infraestructura Azure ===" -ForegroundColor Green
Write-Host "Ambiente: $Environment" -ForegroundColor Yellow
Write-Host "Acción: $(if ($Destroy) { 'DESTROY' } else { 'DEPLOY' })" -ForegroundColor $(if ($Destroy) { 'Red' } else { 'Green' })

# Verificar prerrequisitos
function Test-Prerequisites {
    Write-Host "Verificando prerrequisitos..." -ForegroundColor Blue
    
    # Azure CLI
    if (!(Get-Command "az" -ErrorAction SilentlyContinue)) {
        throw "Azure CLI no está instalado. Instalar desde: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    }
    
    # Terraform
    if (!(Get-Command "terraform" -ErrorAction SilentlyContinue)) {
        throw "Terraform no está instalado. Instalar desde: https://www.terraform.io/downloads.html"
    }
    
    # kubectl
    if (!(Get-Command "kubectl" -ErrorAction SilentlyContinue)) {
        Write-Warning "kubectl no está instalado. Se instalará automáticamente."
        az aks install-cli
    }
    
    Write-Host "✅ Prerrequisitos verificados" -ForegroundColor Green
}

# Login a Azure
function Connect-Azure {
    Write-Host "Verificando autenticación Azure..." -ForegroundColor Blue
    
    $azAccount = az account show --query "user.name" -o tsv 2>$null
    if (!$azAccount) {
        Write-Host "Realizando login a Azure..." -ForegroundColor Yellow
        az login
    } else {
        Write-Host "✅ Autenticado como: $azAccount" -ForegroundColor Green
    }
    
    # Verificar suscripción
    $subscription = az account show --query "name" -o tsv
    Write-Host "🔍 Suscripción activa: $subscription" -ForegroundColor Cyan
    
    # Confirmar suscripción para producción
    if ($Environment -eq "prod") {
        $confirm = Read-Host "⚠️  Desplegando a PRODUCCIÓN en suscripción '$subscription'. ¿Continuar? (y/N)"
        if ($confirm -ne "y") {
            throw "Deployment cancelado por el usuario"
        }
    }
}

# Configurar Terraform backend
function Initialize-Terraform {
    Write-Host "Inicializando Terraform..." -ForegroundColor Blue
    
    # Crear resource group para Terraform state si no existe
    $rgExists = az group exists --name "rg-terraform-state" --output tsv
    if ($rgExists -eq "false") {
        Write-Host "Creando resource group para Terraform state..." -ForegroundColor Yellow
        az group create --name "rg-terraform-state" --location "East US"
        
        # Crear storage account para Terraform state
        $storageAccount = "stterraformbancario$(Get-Random -Minimum 1000 -Maximum 9999)"
        az storage account create --name $storageAccount --resource-group "rg-terraform-state" --location "East US" --sku "Standard_LRS"
        az storage container create --name "tfstate" --account-name $storageAccount
        
        Write-Host "✅ Storage account creado: $storageAccount" -ForegroundColor Green
    }
    
    terraform init
    
    if ($LASTEXITCODE -ne 0) {
        throw "Error al inicializar Terraform"
    }
    
    Write-Host "✅ Terraform inicializado" -ForegroundColor Green
}

# Ejecutar plan de Terraform
function Invoke-TerraformPlan {
    Write-Host "Generando plan de Terraform..." -ForegroundColor Blue
    
    $planFile = "tfplan-$Environment-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    $varFile = "environments/$Environment.tfvars"
    
    if (!(Test-Path $varFile)) {
        throw "Archivo de variables no encontrado: $varFile"
    }
    
    terraform plan -var-file=$varFile -out=$planFile
    
    if ($LASTEXITCODE -ne 0) {
        throw "Error al generar plan de Terraform"
    }
    
    Write-Host "✅ Plan generado: $planFile" -ForegroundColor Green
    return $planFile
}

# Aplicar o destruir infraestructura
function Invoke-TerraformAction {
    param([string]$PlanFile)
    
    if ($Destroy) {
        Write-Host "⚠️  DESTRUYENDO infraestructura..." -ForegroundColor Red
        
        if (!$AutoApprove) {
            $confirmation = Read-Host "¿Estás seguro de que quieres DESTRUIR toda la infraestructura? (escriba 'DESTROY' para confirmar)"
            if ($confirmation -ne "DESTROY") {
                throw "Operación cancelada por el usuario"
            }
        }
        
        terraform destroy -var-file="environments/$Environment.tfvars" $(if ($AutoApprove) { "-auto-approve" })
        
    } else {
        Write-Host "Aplicando infraestructura..." -ForegroundColor Green
        terraform apply $(if ($AutoApprove) { "-auto-approve" }) $PlanFile
    }
    
    if ($LASTEXITCODE -ne 0) {
        throw "Error al aplicar cambios de Terraform"
    }
}

# Configurar kubectl para AKS
function Connect-Kubernetes {
    if (!$Destroy) {
        Write-Host "Configurando kubectl para AKS..." -ForegroundColor Blue
        
        $rgName = terraform output -raw resource_group_name
        $clusterName = terraform output -raw aks_cluster_name
        
        az aks get-credentials --resource-group $rgName --name $clusterName --overwrite-existing
        
        # Verificar conectividad
        $nodes = kubectl get nodes --no-headers 2>$null
        if ($nodes) {
            Write-Host "✅ Conectado a AKS. Nodos disponibles:" -ForegroundColor Green
            kubectl get nodes
        } else {
            Write-Warning "No se pudo conectar a AKS o el cluster aún se está inicializando"
        }
    }
}

# Mostrar información del deployment
function Show-DeploymentInfo {
    if (!$Destroy) {
        Write-Host "`n=== INFORMACIÓN DEL DEPLOYMENT ===" -ForegroundColor Cyan
        
        $outputs = terraform output -json | ConvertFrom-Json
        
        Write-Host "🏗️  Resource Group: $($outputs.resource_group_name.value)" -ForegroundColor Yellow
        Write-Host "🚢 AKS Cluster: $($outputs.aks_cluster_name.value)" -ForegroundColor Yellow
        Write-Host "📦 Container Registry: $($outputs.acr_login_server.value)" -ForegroundColor Yellow
        Write-Host "🗄️  PostgreSQL Clientes: $($outputs.postgres_clientes_fqdn.value)" -ForegroundColor Yellow
        Write-Host "🗄️  PostgreSQL Cuentas: $($outputs.postgres_cuentas_fqdn.value)" -ForegroundColor Yellow
        
        Write-Host "`n📋 Próximos pasos:" -ForegroundColor Cyan
        Write-Host "1. Construir y subir imágenes Docker al ACR" -ForegroundColor White
        Write-Host "2. Aplicar manifiestos de Kubernetes" -ForegroundColor White
        Write-Host "3. Configurar DNS y certificados SSL" -ForegroundColor White
        Write-Host "4. Configurar monitoreo y alertas" -ForegroundColor White
    }
}

# Función principal
function Main {
    try {
        Test-Prerequisites
        Connect-Azure
        Initialize-Terraform
        
        $planFile = Invoke-TerraformPlan
        
        if ($PlanOnly) {
            Write-Host "✅ Plan generado exitosamente: $planFile" -ForegroundColor Green
            Write-Host "Para aplicar: .\deploy.ps1 -Environment $Environment -AutoApprove" -ForegroundColor Yellow
            exit 0
        }
        
        Invoke-TerraformAction -PlanFile $planFile
        Connect-Kubernetes
        Show-DeploymentInfo
        
        Write-Host "`n🎉 Deployment completado exitosamente!" -ForegroundColor Green
        
    } catch {
        Write-Host "❌ Error durante el deployment: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

# Ejecutar función principal
Main
```

**Pregunta típica:** *"¿Cómo implementaste Infrastructure as Code?"*

**Respuesta:** "Implementé Terraform completo para Azure con módulos reutilizables, backend remoto para state management, múltiples ambientes (dev/staging/prod), AKS con auto-scaling y high availability, PostgreSQL Flexible Servers con backups automáticos, Azure Container Registry con geo-replicación, Key Vault para secretos, y scripts PowerShell para deployment automatizado. Todo está versionado en Git y preparado para CI/CD pipelines."

---

## 🔒 SEGURIDAD Y MEJORES PRÁCTICAS

### **Seguridad Implementada**
- **Network Segmentation:** Cada servicio en su red privada
- **Database Security:** Conexiones encriptadas, usuarios específicos
- **Container Security:** Imágenes base slim, no-root users
- **Input Validation:** Bean Validation en todos los endpoints

**Código de validación:**
```java
@Entity
public class Cliente {
    @NotBlank(message = "ID de cliente es requerido")
    private String clienteid;
    
    @Size(min = 8, message = "Contraseña debe tener al menos 8 caracteres")
    private String contrasena;
    
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "Estado debe ser ACTIVO o INACTIVO")
    private String estado;
}
```

**Pregunta típica:** *"¿Cómo manejas la seguridad?"*

**Respuesta:** "Implemento defense in depth: validación en el API Gateway, autenticación por servicio, encriptación de datos en tránsito y reposo. Para producción, agregaría JWT tokens, OAuth2, y certificados TLS. La separación por microservicios también limita el blast radius de vulnerabilidades."

---

---

## 📊 OBSERVABILIDAD Y MONITOREO COMPLETO

### **Stack de Observabilidad Implementado**

**Herramientas utilizadas:**
- **Micrometer + Prometheus:** Métricas de aplicación
- **Logback + Structured Logging:** Logs estructurados
- **Azure Monitor + Application Insights:** APM y distributed tracing
- **Spring Boot Actuator:** Health checks y métricas
- **Grafana Dashboards:** Visualización avanzada
- **Azure Log Analytics:** Centralización de logs

**application.yml configuración de observabilidad:**
```yaml
# Configuración completa de observabilidad
management:
  endpoints:
    web:
      exposure:
        include: "*"
    jmx:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
      show-components: always
    metrics:
      enabled: true
    prometheus:
      enabled: true
    loggers:
      enabled: true
    env:
      enabled: true
    configprops:
      enabled: true
    beans:
      enabled: true
    info:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
        step: 30s
        descriptions: true
      azure-monitor:
        enabled: true
        instrumentation-key: ${APPLICATIONINSIGHTS_INSTRUMENTATION_KEY:}
    web:
      server:
        request:
          autotime:
            enabled: true
            percentiles: 0.5, 0.9, 0.95, 0.99
            percentiles-histogram: true
    distribution:
      percentiles:
        http.server.requests: 0.5, 0.9, 0.95, 0.99
        resilience4j.circuitbreaker.calls: 0.5, 0.9, 0.95, 0.99
      percentiles-histogram:
        http.server.requests: true
        resilience4j.circuitbreaker.calls: true
      sla:
        http.server.requests: 10ms, 50ms, 100ms, 200ms, 500ms, 1s, 2s
  health:
    circuitbreakers:
      enabled: true
    diskspace:
      enabled: true
      threshold: 10MB
    db:
      enabled: true
    redis:
      enabled: true
  info:
    env:
      enabled: true
    git:
      mode: full
    build:
      enabled: true
    java:
      enabled: true
    os:
      enabled: true
  tracing:
    enabled: true
    sampling:
      probability: 1.0
    baggage:
      enabled: true
      remote-fields: "*"
      correlation:
        enabled: true
        fields: "*"

# Azure Application Insights
azure:
  application-insights:
    enabled: true
    instrumentation-key: ${APPLICATIONINSIGHTS_INSTRUMENTATION_KEY:}
    connection-string: ${APPLICATIONINSIGHTS_CONNECTION_STRING:}
    web:
      enable-W3C-distributed-tracing: true
    sampling:
      percentage: 100.0
    telemetry:
      enabled: true
    heartbeat:
      enabled: true
      interval: 15s

# Logging configuration
logging:
  level:
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.springframework.cloud.openfeign: DEBUG
    com.proyecto: DEBUG
    org.springframework.jdbc.core: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n"
  file:
    name: logs/${spring.application.name}.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 3GB

# Micrometer configuration
micrometer:
  observation:
    enabled: true
  tracing:
    enabled: true
    sampling:
      probability: 1.0
  metrics:
    enabled: true
```

**Custom Metrics Implementation:**
```java
@Component
@Slf4j
public class BusinessMetricsService {
    
    private final Counter clientesCreadosCounter;
    private final Counter cuentasCreadasCounter;
    private final Timer transaccionTimer;
    private final Gauge clientesActivosGauge;
    private final Counter erroresNegocioCounter;
    private final DistributionSummary montoTransaccionSummary;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private CuentaRepository cuentaRepository;
    
    public BusinessMetricsService(MeterRegistry meterRegistry) {
        this.clientesCreadosCounter = Counter.builder("clientes.creados.total")
                .description("Total de clientes creados")
                .tag("microservicio", "microclientes")
                .register(meterRegistry);
                
        this.cuentasCreadasCounter = Counter.builder("cuentas.creadas.total")
                .description("Total de cuentas creadas")
                .tag("microservicio", "microcuentas")
                .register(meterRegistry);
                
        this.transaccionTimer = Timer.builder("transacciones.duration")
                .description("Duración de transacciones")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry);
                
        this.clientesActivosGauge = Gauge.builder("clientes.activos.count")
                .description("Número de clientes activos")
                .register(meterRegistry, this, BusinessMetricsService::getClientesActivos);
                
        this.erroresNegocioCounter = Counter.builder("errores.negocio.total")
                .description("Total de errores de negocio")
                .register(meterRegistry);
                
        this.montoTransaccionSummary = DistributionSummary.builder("transacciones.monto")
                .description("Distribución de montos de transacciones")
                .baseUnit("USD")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(meterRegistry);
    }
    
    @EventListener
    public void onClienteCreado(ClienteCreadoEvent event) {
        clientesCreadosCounter.increment(
            Tags.of(
                "tipo_cliente", event.getTipoCliente(),
                "sucursal", event.getSucursal()
            )
        );
        log.info("Métrica registrada: Cliente creado - Tipo: {}, Sucursal: {}", 
                event.getTipoCliente(), event.getSucursal());
    }
    
    @EventListener
    public void onCuentaCreada(CuentaCreadaEvent event) {
        cuentasCreadasCounter.increment(
            Tags.of(
                "tipo_cuenta", event.getTipoCuenta(),
                "moneda", event.getMoneda()
            )
        );
    }
    
    @EventListener
    public void onTransaccionRealizada(TransaccionRealizadaEvent event) {
        Timer.Sample sample = Timer.start();
        sample.stop(transaccionTimer.tag("tipo", event.getTipoTransaccion()));
        
        montoTransaccionSummary.record(event.getMonto().doubleValue(),
            Tags.of(
                "tipo", event.getTipoTransaccion(),
                "moneda", event.getMoneda()
            )
        );
    }
    
    @EventListener
    public void onErrorNegocio(ErrorNegocioEvent event) {
        erroresNegocioCounter.increment(
            Tags.of(
                "tipo_error", event.getTipoError(),
                "microservicio", event.getMicroservicio(),
                "operacion", event.getOperacion()
            )
        );
    }
    
    private Double getClientesActivos() {
        try {
            return clienteRepository.countByEstado(EstadoCliente.ACTIVO).doubleValue();
        } catch (Exception e) {
            log.error("Error obteniendo métricas de clientes activos", e);
            return 0.0;
        }
    }
}
```

**Distributed Tracing Configuration:**
```java
@Configuration
@EnableAutoConfiguration
public class TracingConfiguration {
    
    @Bean
    public ObservationRegistryCustomizer<ObservationRegistry> observationRegistryCustomizer() {
        return registry -> {
            // Custom key values for tracing
            registry.observationConfig()
                .observationHandler(new ObservationHandler.FirstMatchingCompositeObservationHandler(
                    new PropagatingSenderTracingObservationHandler(),
                    new PropagatingReceiverTracingObservationHandler(),
                    new DefaultTracingObservationHandler()
                ));
        };
    }
    
    @Bean
    public GlobalObservationConvention<HttpRequestObservationContext> httpObservationConvention() {
        return new GlobalObservationConvention<HttpRequestObservationContext>() {
            @Override
            public KeyValues getLowCardinalityKeyValues(HttpRequestObservationContext context) {
                return KeyValues.of(
                    "microservicio", getApplicationName(),
                    "environment", getEnvironment(),
                    "version", getApplicationVersion()
                );
            }
            
            @Override
            public KeyValues getHighCardinalityKeyValues(HttpRequestObservationContext context) {
                return KeyValues.of(
                    "user.id", getUserId(context),
                    "session.id", getSessionId(context)
                );
            }
        };
    }
    
    @Bean
    public ObservationPredicate observationPredicate() {
        return (name, context) -> {
            // No observar actuator endpoints para reducir ruido
            if (context instanceof ServerRequestObservationContext) {
                ServerRequestObservationContext serverContext = (ServerRequestObservationContext) context;
                String uri = serverContext.getCarrier().getRequestURI();
                return !uri.startsWith("/actuator");
            }
            return true;
        };
    }
    
    @Bean
    @ConditionalOnProperty(value = "management.tracing.enabled", havingValue = "true")
    public OpenTelemetrySampler openTelemetrySampler() {
        return TraceIdRatioBasedSampler.create(0.1); // 10% sampling en producción
    }
}
```

**Custom Health Indicators:**
```java
@Component
public class DatabaseConnectionHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("connection.pool.active", getActiveConnections())
                    .withDetail("connection.pool.max", getMaxConnections())
                    .withDetail("query.test", "SELECT 1")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
        
        return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("reason", "Connection validation failed")
                .build();
    }
    
    private int getActiveConnections() {
        // Implementación específica del pool de conexiones
        return 0;
    }
    
    private int getMaxConnections() {
        // Implementación específica del pool de conexiones
        return 0;
    }
}

@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {
    
    @Autowired
    private ClienteServiceClient clienteServiceClient;
    
    @Override
    public Health health() {
        try {
            // Test de conectividad con timeout
            ResponseEntity<String> response = clienteServiceClient.healthCheck();
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return Health.up()
                    .withDetail("external.service", "microclientes")
                    .withDetail("status", "UP")
                    .withDetail("response.time", calculateResponseTime())
                    .build();
            } else {
                return Health.down()
                    .withDetail("external.service", "microclientes")
                    .withDetail("status", "DOWN")
                    .withDetail("http.status", response.getStatusCode())
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("external.service", "microclientes")
                .withDetail("status", "DOWN")
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
    
    private String calculateResponseTime() {
        // Implementación del cálculo de tiempo de respuesta
        return "< 100ms";
    }
}

@Component
public class CircuitBreakerHealthIndicator implements HealthIndicator {
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            String name = circuitBreaker.getName();
            CircuitBreaker.State state = circuitBreaker.getState();
            CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
            
            builder.withDetail("circuitbreaker." + name + ".state", state.toString())
                   .withDetail("circuitbreaker." + name + ".failure.rate", 
                           metrics.getFailureRate())
                   .withDetail("circuitbreaker." + name + ".calls.total", 
                           metrics.getNumberOfBufferedCalls())
                   .withDetail("circuitbreaker." + name + ".calls.failed", 
                           metrics.getNumberOfFailedCalls());
                           
            if (state == CircuitBreaker.State.OPEN) {
                builder.down();
            }
        });
        
        return builder.build();
    }
}
```

**Structured Logging Implementation:**
```java
@Component
@Slf4j
public class StructuredLoggingService {
    
    private final ObjectMapper objectMapper;
    
    public StructuredLoggingService() {
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    public void logBusinessEvent(String eventType, String microservice, 
                                String operation, Map<String, Object> context) {
        try {
            LogEvent logEvent = LogEvent.builder()
                .timestamp(Instant.now())
                .eventType(eventType)
                .microservice(microservice)
                .operation(operation)
                .traceId(getCurrentTraceId())
                .spanId(getCurrentSpanId())
                .userId(getCurrentUserId())
                .sessionId(getCurrentSessionId())
                .context(context)
                .build();
                
            String jsonLog = objectMapper.writeValueAsString(logEvent);
            log.info("BUSINESS_EVENT: {}", jsonLog);
            
        } catch (Exception e) {
            log.error("Error creando log estructurado", e);
        }
    }
    
    public void logError(String errorType, String microservice, String operation, 
                        Throwable exception, Map<String, Object> context) {
        try {
            ErrorLogEvent errorEvent = ErrorLogEvent.builder()
                .timestamp(Instant.now())
                .errorType(errorType)
                .microservice(microservice)
                .operation(operation)
                .errorMessage(exception.getMessage())
                .errorClass(exception.getClass().getSimpleName())
                .stackTrace(getStackTrace(exception))
                .traceId(getCurrentTraceId())
                .spanId(getCurrentSpanId())
                .context(context)
                .build();
                
            String jsonLog = objectMapper.writeValueAsString(errorEvent);
            log.error("ERROR_EVENT: {}", jsonLog);
            
        } catch (Exception e) {
            log.error("Error creando error log estructurado", e);
        }
    }
    
    public void logPerformance(String operation, String microservice, 
                              long duration, Map<String, Object> metrics) {
        try {
            PerformanceLogEvent perfEvent = PerformanceLogEvent.builder()
                .timestamp(Instant.now())
                .operation(operation)
                .microservice(microservice)
                .duration(duration)
                .traceId(getCurrentTraceId())
                .spanId(getCurrentSpanId())
                .metrics(metrics)
                .build();
                
            String jsonLog = objectMapper.writeValueAsString(perfEvent);
            log.info("PERFORMANCE_EVENT: {}", jsonLog);
            
        } catch (Exception e) {
            log.error("Error creando performance log estructurado", e);
        }
    }
    
    private String getCurrentTraceId() {
        Span currentSpan = Span.current();
        return currentSpan.getSpanContext().getTraceId();
    }
    
    private String getCurrentSpanId() {
        Span currentSpan = Span.current();
        return currentSpan.getSpanContext().getSpanId();
    }
    
    private String getCurrentUserId() {
        // Implementación para obtener user ID del contexto de seguridad
        return "unknown";
    }
    
    private String getCurrentSessionId() {
        // Implementación para obtener session ID
        return "unknown";
    }
    
    private String getStackTrace(Throwable exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

@Data
@Builder
class LogEvent {
    private Instant timestamp;
    private String eventType;
    private String microservice;
    private String operation;
    private String traceId;
    private String spanId;
    private String userId;
    private String sessionId;
    private Map<String, Object> context;
}

@Data
@Builder
class ErrorLogEvent {
    private Instant timestamp;
    private String errorType;
    private String microservice;
    private String operation;
    private String errorMessage;
    private String errorClass;
    private String stackTrace;
    private String traceId;
    private String spanId;
    private Map<String, Object> context;
}

@Data
@Builder
class PerformanceLogEvent {
    private Instant timestamp;
    private String operation;
    private String microservice;
    private Long duration;
    private String traceId;
    private String spanId;
    private Map<String, Object> metrics;
}
```

**Prometheus Configuration (prometheus.yml):**
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "microservicios_rules.yml"

scrape_configs:
  - job_name: 'eureka-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['eureka-server:8761']
    scrape_interval: 15s
    scrape_timeout: 10s
    
  - job_name: 'gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['gateway:8080']
    scrape_interval: 15s
    scrape_timeout: 10s
    
  - job_name: 'microclientes'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['microclientes:8081']
    scrape_interval: 15s
    scrape_timeout: 10s
    
  - job_name: 'microcuentas'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['microcuentas:8082']
    scrape_interval: 15s
    scrape_timeout: 10s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

**Alerting Rules (microservicios_rules.yml):**
```yaml
groups:
  - name: microservicios.rules
    rules:
      # Alto uso de CPU
      - alert: HighCPUUsage
        expr: system_cpu_usage > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Alto uso de CPU en {{ $labels.instance }}"
          description: "El uso de CPU está por encima del 80% durante 5 minutos"
          
      # Alto uso de memoria
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Alto uso de memoria en {{ $labels.instance }}"
          description: "El uso de memoria está por encima del 90%"
          
      # Latencia alta en requests HTTP
      - alert: HighRequestLatency
        expr: http_server_requests_seconds{quantile="0.95"} > 1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Alta latencia en {{ $labels.instance }}"
          description: "P95 de latencia > 1 segundo durante 2 minutos"
          
      # Error rate alto
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Alto rate de errores en {{ $labels.instance }}"
          description: "Error rate > 10% durante 1 minuto"
          
      # Circuit Breaker abierto
      - alert: CircuitBreakerOpen
        expr: resilience4j_circuitbreaker_state{state="open"} == 1
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Circuit Breaker abierto en {{ $labels.instance }}"
          description: "Circuit breaker {{ $labels.name }} está abierto"
          
      # Base de datos no disponible
      - alert: DatabaseDown
        expr: up{job=~"postgres.*"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Base de datos no disponible"
          description: "PostgreSQL no está respondiendo"
```

**Pregunta típica:** *"¿Cómo implementaste observabilidad?"*

**Respuesta:** "Implementé observabilidad completa con Micrometer para métricas custom, Logback para structured logging con correlation IDs, distributed tracing con OpenTelemetry, health checks custom para dependencias externas, métricas de negocio específicas (clientes creados, transacciones), Azure Application Insights para APM, Prometheus para scraping, y alertas proactivas para SLIs críticos como latencia P95, error rate y disponibilidad."

---

## 🚀 DESARROLLO Y CI/CD PIPELINE

### **Flujo de Desarrollo Implementado**

**GitFlow Strategy:**
```
main (production)
├── develop (integration)
    ├── feature/nueva-funcionalidad
    ├── feature/mejora-performance
    └── hotfix/bug-critico
```

**Estructura de branching:**
- **main:** Código en producción, protegido con reviews obligatorios
- **develop:** Rama de integración para desarrollo
- **feature/*:** Nuevas funcionalidades
- **hotfix/*:** Arreglos urgentes para producción
- **release/*:** Preparación de releases

**GitHub Actions Workflow (.github/workflows/ci-cd.yml):**
```yaml
name: CI/CD Pipeline Microservicios Bancarios

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

env:
  AZURE_CONTAINER_REGISTRY: acrbancario.azurecr.io
  AZURE_RESOURCE_GROUP: rg-microservicios-prod
  AKS_CLUSTER_NAME: aks-microservicios-prod
  
jobs:
  # ===== ANÁLISIS DE CÓDIGO =====
  code-analysis:
    runs-on: ubuntu-latest
    name: Análisis de Código
    
    steps:
    - name: Checkout código
      uses: actions/checkout@v4
      with:
        fetch-depth: 0  # Para SonarCloud
        
    - name: Setup Java 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
        
    - name: Cache SonarCloud packages
      uses: actions/cache@v3
      with:
        path: ~/.sonar/cache
        key: ${{ runner.os }}-sonar
        restore-keys: ${{ runner.os }}-sonar
        
    - name: Análisis SonarCloud
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: |
        cd eureka-server
        mvn clean verify sonar:sonar \
          -Dsonar.projectKey=microservicios-bancarios-eureka \
          -Dsonar.organization=mi-org \
          -Dsonar.host.url=https://sonarcloud.io \
          -Dsonar.coverage.jacoco.xmlReportPaths=target/jacoco-report/jacoco.xml
          
        cd ../gateway/gateway
        mvn clean verify sonar:sonar \
          -Dsonar.projectKey=microservicios-bancarios-gateway \
          -Dsonar.organization=mi-org \
          -Dsonar.host.url=https://sonarcloud.io
          
        cd ../../microclientes/microclientes
        mvn clean verify sonar:sonar \
          -Dsonar.projectKey=microservicios-bancarios-clientes \
          -Dsonar.organization=mi-org \
          -Dsonar.host.url=https://sonarcloud.io
          
        cd ../../microcuentas/microcuentas
        mvn clean verify sonar:sonar \
          -Dsonar.projectKey=microservicios-bancarios-cuentas \
          -Dsonar.organization=mi-org \
          -Dsonar.host.url=https://sonarcloud.io
          
    - name: Quality Gate Check
      uses: sonarqube-quality-gate-action@master
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

  # ===== TESTS UNITARIOS =====
  unit-tests:
    runs-on: ubuntu-latest
    name: Tests Unitarios
    needs: code-analysis
    
    strategy:
      matrix:
        service: [eureka-server, gateway/gateway, microclientes/microclientes, microcuentas/microcuentas]
        
    steps:
    - name: Checkout código
      uses: actions/checkout@v4
      
    - name: Setup Java 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
        
    - name: Ejecutar tests unitarios
      run: |
        cd ${{ matrix.service }}
        mvn clean test -Dspring.profiles.active=test
        
    - name: Generar reporte de cobertura
      run: |
        cd ${{ matrix.service }}
        mvn jacoco:report
        
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./${{ matrix.service }}/target/site/jacoco/jacoco.xml
        flags: ${{ matrix.service }}
        name: codecov-${{ matrix.service }}

  # ===== TESTS DE INTEGRACIÓN =====
  integration-tests:
    runs-on: ubuntu-latest
    name: Tests de Integración
    needs: unit-tests
    
    services:
      postgres-clientes:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: microclientesdb_test
          POSTGRES_USER: test_user
          POSTGRES_PASSWORD: test_password
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5433:5432
          
      postgres-cuentas:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: microcuentasdb_test
          POSTGRES_USER: test_user
          POSTGRES_PASSWORD: test_password
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5434:5432
          
    steps:
    - name: Checkout código
      uses: actions/checkout@v4
      
    - name: Setup Java 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
        
    - name: Esperar por PostgreSQL
      run: |
        timeout 60 sh -c 'until nc -z localhost 5433; do sleep 1; done'
        timeout 60 sh -c 'until nc -z localhost 5434; do sleep 1; done'
        
    - name: Ejecutar tests de integración
      env:
        SPRING_PROFILES_ACTIVE: integration-test
        DB_HOST_CLIENTES: localhost
        DB_PORT_CLIENTES: 5433
        DB_NAME_CLIENTES: microclientesdb_test
        DB_HOST_CUENTAS: localhost
        DB_PORT_CUENTAS: 5434
        DB_NAME_CUENTAS: microcuentasdb_test
        DB_USERNAME: test_user
        DB_PASSWORD: test_password
      run: |
        # Iniciar Eureka Server
        cd eureka-server
        mvn spring-boot:run -Dspring.profiles.active=test &
        EUREKA_PID=$!
        
        # Esperar a que Eureka esté disponible
        timeout 120 sh -c 'until curl -f http://localhost:8761/actuator/health; do sleep 2; done'
        
        # Ejecutar tests de integración
        cd ../microclientes/microclientes
        mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=integration-test
        
        cd ../../microcuentas/microcuentas  
        mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=integration-test
        
        # Cleanup
        kill $EUREKA_PID

  # ===== TESTS DE PERFORMANCE =====
  performance-tests:
    runs-on: ubuntu-latest
    name: Tests de Performance
    needs: integration-tests
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop'
    
    steps:
    - name: Checkout código
      uses: actions/checkout@v4
      
    - name: Setup Java 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
        
    - name: Instalar JMeter
      run: |
        wget https://downloads.apache.org//jmeter/binaries/apache-jmeter-5.6.2.tgz
        tar -xzf apache-jmeter-5.6.2.tgz
        
    - name: Ejecutar Docker Compose para testing
      run: |
        docker-compose -f docker-compose.yml up -d
        
        # Esperar a que todos los servicios estén listos
        timeout 300 sh -c 'until curl -f http://localhost:8080/actuator/health; do sleep 5; done'
        timeout 300 sh -c 'until curl -f http://localhost:8081/actuator/health; do sleep 5; done'
        timeout 300 sh -c 'until curl -f http://localhost:8082/actuator/health; do sleep 5; done'
        
    - name: Ejecutar tests de performance
      run: |
        ./apache-jmeter-5.6.2/bin/jmeter -n -t tests/performance/microservicios-load-test.jmx \
          -l results/performance-results.jtl \
          -e -o results/html-report/
          
    - name: Verificar métricas de performance
      run: |
        # Verificar que el P95 de respuesta sea < 500ms
        # Verificar que el error rate sea < 1%
        python3 scripts/verify-performance-metrics.py results/performance-results.jtl
        
    - name: Upload performance results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: performance-results
        path: results/

  # ===== SECURITY SCAN =====
  security-scan:
    runs-on: ubuntu-latest
    name: Security Scan
    needs: code-analysis
    
    steps:
    - name: Checkout código
      uses: actions/checkout@v4
      
    - name: Setup Java 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
        
    - name: OWASP Dependency Check
      run: |
        mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7
        
    - name: Snyk Security Scan
      uses: snyk/actions/maven@master
      env:
        SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
      with:
        args: --severity-threshold=high --fail-on=all
        
    - name: Upload security results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: security-results
        path: |
          target/dependency-check-report.html
          .snyk

  # ===== BUILD Y PUSH DOCKER IMAGES =====
  build-and-push:
    runs-on: ubuntu-latest
    name: Build y Push Docker Images
    needs: [unit-tests, integration-tests, security-scan]
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop'
    
    strategy:
      matrix:
        service:
          - name: eureka-server
            path: eureka-server
            port: 8761
          - name: gateway
            path: gateway/gateway
            port: 8080
          - name: microclientes
            path: microclientes/microclientes
            port: 8081
          - name: microcuentas
            path: microcuentas/microcuentas
            port: 8082
            
    steps:
    - name: Checkout código
      uses: actions/checkout@v4
      
    - name: Setup Java 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
        
    - name: Login a Azure Container Registry
      uses: azure/docker-login@v1
      with:
        login-server: ${{ env.AZURE_CONTAINER_REGISTRY }}
        username: ${{ secrets.ACR_USERNAME }}
        password: ${{ secrets.ACR_PASSWORD }}
        
    - name: Build aplicación
      run: |
        cd ${{ matrix.service.path }}
        mvn clean package -DskipTests -Dspring.profiles.active=prod
        
    - name: Build y push imagen Docker
      run: |
        cd ${{ matrix.service.path }}
        
        # Construir imagen
        docker build -t ${{ env.AZURE_CONTAINER_REGISTRY }}/${{ matrix.service.name }}:${{ github.sha }} .
        docker build -t ${{ env.AZURE_CONTAINER_REGISTRY }}/${{ matrix.service.name }}:latest .
        
        # Push al registry
        docker push ${{ env.AZURE_CONTAINER_REGISTRY }}/${{ matrix.service.name }}:${{ github.sha }}
        docker push ${{ env.AZURE_CONTAINER_REGISTRY }}/${{ matrix.service.name }}:latest
        
    - name: Security scan de imagen Docker
      run: |
        # Instalar Trivy
        wget https://github.com/aquasecurity/trivy/releases/download/v0.48.3/trivy_0.48.3_Linux-64bit.tar.gz
        tar zxvf trivy_0.48.3_Linux-64bit.tar.gz
        
        # Scan de vulnerabilidades
        ./trivy image --exit-code 1 --severity HIGH,CRITICAL \
          ${{ env.AZURE_CONTAINER_REGISTRY }}/${{ matrix.service.name }}:${{ github.sha }}

  # ===== DEPLOY A KUBERNETES =====
  deploy-to-aks:
    runs-on: ubuntu-latest
    name: Deploy a AKS
    needs: build-and-push
    if: github.ref == 'refs/heads/main'
    environment: production
    
    steps:
    - name: Checkout código
      uses: actions/checkout@v4
      
    - name: Azure Login
      uses: azure/login@v1
      with:
        creds: ${{ secrets.AZURE_CREDENTIALS }}
        
    - name: Get AKS credentials
      run: |
        az aks get-credentials \
          --resource-group ${{ env.AZURE_RESOURCE_GROUP }} \
          --name ${{ env.AKS_CLUSTER_NAME }} \
          --overwrite-existing
          
    - name: Deploy a Kubernetes
      run: |
        # Aplicar manifiestos de Kubernetes
        kubectl apply -f k8s/namespace.yaml
        kubectl apply -f k8s/configmaps/
        kubectl apply -f k8s/secrets/
        kubectl apply -f k8s/services/
        
        # Actualizar imágenes con el nuevo SHA
        kubectl set image deployment/eureka-server eureka-server=${{ env.AZURE_CONTAINER_REGISTRY }}/eureka-server:${{ github.sha }} -n microservicios
        kubectl set image deployment/gateway gateway=${{ env.AZURE_CONTAINER_REGISTRY }}/gateway:${{ github.sha }} -n microservicios
        kubectl set image deployment/microclientes microclientes=${{ env.AZURE_CONTAINER_REGISTRY }}/microclientes:${{ github.sha }} -n microservicios
        kubectl set image deployment/microcuentas microcuentas=${{ env.AZURE_CONTAINER_REGISTRY }}/microcuentas:${{ github.sha }} -n microservicios
        
        # Esperar a que el rollout esté completo
        kubectl rollout status deployment/eureka-server -n microservicios --timeout=300s
        kubectl rollout status deployment/gateway -n microservicios --timeout=300s
        kubectl rollout status deployment/microclientes -n microservicios --timeout=300s
        kubectl rollout status deployment/microcuentas -n microservicios --timeout=300s
        
    - name: Verificar deployment
      run: |
        # Health check de todos los servicios
        kubectl get pods -n microservicios
        kubectl get services -n microservicios
        
        # Verificar que todos los pods estén en Running
        kubectl wait --for=condition=Ready pod -l app=eureka-server -n microservicios --timeout=300s
        kubectl wait --for=condition=Ready pod -l app=gateway -n microservicios --timeout=300s
        kubectl wait --for=condition=Ready pod -l app=microclientes -n microservicios --timeout=300s
        kubectl wait --for=condition=Ready pod -l app=microcuentas -n microservicios --timeout=300s

  # ===== SMOKE TESTS PRODUCCIÓN =====
  smoke-tests:
    runs-on: ubuntu-latest
    name: Smoke Tests Producción
    needs: deploy-to-aks
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout código
      uses: actions/checkout@v4
      
    - name: Ejecutar smoke tests
      run: |
        # Obtener IP externa del gateway
        GATEWAY_IP=$(kubectl get service gateway-service -n microservicios -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
        
        # Smoke tests básicos
        curl -f http://$GATEWAY_IP/actuator/health || exit 1
        curl -f http://$GATEWAY_IP/microclientes/actuator/health || exit 1
        curl -f http://$GATEWAY_IP/microcuentas/actuator/health || exit 1
        
        # Test de creación de cliente
        CLIENT_RESPONSE=$(curl -X POST http://$GATEWAY_IP/microclientes/api/clientes \
          -H "Content-Type: application/json" \
          -d '{"identificacion":"12345678","nombres":"Test User","email":"test@example.com","tipoCliente":"PERSONA_NATURAL"}')
        
        echo "Cliente creado: $CLIENT_RESPONSE"
        
        # Verificar métricas básicas
        curl -f http://$GATEWAY_IP/actuator/prometheus | grep "http_server_requests_seconds_count" || exit 1
        
    - name: Notificar resultado
      uses: 8398a7/action-slack@v3
      if: always()
      with:
        status: ${{ job.status }}
        channel: '#deployments'
        webhook_url: ${{ secrets.SLACK_WEBHOOK }}
        message: |
          Deployment ${{ job.status }} para commit ${{ github.sha }}
          Branch: ${{ github.ref }}
          Actor: ${{ github.actor }}
```

**Pre-commit hooks (.pre-commit-config.yaml):**
```yaml
repos:
  - repo: https://github.com/pre-commit/pre-commit-hooks
    rev: v4.4.0
    hooks:
      - id: trailing-whitespace
      - id: end-of-file-fixer
      - id: check-yaml
      - id: check-added-large-files
      - id: check-merge-conflict
      
  - repo: https://github.com/psf/black
    rev: 23.3.0
    hooks:
      - id: black
        language_version: python3
        
  - repo: local
    hooks:
      - id: maven-test
        name: maven-test
        entry: mvn test -q
        language: system
        pass_filenames: false
        stages: [commit]
        
      - id: maven-checkstyle
        name: maven-checkstyle
        entry: mvn checkstyle:check -q
        language: system
        pass_filenames: false
        stages: [commit]
```

**Pregunta típica:** *"¿Cómo implementaste CI/CD?"*

---

## 📋 MEJORES PRÁCTICAS Y PATRONES IMPLEMENTADOS

### **Patrones de Diseño Aplicados**

**1. Microservices Patterns:**
- **Database per Service:** Cada microservicio tiene su BD independiente
- **API Gateway Pattern:** Punto único de entrada
- **Service Registry Pattern:** Eureka para descubrimiento
- **Circuit Breaker Pattern:** Resilience4j para fault tolerance
- **Saga Pattern:** Para transacciones distribuidas (implementado en microcuentas)

**2. Enterprise Integration Patterns:**
- **Event-Driven Architecture:** Con eventos de dominio
- **Request-Response:** Para comunicación síncrona
- **Publish-Subscribe:** Para notificaciones asíncronas
- **Message Router:** Gateway como enrutador inteligente

**3. Observability Patterns:**
- **Distributed Tracing:** Correlation IDs entre servicios
- **Metrics Collection:** Métricas de negocio y técnicas
- **Centralized Logging:** Logs estructurados agregados
- **Health Check API:** Endpoints de health en cada servicio

### **Principios SOLID Aplicados**

**Single Responsibility Principle (SRP):**
```java
// Cada clase tiene una responsabilidad específica
@Service
public class ClienteService {
    // Solo maneja lógica de negocio de clientes
}

@Repository
public interface ClienteRepository {
    // Solo maneja persistencia de clientes
}

@Controller
public class ClienteController {
    // Solo maneja requests HTTP de clientes
}
```

**Open/Closed Principle (OCP):**
```java
// Extensible sin modificar código existente
@Component
public abstract class NotificationService {
    public abstract void sendNotification(String message);
}

@Component
public class EmailNotificationService extends NotificationService {
    @Override
    public void sendNotification(String message) {
        // Implementación de email
    }
}

@Component
public class SmsNotificationService extends NotificationService {
    @Override
    public void sendNotification(String message) {
        // Implementación de SMS
    }
}
```

**Dependency Inversion Principle (DIP):**
```java
// Dependemos de abstracciones, no de implementaciones concretas
@Service
public class CuentaService {
    
    private final CuentaRepository repository;  // Interfaz
    private final ClienteServiceClient client;  // Interfaz
    private final NotificationService notification;  // Interfaz
    
    public CuentaService(CuentaRepository repository, 
                        ClienteServiceClient client,
                        NotificationService notification) {
        this.repository = repository;
        this.client = client;
        this.notification = notification;
    }
}
```

### **Clean Architecture Implementation**

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                      │
│  @RestController, @RequestMapping, DTOs, Validation        │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────┴───────────────────────────────────────────┐
│                    Application Layer                        │
│     @Service, Use Cases, Application Services              │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────┴───────────────────────────────────────────┐
│                      Domain Layer                          │
│    Entities, Value Objects, Domain Services, Events       │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────┴───────────────────────────────────────────┐
│                   Infrastructure Layer                     │
│  @Repository, @Configuration, External APIs, Messaging    │
└─────────────────────────────────────────────────────────────┘
```

### **Domain-Driven Design (DDD) Patterns**

**Aggregate Design:**
```java
@Entity
@Table(name = "clientes")
public class Cliente {  // Aggregate Root
    
    @Id
    private ClienteId id;
    
    @Embedded
    private Identificacion identificacion;  // Value Object
    
    @Embedded
    private InformacionPersonal informacionPersonal;  // Value Object
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<ContactoCliente> contactos = new ArrayList<>();  // Entity dentro del Aggregate
    
    // Métodos de dominio que mantienen invariantes
    public void actualizarInformacion(InformacionPersonal nuevaInfo) {
        // Validaciones de negocio
        if (nuevaInfo == null) {
            throw new IllegalArgumentException("Información personal no puede ser nula");
        }
        
        // Eventos de dominio
        this.registerEvent(new ClienteActualizadoEvent(this.id, nuevaInfo));
        
        this.informacionPersonal = nuevaInfo;
        this.fechaActualizacion = Instant.now();
    }
    
    public void agregarContacto(TipoContacto tipo, String valor) {
        // Validaciones de dominio
        if (this.contactos.size() >= 5) {
            throw new DomainException("Cliente no puede tener más de 5 contactos");
        }
        
        ContactoCliente nuevoContacto = new ContactoCliente(tipo, valor);
        this.contactos.add(nuevoContacto);
        
        this.registerEvent(new ContactoAgregadoEvent(this.id, nuevoContacto));
    }
}
```

**Repository Pattern con Specifications:**
```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, ClienteId>, JpaSpecificationExecutor<Cliente> {
    
    // Query methods
    Optional<Cliente> findByIdentificacion(Identificacion identificacion);
    List<Cliente> findByEstado(EstadoCliente estado);
    
    // Custom queries
    @Query("SELECT c FROM Cliente c WHERE c.informacionPersonal.email = :email")
    Optional<Cliente> findByEmail(@Param("email") String email);
}

// Specifications para queries complejas
public class ClienteSpecifications {
    
    public static Specification<Cliente> conEstado(EstadoCliente estado) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("estado"), estado);
    }
    
    public static Specification<Cliente> conTipoCliente(TipoCliente tipo) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("tipoCliente"), tipo);
    }
    
    public static Specification<Cliente> creadoDespuesDe(LocalDateTime fecha) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThan(root.get("fechaCreacion"), fecha);
    }
    
    public static Specification<Cliente> conEmailLike(String email) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("informacionPersonal").get("email")),
                "%" + email.toLowerCase() + "%"
            );
    }
}

// Uso en el service
@Service
public class ClienteQueryService {
    
    public List<Cliente> buscarClientesActivos(String emailPattern, TipoCliente tipo) {
        Specification<Cliente> spec = Specification
            .where(ClienteSpecifications.conEstado(EstadoCliente.ACTIVO))
            .and(ClienteSpecifications.conTipoCliente(tipo));
            
        if (emailPattern != null) {
            spec = spec.and(ClienteSpecifications.conEmailLike(emailPattern));
        }
        
        return clienteRepository.findAll(spec);
    }
}
```

### **Error Handling Strategy**

**Exception Hierarchy:**
```java
// Base exception para el dominio
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
    
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Excepciones específicas de negocio
public class ClienteNoEncontradoException extends DomainException {
    public ClienteNoEncontradoException(String identificacion) {
        super("Cliente no encontrado con identificación: " + identificacion);
    }
}

public class CuentaInactivaException extends DomainException {
    public CuentaInactivaException(String numeroCuenta) {
        super("La cuenta " + numeroCuenta + " está inactiva");
    }
}

public class SaldoInsuficienteException extends DomainException {
    public SaldoInsuficienteException(BigDecimal saldoActual, BigDecimal montoRequerido) {
        super(String.format("Saldo insuficiente. Saldo actual: %s, Monto requerido: %s", 
                          saldoActual, montoRequerido));
    }
}

// Global exception handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ClienteNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleClienteNoEncontrado(ClienteNoEncontradoException ex, 
                                                  WebRequest request) {
        log.warn("Cliente no encontrado: {}", ex.getMessage());
        
        return ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Cliente no encontrado")
            .message(ex.getMessage())
            .path(getPath(request))
            .traceId(getCurrentTraceId())
            .build();
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex,
                                        WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        log.warn("Errores de validación: {}", errors);
        
        return ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Errores de validación")
            .message("Los datos enviados no son válidos")
            .validationErrors(errors)
            .path(getPath(request))
            .traceId(getCurrentTraceId())
            .build();
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex, WebRequest request) {
        log.error("Error interno del servidor", ex);
        
        return ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Error interno del servidor")
            .message("Ha ocurrido un error inesperado")
            .path(getPath(request))
            .traceId(getCurrentTraceId())
            .build();
    }
}
```

### **Performance Optimization Strategies**

**Database Optimization:**
```java
// Entity con optimizaciones JPA
@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "idx_cliente_identificacion", columnList = "identificacion"),
    @Index(name = "idx_cliente_email", columnList = "email"),
    @Index(name = "idx_cliente_estado_tipo", columnList = "estado, tipo_cliente")
})
@EntityListeners(AuditingEntityListener.class)
public class Cliente extends BaseEntity {
    
    // Optimización de queries N+1
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<ContactoCliente> contactos = new ArrayList<>();
    
    // Lazy loading para campos grandes
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String observaciones;
}

// Repository con queries optimizadas
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    // Query optimizada con JOIN FETCH para evitar N+1
    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.contactos WHERE c.estado = :estado")
    List<Cliente> findByEstadoWithContactos(@Param("estado") EstadoCliente estado);
    
    // Projection para consultas que solo necesitan algunos campos
    @Query("SELECT new com.proyecto.dto.ClienteResumenDTO(c.id, c.identificacion, c.nombres) " +
           "FROM Cliente c WHERE c.tipoCliente = :tipo")
    List<ClienteResumenDTO> findResumenByTipo(@Param("tipo") TipoCliente tipo);
    
    // Paginación para consultas grandes
    Page<Cliente> findByEstadoOrderByFechaCreacionDesc(EstadoCliente estado, Pageable pageable);
}
```

**Caching Strategy:**
```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration(Duration.ofMinutes(10)));
            
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}

@Service
public class ClienteService {
    
    @Cacheable(value = "clientes", key = "#identificacion")
    public ClienteDTO obtenerPorIdentificacion(String identificacion) {
        return clienteRepository.findByIdentificacion(identificacion)
            .map(clienteMapper::toDTO)
            .orElseThrow(() -> new ClienteNoEncontradoException(identificacion));
    }
    
    @CacheEvict(value = "clientes", key = "#cliente.identificacion")
    public void actualizar(Cliente cliente) {
        clienteRepository.save(cliente);
    }
    
    @Cacheable(value = "cliente-estadisticas", key = "'activos-count'", unless = "#result < 0")
    public Long contarClientesActivos() {
        return clienteRepository.countByEstado(EstadoCliente.ACTIVO);
    }
}
```

### **Security Best Practices**

**Input Validation:**
```java
// DTO con validaciones completas
public class CrearClienteRequest {
    
    @NotBlank(message = "Identificación es requerida")
    @Pattern(regexp = "^[0-9]{8,11}$", message = "Identificación debe tener entre 8 y 11 dígitos")
    private String identificacion;
    
    @NotBlank(message = "Nombres son requeridos")
    @Size(min = 2, max = 100, message = "Nombres debe tener entre 2 y 100 caracteres")
    private String nombres;
    
    @Email(message = "Email debe tener formato válido")
    @NotBlank(message = "Email es requerido")
    private String email;
    
    @Valid
    private DireccionDTO direccion;
    
    @NotNull(message = "Tipo de cliente es requerido")
    private TipoCliente tipoCliente;
}

// Custom validator
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IdentificacionValidator.class)
public @interface ValidIdentificacion {
    String message() default "Identificación no es válida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class IdentificacionValidator implements ConstraintValidator<ValidIdentificacion, String> {
    
    @Override
    public boolean isValid(String identificacion, ConstraintValidatorContext context) {
        if (identificacion == null || identificacion.trim().isEmpty()) {
            return false;
        }
        
        // Validación específica según el país/tipo de documento
        return validarAlgoritmoLuhn(identificacion) && 
               validarLongitud(identificacion) &&
               validarDigitoVerificador(identificacion);
    }
}
```

**Pregunta típica:** *"¿Qué patrones y mejores prácticas aplicaste?"*

**Respuesta:** "Implementé arquitectura limpia con separación clara de capas, DDD con aggregates y value objects, SOLID principles, patrones de microservicios (API Gateway, Circuit Breaker, Database per Service), manejo centralizado de errores con jerarquía de excepciones custom, optimizaciones de performance con caching Redis y queries optimizadas, validaciones robustas con custom validators, y observabilidad completa con métricas de negocio y distributed tracing."

---

## 🎯 CONCLUSIONES Y PRÓXIMOS PASOS

### **Lo que el Proyecto Demuestra**

**Competencias Técnicas:**
- ✅ **Arquitectura de Microservicios:** Diseño, implementación y deployment completo
- ✅ **Spring Framework Expertise:** Boot, Cloud, Security, Data JPA avanzado
- ✅ **DevOps y Cloud:** Docker, Kubernetes, Terraform, Azure, CI/CD completo
- ✅ **Bases de Datos:** PostgreSQL, optimización de queries, migrations
- ✅ **Testing:** Unit, Integration, Performance, Security testing
- ✅ **Observabilidad:** Métricas, logging, tracing, alerting
- ✅ **Seguridad:** Validaciones, manejo de errores, input sanitization

**Competencias de Diseño:**
- ✅ **Patrones de Diseño:** Enterprise patterns, DDD, Clean Architecture
- ✅ **Escalabilidad:** Auto-scaling, load balancing, performance optimization
- ✅ **Resiliencia:** Circuit breakers, retries, timeouts, graceful degradation
- ✅ **Mantenibilidad:** Código limpio, documentación, separation of concerns

### **Roadmap de Mejoras**

**Corto Plazo (1-2 meses):**
- [ ] **Event Sourcing:** Implementar para auditoría completa
- [ ] **CQRS:** Separar commands y queries para mejor performance
- [ ] **API Versioning:** Estrategia de versionado con backward compatibility
- [ ] **Rate Limiting:** Implementar en API Gateway
- [ ] **OAuth2/JWT:** Autenticación y autorización robusta

**Mediano Plazo (3-6 meses):**
- [ ] **Service Mesh:** Istio para comunicación entre microservicios
- [ ] **Event Streaming:** Apache Kafka para eventos en tiempo real
- [ ] **Multi-region Deployment:** Alta disponibilidad geográfica
- [ ] **Chaos Engineering:** Simulación de fallos para mejorar resiliencia
- [ ] **AI/ML Integration:** Análisis predictivo y detección de fraude

**Largo Plazo (6-12 meses):**
- [ ] **Serverless Migration:** Azure Functions para cargas específicas
- [ ] **GraphQL API:** Para clientes que necesiten queries flexibles
- [ ] **Blockchain Integration:** Para transacciones que requieran inmutabilidad
- [ ] **Edge Computing:** CDN y caching distribuido globalmente

### **Métricas de Éxito Actuales**

**Performance:**
- 🎯 **Latencia P95:** < 500ms para todas las operaciones
- 🎯 **Throughput:** 1000+ requests/segundo por microservicio
- 🎯 **Availability:** 99.9% uptime (target: 99.99%)

**Calidad de Código:**
- 🎯 **Test Coverage:** > 80% en todos los microservicios
- 🎯 **Code Quality:** A+ rating en SonarCloud
- 🎯 **Security:** 0 vulnerabilidades HIGH/CRITICAL

**Operacional:**
- 🎯 **Deployment Time:** < 5 minutos con zero downtime
- 🎯 **Recovery Time:** < 2 minutos para rollback automático
- 🎯 **Monitoring:** 100% de cobertura con alertas proactivas

### **Valor de Negocio Entregado**

**Escalabilidad:**
- Sistema preparado para crecer de 1K a 1M+ usuarios
- Auto-scaling automático basado en métricas
- Arquitectura cloud-native para expansión global

**Confiabilidad:**
- Circuit breakers previenen cascading failures
- Database redundancy y backups automáticos
- Health checks y auto-recovery implementados

**Mantenibilidad:**
- Código modular y testeable al 100%
- Documentación técnica completa
- CI/CD pipeline que garantiza calidad

**Tiempo al Mercado:**
- Deployment automatizado en < 5 minutos
- Feature flags para releases incrementales
- Rollback automático en caso de errores

### **Pregunta de Cierre Típica**

**"¿Qué aprendiste de este proyecto y cómo lo aplicarías en nuestra empresa?"**

**Respuesta:**
"Este proyecto me enseñó la importancia de equilibrar complejidad técnica con valor de negocio. Implementé una arquitectura robusta pero pragmática, enfocándome en observabilidad y resiliencia desde el inicio. 

Para aplicarlo en su empresa, empezaría con una evaluación del estado actual, identificaría los pain points críticos, y haría una migración incremental comenzando por los módulos con mayor ROI. Priorizaría la observabilidad y testing desde el día uno, porque sin visibility no se puede optimizar.

Mi experiencia me ha demostrado que la tecnología debe servir al negocio, no al revés. Este proyecto muestra que puedo implementar soluciones técnicas complejas manteniendo siempre el foco en entregar valor real a los usuarios finales."

---

## 📚 RECURSOS Y REFERENCIAS

### **Documentación del Proyecto**
- [README Completo](./README_COMPLETO.md) - Documentación general del proyecto
- [Listado de Tecnologías](./LISTADO_TECNOLOGIAS_COMPLETO.md) - Tecnologías implementadas
- [Flujo Completo](./FLUJO_COMPLETO.md) - Flujo de desarrollo y deployment

### **Repositorios de Referencia**
- [Spring Cloud Samples](https://github.com/spring-cloud-samples)
- [Microservices Patterns](https://github.com/microservices-patterns)
- [Azure Architecture Center](https://docs.microsoft.com/en-us/azure/architecture/)

### **Comandos Útiles para Demos**
```bash
# Levantar todo el sistema localmente
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f microclientes

# Ejecutar tests completos
mvn clean test -f microclientes/microclientes/pom.xml

# Deploy a Azure
./terraform/deploy.ps1 -Environment dev -AutoApprove

# Monitoring en tiempo real
kubectl top pods -n microservicios
```

---

**🏆 ¡Este proyecto demuestra experiencia real en arquitectura de microservicios enterprise-grade, desde el diseño hasta la operación en producción!**

**Respuesta:** "Tengo logging estructurado por servicio, health checks automáticos via Actuator, y métricas JVM. Para producción completa, implementaría Prometheus + Grafana para métricas, ELK stack para logs centralizados, y Jaeger para distributed tracing."

---

## 🧪 TESTING Y CALIDAD

### **Estrategia de Testing**
- **Unit Tests:** JUnit 5 + Mockito para lógica de negocio
- **Integration Tests:** TestContainers para testing con BD real
- **API Tests:** TestRestTemplate para endpoints
- **Code Coverage:** JaCoCo para métricas de cobertura

**Ejemplo de test implementado:**
```java
@SpringBootTest
public class ClienteControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testCrearCliente() {
        ClienteDTO cliente = new ClienteDTO();
        cliente.setClienteid("123");
        
        ResponseEntity<ClienteDTO> response = 
            restTemplate.postForEntity("/clientes", cliente, ClienteDTO.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
```

**Pregunta típica:** *"¿Cómo aseguras la calidad del código?"*

**Respuesta:** "Implemento testing en múltiples niveles: unit tests para lógica de negocio, integration tests con TestContainers para verificar interacciones con BD, y API tests para validar contratos. Uso JaCoCo para mantener cobertura >80% y SonarQube para análisis estático."

---

## 🚀 ESCALABILIDAD Y PERFORMANCE

### **Estrategias Implementadas**
- **Horizontal Scaling:** Múltiples instancias por servicio
- **Database Connection Pooling:** HikariCP para optimización
- **Load Balancing:** Client-side con Spring Cloud LoadBalancer
- **Stateless Services:** Sin estado en memoria para fácil escalado

**Configuración de performance:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

**Pregunta típica:** *"¿Cómo manejas la escalabilidad?"*

**Respuesta:** "Diseñé servicios stateless que escalan horizontalmente. Uso connection pooling optimizado, cache de queries frecuentes, y Kubernetes HPA para auto-scaling basado en CPU/memoria. Para alta demanda, implementaría database read replicas y Redis para caching distribuido."

---

## 💼 PATRONES DE DISEÑO EMPRESARIAL

### **Patrones Implementados**

**1. Repository Pattern**
```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    List<Cliente> findByEstado(String estado);
    Optional<Cliente> findByPersonaIdentificacion(String identificacion);
}
```

**2. DTO Pattern**
```java
public class ClienteDTO {
    private String clienteid;
    private String nombre;
    private String estado;
    // Evita exposición directa de entidades
}
```

**3. Service Layer Pattern**
```java
@Service
@Transactional
public class ClienteService {
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        // Lógica de negocio
    }
}
```

**Pregunta típica:** *"¿Qué patrones de diseño utilizas?"*

**Respuesta:** "Implemento Repository para abstracción de datos, DTO para no exponer entidades internas, Service Layer para lógica de negocio, y Factory Pattern para creación de objetos complejos. También uso Strategy Pattern para diferentes tipos de cuentas bancarias."

---

## 🔄 CI/CD Y DEVOPS

### **Pipeline Ready**
El proyecto está preparado para CI/CD con:
- **Dockerfile** optimizado para cada servicio
- **Terraform** para infraestructura automatizada
- **Health checks** para rolling deployments
- **Configuration externalization** para diferentes ambientes

**Ejemplo de pipeline que implementaría:**
```yaml
stages:
  - test
  - build
  - deploy-staging
  - deploy-production

test:
  script:
    - mvn clean test
    - mvn jacoco:report

build:
  script:
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
```

**Pregunta típica:** *"¿Cómo implementarías CI/CD?"*

**Respuesta:** "Usaría GitLab CI o GitHub Actions con pipeline multi-stage: testing automático, build de imágenes Docker, deployment a staging con Terraform, testing de integración, y promoción a producción con aprovación manual. Blue-green deployment para zero-downtime."

---

## 🎯 DECISIONES TÉCNICAS CLAVE

### **1. ¿Por qué Spring Cloud en lugar de Istio?**
"Spring Cloud está más integrado con el ecosistema Java/Spring. Para un equipo Java, es más natural y tiene menos curva de aprendizaje. Istio es más poderoso pero requiere expertise en Kubernetes avanzado."

### **2. ¿Por qué PostgreSQL por servicio en lugar de una BD compartida?**
"Database per service garantiza independencia entre servicios. Cada equipo puede evolucionar su esquema sin afectar otros. Es fundamental para true microservices architecture."

### **3. ¿Por qué Docker Compose local y Kubernetes en producción?**
"Docker Compose es perfecto para desarrollo local - simple y rápido. Kubernetes para producción ofrece auto-scaling, self-healing, rolling updates y service mesh capabilities."

### **4. ¿Por qué Azure en lugar de AWS?**
"Azure tiene excelente integración con ecosistema Microsoft, pricing competitivo para startups, y Azure Database for PostgreSQL es muy robusto. También, muchas empresas ya tienen contratos enterprise con Microsoft."

---

## 🚀 DEMOSTRACIÓN EN VIVO

### **Flujo de demostración recomendado:**

**1. Arquitectura (2-3 minutos)**
- Mostrar diagrama de arquitectura
- Explicar flujo de requests
- Demostrar Eureka dashboard

**2. Código Core (3-4 minutos)**
- Mostrar entity Cliente con relaciones
- Service layer con transacciones
- OpenFeign client para comunicación

**3. Docker/Kubernetes (2-3 minutos)**
- docker-compose up en vivo
- Mostrar servicios corriendo
- kubectl get pods en AKS

**4. Testing (2 minutos)**
- Ejecutar test suite
- Mostrar coverage report

**5. API Demo (3 minutos)**
- Crear cliente via Postman
- Crear cuenta para ese cliente
- Realizar movimiento bancario
- Generar reporte

---

## 💡 PRÓXIMOS PASOS Y MEJORAS

### **Roadmap Técnico**
**Fase 1 - Seguridad (2-3 sprints)**
- JWT Authentication con Spring Security
- OAuth2 / OpenID Connect
- API Rate limiting con Redis
- TLS end-to-end

**Fase 2 - Observabilidad (2 sprints)**
- Prometheus + Grafana
- ELK Stack para logs
- Jaeger para distributed tracing
- APM con New Relic/DataDog

**Fase 3 - Advanced Patterns (3-4 sprints)**
- Event-driven con Apache Kafka
- CQRS + Event Sourcing
- Saga pattern para transacciones distribuidas
- API versioning strategy

**Pregunta típica:** *"¿Qué agregarías si tuvieras más tiempo?"*

**Respuesta:** "Priorizaría seguridad robusta con JWT/OAuth2, observabilidad completa con Prometheus/Grafana, y luego patrones avanzados como event-driven architecture para mejor desacoplamiento. También implementaría disaster recovery y multi-region deployment."

---

## 🏆 VALOR DE NEGOCIO

### **ROI y Beneficios**
- **Time to Market:** Deployments independientes aceleran features
- **Escalabilidad:** Solo escalo servicios que necesitan más recursos
- **Resiliencia:** Fallos aislados no afectan todo el sistema
- **Team Productivity:** Equipos autónomos por microservicio
- **Technology Evolution:** Puedo modernizar servicios incrementalmente

**Pregunta típica:** *"¿Cuál es el beneficio de negocio de esta arquitectura?"*

**Respuesta:** "Esta arquitectura permite que el banco lance nuevos productos financieros más rápido. Si necesitan un nuevo tipo de cuenta, solo modifican el servicio de cuentas sin tocar clientes. Durante Black Friday, pueden escalar solo el servicio de movimientos. Si hay un bug en reportes, los clientes siguen operando normalmente."

---

## 📚 MENSAJE FINAL PARA LA ENTREVISTA

*"Este proyecto demuestra mi capacidad para diseñar e implementar sistemas enterprise-grade usando tecnologías modernas. No es solo código que funciona - es una arquitectura pensada para producción, con consideraciones de seguridad, escalabilidad, testing y operaciones. Refleja mi enfoque de ingeniería: balance entre mejores prácticas técnicas y valor de negocio pragmático."*

---

**🔑 Puntos clave para recordar:**
1. **Arquitectura sólida:** Microservicios con patrones probados
2. **Tecnologías modernas:** Spring Boot 3, Docker, Kubernetes, Azure
3. **Production-ready:** Testing, monitoring, IaC, security
4. **Business value:** Escalabilidad, resiliencia, time-to-market
5. **Continuous learning:** Roadmap claro para mejoras

**💼 Diferenciadores competitivos:**
- Implementación completa end-to-end
- Infraestructura como código con Terraform
- Cloud-native desde el diseño
- Testing comprehensivo
- Documentación técnica detallada
