@echo off
echo ========================================
echo    API Gateway - Test Script
echo ========================================
echo.

echo 1. Verificando que el gateway esté ejecutándose...
curl -s http://localhost:8083/gateway/health
echo.
echo.

echo 2. Obteniendo información de rutas...
curl -s http://localhost:8083/gateway/routes
echo.
echo.

echo 3. Probando endpoint de clientes...
curl -s http://localhost:8083/api/v1/clientes
echo.
echo.

echo 4. Probando endpoint de cuentas...
curl -s http://localhost:8083/api/v1/cuentas
echo.
echo.

echo ========================================
echo    Test completado
echo ========================================
pause 