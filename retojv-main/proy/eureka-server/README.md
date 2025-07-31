# 🎯 Eureka Server - Service Discovery

## 📋 Descripción

El Eureka Server es el componente central de Service Discovery en la arquitectura de microservicios. Permite que los microservicios se registren automáticamente y descubran otros servicios de manera dinámica.

## 🏗️ Arquitectura

```
┌─────────────────┐
│  Eureka Server  │
│   (Puerto 8761) │
└─────────────────┘
         │
         │ Registro y Descubrimiento
         │
    ┌────┴────┐
    │         │
┌─────────┐ ┌─────────┐
│Microclie│ │Microcuen│
│ntes     │ │tas      │
│(8080)   │ │(8081)   │
└─────────┘ └─────────┘
```

## 🚀 Inicio Rápido

### 1. **Compilar el Proyecto**
```bash
./mvnw clean compile
```

### 2. **Ejecutar el Servidor**
```bash
./mvnw spring-boot:run
```

### 3. **Acceder al Dashboard**
- **URL**: http://localhost:8761
- **Usuario**: No requiere autenticación (desarrollo)

## 🔧 Configuración

### **Puerto**
- **Eureka Server**: 8761 (puerto estándar)

### **Endpoints Disponibles**
- **Dashboard**: http://localhost:8761
- **Health Check**: http://localhost:8761/actuator/health
- **Info**: http://localhost:8761/actuator/info

### **Configuración de Microservicios**

Para que los microservicios se registren en Eureka, deben incluir:

```xml
<!-- En pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

```properties
# En application.properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

## 📊 Dashboard de Eureka

### **Información Disponible**
- **Instancias Registradas**: Lista de todos los servicios
- **Estado de Salud**: Estado de cada instancia
- **Metadatos**: Información detallada de cada servicio
- **Estadísticas**: Métricas de registro y descubrimiento

### **Funcionalidades**
- **Registro Automático**: Los servicios se registran al iniciar
- **Heartbeat**: Verificación periódica de salud
- **Auto-cancelación**: Eliminación automática de servicios caídos
- **Load Balancing**: Distribución de carga automática

## 🔍 Monitoreo

### **Health Checks**
```bash
curl http://localhost:8761/actuator/health
```

### **Logs**
```bash
# Ver logs en tiempo real
tail -f logs/eureka-server.log
```

### **Métricas**
- **Instancias Activas**: Número de servicios registrados
- **Tiempo de Respuesta**: Latencia del servidor
- **Errores**: Fallos en el registro/descubrimiento

## 🛠️ Desarrollo

### **Estructura del Proyecto**
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── proyecto/
│   │           └── eureka_server/
│   │               └── EurekaServerApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── proyecto/
                └── eureka_server/
                    └── EurekaServerApplicationTests.java
```

### **Dependencias Principales**
- **spring-cloud-starter-netflix-eureka-server**: Servidor Eureka
- **spring-boot-starter-actuator**: Monitoreo y métricas
- **spring-boot-starter-web**: Servidor web

## 🔒 Seguridad

### **Configuración Actual**
- **Autenticación**: Deshabilitada para desarrollo
- **HTTPS**: No configurado (desarrollo)

### **Configuración de Producción**
```properties
# Habilitar autenticación
eureka.client.service-url.defaultZone=http://user:password@localhost:8761/eureka/
```

## 🚨 Troubleshooting

### **Problemas Comunes**

#### **1. Servicio no se registra**
- Verificar que el cliente Eureka esté configurado
- Comprobar conectividad de red
- Revisar logs del servicio

#### **2. Dashboard no carga**
- Verificar que el puerto 8761 esté libre
- Comprobar logs del servidor
- Validar configuración de firewall

#### **3. Servicios desaparecen**
- Verificar heartbeat configuration
- Comprobar timeouts
- Revisar logs de red

### **Logs Útiles**
```bash
# Ver logs del servidor
./mvnw spring-boot:run 2>&1 | tee eureka-server.log

# Filtrar logs de registro
grep "REGISTER" eureka-server.log

# Filtrar logs de heartbeat
grep "HEARTBEAT" eureka-server.log
```

## 📈 Escalabilidad

### **Configuración de Alta Disponibilidad**
```properties
# Múltiples instancias de Eureka
eureka.client.service-url.defaultZone=http://eureka1:8761/eureka/,http://eureka2:8761/eureka/
```

### **Load Balancing**
- **Client-side**: Ribbon (integrado con Eureka)
- **Server-side**: Configurable en el gateway

## 🔄 Integración con Otros Componentes

### **Spring Cloud Gateway**
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
```

### **OpenFeign**
```java
@FeignClient(name = "microclientes")
public interface ClienteClient {
    // Métodos del cliente
}
```

## 📚 Recursos Adicionales

- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Eureka Documentation](https://github.com/Netflix/eureka/wiki)
- [Spring Cloud Reference](https://docs.spring.io/spring-cloud/docs/current/reference/html/)

---

**Desarrollado con Spring Boot y Spring Cloud Netflix Eureka** 