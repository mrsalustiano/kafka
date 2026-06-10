#Requires -Version 5.1
<#
.SYNOPSIS
  Simula um pipeline CI/CD local no Docker Desktop para o projeto kafka-pedidos.

.DESCRIPTION
  Fases executadas:
    1. Build Maven (compile + test + package)
    2. Build das imagens Docker dos 4 microsserviços
    3. Subida da infraestrutura (MySQL, Kafka, Redis, UI, SonarQube)
    4. Criação dos tópicos Kafka
    5. Deploy dos microsserviços em containers
    6. Verificação de health das APIs

.PARAMETER SkipTests
  Executa mvn package sem testes (build mais rápido).

.PARAMETER SkipMaven
  Pula o build Maven (usa JARs já existentes em target/).

.PARAMETER InfraOnly
  Sobe apenas a infraestrutura, sem microsserviços.

.PARAMETER AppsOnly
  Sobe apenas os microsserviços (infraestrutura já deve estar rodando).

.PARAMETER NoCache
  Força rebuild das imagens Docker sem cache.

.EXAMPLE
  .\deploy-docker-desktop.ps1

.EXAMPLE
  .\deploy-docker-desktop.ps1 -SkipTests

.EXAMPLE
  .\deploy-docker-desktop.ps1 -AppsOnly
