# kafka Project



## **Estrutura gerada**

kafka-pedidos/

├── pom.xml                          (agregador)

├── infraestrutura/

├── docs/

├── scripts/

└── servicos/

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

