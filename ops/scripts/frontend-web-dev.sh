#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts
# @ClassName: frontend-web-dev
# @Author: atluofu
# @CreateTime: 2026Year-05Month-19Day
# @Description: 一键启动 Web 前端本地预览服务
# @Version: 1.0
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
source "${ROOT_DIR}/ops/scripts/lib/runtime.sh"

if ! parse_command_environment "$@"; then
  print_runtime_usage "$0"
  exit 1
fi

SERVICE_NAME="frontend-web"
WEB_PORT="${FRONTEND_WEB_PORT:-8081}"
WEB_API_PREFIX="${FRONTEND_WEB_API_PREFIX:-${NGINX_API_PREFIX:-/api}}"
API_BASE_URL="${EXPO_PUBLIC_API_BASE_URL:-${WEB_API_PREFIX}}"
WEB_GATEWAY_PROXY="${FRONTEND_WEB_GATEWAY_PROXY:-${NGINX_GATEWAY_PROXY:-http://127.0.0.1:9527}}"
FRONTEND_DIST_DIR="${ROOT_DIR}/frontend/apps/web/dist"

# 构建 Web 静态资源。
build_frontend_web_dist() {
  ensure_frontend_dependencies
  EXPO_PUBLIC_API_BASE_URL="${API_BASE_URL}" node "${ROOT_DIR}/frontend/scripts/build-web-if-needed.mjs"
}

# 启动带 API 代理的本地 Web 预览服务。
start_frontend_web_preview() {
  build_frontend_web_dist
  start_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}" "${ROOT_DIR}" \
    node "${ROOT_DIR}/ops/scripts/frontend-preview-server.mjs" \
    --root "${FRONTEND_DIST_DIR}" \
    --port "${WEB_PORT}" \
    --api-prefix "${WEB_API_PREFIX}" \
    --gateway "${WEB_GATEWAY_PROXY}"
}

case "${COMMAND}" in
  status)
    status_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}"
    ;;
  start)
    start_frontend_web_preview
    ;;
  stop)
    stop_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}"
    ;;
  restart)
    stop_managed_process "${SERVICE_NAME}" "${ENVIRONMENT}"
    start_frontend_web_preview
    ;;
esac
