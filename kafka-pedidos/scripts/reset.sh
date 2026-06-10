#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="${SCRIPT_DIR}/../infraestrutura"

cd "${INFRA_DIR}"

echo "Resetando infraestrutura kafka-pedidos (removendo volumes)..."
docker compose down -v

echo "Recriando infraestrutura..."
docker compose up -d

echo "Aguardando Kafka ficar disponivel..."
until docker compose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; do
  sleep 3
done

echo "Criando topicos Kafka..."
docker compose exec -T kafka bash /opt/kafka/create-topics.sh

echo "Infraestrutura resetada."
docker compose ps
