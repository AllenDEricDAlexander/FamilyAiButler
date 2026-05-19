#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts
# @ClassName: docker-compose-up
# @Author: atluofu
# @CreateTime: 2026Year-05Month-19Day
# @Description: 启动本地 Docker Compose 环境
# @Version: 1.0
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
source "${ROOT_DIR}/ops/scripts/lib/runtime.sh"

if ! parse_command_environment "$@"; then
  print_runtime_usage "$0"
  exit 1
fi

COMPOSE_DIR="${ROOT_DIR}/ops/docker-compose"
ENV_FILE="$(resolve_compose_env_file "${COMPOSE_DIR}" "${ENVIRONMENT}")"

cd "${COMPOSE_DIR}"

case "${COMMAND}" in
  status)
    docker compose --env-file "${ENV_FILE}" ps
    ;;
  start)
    docker compose --env-file "${ENV_FILE}" up -d --build
    ;;
  stop)
    docker compose --env-file "${ENV_FILE}" down
    ;;
  restart)
    docker compose --env-file "${ENV_FILE}" down
    docker compose --env-file "${ENV_FILE}" up -d --build
    ;;
esac
