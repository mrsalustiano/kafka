# produtos-service

Microsservico de gerenciamento de produtos do projeto **kafka-pedidos**.

## Execucao

```bash
# Subir infraestrutura
../../scripts/start.bat

# Executar servico
mvn spring-boot:run
```

Porta: **8082**

## API

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/api/v1/produtos` | Criar produto |
| GET | `/api/v1/produtos/{id}` | Buscar por ID (Redis -> MySQL) |
| GET | `/api/v1/produtos` | Listar paginado (`page`, `size`, `sort`) |
| PUT | `/api/v1/produtos/{id}` | Atualizar produto |
| DELETE | `/api/v1/produtos/{id}` | Exclusao logica (`ativo = N`) |

## Cache Redis

- Chaves: `produto:{id}`, `produto:all`
- TTL: 60 minutos
- Carga inicial ao subir a aplicacao
- Scheduler configuravel: `app.cache.produtos.refresh-minutes` (padrao 30)

## Documentacao

- Swagger: http://localhost:8082/swagger-ui.html
- Actuator: http://localhost:8082/actuator/health

## Testes e cobertura

```bash
mvn test verify
```

Cobertura minima JaCoCo: **95%**
