# Docker e Infraestrutura

## Localização

```
infraestrutura/
├── docker-compose.yml
├── kafka/create-topics.sh
├── mysql/flyway/          # migrations canônicas V1–V7
├── redis/redis.conf
└── sonarqube/conf/sonar.properties
```

## Subir ambiente

### Apenas infraestrutura

```bash
# Windows
scripts\start.bat

# Linux/macOS
./scripts/start.sh

# Ou diretamente
cd infraestrutura
docker compose up -d
```

O script `start` aguarda o Kafka ficar saudável e executa `create-topics.sh`.

### Stack completa (infra + microsserviços) — simulação CI/CD

Sobe MySQL, Kafka, Redis, SonarQube **e** os 4 microsserviços em containers:

```powershell
# Windows
cd docs
.\deploy-docker-desktop.ps1
```

```bash
# Linux/macOS
cd docs
./deploy-docker-desktop.sh
```

Detalhes, opções e troubleshooting: [docker-cicd.md](docker-cicd.md).

## Containers

| Container | Imagem | Porta | Healthcheck |
|-----------|--------|-------|-------------|
| mysql | mysql:8.0 | 3306 | ✓ |
| zookeeper | cp-zookeeper:7.6.1 | 2181 | ✓ |
| kafka | cp-kafka:7.6.1 | 9092 / 29092 | ✓ |
| redis | redis:7.2-alpine | 6379 | ✓ |
| kafka-ui | kafka-ui:v0.7.2 | 8080 | ✓ |
| redis-insight | redisinsight:2.54.0 | 5540 | ✓ |
| sonarqube | sonarqube:10.4.1-community | 9000 | ✓ |

Rede: `kafka-pedidos`. Volumes persistentes nomeados para cada serviço.

## MySQL

- Banco: `kafka_pedidos`
- Usuário/senha: `kafka_user` / `kafka_pass`
- Charset: `utf8mb4`

## Kafka

- Listener interno: `kafka:9092`
- Listener host: `localhost:29092`
- Auto-create topics: **desabilitado** — tópicos criados via script

### Tópicos

| Tópico | Uso |
|--------|-----|
| client-create | Criação de cliente |
| client-response | Resposta do broker |
| pedido-create | Criação de pedido |
| pedido-response | Atualização de status |
| client-create-dlt | DLT consumer broker |
| client-response-dlt | DLT consumer clientes |
| pedido-create-dlt | DLT producer pedidos |
| pedido-response-dlt | DLT consumer pedidos |

## Redis

- Persistência AOF
- `maxmemory` 256MB
- Chaves: `produto:{id}`, `produto:all`

## URLs de acesso

- Kafka UI: http://localhost:8080
- Redis Insight: http://localhost:5540
- SonarQube: http://localhost:9000 (admin/admin)

## Dockerfile dos serviços

Cada microsserviço possui `Dockerfile` baseado em `eclipse-temurin:21-jre-alpine`.

```bash
cd servicos/clientes-service
docker build -t clientes-service .
```
