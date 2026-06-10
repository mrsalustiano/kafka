# JaCoCo e SonarQube

## JaCoCo

Configurado em cada `pom.xml` de serviço:

- `prepare-agent` — instrumentação em testes
- `report` — relatório HTML/XML na fase `test`
- `check` — gate de **95%** cobertura de linhas na fase `verify`

### Executar

```bash
# Por serviço
cd servicos/clientes-service
mvn test verify

# Todos os serviços (script)
powershell -ExecutionPolicy Bypass -File scripts/coverage-report.ps1
```

### Relatórios

```
servicos/{service}/target/site/jacoco/index.html
servicos/{service}/target/site/jacoco/jacoco.xml   # SonarQube
```

### Exclusões

- `*ServiceApplication.class` (classe main)

### Cobertura atual (após revisão)

| Serviço | Linhas | Branches |
|---------|--------|----------|
| clientes-service | 100% | ~97% |
| produtos-service | ~99% | 95% |
| pedidos-service | 100% | ~96% |
| broker-service | 100% | 100% |

## SonarQube

### Servidor

```bash
cd infraestrutura
docker compose up -d sonarqube
```

Acesso: http://localhost:9000

### Análise por serviço

Cada serviço possui `sonar-project.properties`:

```
servicos/clientes-service/sonar-project.properties
servicos/produtos-service/sonar-project.properties
servicos/pedidos-service/sonar-project.properties
servicos/broker-service/sonar-project.properties
```

### Executar análise

```bash
cd servicos/clientes-service
mvn test verify
sonar-scanner
```

Requisitos: SonarScanner CLI instalado, servidor SonarQube rodando, token configurado.

### Integração JaCoCo

```properties
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.java.source=21
```
