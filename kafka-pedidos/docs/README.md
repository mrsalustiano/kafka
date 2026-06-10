# Documentação kafka-pedidos

Índice da documentação técnica do projeto.

## Visão geral

| Documento | Conteúdo |
|-----------|----------|
| [arquitetura.md](arquitetura.md) | Visão arquitetural, fluxos e portas |
| [docker-infraestrutura.md](docker-infraestrutura.md) | Docker Compose, containers e scripts |
| [docker-cicd.md](docker-cicd.md) | Deploy completo no Docker Desktop (simulação CI/CD) |
| [kafka.md](kafka.md) | Tópicos, produtores, consumidores, DLT e retry |
| [redis.md](redis.md) | Cache de produtos e configuração |
| [flyway-jdbi.md](flyway-jdbi.md) | Migrations e acesso a dados |
| [jacoco-sonarqube.md](jacoco-sonarqube.md) | Cobertura de testes e análise estática |
| [qualidade-codigo.md](qualidade-codigo.md) | SOLID, Clean Code, duplicação e concorrência |

## Microsserviços

| Documento | Porta |
|-----------|-------|
| [clientes-service.md](clientes-service.md) | 8081 |
| [produtos-service.md](produtos-service.md) | 8082 |
| [pedidos-service.md](pedidos-service.md) | 8083 |
| [broker-service.md](broker-service.md) | 8084 |

## Testes locais

| Recurso | Descrição |
|---------|-----------|
| [postman/kafka-pedidos.postman_collection.json](postman/kafka-pedidos.postman_collection.json) | Coleção Postman para APIs REST |
| [postman/kafka-pedidos.postman_environment.json](postman/kafka-pedidos.postman_environment.json) | Variáveis de ambiente local |

## Relatório de revisão

| Documento | Conteúdo |
|-----------|----------|
| [RELATORIO-REVISAO.md](RELATORIO-REVISAO.md) | Revisão completa e ajustes realizados |
