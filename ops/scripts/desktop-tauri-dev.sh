#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts
# @ClassName: desktop-tauri-dev
# @Author: atluofu
# @CreateTime: 2026Year-05Month-19Day
# @Description: 一键启动 Tauri 桌面端开发服务
# @Version: 1.0
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
source "${ROOT_DIR}/ops/scripts/lib/runtime.sh"

if ! parse_command_environment "$@"; then
  print_runtime_usage "$0"
  exit 1
fi

SERVICE_NAME="desktop-tauri"
API_BASE_URL="${EXPO_PUBLIC_API_BASE_URL:-$(default_frontend_api_base_url "${ENVIRONMENT}")}"

case "${COMMAND}" in
  status)
    status_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}"
    ;;
  start)
    ensure_frontend_dependencies
    start_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}" "${ROOT_DIR}/frontend" \
      env "EXPO_PUBLIC_API_BASE_URL=${API_BASE_URL}" corepack pnpm dev:desktop
    ;;
  stop)
    stop_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}"
    ;;
  restart)
    stop_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}"
    ensure_frontend_dependencies
    start_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}" "${ROOT_DIR}/frontend" \
      env "EXPO_PUBLIC_API_BASE_URL=${API_BASE_URL}" corepack pnpm dev:desktop
    ;;
esac
