# clientes-service

Microsservico de gerenciamento de clientes do projeto **kafka-pedidos**.

## Execucao

```bash
# Subir infraestrutura
../../scripts/start.bat

# Executar servico
mvn spring-boot:run
```

Porta: **8081**

## API

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/api/v1/clientes` | Solicitar criacao de cliente via Kafka (retorna 202) |
| GET | `/api/v1/clientes/{id}` | Buscar cliente por ID |
| GET | `/api/v1/clientes` | Listar paginado (`page`, `size`, `sort`) |
| PUT | `/api/v1/clientes/{id}` | Atualizar cliente |
| DELETE | `/api/v1/clientes/{id}` | Exclusao logica (`ativo = N`) |
| GET | `/api/v1/clientes/status/{id}` | Consultar status de criacao do cliente |

## Kafka

- Topico de publicacao: `client-create`
- Topico de consumo: `client-response`
- DLT: `client-create-dlt`, `client-response-dlt`
- Idempotencia por `eventId` na tabela `mensagens_processadas`

## Documentacao

- Swagger: http://localhost:8081/swagger-ui.html
- Actuator: http://localhost:8081/actuator/health

## Testes e cobertura

```bash
mvn test verify
```

Cobertura minima JaCoCo: **95%**
