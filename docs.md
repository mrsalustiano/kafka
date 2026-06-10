Criei na pasta docs um conjunto para subir toda a stack no Docker Desktop simulando CI/CD.

Arquivos criados
Arquivo	Função
docs/deploy-docker-desktop.ps1
Script principal (Windows / PowerShell)
docs/deploy-docker-desktop.sh
Script para Linux / macOS / Git Bash
docs/docker-compose.apps.yml
Definição Docker dos 4 microsserviços
docs/docker-cicd.md
Documentação completa
Pipeline simulado
CI — Maven: mvn clean verify (ou package -DskipTests)
CI — Docker: build das 4 imagens (clientes, produtos, pedidos, broker)
CD — Infra: MySQL, Kafka, Zookeeper, Redis, Kafka UI, Redis Insight, SonarQube
CD — Init: criação dos tópicos Kafka
CD — Apps: deploy dos microsserviços com health check
Como usar (Windows)
cd d:\DEV\workspace\kafka\kafka-pedidos\docs
.\deploy-docker-desktop.ps1
Opções úteis:

.\deploy-docker-desktop.ps1 -SkipTests    # build mais rápido
.\deploy-docker-desktop.ps1 -InfraOnly    # só infraestrutura
.\deploy-docker-desktop.ps1 -AppsOnly     # só apps (infra já rodando)
URLs após o deploy
clientes: http://localhost:8081/swagger-ui.html
produtos: http://localhost:8082/swagger-ui.html
pedidos: http://localhost:8083/swagger-ui.html
broker: http://localhost:8084/actuator/health
Kafka UI: http://localhost:8080
Redis Insight: http://localhost:5540
SonarQube: http://localhost:9000
Os containers usam a rede Docker (mysql, kafka:9092, redis); no host você continua usando localhost nas portas expostas.

Para parar tudo: scripts\stop.bat na raiz do projeto.