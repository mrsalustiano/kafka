# Redis

## Serviços que utilizam Redis

| Serviço | Uso |
|---------|-----|
| produtos-service | Escrita do cache (`ProdutoCacheService`) |
| pedidos-service | Leitura do cache (`ProdutoConsultaService`) |

`clientes-service` e `broker-service` **não** utilizam Redis.

## Chaves

| Chave | Conteúdo | TTL |
|-------|----------|-----|
| `produto:{id}` | JSON do produto | Configurável (`app.redis.ttl-minutes`) |
| `produto:all` | Lista completa de produtos ativos | Idem |

## Configuração

```yaml
app:
  redis:
    ttl-minutes: 60
    timeout-ms: 3000
    retry:
      max-attempts: 3
      initial-delay-ms: 100
      multiplier: 1.5
```

## Resiliência

- `@Retryable` + `@Recover` em operações de cache
- `isDisponivel()` verifica conectividade (PING)
- Fallback para MySQL em `pedidos-service` quando cache indisponível
- Exceção: `RedisException` → HTTP 503

## Scheduler

`ProdutoCacheScheduler` recarrega cache periodicamente em `produtos-service`.
