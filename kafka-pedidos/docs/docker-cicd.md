# Deploy local — simulação CI/CD no Docker Desktop

Scripts para subir **toda a stack** (infraestrutura + 4 microsserviços) no Docker Desktop, simulando as fases de um pipeline CI/CD.

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) em execução
- Java 21 e Maven 3.9+ (para o build local)
- Portas livres: `3306`, `6379`, `8080-8084`, `9000`, `9092`, `29092`, `2181`, `5540`

## Arquivos

| Arquivo | Descrição |
|---------|-----------|
| `deploy-docker-desktop.ps1` | Script principal (Windows / PowerShell) |
| `deploy-docker-desktop.sh` | Script principal (Linux / macOS / Git Bash) |
| `docker-compose.apps.yml` | Definição Docker dos 4 microsserviços |

A infraestrutura base continua em `infraestrutura/docker-compose.yml` (MySQL, Kafka, Zookeeper, Redis, Kafka UI, Redis Insight, SonarQube).

## Pipeline simulado

```
┌─────────────┐    ┌──────────────┐    ┌─────────────────┐    ┌──────────────────┐
│ Maven Build │ -> │ Docker Build │ -> │ Infra + Topics  │ -> │ Deploy Apps      │
│ (CI)        │    │ (CI)         │    │ (CD)            │    │ + Health Check   │
└─────────────┘    └──────────────┘    └─────────────────┘    └──────────────────┘
```

### Fase CI

1. `mvn clean verify` (ou `package -DskipTests` com flag)
2. `docker compose build` das imagens:
   - `kafka-pedidos/clientes-service:1.0.0-SNAPSHOT`
   - `kafka-pedidos/produtos-service:1.0.0-SNAPSHOT`
   - `kafka-pedidos/pedidos-service:1.0.0-SNAPSHOT`
   - `kafka-pedidos/broker-service:1.0.0-SNAPSHOT`

### Fase CD

1. Sobe MySQL, Zookeeper, Kafka, Redis, Kafka UI, Redis Insight e SonarQube
2. Aguarda healthchecks e cria tópicos Kafka
3. Sobe os 4 microsserviços na rede `kafka-pedidos`
4. Valida `/actuator/health` de cada API

## Uso

### Windows (PowerShell)

```powershell
cd docs
.\deploy-docker-desktop.ps1
```

Opções:

```powershell
.\deploy-docker-desktop.ps1 -SkipTests      # build mais rápido
.\deploy-docker-desktop.ps1 -SkipMaven      # usa JARs já compilados
.\deploy-docker-desktop.ps1 -InfraOnly      # só infraestrutura
.\deploy-docker-desktop.ps1 -AppsOnly       # só apps (infra já rodando)
.\deploy-docker-desktop.ps1 -NoCache        # rebuild Docker sem cache
```

### Linux / macOS

```bash
cd docs
chmod +x deploy-docker-desktop.sh
./deploy-docker-desktop.sh
```

```bash
./deploy-docker-desktop.sh --skip-tests
./deploy-docker-desktop.sh --infra-only
./deploy-docker-desktop.sh --apps-only
```

## URLs após o deploy

| Serviço | URL |
|---------|-----|
| clientes-service | http://localhost:8081/swagger-ui.html |
| produtos-service | http://localhost:8082/swagger-ui.html |
| pedidos-service | http://localhost:8083/swagger-ui.html |
| broker-service | http://localhost:8084/actuator/health |
| Kafka UI | http://localhost:8080 |
| Redis Insight | http://localhost:5540 |
| SonarQube | http://localhost:9000 |

## Variáveis de ambiente nos containers

Os microsserviços recebem overrides para a rede Docker:

| Variável | Valor no container |
|----------|-------------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql:3306/kafka_pedidos?...` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` |
| `SPRING_DATA_REDIS_HOST` | `redis` (produtos e pedidos) |

No host (Postman, IDE), continue usando `localhost:3306`, `localhost:29092` e `localhost:6379`.

## Comandos úteis

```bash
# Status de todos os containers
docker compose -f infraestrutura/docker-compose.yml -f docs/docker-compose.apps.yml ps

# Logs de um microsserviço
docker compose -f infraestrutura/docker-compose.yml -f docs/docker-compose.apps.yml logs -f clientes-service

# Parar tudo
scripts/stop.bat        # Windows
./scripts/stop.sh      # Linux/macOS
```

## Parar apenas os microsserviços

```bash
docker compose -f infraestrutura/docker-compose.yml -f docs/docker-compose.apps.yml stop \
  clientes-service produtos-service pedidos-service broker-service
```

## Troubleshooting

| Problema | Solução |
|----------|---------|
| `Docker Desktop nao esta em execucao` | Abra o Docker Desktop e aguarde ficar pronto |
| `JAR nao encontrado` | Execute sem `-SkipMaven` ou rode `mvn clean package` |
| Health check timeout | Verifique logs: `docker compose ... logs <servico>` |
| Porta em uso | Pare containers antigos com `scripts/stop.bat` |
| Flyway / migration | Primeiro serviço a subir aplica migrations; aguarde MySQL healthy |

## Integração com Postman

Após o deploy, importe a coleção em `docs/postman/` e use o environment `kafka-pedidos-local`. As URLs `8081-8083` apontam para os containers.
