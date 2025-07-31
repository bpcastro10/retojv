# 🏦 Sistema de Microservicios Bancarios - Proyecto Completo

## 📋 Descripción General

Este proyecto implementa un **sistema bancario completo** basado en **arquitectura de microservicios** utilizando **Spring Boot**, **Spring Cloud**, **Docker** y **Azure Kubernetes Service (AKS)**. El sistema gestiona clientes, cuentas bancarias, movimientos financieros y genera reportes en tiempo real.

---

## 🏗️ Arquitectura del Sistema

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENTE / USUARIO                        │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP/HTTPS
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                 API GATEWAY (Puerto 8083)                   │
│              Punto de Entrada Único                         │
│                                                             │
│ • Enrutamiento Inteligente                                 │
│ • Balanceo de Carga                                        │
│ • Filtros de Seguridad                                     │
│ • Logging y Monitoreo                                      │
│ • CORS Configuration                                        │
└─────────────────────┬───────────────────────────────────────┘
                      │ Service Discovery
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                EUREKA SERVER (Puerto 8761)                  │
│                  Service Registry                           │
│                                                             │
│ • Descubrimiento de Servicios                              │
│ • Health Checks                                            │
│ • Dashboard de Monitoreo                                   │
│ • Auto-registro de Servicios                               │
│ • Failover y Redundancia                                   │
└─────────────────────┬───────────────────────────────────────┘
                      │ Service Registration
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│MICROCLIENTES│ │MICROCUENTAS │ │   GATEWAY   │
│(Puerto 0*)  │ │(Puerto 0*)  │ │(Puerto 8083)│
│             │ │             │ │             │
│• Gestión    │ │• Gestión    │ │• Ruteo      │
│  Personas   │ │  Cuentas    │ │• Filters    │
│• Gestión    │ │• Movimientos│ │• Security   │
│  Clientes   │ │• Reportes   │ │• Load Balance│
│• Validación │ │• Feign      │ │             │
│• REST APIs  │ │  Client     │ │             │
└─────┬───────┘ └─────┬───────┘ └─────────────┘
      │               │
      │               │ OpenFeign
      │               │ Communication
      ▼               ▼
┌─────────────┐ ┌─────────────┐
│ POSTGRESQL  │ │ POSTGRESQL  │
│ CLIENTES    │ │  CUENTAS    │
│(Puerto 5432)│ │(Puerto 5433)│
│             │ │             │
│ • Tabla     │ │ • Tabla     │
│   persona   │ │   cuenta    │
│ • Tabla     │ │ • Tabla     │
│   cliente   │ │   movimiento│
└─────────────┘ └─────────────┘

* Puerto 0 = Puerto aleatorio para escalabilidad
```

### Arquitectura Cloud (Azure AKS)

```
┌─────────────────────────────────────────────────────────────┐
│                        AZURE CLOUD                          │
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │              AZURE KUBERNETES SERVICE                   │ │
│ │                                                         │ │
│ │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│ │  │ GATEWAY     │ │EUREKA SERVER│ │LOAD BALANCER│       │ │
│ │  │(2 Replicas) │ │(2 Replicas) │ │   (Azure)   │       │ │
│ │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│ │                                                         │ │
│ │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│ │  │MICROCLIENTES│ │MICROCUENTAS │ │  NAMESPACE  │       │ │
│ │  │(3 Replicas) │ │(3 Replicas) │ │microservicios│      │ │
│ │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             │
│ │ AZURE       │ │ AZURE       │ │ AZURE       │             │
│ │ POSTGRESQL  │ │ POSTGRESQL  │ │ CONTAINER   │             │
│ │ CLIENTES    │ │ CUENTAS     │ │ REGISTRY    │             │
│ │(Managed DB) │ │(Managed DB) │ │             │             │
│ └─────────────┘ └─────────────┘ └─────────────┘             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Tecnologías Implementadas

### **Backend Framework & Core**
- **Java 17** - Versión LTS para máximo rendimiento
- **Spring Boot 3.4.5** - Framework principal para microservicios
- **Spring Cloud 2023.0.6** - Ecosistema de microservicios
- **Spring Data JPA** - Persistencia y ORM
- **Spring Web** - APIs REST
- **Spring Validation** - Validación de datos

### **Microservicios & Service Discovery**
- **Netflix Eureka Server** - Service Registry y Discovery
- **Spring Cloud Gateway** - API Gateway con enrutamiento dinámico
- **OpenFeign** - Cliente HTTP declarativo para comunicación entre servicios
- **Spring Cloud LoadBalancer** - Balanceo de carga client-side

