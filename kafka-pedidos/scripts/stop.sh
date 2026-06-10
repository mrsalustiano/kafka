#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="${SCRIPT_DIR}/../infraestrutura"

cd "${INFRA_DIR}"

echo "Parando infraestrutura kafka-pedidos..."
docker compose down

echo "Infraestrutura parada."
