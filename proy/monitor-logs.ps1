# Script para monitorear logs de microservicios (PowerShell)
# Uso: .\monitor-logs.ps1 [servicio] [tipo_log]

param(
    [string]$Service,
    [string]$LogType
)

# Colores para output
$Red = "Red"
$Green = "Green"
$Yellow = "Yellow"
$Blue = "Blue"
$Magenta = "Magenta"
$Cyan = "Cyan"
$White = "White"

# Función para mostrar ayuda
function Show-Help {
    Write-Host "=== Monitor de Logs de Microservicios ===" -ForegroundColor $Cyan
    Write-Host ""
    Write-Host "Uso: .\monitor-logs.ps1 [servicio] [tipo_log]"
    Write-Host ""
    Write-Host "Servicios disponibles:" -ForegroundColor $Green
    Write-Host "  eureka     - Eureka Server" -ForegroundColor $Green
    Write-Host "  gateway    - API Gateway" -ForegroundColor $Green
    Write-Host "  clientes   - Microservicio de Clientes" -ForegroundColor $Green
    Write-Host "  cuentas    - Microservicio de Cuentas" -ForegroundColor $Green
    Write-Host "  all        - Todos los servicios" -ForegroundColor $Green
    Write-Host ""
    Write-Host "Tipos de log disponibles:" -ForegroundColor $Yellow
    Write-Host "  info      - Logs informativos" -ForegroundColor $Yellow
    Write-Host "  error     - Logs de error" -ForegroundColor $Red
    Write-Host "  debug     - Logs de debug" -ForegroundColor $Blue
    Write-Host "  sql       - Logs SQL (solo microservicios)" -ForegroundColor $Magenta
    Write-Host "  feign     - Logs Feign (solo microcuentas)" -ForegroundColor $Magenta
    Write-Host ""
    Write-Host "Comandos especiales:" -ForegroundColor $Cyan
    Write-Host "  search    - Buscar errores en todos los logs" -ForegroundColor $Cyan
    Write-Host "  clean     - Limpiar logs antiguos" -ForegroundColor $Cyan
    Write-Host "  stats     - Mostrar estadísticas de logs" -ForegroundColor $Cyan
    Write-Host ""
    Write-Host "Ejemplos:" -ForegroundColor $White
    Write-Host "  .\monitor-logs.ps1 eureka info      - Ver logs informativos de Eureka"
    Write-Host "  .\monitor-logs.ps1 clientes error   - Ver logs de error de Clientes"
    Write-Host "  .\monitor-logs.ps1 cuentas sql      - Ver logs SQL de Cuentas"
    Write-Host "  .\monitor-logs.ps1 all info         - Ver logs informativos de todos los servicios"
    Write-Host "  .\monitor-logs.ps1 search           - Buscar errores"
    Write-Host ""
}

# Función para verificar si existe el archivo de log
function Test-LogFile {
    param([string]$LogFile)
    
    if (-not (Test-Path $LogFile)) {
        Write-Host "❌ Archivo de log no encontrado: $LogFile" -ForegroundColor $Red
        Write-Host "💡 Asegúrate de que el servicio esté ejecutándose" -ForegroundColor $Yellow
        return $false
    }
    return $true
}

