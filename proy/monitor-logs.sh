#!/bin/bash

# Script para monitorear logs de microservicios
# Uso: ./monitor-logs.sh [servicio] [tipo_log]

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Función para mostrar ayuda
show_help() {
    echo -e "${CYAN}=== Monitor de Logs de Microservicios ===${NC}"
    echo ""
    echo "Uso: $0 [servicio] [tipo_log]"
    echo ""
    echo "Servicios disponibles:"
    echo -e "  ${GREEN}eureka${NC}     - Eureka Server"
    echo -e "  ${GREEN}gateway${NC}    - API Gateway"
    echo -e "  ${GREEN}clientes${NC}   - Microservicio de Clientes"
    echo -e "  ${GREEN}cuentas${NC}    - Microservicio de Cuentas"
    echo -e "  ${GREEN}all${NC}        - Todos los servicios"
    echo ""
    echo "Tipos de log disponibles:"
    echo -e "  ${YELLOW}info${NC}      - Logs informativos"
    echo -e "  ${RED}error${NC}     - Logs de error"
    echo -e "  ${BLUE}debug${NC}     - Logs de debug"
    echo -e "  ${PURPLE}sql${NC}      - Logs SQL (solo microservicios)"
    echo -e "  ${PURPLE}feign${NC}    - Logs Feign (solo microcuentas)"
    echo ""
    echo "Ejemplos:"
    echo "  $0 eureka info      - Ver logs informativos de Eureka"
    echo "  $0 clientes error   - Ver logs de error de Clientes"
    echo "  $0 cuentas sql      - Ver logs SQL de Cuentas"
    echo "  $0 all info         - Ver logs informativos de todos los servicios"
    echo ""
}

# Función para verificar si existe el archivo de log
check_log_file() {
    local log_file=$1
    if [ ! -f "$log_file" ]; then
        echo -e "${RED}❌ Archivo de log no encontrado: $log_file${NC}"
        echo -e "${YELLOW}💡 Asegúrate de que el servicio esté ejecutándose${NC}"
        return 1
    fi
    return 0
}

# Función para mostrar logs de un servicio
show_service_logs() {
    local service=$1
    local log_type=$2
    
    case $service in
        "eureka")
            case $log_type in
                "info")
                    log_file="logs/eureka-server-info.log"
                    ;;
                "error")
                    log_file="logs/eureka-server-error.log"
                    ;;
                "debug")
                    log_file="logs/eureka-server-debug.log"
                    ;;
                *)
                    echo -e "${RED}❌ Tipo de log no válido para Eureka: $log_type${NC}"
                    return 1
                    ;;
            esac
            ;;
        "gateway")
            case $log_type in
                "info")
                    log_file="logs/gateway-info.log"
                    ;;
                "error")
                    log_file="logs/gateway-error.log"
                    ;;
                "debug")
                    log_file="logs/gateway-debug.log"
                    ;;
                *)
                    echo -e "${RED}❌ Tipo de log no válido para Gateway: $log_type${NC}"
                    return 1
                    ;;
            esac
            ;;
        "clientes")
            case $log_type in
                "info")
                    log_file="logs/microclientes-info.log"
                    ;;
                "error")
                    log_file="logs/microclientes-error.log"
                    ;;
                "debug")
                    log_file="logs/microclientes-debug.log"
                    ;;
                "sql")
                    log_file="logs/microclientes-sql.log"
                    ;;
                *)
                    echo -e "${RED}❌ Tipo de log no válido para Clientes: $log_type${NC}"
                    return 1
                    ;;
            esac
            ;;
        "cuentas")
            case $log_type in
                "info")
                    log_file="logs/microcuentas-info.log"
                    ;;
                "error")
                    log_file="logs/microcuentas-error.log"
                    ;;
                "debug")
                    log_file="logs/microcuentas-debug.log"
                    ;;
                "sql")
                    log_file="logs/microcuentas-sql.log"
                    ;;
                "feign")
                    log_file="logs/microcuentas-feign.log"
                    ;;
                *)
                    echo -e "${RED}❌ Tipo de log no válido para Cuentas: $log_type${NC}"
                    return 1
                    ;;
            esac
            ;;
        *)
            echo -e "${RED}❌ Servicio no válido: $service${NC}"
            return 1
            ;;
    esac
    
    if check_log_file "$log_file"; then
        echo -e "${GREEN}📋 Mostrando logs de $service ($log_type):${NC}"
        echo -e "${CYAN}Archivo: $log_file${NC}"
        echo -e "${YELLOW}Presiona Ctrl+C para salir${NC}"
        echo ""
        tail -f "$log_file"
    fi
}

