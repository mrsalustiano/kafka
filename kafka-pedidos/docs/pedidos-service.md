# pedidos-service

Microsserviço de pedidos com validação de cliente/produto e integração Kafka.

## Informações gerais

| Item | Valor |
|------|-------|
| Porta | 8083 |
| Package | `com.empresa.pedidos` |
| Banco | MySQL via JDBI |
| Redis | Leitura de cache de produtos |
| Kafka | Producer + Consumer |

## API REST

| Método | Endpoint | Status | Descrição |
|--------|----------|--------|-----------|
| POST | `/api/v1/pedidos` | 201 | Cria pedido e publica Kafka |
| GET | `/api/v1/pedidos/{id}` | 200 | Busca por ID |
| GET | `/api/v1/pedidos` | 200 | Lista paginada |
| PUT | `/api/v1/pedidos/{id}` | 200 | Atualiza pedido |
| PATCH | `/api/v1/pedidos/{id}/status` | 200 | Atualiza status |

**Nota**: não possui endpoint DELETE (regra de negócio).

## Status válidos

`EM_PROCESSAMENTO`, `FINALIZADO`, `EM_SEPARACAO`, `ENTREGUE`, `CANCELADO`, `EM_ROTA_DE_ENTREGA`

## Kafka

- **Publica**: `pedido-create`
- **Consome**: `pedido-response`
- **DLT**: `pedido-response-dlt`

## Validações

- Cliente ativo no MySQL
- Produto ativo via Redis → fallback MySQL

## Execução

```bash
cd servicos/pedidos-service
mvn spring-boot:run
```

## Links

- Swagger: http://localhost:8083/swagger-ui.html
- Health: http://localhost:8083/actuator/health