### **Base de Datos**
- **PostgreSQL 15** - Base de datos relacional principal
- **Hibernate/JPA** - ORM para mapeo objeto-relacional
- **Database per Service Pattern** - Aislamiento de datos por microservicio

### **Containerización & Orquestación**
- **Docker & Docker Compose** - Containerización y orquestación local
- **Kubernetes** - Orquestación en producción
- **Azure Kubernetes Service (AKS)** - Managed Kubernetes en Azure

### **Infrastructure as Code (IaC)**
- **Terraform** - Provisioning de infraestructura en Azure
- **Azure Container Registry** - Registro de imágenes Docker
- **Azure Database for PostgreSQL** - Base de datos managed

### **Observabilidad & Logging**
- **Spring Boot Actuator** - Métricas y health checks
- **Logback** - Sistema de logging estructurado
- **Centralized Logging** - Logs centralizados por servicio

### **Testing & Quality**
- **JUnit 5** - Framework de testing
- **Spring Boot Test** - Testing de integración
- **Mockito** - Mocking para unit tests
- **JaCoCo** - Code coverage

---

## 📦 Componentes del Sistema

### 🔍 **1. Eureka Server (Service Registry)**
**Puerto:** 8761  
**Función:** Service Discovery y Registry

**Características Técnicas:**
- **Alta Disponibilidad:** Configuración cluster-ready
- **Auto-registro:** Los servicios se registran automáticamente
- **Health Monitoring:** Monitoreo continuo de servicios
- **Dashboard Web:** Interfaz gráfica en `http://localhost:8761`
- **Sin Base de Datos:** Utiliza memoria interna para el registry

**Implementación:**
```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### 🚪 **2. API Gateway (Punto de Entrada Único)**
**Puerto:** 8083  
**Función:** Enrutamiento y gestión de tráfico

**Características Técnicas:**
- **Enrutamiento Dinámico:** Basado en Service Discovery
- **Load Balancing:** Distribución automática de carga
- **Filtros Personalizados:** Logging, seguridad, transformación
- **CORS Configuration:** Manejo de políticas de origen cruzado
- **Circuit Breaker:** Patrón de tolerancia a fallos
- **Rate Limiting:** Control de tasa de requests

**Configuración de Rutas:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: microclientes-route
          uri: lb://microclientes
          predicates:
            - Path=/clientes/**
        - id: microcuentas-route
          uri: lb://microcuentas
          predicates:
            - Path=/cuentas/**, /movimientos/**, /reportes/**
```

### 👥 **3. Microservicio: Microclientes**
**Puerto:** Aleatorio (0)  
**Función:** Gestión de Personas y Clientes

**Modelo de Datos:**
```java
@Entity
public class Persona {
    @Id
    private String identificacion;
    private String nombre;
    private String genero;
    private Integer edad;
    private String direccion;
    private String telefono;
}

@Entity
public class Cliente {
    @Id
    private String clienteid;
    private String contrasena;
    private String estado;
    
    @ManyToOne
    @JoinColumn(name = "identificacion")
    private Persona persona;
}
```

**APIs Implementadas:**
- `GET /clientes` - Listar todos los clientes
- `GET /clientes/{id}` - Obtener cliente por ID
- `POST /clientes` - Crear nuevo cliente
- `PUT /clientes/{id}` - Actualizar cliente
- `DELETE /clientes/{id}` - Eliminar cliente
- `GET /personas` - Gestión de personas
- `POST /personas` - Crear persona

**Características Técnicas:**
- **Base de Datos:** PostgreSQL independiente (puerto 5432)
- **Validación:** Bean Validation con anotaciones
- **Escalabilidad:** Múltiples instancias con puerto aleatorio
- **Logging:** Logs estructurados por operación

### 💰 **4. Microservicio: Microcuentas**
**Puerto:** Aleatorio (0)  
**Función:** Gestión de Cuentas, Movimientos y Reportes

**Modelo de Datos:**
```java
@Entity
public class Cuenta {
    @Id
    private String numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

@Entity
public class Movimiento {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime fecha;
    private String tipoMovimiento;
    private BigDecimal valor;
    private BigDecimal saldo;
    
    @ManyToOne
    private Cuenta cuenta;
}
```

**APIs Implementadas:**
- `GET /cuentas` - Listar cuentas
- `POST /cuentas` - Crear cuenta
- `PUT /cuentas/{numero}` - Actualizar cuenta
- `DELETE /cuentas/{numero}` - Eliminar cuenta
- `POST /movimientos` - Registrar movimiento
- `GET /movimientos/cuenta/{numero}` - Movimientos por cuenta
- `GET /reportes/cliente/{id}` - Reporte por cliente
- `GET /reportes/fechas` - Reporte por rangos de fecha

