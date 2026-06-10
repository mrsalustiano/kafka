# pedidos-service

Microsservico de gerenciamento de pedidos do projeto **kafka-pedidos**.

## Execucao

```bash
# Subir infraestrutura
../../scripts/start.bat

# Executar servico
mvn spring-boot:run
```

Porta: **8083**

## API

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/api/v1/pedidos` | Criar pedido (valida cliente/produto, publica `pedido-create`) |
| GET | `/api/v1/pedidos/{id}` | Buscar pedido por ID |
| GET | `/api/v1/pedidos` | Listar paginado (`page`, `size`, `sort`) |
| PUT | `/api/v1/pedidos/{id}` | Atualizar pedido |
| PATCH | `/api/v1/pedidos/{id}/status` | Atualizar status do pedido |

## Kafka

- Topico de publicacao: `pedido-create`
- Topico de consumo: `pedido-response`
- DLT: `pedido-create-dlt`, `pedido-response-dlt`
- Idempotencia por `eventId` na tabela `mensagens_processadas`

## Redis

- Consulta de produto: `produto:{id}` (Redis primeiro, fallback MySQL, atualiza cache)

## Documentacao

- Swagger: http://localhost:8083/swagger-ui.html
- Actuator: http://localhost:8083/actuator/health

## Testes e cobertura

```bash
mvn test verify
```

Cobertura minima JaCoCo: **95%**