# Función para mostrar logs de todos los servicios
show_all_logs() {
    local log_type=$1
    
    echo -e "${GREEN}📋 Mostrando logs de todos los servicios ($log_type):${NC}"
    echo -e "${YELLOW}Presiona Ctrl+C para salir${NC}"
    echo ""
    
    # Crear archivos temporales para cada servicio
    temp_files=()
    
    case $log_type in
        "info")
            services=("eureka" "gateway" "clientes" "cuentas")
            log_files=("logs/eureka-server-info.log" "logs/gateway-info.log" "logs/microclientes-info.log" "logs/microcuentas-info.log")
            ;;
        "error")
            services=("eureka" "gateway" "clientes" "cuentas")
            log_files=("logs/eureka-server-error.log" "logs/gateway-error.log" "logs/microclientes-error.log" "logs/microcuentas-error.log")
            ;;
        "debug")
            services=("eureka" "gateway" "clientes" "cuentas")
            log_files=("logs/eureka-server-debug.log" "logs/gateway-debug.log" "logs/microclientes-debug.log" "logs/microcuentas-debug.log")
            ;;
        *)
            echo -e "${RED}❌ Tipo de log no válido para 'all': $log_type${NC}"
            return 1
            ;;
    esac
    
    # Crear archivos temporales y verificar existencia
    for i in "${!services[@]}"; do
        if [ -f "${log_files[$i]}" ]; then
            temp_file=$(mktemp)
            temp_files+=("$temp_file")
            # Agregar prefijo con el nombre del servicio
            tail -f "${log_files[$i]}" | sed "s/^/[${services[$i]}] /" > "$temp_file" &
        fi
    done
    
    # Esperar un momento para que los archivos se creen
    sleep 1
    
    # Mostrar todos los logs combinados
    if [ ${#temp_files[@]} -gt 0 ]; then
        tail -f "${temp_files[@]}" &
        tail_pid=$!
        
        # Esperar señal de interrupción
        trap "kill $tail_pid 2>/dev/null; rm -f ${temp_files[*]} 2>/dev/null; exit" INT
        wait $tail_pid
    else
        echo -e "${RED}❌ No se encontraron archivos de log para mostrar${NC}"
    fi
    
    # Limpiar archivos temporales
    rm -f "${temp_files[@]}" 2>/dev/null
}

# Función para buscar errores en todos los logs
search_errors() {
    echo -e "${RED}🔍 Buscando errores en todos los logs...${NC}"
    echo ""
    
    if [ -d "logs" ]; then
        echo -e "${YELLOW}Errores encontrados:${NC}"
        grep -r "ERROR" logs/ --color=always | head -20
        echo ""
        echo -e "${YELLOW}Excepciones encontradas:${NC}"
        grep -r "Exception" logs/ --color=always | head -20
    else
        echo -e "${RED}❌ Directorio de logs no encontrado${NC}"
    fi
}

# Función para limpiar logs antiguos
clean_old_logs() {
    echo -e "${YELLOW}🧹 Limpiando logs antiguos (más de 30 días)...${NC}"
    
    if [ -d "logs" ]; then
        find logs/ -name "*.log" -mtime +30 -delete
        find logs/archive/ -name "*.log" -mtime +30 -delete 2>/dev/null
        echo -e "${GREEN}✅ Logs antiguos eliminados${NC}"
    else
        echo -e "${RED}❌ Directorio de logs no encontrado${NC}"
    fi
}

# Función para mostrar estadísticas de logs
show_log_stats() {
    echo -e "${CYAN}📊 Estadísticas de logs:${NC}"
    echo ""
    
    if [ -d "logs" ]; then
        echo -e "${GREEN}Archivos de log encontrados:${NC}"
        ls -la logs/*.log 2>/dev/null | while read line; do
            echo "  $line"
        done
        
        echo ""
        echo -e "${GREEN}Tamaño de archivos de log:${NC}"
        du -h logs/*.log 2>/dev/null | while read line; do
            echo "  $line"
        done
        
        echo ""
        echo -e "${GREEN}Archivos de archivo:${NC}"
        ls -la logs/archive/ 2>/dev/null | head -10
    else
        echo -e "${RED}❌ Directorio de logs no encontrado${NC}"
    fi
}

# Función principal
main() {
    # Verificar argumentos
    if [ $# -eq 0 ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_help
        exit 0
    fi
    
    local service=$1
    local log_type=$2
    
    # Verificar que se proporcionen ambos argumentos
    if [ -z "$log_type" ]; then
        echo -e "${RED}❌ Error: Debes especificar el tipo de log${NC}"
        echo ""
        show_help
        exit 1
    fi
    
    # Crear directorio de logs si no existe
    mkdir -p logs/archive
    
    # Procesar comandos especiales
    case $service in
        "search")
            search_errors
            exit 0
            ;;
        "clean")
            clean_old_logs
            exit 0
            ;;
        "stats")
            show_log_stats
            exit 0
            ;;
    esac
    
    # Mostrar logs según el servicio
    if [ "$service" = "all" ]; then
        show_all_logs "$log_type"
    else
        show_service_logs "$service" "$log_type"
    fi
}

# Ejecutar función principal
main "$@" 