@echo off
echo ========================================
echo    PRUEBA DEL EUREKA SERVER
echo ========================================
echo.

echo [1/3] Compilando el proyecto...
call mvnw clean compile
if %errorlevel% neq 0 (
    echo Error en la compilación
    pause
    exit /b 1
)

echo [2/3] Ejecutando el Eureka Server...
start "Eureka Server" cmd /k "mvnw spring-boot:run"
timeout /t 15 /nobreak >nul

echo [3/3] Probando el servidor...
echo.
echo Probando health check...
curl -s http://localhost:8761/actuator/health
echo.
echo.

echo Probando dashboard...
curl -s http://localhost:8761/ | findstr "Eureka"
echo.
echo.

echo ========================================
echo    INFORMACION DEL SERVIDOR
echo ========================================
echo.
echo Eureka Dashboard: http://localhost:8761
echo Health Check: http://localhost:8761/actuator/health
echo Info: http://localhost:8761/actuator/info
echo.
echo ========================================
echo    VERIFICACION
echo ========================================
echo.
echo 1. Abre http://localhost:8761 en tu navegador
echo 2. Deberías ver el dashboard de Eureka
echo 3. No debería haber servicios registrados aún
echo.
echo Presiona cualquier tecla para detener el servidor...
pause

taskkill /f /im java.exe
echo Servidor detenido. 