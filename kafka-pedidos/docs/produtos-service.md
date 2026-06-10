# produtos-service

Microsserviço de catálogo de produtos com cache Redis.

## Informações gerais

| Item | Valor |
|------|-------|
| Porta | 8082 |
| Package | `com.empresa.produtos` |
| Banco | MySQL via JDBI |
| Redis | Cache de produtos |
| Kafka | Apenas health (sem producer/consumer) |

## API REST

| Método | Endpoint | Status | Descrição |
|--------|----------|--------|-----------|
| POST | `/api/v1/produtos` | 201 | Cria produto |
| GET | `/api/v1/produtos/{id}` | 200 | Busca por ID |
| GET | `/api/v1/produtos` | 200 | Lista paginada |
| PUT | `/api/v1/produtos/{id}` | 200 | Atualiza produto |
| DELETE | `/api/v1/produtos/{id}` | 204 | Exclusão lógica |

## Cache Redis

- Escreve `produto:{id}` e `produto:all` após CRUD
- Scheduler recarrega cache periodicamente
- `@Retryable` em falhas Redis

## Execução

```bash
cd servicos/produtos-service
mvn spring-boot:run
```

## Links

- Swagger: http://localhost:8082/swagger-ui.html
- Health: http://localhost:8082/actuator/health
