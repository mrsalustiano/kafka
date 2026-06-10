# Flyway e JDBI

## Flyway

### Localização

- Canônico: `infraestrutura/mysql/flyway/`
- Runtime: `servicos/*/src/main/resources/db/migration/`

### Migrations

| Versão | Tabela |
|--------|--------|
| V1 | clientes |
| V2 | produtos |
| V3 | pedidos |
| V4 | pagamentos |
| V5 | auditoria |
| V6 | mensagens_processadas |
| V7 | cliente_status |
| V8 | clientes (coluna `cpf`) |

### Configuração

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

Todos os serviços compartilham o banco `kafka_pedidos`. O histórico Flyway é único — a primeira instância a subir aplica as migrations pendentes.

## JDBI

### Configuração

`JdbiConfig` registra beans `Jdbi` e repositórios via `RepositoryConfig` (`onDemand`).

### Padrão Repository

```java
public interface ClienteRepository {
    @SqlQuery("SELECT ...")
    Optional<Cliente> findById(@Bind("id") Long id);

    @SqlUpdate("INSERT ...")
    @GetGeneratedKeys
    long insert(@BindBean Cliente cliente);
}
```

### Transações

`@Transactional` do Spring em métodos de service que alteram múltiplas tabelas.

### Tratamento de erros

Falhas de banco são encapsuladas em `DatabaseException` nos services.
