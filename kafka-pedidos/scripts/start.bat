@echo off
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "INFRA_DIR=%SCRIPT_DIR%..\infraestrutura"

cd /d "%INFRA_DIR%"

echo Iniciando infraestrutura kafka-pedidos...
docker compose up -d

echo Aguardando Kafka ficar disponivel...
:wait_kafka
docker compose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >nul 2>&1
if errorlevel 1 (
  timeout /t 3 /nobreak >nul
  goto wait_kafka
)

echo Criando topicos Kafka...
docker compose exec -T kafka bash /opt/kafka/create-topics.sh

echo Infraestrutura iniciada.
docker compose ps

endlocal