# Función para mostrar logs de un servicio
function Show-ServiceLogs {
    param([string]$Service, [string]$LogType)
    
    $logFile = ""
    
    switch ($Service) {
        "eureka" {
            switch ($LogType) {
                "info" { $logFile = "logs\eureka-server-info.log" }
                "error" { $logFile = "logs\eureka-server-error.log" }
                "debug" { $logFile = "logs\eureka-server-debug.log" }
                default {
                    Write-Host "❌ Tipo de log no válido para Eureka: $LogType" -ForegroundColor $Red
                    return
                }
            }
        }
        "gateway" {
            switch ($LogType) {
                "info" { $logFile = "logs\gateway-info.log" }
                "error" { $logFile = "logs\gateway-error.log" }
                "debug" { $logFile = "logs\gateway-debug.log" }
                default {
                    Write-Host "❌ Tipo de log no válido para Gateway: $LogType" -ForegroundColor $Red
                    return
                }
            }
        }
        "clientes" {
            switch ($LogType) {
                "info" { $logFile = "logs\microclientes-info.log" }
                "error" { $logFile = "logs\microclientes-error.log" }
                "debug" { $logFile = "logs\microclientes-debug.log" }
                "sql" { $logFile = "logs\microclientes-sql.log" }
                default {
                    Write-Host "❌ Tipo de log no válido para Clientes: $LogType" -ForegroundColor $Red
                    return
                }
            }
        }
        "cuentas" {
            switch ($LogType) {
                "info" { $logFile = "logs\microcuentas-info.log" }
                "error" { $logFile = "logs\microcuentas-error.log" }
                "debug" { $logFile = "logs\microcuentas-debug.log" }
                "sql" { $logFile = "logs\microcuentas-sql.log" }
                "feign" { $logFile = "logs\microcuentas-feign.log" }
                default {
                    Write-Host "❌ Tipo de log no válido para Cuentas: $LogType" -ForegroundColor $Red
                    return
                }
            }
        }
        default {
            Write-Host "❌ Servicio no válido: $Service" -ForegroundColor $Red
            return
        }
    }
    
    if (Test-LogFile $logFile) {
        Write-Host "📋 Mostrando logs de $Service ($LogType):" -ForegroundColor $Green
        Write-Host "Archivo: $logFile" -ForegroundColor $Cyan
        Write-Host "Presiona Ctrl+C para salir" -ForegroundColor $Yellow
        Write-Host ""
        
        try {
            Get-Content $logFile -Wait -Tail 0
        }
        catch {
            Write-Host "Error al leer el archivo de log: $_" -ForegroundColor $Red
        }
    }
}

# Función para mostrar logs de todos los servicios
function Show-AllLogs {
    param([string]$LogType)
    
    Write-Host "📋 Mostrando logs de todos los servicios ($LogType):" -ForegroundColor $Green
    Write-Host "Presiona Ctrl+C para salir" -ForegroundColor $Yellow
    Write-Host ""
    
    $logFiles = @()
    
    switch ($LogType) {
        "info" {
            $logFiles = @(
                "logs\eureka-server-info.log",
                "logs\gateway-info.log", 
                "logs\microclientes-info.log",
                "logs\microcuentas-info.log"
            )
        }
        "error" {
            $logFiles = @(
                "logs\eureka-server-error.log",
                "logs\gateway-error.log",
                "logs\microclientes-error.log", 
                "logs\microcuentas-error.log"
            )
        }
        "debug" {
            $logFiles = @(
                "logs\eureka-server-debug.log",
                "logs\gateway-debug.log",
                "logs\microclientes-debug.log",
                "logs\microcuentas-debug.log"
            )
        }
        default {
            Write-Host "❌ Tipo de log no válido para 'all': $LogType" -ForegroundColor $Red
            return
        }
    }
    
    # Filtrar solo archivos que existen
    $existingFiles = $logFiles | Where-Object { Test-Path $_ }
    
    if ($existingFiles.Count -eq 0) {
        Write-Host "❌ No se encontraron archivos de log para mostrar" -ForegroundColor $Red
        return
    }
    
    try {
        # Mostrar logs combinados
        $jobs = @()
        foreach ($file in $existingFiles) {
            $job = Start-Job -ScriptBlock {
                param($logFile)
                Get-Content $logFile -Wait -Tail 0 | ForEach-Object { "[$($logFile.Split('\')[-1].Split('-')[0])] $_" }
            } -ArgumentList $file
            $jobs += $job
        }
        
        # Esperar y mostrar resultados
        while ($jobs | Where-Object { $_.State -eq "Running" }) {
            $jobs | Receive-Job
            Start-Sleep -Milliseconds 100
        }
    }
    catch {
        Write-Host "Error al mostrar logs: $_" -ForegroundColor $Red
    }
    finally {
        $jobs | Stop-Job -ErrorAction SilentlyContinue
        $jobs | Remove-Job -ErrorAction SilentlyContinue
    }
}

