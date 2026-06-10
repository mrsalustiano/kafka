# Qualidade de Código

## SOLID

| Princípio | Aplicação no projeto |
|-----------|---------------------|
| **S** — Single Responsibility | Controllers (HTTP), Services (regra), Repositories (dados), Producers/Consumers (Kafka) |
| **O** — Open/Closed | Extensão via novos handlers de exceção sem alterar existentes |
| **L** — Liskov Substitution | Exceções de domínio estendem `RuntimeException` de forma consistente |
| **I** — Interface Segregation | Repositories JDBI focados por entidade |
| **D** — Dependency Inversion | Injeção via construtor (`@RequiredArgsConstructor`) |

### Oportunidade de melhoria

Extrair módulo `common` para classes duplicadas (CorrelationId, exceções, KafkaConfig base). Mantido separado por simplicidade do estudo.

## Clean Code

- Nomes em português para domínio de negócio
- Records para DTOs imutáveis
- Logs estruturados com `correlationId` no MDC
- Validação Bean Validation nos controllers
- Sem lógica de negócio nos controllers

## Duplicação de código

Classes repetidas entre serviços:

- `CorrelationIdFilter`, `CorrelationIdUtil`
- Pacote `exception` completo
- `KafkaConfig` (variantes por serviço)
- `AuditoriaService`, `IdempotenciaService`
- `PageResponseUtil`, `GlobalExceptionHandler`

**Decisão**: duplicação aceita em projeto de estudo; documentada para futura extração em biblioteca compartilhada.

## Concorrência

| Área | Comportamento |
|------|---------------|
| CorrelationId | `ThreadLocal` — isolado por thread de request/consumer |
| Kafka consumers | `finally` garante limpeza de ThreadLocal/MDC |
| Idempotência | Check-then-act com `event_id` UNIQUE; operações de negócio idempotentes (upsert) |
| Cache Redis | Leitura/escrita sem lock distribuído; TTL e scheduler mitigam inconsistência |

### Nota sobre idempotência

Em cenário de múltiplas partições/consumidores, existe janela entre `jaProcessado()` e `registrar()`. Mitigado por:

- Constraint UNIQUE em `event_id`
- Operações idempotentes (`upsert` de status)
- Retry do Kafka com DLT para falhas reais

## Tratamento de erros

- `GlobalExceptionHandler` (`@RestControllerAdvice`) em serviços REST
- `ErrorResponse` padronizado com `correlationId`
- Exceções de domínio por tipo de falha
- Kafka: `KafkaPublishException`, `KafkaConsumeException`
- Infra: `DatabaseException`, `RedisException`, `TimeoutException`
