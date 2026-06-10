@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "INFRA_DIR=%SCRIPT_DIR%..\infraestrutura"

cd /d "%INFRA_DIR%"

echo Parando infraestrutura kafka-pedidos...
docker compose down

echo Infraestrutura parada.

endlocal