#>
[CmdletBinding()]
param(
    [switch] $SkipTests,
    [switch] $SkipMaven,
    [switch] $InfraOnly,
    [switch] $AppsOnly,
    [switch] $NoCache
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$DocsDir = $PSScriptRoot
$ProjectRoot = Split-Path -Parent $DocsDir
$InfraDir = Join-Path $ProjectRoot "infraestrutura"
$InfraCompose = Join-Path $InfraDir "docker-compose.yml"
$AppsCompose = Join-Path $DocsDir "docker-compose.apps.yml"
$AppVersion = "1.0.0-SNAPSHOT"

$Services = @(
    "clientes-service",
    "produtos-service",
    "pedidos-service",
    "broker-service"
)

$AppHealthUrls = @{
    "clientes-service" = "http://localhost:8081/actuator/health"
    "produtos-service" = "http://localhost:8082/actuator/health"
    "pedidos-service"  = "http://localhost:8083/actuator/health"
    "broker-service"   = "http://localhost:8084/actuator/health"
}

function Write-Phase([string] $Message) {
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Assert-DockerRunning {
    Write-Phase "Verificando Docker Desktop"
    docker info *> $null
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Desktop nao esta em execucao. Inicie o Docker e tente novamente."
    }
    Write-Host "Docker OK"
}

function Invoke-MavenBuild {
    if ($SkipMaven) {
        Write-Phase "Build Maven ignorado (-SkipMaven)"
        foreach ($service in $Services) {
            $jar = Get-ChildItem -Path (Join-Path $ProjectRoot "servicos\$service\target") -Filter "*.jar" -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -notmatch "original" } |
                Select-Object -First 1
            if (-not $jar) {
                throw "JAR nao encontrado em servicos/$service/target. Execute o build Maven antes."
            }
        }
        return
    }

    Write-Phase "Fase CI: Build Maven"
    Push-Location $ProjectRoot
    try {
        if ($SkipTests) {
            mvn clean package -DskipTests
        }
        else {
            mvn clean verify
        }
        if ($LASTEXITCODE -ne 0) {
            throw "Build Maven falhou com codigo $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
    Write-Host "Maven OK"
}

function Build-DockerImages {
    Write-Phase "Fase CI: Build das imagens Docker"
    $buildArgs = @("compose", "-f", $InfraCompose, "-f", $AppsCompose, "build")
    if ($NoCache) {
        $buildArgs += "--no-cache"
    }
    $buildArgs += $Services

    Push-Location $ProjectRoot
    try {
        $env:APP_VERSION = $AppVersion
        docker @buildArgs
        if ($LASTEXITCODE -ne 0) {
            throw "Build Docker falhou com codigo $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
    Write-Host "Imagens Docker OK"
}

function Start-Infrastructure {
    Write-Phase "Fase CD: Subindo infraestrutura"
    Push-Location $InfraDir
    try {
        docker compose up -d
        if ($LASTEXITCODE -ne 0) {
            throw "Falha ao subir infraestrutura"
        }
    }
    finally {
        Pop-Location
    }

    Write-Host "Aguardando MySQL..."
    $mysqlDeadline = (Get-Date).AddMinutes(3)
    do {
        docker compose -f $InfraCompose exec -T mysql mysqladmin ping -h 127.0.0.1 -uroot -proot *> $null
        if ($LASTEXITCODE -eq 0) { break }
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $mysqlDeadline)
    if ($LASTEXITCODE -ne 0) { throw "Timeout aguardando MySQL" }

    Write-Host "Aguardando Kafka..."
    $kafkaDeadline = (Get-Date).AddMinutes(3)
    do {
        docker compose -f $InfraCompose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 *> $null
        if ($LASTEXITCODE -eq 0) { break }
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $kafkaDeadline)
    if ($LASTEXITCODE -ne 0) { throw "Timeout aguardando Kafka" }

    Write-Host "Aguardando Redis..."
    $redisDeadline = (Get-Date).AddMinutes(2)
    do {
        docker compose -f $InfraCompose exec -T redis redis-cli ping *> $null
        if ($LASTEXITCODE -eq 0) { break }
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $redisDeadline)
    if ($LASTEXITCODE -ne 0) { throw "Timeout aguardando Redis" }

    Write-Phase "Criando topicos Kafka"
    docker compose -f $InfraCompose exec -T kafka bash /opt/kafka/create-topics.sh
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao criar topicos Kafka"
    }

    Write-Host "Infraestrutura OK"
}

function Start-Applications {
    Write-Phase "Fase CD: Deploy dos microsservicos"
    Push-Location $ProjectRoot
    try {
        $env:APP_VERSION = $AppVersion
        docker compose -f $InfraCompose -f $AppsCompose up -d --no-build $Services
        if ($LASTEXITCODE -ne 0) {
            throw "Falha ao subir microsservicos"
        }
    }
    finally {
        Pop-Location
    }

    Write-Phase "Verificando health dos microsservicos"
    foreach ($service in $Services) {
        $url = $AppHealthUrls[$service]
        $deadline = (Get-Date).AddMinutes(3)
        $healthy = $false
        do {
            try {
                $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
                if ($response.StatusCode -eq 200) {
                    $healthy = $true
                    break
                }
            }
            catch {
                # aguarda aplicacao subir
            }
            Start-Sleep -Seconds 5
        } while ((Get-Date) -lt $deadline)

        if (-not $healthy) {
            throw "Health check falhou para $service ($url)"
        }
        Write-Host "$service OK - $url"
    }
}

function Show-Summary {
    Write-Phase "Deploy concluido"
    Write-Host @"

Infraestrutura:
  MySQL .............. localhost:3306  (kafka_user / kafka_pass)
  Kafka .............. localhost:29092 (host) | kafka:9092 (rede Docker)
  Redis .............. localhost:6379
  Kafka UI ........... http://localhost:8080
  Redis Insight ...... http://localhost:5540
  SonarQube .......... http://localhost:9000  (admin / admin)

Microsservicos:
  clientes-service ... http://localhost:8081/swagger-ui.html
  produtos-service ... http://localhost:8082/swagger-ui.html
  pedidos-service .... http://localhost:8083/swagger-ui.html
  broker-service ..... http://localhost:8084/actuator/health

Comandos uteis:
  docker compose -f infraestrutura/docker-compose.yml -f docs/docker-compose.apps.yml ps
  docker compose -f infraestrutura/docker-compose.yml -f docs/docker-compose.apps.yml logs -f clientes-service
  scripts\stop.bat

"@
}

# --- Execucao ---

Assert-DockerRunning

if ($AppsOnly) {
    if (-not $SkipMaven) {
        Invoke-MavenBuild
    }
    Build-DockerImages
    Start-Applications
    Show-Summary
    exit 0
}

if (-not $InfraOnly) {
    Invoke-MavenBuild
    Build-DockerImages
}

Start-Infrastructure

if (-not $InfraOnly) {
    Start-Applications
}

Show-Summary
