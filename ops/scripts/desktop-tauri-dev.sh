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
TAURI_DEV_PORT="8081"
API_BASE_URL="${EXPO_PUBLIC_API_BASE_URL:-$(default_frontend_api_base_url "${ENVIRONMENT}")}"

# 检查 Tauri 开发服务依赖的 Expo Web 端口是否可用。
ensure_tauri_dev_port_available() {
  local port="$1"
  local listener

  if ! command -v lsof >/dev/null 2>&1; then
    return
  fi

  listener="$(lsof -nP -iTCP:"${port}" -sTCP:LISTEN 2>/dev/null || true)"
  if [ -z "${listener}" ]; then
    return
  fi

  echo "Tauri desktop dev requires localhost:${port}, but the port is already in use." >&2
  echo "${listener}" >&2
  echo "Stop the existing web preview first, for example: ./ops/scripts/frontend-web-dev.sh stop dev" >&2
  return 1
}

case "${COMMAND}" in
  status)
    status_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}"
    ;;
  start)
    ensure_tauri_dev_port_available "${TAURI_DEV_PORT}"
    ensure_frontend_dependencies
    start_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}" "${ROOT_DIR}/frontend" \
      env "EXPO_PUBLIC_API_BASE_URL=${API_BASE_URL}" corepack pnpm dev:desktop
    ;;
  stop)
    stop_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}"
    ;;
  restart)
    stop_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}"
    ensure_tauri_dev_port_available "${TAURI_DEV_PORT}"
    ensure_frontend_dependencies
    start_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}" "${ROOT_DIR}/frontend" \
      env "EXPO_PUBLIC_API_BASE_URL=${API_BASE_URL}" corepack pnpm dev:desktop
    ;;
esac