**Características Técnicas:**
- **Base de Datos:** PostgreSQL independiente (puerto 5433)
- **Comunicación:** OpenFeign client para consumir microclientes
- **Transacciones:** Manejo ACID para movimientos bancarios
- **Reportes:** Generación dinámica de reportes financieros

**Implementación de Comunicación:**
```java
@FeignClient(name = "microclientes")
public interface ClienteClient {
    @GetMapping("/clientes/{id}")
    ClienteDTO obtenerCliente(@PathVariable("id") Long id);
}
```

---

## 🗄️ Base de Datos - Database per Service Pattern

### **Microclientes Database (Puerto 5432)**
```sql
-- Tabla persona
CREATE TABLE persona (
    identificacion VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero VARCHAR(10),
    edad INTEGER,
    direccion VARCHAR(200),
    telefono VARCHAR(15)
);

-- Tabla cliente
CREATE TABLE cliente (
    clienteid VARCHAR(20) PRIMARY KEY,
    contrasena VARCHAR(100) NOT NULL,
    estado VARCHAR(10) NOT NULL,
    identificacion VARCHAR(20),
    FOREIGN KEY (identificacion) REFERENCES persona(identificacion)
);
```

### **Microcuentas Database (Puerto 5433)**
```sql
-- Tabla cuenta
CREATE TABLE cuenta (
    numero_cuenta VARCHAR(20) PRIMARY KEY,
    tipo_cuenta VARCHAR(20) NOT NULL,
    saldo_inicial DECIMAL(15,2) NOT NULL,
    estado VARCHAR(10) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla movimiento
CREATE TABLE movimiento (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor DECIMAL(15,2) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL,
    numero_cuenta VARCHAR(20),
    FOREIGN KEY (numero_cuenta) REFERENCES cuenta(numero_cuenta)
);
```

---

## 🐳 Containerización con Docker

### **Docker Compose Configuration**
```yaml
version: '3.8'
services:
  # Base de datos para microclientes
  postgres-clientes:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: microclientesdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 123
    ports:
      - "5432:5432"
    volumes:
      - postgres_clientes_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Eureka Server
  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  # API Gateway
  gateway:
    build: ./gateway/gateway
    ports:
      - "8083:8083"
    depends_on:
      - eureka-server
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

### **Dockerfile para Microservicios**
```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

## ☁️ Infraestructura como Código (Terraform)

### **Azure AKS Infrastructure**
```terraform
# Resource Group
resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location
}

# AKS Cluster
resource "azurerm_kubernetes_cluster" "main" {
  name                = var.cluster_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "${var.cluster_name}-dns"

  default_node_pool {
    name       = "default"
    node_count = var.node_count
    vm_size    = var.vm_size
  }

  identity {
    type = "SystemAssigned"
  }
}

# Azure Container Registry
resource "azurerm_container_registry" "main" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Standard"
  admin_enabled       = true
}

# PostgreSQL Servers
resource "azurerm_postgresql_flexible_server" "clientes" {
  name                   = "${var.cluster_name}-clientes-db"
  resource_group_name    = azurerm_resource_group.main.name
  location              = azurerm_resource_group.main.location
  version               = "15"
  administrator_login    = var.db_admin_username
  administrator_password = var.db_admin_password
  
  storage_mb = 32768
  sku_name   = "B_Standard_B1ms"
}
```

---

## 🔧 Configuración y Despliegue

### **1. Despliegue Local con Docker**
```bash
# Clonar el repositorio
git clone <repository-url>
cd retojv-main/proy

# Construir y desplegar todos los servicios
docker-compose up --build -d

# Verificar el estado
docker-compose ps

# Ver logs
docker-compose logs -f
```

### **2. Despliegue en Azure AKS**
```bash
# Navegar al directorio de Terraform
cd terraform

# Inicializar Terraform
terraform init

# Planificar el despliegue
terraform plan

# Aplicar la infraestructura
terraform apply -auto-approve

# Configurar kubectl
az aks get-credentials --resource-group rg-microservicios --name aks-microservicios

# Desplegar aplicaciones
kubectl apply -f kubernetes/
```

---

## 📊 Monitoreo y Logging

### **Health Checks**
- **Eureka Dashboard:** `http://localhost:8761`
- **Actuator Endpoints:** `http://localhost:{port}/actuator/health`
- **Application Metrics:** `http://localhost:{port}/actuator/metrics`