# Función para buscar errores en todos los logs
function Search-Errors {
    Write-Host "🔍 Buscando errores en todos los logs..." -ForegroundColor $Red
    Write-Host ""
    
    if (Test-Path "logs") {
        Write-Host "Errores encontrados:" -ForegroundColor $Yellow
        Get-ChildItem -Path "logs" -Filter "*.log" -Recurse | ForEach-Object {
            $content = Get-Content $_.FullName -ErrorAction SilentlyContinue
            $errors = $content | Select-String "ERROR" | Select-Object -First 5
            if ($errors) {
                Write-Host "Archivo: $($_.Name)" -ForegroundColor $Cyan
                $errors | ForEach-Object { Write-Host "  $_" -ForegroundColor $Red }
                Write-Host ""
            }
        }
        
        Write-Host "Excepciones encontradas:" -ForegroundColor $Yellow
        Get-ChildItem -Path "logs" -Filter "*.log" -Recurse | ForEach-Object {
            $content = Get-Content $_.FullName -ErrorAction SilentlyContinue
            $exceptions = $content | Select-String "Exception" | Select-Object -First 5
            if ($exceptions) {
                Write-Host "Archivo: $($_.Name)" -ForegroundColor $Cyan
                $exceptions | ForEach-Object { Write-Host "  $_" -ForegroundColor $Red }
                Write-Host ""
            }
        }
    }
    else {
        Write-Host "❌ Directorio de logs no encontrado" -ForegroundColor $Red
    }
}

# Función para limpiar logs antiguos
function Clean-OldLogs {
    Write-Host "🧹 Limpiando logs antiguos (más de 30 días)..." -ForegroundColor $Yellow
    
    if (Test-Path "logs") {
        $cutoffDate = (Get-Date).AddDays(-30)
        
        $oldFiles = Get-ChildItem -Path "logs" -Filter "*.log" -Recurse | Where-Object { $_.LastWriteTime -lt $cutoffDate }
        $oldFiles | Remove-Item -Force
        
        if (Test-Path "logs\archive") {
            $oldArchiveFiles = Get-ChildItem -Path "logs\archive" -Filter "*.log" -Recurse | Where-Object { $_.LastWriteTime -lt $cutoffDate }
            $oldArchiveFiles | Remove-Item -Force
        }
        
        Write-Host "✅ Logs antiguos eliminados" -ForegroundColor $Green
    }
    else {
        Write-Host "❌ Directorio de logs no encontrado" -ForegroundColor $Red
    }
}

# Función para mostrar estadísticas de logs
function Show-LogStats {
    Write-Host "📊 Estadísticas de logs:" -ForegroundColor $Cyan
    Write-Host ""
    
    if (Test-Path "logs") {
        Write-Host "Archivos de log encontrados:" -ForegroundColor $Green
        Get-ChildItem -Path "logs" -Filter "*.log" | ForEach-Object {
            $size = [math]::Round($_.Length / 1MB, 2)
            Write-Host "  $($_.Name) - $size MB" -ForegroundColor $White
        }
        
        Write-Host ""
        Write-Host "Tamaño total de logs:" -ForegroundColor $Green
        $totalSize = (Get-ChildItem -Path "logs" -Filter "*.log" -Recurse | Measure-Object -Property Length -Sum).Sum
        $totalSizeMB = [math]::Round($totalSize / 1MB, 2)
        Write-Host "  $totalSizeMB MB" -ForegroundColor $White
        
        if (Test-Path "logs\archive") {
            Write-Host ""
            Write-Host "Archivos de archivo:" -ForegroundColor $Green
            Get-ChildItem -Path "logs\archive" | Select-Object -First 10 | ForEach-Object {
                $size = [math]::Round($_.Length / 1MB, 2)
                Write-Host "  $($_.Name) - $size MB" -ForegroundColor $White
            }
        }
    }
    else {
        Write-Host "❌ Directorio de logs no encontrado" -ForegroundColor $Red
    }
}

# Función principal
function Main {
    # Verificar argumentos
    if (-not $Service -or $Service -eq "-h" -or $Service -eq "--help") {
        Show-Help
        return
    }
    
    # Verificar que se proporcione el tipo de log
    if (-not $LogType) {
        Write-Host "❌ Error: Debes especificar el tipo de log" -ForegroundColor $Red
        Write-Host ""
        Show-Help
        return
    }
    
    # Crear directorio de logs si no existe
    if (-not (Test-Path "logs")) {
        New-Item -ItemType Directory -Path "logs" -Force | Out-Null
    }
    if (-not (Test-Path "logs\archive")) {
        New-Item -ItemType Directory -Path "logs\archive" -Force | Out-Null
    }
    
    # Procesar comandos especiales
    switch ($Service) {
        "search" {
            Search-Errors
            return
        }
        "clean" {
            Clean-OldLogs
            return
        }
        "stats" {
            Show-LogStats
            return
        }
    }
    
    # Mostrar logs según el servicio
    if ($Service -eq "all") {
        Show-AllLogs $LogType
    }
    else {
        Show-ServiceLogs $Service $LogType
    }
}

# Ejecutar función principal
Main 