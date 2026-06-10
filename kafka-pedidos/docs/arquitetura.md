# Arquitetura

## Stack tecnológica

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| Java | 21 | Linguagem base |
| Spring Boot | 3.4.0 | Framework dos microsserviços |
| JDBI | 3.45.4 | Acesso a dados (SQL Object) |
| Flyway | (BOM Spring Boot) | Migrations de banco |
| Kafka | 7.6.1 (Confluent) | Mensageria assíncrona |
| Redis | 7.2 | Cache de produtos |
| MySQL | 8.0 | Banco relacional compartilhado |
| JaCoCo | 0.8.12 | Cobertura de testes (≥ 95%) |
| SonarQube | 10.4.1 | Análise estática de código |

## Microsserviços

```
                    ┌─────────────────┐
                    │ clientes-service│ :8081
                    │  REST + Kafka   │
                    └────────┬────────┘
                             │ client-create
                             ▼
                    ┌─────────────────┐
                    │  broker-service │ :8084
                    │  Kafka only     │
                    └────────┬────────┘
                             │ client-response
                             ▼
                    ┌─────────────────┐
                    │ clientes-service│ (consumer)
                    └─────────────────┘

┌─────────────────┐     pedido-create      ┌─────────────────┐
│ pedidos-service │ ─────────────────────► │  (futuro broker)│
│  REST + Kafka   │                        └─────────────────┘
│  Redis cache    │
└─────────────────┘

┌─────────────────┐
│ produtos-service│ :8082 — REST + Redis (sem Kafka consumer)
└─────────────────┘
```

## Fluxo de criação de cliente

1. `POST /api/v1/clientes` → publica `client-create` (HTTP 202)
2. `broker-service` consome, persiste cliente, publica `client-response`
3. `clientes-service` consome `client-response`, atualiza `cliente_status`
4. `GET /api/v1/clientes/status/{id}` consulta status

## Fluxo de pedido

1. `POST /api/v1/pedidos` valida cliente/produto ativos
2. Consulta produto no Redis (fallback MySQL)
3. Persiste pedido e publica `pedido-create`
4. Consumer `pedido-response` atualiza status (quando disponível)

## Padrões transversais

- **CorrelationId**: header `X-Correlation-Id` em HTTP, logs (MDC), Kafka e auditoria
- **Idempotência**: tabela `mensagens_processadas` com `event_id` UNIQUE
- **Auditoria**: tabela `auditoria` em todas as operações relevantes
- **DLT**: Dead Letter Topic com retry exponencial (`ExponentialBackOff`)
- **Exceções**: `GlobalExceptionHandler` com `ErrorResponse` padronizado

## Portas

| Componente | Porta |
|------------|-------|
| clientes-service | 8081 |
| produtos-service | 8082 |
| pedidos-service | 8083 |
| broker-service | 8084 |
| Kafka UI | 8080 |
| MySQL | 3306 |
| Kafka (host) | 29092 |
| Redis | 6379 |
| SonarQube | 9000 |
