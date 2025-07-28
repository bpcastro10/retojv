FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar el archivo JAR del proyecto
COPY target/*.jar app.jar

# Exponer el puerto del Eureka Server
EXPOSE 8761

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 