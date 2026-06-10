#!/bin/bash
set -euo pipefail

BOOTSTRAP_SERVER="${KAFKA_BOOTSTRAP_SERVER:-kafka:9092}"

TOPICS=(
  "client-create"
  "client-response"
  "pedido-create"
  "pedido-response"
  "client-create-dlt"
  "pedido-create-dlt"
)

echo "Criando tópicos Kafka em ${BOOTSTRAP_SERVER}..."

for topic in "${TOPICS[@]}"; do
  kafka-topics \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --create \
    --if-not-exists \
    --topic "${topic}" \
    --partitions 3 \
    --replication-factor 1
  echo "Tópico ${topic} OK"
done

echo "Todos os tópicos foram criados."
