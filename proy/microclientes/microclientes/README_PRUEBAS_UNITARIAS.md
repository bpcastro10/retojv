# Pruebas Unitarias - Microservicio de Clientes

## Descripción
Este documento describe las pruebas unitarias implementadas para el dominio de cliente del microservicio de microclientes.

## Estructura de Pruebas

### 1. Pruebas de Entidades
- **ClienteTest.java**: Pruebas para la entidad Cliente
  - Getters y setters de todos los campos
  - Manejo de valores nulos
  - Relación con la entidad Persona
  - Actualización de campos

- **PersonaTest.java**: Pruebas para la entidad Persona
  - Getters y setters de todos los campos
  - Manejo de valores nulos
  - Casos especiales (edad cero, negativa)

### 2. Pruebas de Servicio
- **ClienteServiceTest.java**: Pruebas para ClienteService
  - Listar todos los clientes
  - Buscar cliente por ID
  - Guardar cliente nuevo
  - Actualizar cliente existente
  - Eliminar cliente
  - Manejo de excepciones
  - Casos edge (lista vacía, cliente no encontrado)

### 3. Pruebas de Repositorio
- **ClienteRepositoryTest.java**: Pruebas de integración para ClienteRepository
  - Operaciones CRUD completas
  - Persistencia en base de datos H2 en memoria
  - Validación de relaciones con Persona
  - Manejo de múltiples clientes con diferentes personas

- **PersonaRepositoryTest.java**: Pruebas de integración para PersonaRepository
  - Operaciones CRUD completas
  - Persistencia en base de datos H2 en memoria
  - Casos especiales (edad cero, negativa, valores nulos)

### 4. Pruebas de Integración
- **ClienteIntegrationTest.java**: Pruebas de integración completa
  - Flujo completo desde servicio hasta base de datos
  - Operaciones CRUD a través del servicio
  - Manejo de relaciones entre entidades
  - Casos edge y excepciones
  - Operaciones concurrentes
  - Transacciones automáticas

## Configuración

### Archivos de Configuración
- **application-test.properties**: Configuración específica para pruebas
  - Base de datos H2 en memoria
  - Eureka deshabilitado
  - Logging optimizado para pruebas

### Dependencias
Las pruebas utilizan las siguientes dependencias (ya incluidas en pom.xml):
- JUnit 5
- Mockito
- Spring Boot Test
- H2 Database (para pruebas)

## Ejecución de Pruebas

### Opción 1: Scripts Automáticos (Recomendados)
```bash
# Script más simple (usa variable de entorno)
test-simple.bat

# Script con Maven Wrapper
ejecutar-pruebas.bat

# Script que intenta Maven primero
test-maven.bat

# Script rápido
test-rapido.bat

# Script completo con limpieza
test-unitarias.bat

# Script solo pruebas de integración
test-integracion.bat

# Script todas las pruebas (unitarias + integración)
test-completas.bat
```

### Opción 2: Comando Maven Wrapper
```bash
# Ejecutar todas las pruebas
call mvnw.cmd test "-Dspring.profiles.active=test"

# Ejecutar pruebas específicas
call mvnw.cmd test -Dtest=ClienteTest
call mvnw.cmd test -Dtest=ClienteServiceTest
call mvnw.cmd test -Dtest=ClienteRepositoryTest
```

### Opción 3: Comando Maven (si está instalado)
```bash
# Ejecutar todas las pruebas
mvn test -Dspring.profiles.active=test

# Ejecutar pruebas específicas
mvn test -Dtest=ClienteTest
mvn test -Dtest=ClienteServiceTest
mvn test -Dtest=ClienteRepositoryTest
```

### Opción 3: Desde IDE
1. Ejecutar individualmente cada clase de prueba
2. Usar el perfil "test" en la configuración
3. Verificar que la base de datos H2 esté disponible

## Cobertura de Pruebas

### Entidad Cliente
- ✅ ClienteID (getter/setter)
- ✅ Contraseña (getter/setter)
- ✅ Estado (getter/setter)
- ✅ Persona (getter/setter)
- ✅ Valores nulos
- ✅ Actualización completa

### Entidad Persona
- ✅ Identificación (getter/setter)
- ✅ Nombre (getter/setter)
- ✅ Género (getter/setter)
- ✅ Edad (getter/setter)
- ✅ Dirección (getter/setter)
- ✅ Teléfono (getter/setter)
- ✅ Casos especiales de edad

### Servicio ClienteService
- ✅ Listar clientes
- ✅ Buscar por ID
- ✅ Guardar cliente
- ✅ Actualizar cliente
- ✅ Eliminar cliente
- ✅ Manejo de excepciones
- ✅ Casos edge

### Repositorio ClienteRepository
- ✅ Operaciones CRUD
- ✅ Persistencia
- ✅ Relaciones con Persona
- ✅ Conteo de registros
- ✅ Múltiples clientes con diferentes personas

### Repositorio PersonaRepository
- ✅ Operaciones CRUD
- ✅ Persistencia
- ✅ Casos especiales (edad cero, negativa)
- ✅ Valores nulos
- ✅ Conteo de registros

### Pruebas de Integración
- ✅ Flujo completo CRUD
- ✅ Operaciones a través del servicio
- ✅ Relaciones entre entidades
- ✅ Casos edge y excepciones
- ✅ Operaciones concurrentes
- ✅ Transacciones automáticas

## Ventajas de esta Implementación

1. **Aislamiento**: Las pruebas no interfieren con el servicio en ejecución
2. **Velocidad**: Uso de base de datos en memoria (H2)
3. **Confiabilidad**: Mocking de dependencias externas
4. **Cobertura**: Pruebas de todas las capas del dominio
5. **Mantenibilidad**: Código de prueba limpio y documentado
6. **Integración Completa**: Pruebas que validan el flujo completo
7. **Transacciones**: Rollback automático para evitar contaminación de datos
8. **Escalabilidad**: Pruebas que manejan operaciones concurrentes

## Reportes

Los reportes de pruebas se generan en:
- `target/surefire-reports/` - Reportes de ejecución
- `target/site/jacoco/` - Reporte de cobertura (si se configura)

## Notas Importantes

- Las pruebas usan el perfil "test" para evitar conflictos
- Eureka está deshabilitado en las pruebas
- La base de datos se crea y destruye automáticamente
- No se requieren servicios externos para ejecutar las pruebas

## Solución de Problemas

### Error: "No tests found"
- Verificar que las clases de prueba estén en el directorio correcto
- Asegurar que los nombres de clase terminen en "Test"

### Error: "Database connection failed"
- Verificar que H2 esté en el classpath
- Revisar la configuración en application-test.properties

### Error: "Mockito not found"
- Verificar que spring-boot-starter-test esté en las dependencias
- Limpiar y recompilar el proyecto 