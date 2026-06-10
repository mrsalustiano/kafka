#!/usr/bin/env bash
# Simula um pipeline CI/CD local no Docker Desktop para o projeto kafka-pedidos.
#
# Uso:
#   ./deploy-docker-desktop.sh
#   ./deploy-docker-desktop.sh --skip-tests
#   ./deploy-docker-desktop.sh --infra-only
#   ./deploy-docker-desktop.sh --apps-only
#   ./deploy-docker-desktop.sh --no-cache
set -euo pipefail

DOCS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${DOCS_DIR}/.." && pwd)"
INFRA_DIR="${PROJECT_ROOT}/infraestrutura"
INFRA_COMPOSE="${INFRA_DIR}/docker-compose.yml"
APPS_COMPOSE="${DOCS_DIR}/docker-compose.apps.yml"
APP_VERSION="1.0.0-SNAPSHOT"

SERVICES=(
  clientes-service
  produtos-service
  pedidos-service
  broker-service
)

SKIP_TESTS=false
SKIP_MAVEN=false
INFRA_ONLY=false
APPS_ONLY=false
NO_CACHE=false

usage() {
  cat <<'EOF'
Uso: ./deploy-docker-desktop.sh [opcoes]

Opcoes:
  --skip-tests   Build Maven sem testes (mvn package -DskipTests)
  --skip-maven   Pula o build Maven (usa JARs em target/)
  --infra-only   Sobe apenas infraestrutura
  --apps-only    Sobe apenas microsservicos (infra ja deve estar rodando)
  --no-cache     Rebuild Docker sem cache
  -h, --help     Exibe esta ajuda
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-tests) SKIP_TESTS=true ;;
    --skip-maven) SKIP_MAVEN=true ;;
    --infra-only) INFRA_ONLY=true ;;
    --apps-only) APPS_ONLY=true ;;
    --no-cache) NO_CACHE=true ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Opcao desconhecida: $1" >&2; usage; exit 1 ;;
  esac
  shift
done

phase() {
  echo ""
  echo "==> $1"
}

assert_docker() {
  phase "Verificando Docker Desktop"
  docker info >/dev/null
  echo "Docker OK"
}

maven_build() {
  if [[ "${SKIP_MAVEN}" == "true" ]]; then
    phase "Build Maven ignorado (--skip-maven)"
    for service in "${SERVICES[@]}"; do
      if ! ls "${PROJECT_ROOT}/servicos/${service}/target/"*.jar >/dev/null 2>&1; then
        echo "JAR nao encontrado em servicos/${service}/target" >&2
        exit 1
      fi
    done
    return
  fi

  phase "Fase CI: Build Maven"
  cd "${PROJECT_ROOT}"
  if [[ "${SKIP_TESTS}" == "true" ]]; then
    mvn clean package -DskipTests
  else
    mvn clean verify
  fi
  echo "Maven OK"
}

docker_build() {
  phase "Fase CI: Build das imagens Docker"
  cd "${PROJECT_ROOT}"
  export APP_VERSION="${APP_VERSION}"
  local args=(compose -f "${INFRA_COMPOSE}" -f "${APPS_COMPOSE}" build)
  if [[ "${NO_CACHE}" == "true" ]]; then
    args+=(--no-cache)
  fi
  args+=("${SERVICES[@]}")
  docker "${args[@]}"
  echo "Imagens Docker OK"
}

wait_for() {
  local label="$1"
  local timeout="$2"
  shift 2
  echo "Aguardando ${label}..."
  local start=${SECONDS}
  until "$@" >/dev/null 2>&1; do
    if (( SECONDS - start > timeout )); then
      echo "Timeout aguardando ${label}" >&2
      exit 1
    fi
    sleep 3
  done
}

wait_mysql() {
  wait_for "MySQL" 180 \
    docker compose -f "${INFRA_COMPOSE}" exec -T mysql mysqladmin ping -h 127.0.0.1 -uroot -proot
}

wait_kafka() {
  wait_for "Kafka" 180 \
    docker compose -f "${INFRA_COMPOSE}" exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092
}

wait_redis() {
  wait_for "Redis" 120 \
    docker compose -f "${INFRA_COMPOSE}" exec -T redis redis-cli ping
}

start_infra() {
  phase "Fase CD: Subindo infraestrutura"
  cd "${INFRA_DIR}"
  docker compose up -d
  wait_mysql
  wait_kafka
  wait_redis

  phase "Criando topicos Kafka"
  docker compose -f "${INFRA_COMPOSE}" exec -T kafka bash /opt/kafka/create-topics.sh
  echo "Infraestrutura OK"
}

wait_http_health() {
  local name="$1"
  local url="$2"
  local start=${SECONDS}
  until curl -sf "${url}" >/dev/null 2>&1; do
    if (( SECONDS - start > 180 )); then
      echo "Health check falhou para ${name} (${url})" >&2
      exit 1
    fi
    sleep 5
  done
  echo "${name} OK - ${url}"
}

start_apps() {
  phase "Fase CD: Deploy dos microsservicos"
  cd "${PROJECT_ROOT}"
  export APP_VERSION="${APP_VERSION}"
  docker compose -f "${INFRA_COMPOSE}" -f "${APPS_COMPOSE}" up -d --no-build "${SERVICES[@]}"

  phase "Verificando health dos microsservicos"
  wait_http_health "clientes-service" "http://localhost:8081/actuator/health"
  wait_http_health "produtos-service" "http://localhost:8082/actuator/health"
  wait_http_health "pedidos-service" "http://localhost:8083/actuator/health"
  wait_http_health "broker-service" "http://localhost:8084/actuator/health"
}

show_summary() {
  phase "Deploy concluido"
  cat <<'EOF'

Infraestrutura:
  MySQL .............. localhost:3306  (kafka_user / kafka_pass)
  Kafka .............. localhost:29092 (host) | kafka:9092 (rede Docker)
  Redis .............. localhost:6379
  Kafka UI ........... http://localhost:8080
  Redis Insight ...... http://localhost:5540
  SonarQube .......... http://localhost:9000  (admin / admin)

Microsservicos:
  clientes-service ... http://localhost:8081/swagger-ui.html
  produtos-service ... http://localhost:8082/swagger-ui.html
  pedidos-service .... http://localhost:8083/swagger-ui.html
  broker-service ..... http://localhost:8084/actuator/health

Comandos uteis:
  docker compose -f infraestrutura/docker-compose.yml -f docs/docker-compose.apps.yml ps
  docker compose -f infraestrutura/docker-compose.yml -f docs/docker-compose.apps.yml logs -f clientes-service
  ./scripts/stop.sh

EOF
}

assert_docker

if [[ "${APPS_ONLY}" == "true" ]]; then
  if [[ "${SKIP_MAVEN}" != "true" ]]; then
    maven_build
  fi
  docker_build
  start_apps
  show_summary
  exit 0
fi

if [[ "${INFRA_ONLY}" != "true" ]]; then
  maven_build
  docker_build
fi

start_infra

if [[ "${INFRA_ONLY}" != "true" ]]; then
  start_apps
fi

show_summary
