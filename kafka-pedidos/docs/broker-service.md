# broker-service

Microsserviço intermediário Kafka para processamento de criação de clientes.

## Informações gerais

| Item | Valor |
|------|-------|
| Porta | 8084 |
| Package | `com.empresa.broker` |
| Banco | MySQL via JDBI |
| Kafka | Consumer + Producer |
| REST API | Não possui controllers |

## Papel

1. Consome `client-create`
2. Valida idempotência (`event_id`)
3. Valida CPF (dígitos verificadores) e unicidade entre clientes ativos
4. Persiste cliente no banco (CPF normalizado, 11 dígitos)
5. Publica `client-response`

## Kafka

- **Consome**: `client-create`
- **Publica**: `client-response`
- **DLT**: `client-create-dlt`

## Endpoints disponíveis

| Endpoint | Descrição |
|----------|-----------|
| `/actuator/health` | Health check |
| `/swagger-ui.html` | Swagger (sem controllers REST) |

## Execução

```bash
cd servicos/broker-service
mvn spring-boot:run
```

## Testes

```bash
mvn test verify
```

Cobertura JaCoCo ≥ 95%.
