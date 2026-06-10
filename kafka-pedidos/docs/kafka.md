# Kafka

## Configuração

Cada serviço com Kafka define em `application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:29092
    consumer:
      group-id: {nome}-service
```

Propriedades customizadas em `app.kafka`:

- `timeout-ms`: timeout de producer/admin
- `retry.max-attempts`, `initial-delay-ms`, `multiplier`
- `topics.*`: nomes dos tópicos e DLT

## Produtores

| Serviço | Classe | Tópico |
|---------|--------|--------|
| clientes-service | `ClienteCreateProducer` | client-create |
| broker-service | `ClienteResponseProducer` | client-response |
| pedidos-service | `PedidoCreateProducer` | pedido-create |

Comportamento:

- Header `X-Correlation-Id` propagado quando presente
- `@Retryable` em falhas de publicação
- Timeout configurável via `app.kafka.timeout-ms`
- Exceções: `TimeoutException`, `KafkaPublishException`

## Consumidores

| Serviço | Classe | Tópico |
|---------|--------|--------|
| broker-service | `ClienteCreateConsumer` | client-create |
| clientes-service | `ClienteResponseConsumer` | client-response |
| pedidos-service | `PedidoResponseConsumer` | pedido-response |

Comportamento:

- Idempotência via `IdempotenciaService`
- `CorrelationId` do header Kafka
- `finally` limpa ThreadLocal e MDC
- Exceção: `KafkaConsumeException` (aciona retry/DLT)

## DLT e Retry

`KafkaConfig.kafkaErrorHandler()` configura:

- `DeadLetterPublishingRecoverer` → tópico DLT correspondente
- `ExponentialBackOff` com intervalo inicial, multiplicador e max elapsed time

## Health

`KafkaHealthIndicator` expõe status do cluster no Actuator (`/actuator/health`).