### **Logging Configuration**
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
</configuration>
```

---

## 🧪 Testing

### **Unit Tests**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ClienteControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testCrearCliente() {
        ClienteDTO cliente = new ClienteDTO();
        cliente.setClienteid("123");
        cliente.setNombre("Test Cliente");
        
        ResponseEntity<ClienteDTO> response = restTemplate.postForEntity("/clientes", cliente, ClienteDTO.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
```

### **Integration Tests**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class MicroclientesIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testFlujCompleto() {
        // Test complete workflow
    }
}
```

---

## 🛡️ Seguridad y Buenas Prácticas

### **Security Headers**
- CORS configuration
- Rate limiting
- Input validation
- SQL injection prevention

### **Database Security**
- Encrypted connections
- Parameterized queries
- Database user segregation
- Regular backups

### **Container Security**
- Non-root user execution
- Minimal base images
- Security scanning
- Resource limits

---

## 📈 Escalabilidad y Performance

### **Horizontal Scaling**
- Multiple service instances
- Load balancing
- Database read replicas
- Caching strategies

### **Performance Optimization**
- Connection pooling
- JPA query optimization
- Async processing
- Circuit breaker pattern

---

## 🚀 URLs y Endpoints

### **Servicios Principales**
- **Eureka Server:** http://localhost:8761
- **API Gateway:** http://localhost:8083
- **Microclientes:** http://localhost:8080 (Docker) / Dinámico (Kubernetes)
- **Microcuentas:** http://localhost:8081 (Docker) / Dinámico (Kubernetes)

### **APIs del Sistema**
```
# Gestión de Clientes
GET    /clientes                 # Listar clientes
GET    /clientes/{id}            # Obtener cliente
POST   /clientes                 # Crear cliente
PUT    /clientes/{id}            # Actualizar cliente
DELETE /clientes/{id}            # Eliminar cliente

# Gestión de Personas
GET    /personas                 # Listar personas
POST   /personas                 # Crear persona
PUT    /personas/{id}            # Actualizar persona
DELETE /personas/{id}            # Eliminar persona

# Gestión de Cuentas
GET    /cuentas                  # Listar cuentas
GET    /cuentas/{numero}         # Obtener cuenta
POST   /cuentas                  # Crear cuenta
PUT    /cuentas/{numero}         # Actualizar cuenta
DELETE /cuentas/{numero}         # Eliminar cuenta

# Gestión de Movimientos
GET    /movimientos              # Listar movimientos
POST   /movimientos              # Crear movimiento
GET    /movimientos/cuenta/{numero} # Movimientos por cuenta

# Reportes
GET    /reportes/cliente/{id}    # Reporte por cliente
GET    /reportes/fechas?desde={fecha}&hasta={fecha} # Reporte por fechas
```

---

## 🎯 Beneficios de la Arquitectura

### **Microservicios**
✅ **Escalabilidad Independiente:** Cada servicio escala según demanda  
✅ **Tecnología Heterogénea:** Diferentes stacks por servicio  
✅ **Deployments Independientes:** Sin afectar otros servicios  
✅ **Fault Isolation:** Fallos aislados por servicio  
✅ **Team Autonomy:** Equipos independientes por servicio  

### **Cloud Native**
✅ **Auto-scaling:** Escalado automático en Kubernetes  
✅ **High Availability:** Múltiples replicas y health checks  
✅ **Infrastructure as Code:** Terraform para reproducibilidad  
✅ **Container Orchestration:** Kubernetes para gestión avanzada  
✅ **Managed Services:** Azure Database, ACR, Load Balancer  

### **DevOps**
✅ **CI/CD Ready:** Prepared for automated pipelines  
✅ **Infrastructure Automation:** Complete Terraform setup  
✅ **Monitoring:** Comprehensive logging and metrics  
✅ **Testing:** Unit, integration and e2e tests  
✅ **Documentation:** Complete technical documentation  

---

## 📝 Conclusión

Este proyecto demuestra una implementación completa y profesional de una **arquitectura de microservicios moderna** utilizando las mejores prácticas de la industria. Combina tecnologías probadas como **Spring Boot**, **Docker**, **Kubernetes** y **Azure Cloud** para crear un sistema bancario escalable, resiliente y maintible.

La arquitectura está diseñada para **producción**, con consideraciones de seguridad, monitoreo, testing y deployments automatizados. Es un ejemplo perfecto de **Enterprise-grade software** que puede manejar cargas de trabajo reales en entornos bancarios y financieros.
