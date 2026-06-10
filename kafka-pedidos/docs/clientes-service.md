# clientes-service

Microsserviço de gerenciamento de clientes com integração Kafka.

## Informações gerais

| Item | Valor |
|------|-------|
| Porta | 8081 |
| Package | `com.empresa.clientes` |
| Banco | MySQL via JDBI |
| Kafka | Producer + Consumer |
| Redis | Não utilizado |

## API REST

| Método | Endpoint | Status | Descrição |
|--------|----------|--------|-----------|
| POST | `/api/v1/clientes` | 202 | Solicita criação via Kafka |
| GET | `/api/v1/clientes/cpf/{cpf}` | 200 | Busca por CPF (com ou sem máscara) |
| GET | `/api/v1/clientes/{id}` | 200 | Busca por ID |
| GET | `/api/v1/clientes` | 200 | Lista paginada |
| PUT | `/api/v1/clientes/{id}` | 200 | Atualiza cliente |
| DELETE | `/api/v1/clientes/{id}` | 204 | Exclusão lógica |
| GET | `/api/v1/clientes/status/{id}` | 200 | Status de criação |

### Campo CPF

- Obrigatório em criação e atualização (`POST` / `PUT`)
- Armazenado como string de 11 dígitos (sem pontuação)
- Único entre clientes ativos (`ativo = 'S'`)
- Validado pelo algoritmo de dígitos verificadores
- Aceita entrada com ou sem máscara (`529.982.247-25` ou `52998224725`)

### Parâmetros de listagem

- `page` (default 0)
- `size` (default 10)
- `sort` (ex: `nome,asc`, `cpf,desc`)

## Kafka

- **Publica**: `client-create` (inclui campo `cpf` normalizado)
- **Consome**: `client-response`
- **DLT**: `client-response-dlt`

## Execução

```bash
cd servicos/clientes-service
mvn spring-boot:run
```

## Links

- Swagger: http://localhost:8081/swagger-ui.html
- Health: http://localhost:8081/actuator/health

## Testes

```bash
mvn test verify
```
