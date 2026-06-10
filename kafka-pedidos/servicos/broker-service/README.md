# broker-service

Microsservico broker do projeto **kafka-pedidos**. Consome eventos de criacao de cliente, persiste no MySQL e publica a resposta.

## Execucao

```bash
# Subir infraestrutura
../../scripts/start.bat

# Executar servico
mvn spring-boot:run
```

Porta: **8084**

## Kafka

| Direcao | Topico | Descricao |
|---------|--------|-----------|
| Consumo | `client-create` | Recebe solicitacao de criacao de cliente |
| Publicacao | `client-response` | Retorna codigo e status do cliente criado |
| DLT | `client-create-dlt` | Mensagens com falha apos retry |

## Funcionalidades

- Persistencia de cliente via JDBI
- Idempotencia por `eventId` na tabela `mensagens_processadas`
- Retry e timeout no producer
- DLQ e retry exponencial no consumer
- Auditoria na tabela `auditoria`
- CorrelationId propagado via header Kafka `X-Correlation-Id`

## Documentacao

- Swagger: http://localhost:8084/swagger-ui.html
- Actuator: http://localhost:8084/actuator/health

## Testes e cobertura

```bash
mvn test verify
```

Cobertura minima JaCoCo: **95%**
