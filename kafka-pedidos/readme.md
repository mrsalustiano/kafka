# kafka Project

## **Estrutura gerada**

kafka-pedidos/

├── pom.xml                          (agregador)

├── infraestrutura/

├── docs/

├── scripts/

└── servicos/

```
├── clientes-service/

│   ├── pom.xml

│   ├── Dockerfile

│   └── src/

│       ├── main/java/

│       ├── main/resources/

│       └── test/java/

├── produtos-service/            (mesma estrutura)

├── pedidos-service/             (mesma estrutura)

└── broker-service/              (mesma estrutura)
```

## **Arquivos criados**


| **Item**                 | **Detalhe**                                                               |
| ------------------------ | ------------------------------------------------------------------------- |
| **pom.xml raiz**         | `packaging=pom`, Java 21, Spring Boot 3.4.0 BOM, 4 módulos em `servicos/` |
| **pom.xml dos serviços** | Herdam do pai (`com.empresa:kafka-pedidos:1.0.0-SNAPSHOT`)                |
| **Dockerfile**           | Template base com `eclipse-temurin:21-jre-alpine`                         |
| **Diretórios vazios**    | `.gitkeep` em `infraestrutura/`, `docs/`, `scripts/` e pastas `src/`      |


## **Validação**

`mvn validate` executou com sucesso na raiz do projeto.

Nenhuma regra de negócio, classe Java, `application.yml` ou dependências dos serviços foram adicionados — apenas a estrutura inicial solicitada.

## **Containers (rede** `kafka-pedidos`**)**


| **Container** | **Imagem**                      | **Porta**    |
| ------------- | ------------------------------- | ------------ |
| mysql         | mysql:8.0                       | 3306         |
| zookeeper     | confluentinc/cp-zookeeper:7.6.1 | 2181         |
| kafka         | confluentinc/cp-kafka:7.6.1     | 9092 / 29092 |
| redis         | redis:7.2-alpine                | 6379         |
| kafka-ui      | provectuslabs/kafka-ui:v0.7.2   | 8080         |
| redis-insight | redis/redisinsight:2.54.0       | 5540         |
| sonarqube     | sonarqube:10.4.1-community      | 9000         |


Todos possuem **healthcheck**, **volumes persistentes nomeados** e estão na rede `kafka-pedidos`.

## **Configurações**

- **MySQL**: banco `kafka_pedidos`, usuário `kafka_user` / `kafka_pass`, charset `utf8mb4`
- **Kafka**: listeners interno (`kafka:9092`) e host (`localhost:29092`); tópicos criados via script:
  - `client-create`, `client-response`, `pedido-create`, `pedido-response`, `client-create-dlt`, `pedido-create-dlt`
- **Redis**: persistência AOF, `maxmemory` 256MB, chaves documentadas (`produto:{id}`, `produto:all`)
- **SonarQube**: projeto `kafka-pedidos`, Java 21, integração JaCoCo

## **Como usar**

*# Linux/macOS*

./scripts/[start.sh](http://start.sh)

./scripts/[stop.sh](http://stop.sh)

./scripts/[reset.sh](http://reset.sh)

*# Windows*

scripts\start.bat

scripts\stop.bat

scripts\reset.bat

Ou diretamente:

cd infraestrutura

docker compose up -d

O `start` e o `reset` aguardam o Kafka ficar saudável e executam a criação dos tópicos automaticamente.

## **URLs de acesso**

- Kafka UI: [http://localhost:8080](http://localhost:8080)
- Redis Insight: [http://localhost:5540](http://localhost:5540)
- SonarQube: [http://localhost:9000](http://localhost:9000) (login padrão: `admin` / `admin`)

Migrations Flyway criadas conforme a skill **kafka-pedidos**, compatíveis com **MySQL 8**.

## **Localização**

infraestrutura/mysql/flyway/

├── V1__create_clientes.sql

├── V2__create_produtos.sql

├── V3__create_pedidos.sql

├── V4__create_pagamentos.sql

├── V5__create_auditoria.sql

├── V6__create_mensagens_processadas.sql

├── V7__create_cliente_status.sql

└── V8__add_cpf_clientes.sql

## **Resumo do schema**


| **Migration** | **Tabela**              | **Relacionamentos**                    |
| ------------- | ----------------------- | -------------------------------------- |
| V1            | `clientes`              | —                                      |
| V2            | `produtos`              | —                                      |
| V3            | `pedidos`               | FK → `clientes`, `produtos`            |
| V4            | `pagamentos`            | FK → `pedidos` (1:1)                   |
| V5            | `auditoria`             | —                                      |
| V6            | `mensagens_processadas` | `event_id` UNIQUE (idempotência Kafka) |
| V7            | `cliente_status`        | Status assíncrono de criação           |
| V8            | `clientes`              | Coluna `cpf` VARCHAR(11) UNIQUE        |


## **Constraints aplicadas**

- `clientes` **/** `produtos`: `ativo IN ('S', 'N')`
- `produtos`: `valor > 0`
- `pedidos`: `quantidade > 0`, `valor_unitario > 0`, status enum da skill
- `pagamentos`: `tipo_pagamento IN ('CC', 'DEBITO', 'BOLETO', 'PIX')`
- **Foreign keys**: `ON DELETE RESTRICT` (pedidos não podem ser excluídos em cascata)
- **Engine**: `InnoDB`, charset `utf8mb4_unicode_ci`

## **Índices recomendados**

- **clientes**: `ativo`, `cpf` (UNIQUE), `email`, `nome`, `data_criacao`
- **produtos**: `ativo`, `descricao`, `data_criacao`
- **pedidos**: `codigo_cliente`, `codigo_produto`, `status`, `data_pedido`, `data_criacao`, composto `(codigo_cliente, status)`
- **pagamentos**: `tipo_pagamento`, `efetivado`
- **auditoria**: `data_evento`, `operacao`, `entidade`, `id_entidade`, `correlation_id`, composto `(entidade, id_entidade)`
- **mensagens_processadas**: `data_processamento` (+ UNIQUE em `event_id`)

## **Execução manual (Flyway CLI ou MySQL)**

Com a infraestrutura rodando:

cd infraestrutura

docker compose up -d mysql

*# via cliente MySQL*

docker exec -i kafka-pedidos-mysql mysql -ukafka_user -pkafka_pass kafka_pedidos \

< mysql/flyway/V1__create_clientes.sql

*# ... repetir V2 a V6 na ordem*

Quando os microsserviços forem implementados, aponte o Flyway para `classpath:db/migration` (copiando estes arquivos) ou configure `spring.flyway.locations=filesystem:../../infraestrutura/mysql/flyway` no serviço que executará as migrations.