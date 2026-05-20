#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts
# @ClassName: frontend-nginx-up
# @Author: atluofu
# @CreateTime: 2026Year-05Month-19Day
# @Description: 一键构建并启动本机 Nginx 前端代理
# @Version: 1.0
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
source "${ROOT_DIR}/ops/scripts/lib/runtime.sh"

# 输出前端 Nginx 脚本参数说明。
print_frontend_nginx_usage() {
  print_runtime_usage "$0"
  echo "Environment variables:" >&2
  echo "       FRONTEND_NGINX_PORT=80" >&2
  echo "       NGINX_API_PREFIX=/api" >&2
  echo "       NGINX_GATEWAY_PROXY=http://127.0.0.1:9527" >&2
}

if ! parse_command_environment "$@"; then
  print_frontend_nginx_usage
  exit 1
fi

SERVICE_NAME="frontend-nginx"
NGINX_REQUESTED_PORT="${FRONTEND_NGINX_PORT:-${FRONTEND_PORT:-80}}"
NGINX_PORT="${NGINX_REQUESTED_PORT}"
NGINX_API_PREFIX="${NGINX_API_PREFIX:-/api}"
NGINX_SERVER_TEMPLATE_FILE="${ROOT_DIR}/ops/nginx/family-ai-butler.local.conf"
NGINX_MAIN_TEMPLATE_FILE="${ROOT_DIR}/ops/nginx/nginx.local.conf"
NGINX_RUNTIME_DIR="$(runtime_dir)/nginx/${ENVIRONMENT}"
NGINX_PORT_FILE="${NGINX_RUNTIME_DIR}/frontend-nginx.port"
NGINX_SERVER_CONF="${NGINX_RUNTIME_DIR}/conf.d/default.conf"
NGINX_MAIN_CONF="${NGINX_RUNTIME_DIR}/nginx.conf"
NGINX_LOCAL_PREFIX="${NGINX_RUNTIME_DIR}/prefix"
NGINX_PID_FILE="${NGINX_LOCAL_PREFIX}/logs/nginx.pid"
FRONTEND_DIST_DIR="${ROOT_DIR}/frontend/apps/web/dist"

# 转义 sed 替换值。
escape_sed_replacement() {
  printf "%s" "$1" | sed "s/[&|\\\\]/\\\\&/g"
}

# 查找本机 Nginx mime.types 文件。
resolve_nginx_mime_types() {
  local candidate
  for candidate in \
    "/opt/homebrew/etc/nginx/mime.types" \
    "/usr/local/etc/nginx/mime.types" \
    "/etc/nginx/mime.types"; do
    if [ -f "${candidate}" ]; then
      echo "${candidate}"
      return
    fi
  done
  echo ""
}

# 获取 Nginx 可执行文件路径。
resolve_nginx_bin() {
  command -v nginx
}

# 规范化 Nginx API 前缀。
normalize_path_prefix() {
  local prefix="$1"
  prefix="/${prefix#/}"
  echo "${prefix%/}"
}

# 判断 TCP 监听端口是否已被占用。
is_tcp_port_in_use() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    if lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1; then
      return 0
    fi
    return 1
  fi
  if command -v ss >/dev/null 2>&1; then
    if ss -ltn | awk '{print $4}' | grep -Eq "[:.]${port}$"; then
      return 0
    fi
    return 1
  fi
  if command -v netstat >/dev/null 2>&1; then
    if netstat -an | grep -Eq "[.:]${port}[[:space:]].*LISTEN"; then
      return 0
    fi
    return 1
  fi
  return 1
}

# 判断端口号是否为数字。
is_port_number() {
  local port="$1"
  case "${port}" in
    ""|*[!0-9]*)
      return 1
      ;;
    *)
      return 0
      ;;
  esac
}

# 选择本机 Nginx 监听端口。
select_nginx_listen_port() {
  if ! is_port_number "${NGINX_REQUESTED_PORT}"; then
    echo "frontend-nginx port must be a number: ${NGINX_REQUESTED_PORT}" >&2
    return 1
  fi
  if is_tcp_port_in_use "${NGINX_REQUESTED_PORT}"; then
    echo "frontend-nginx port ${NGINX_REQUESTED_PORT} is already in use." >&2
    echo "Stop the process using this port or set FRONTEND_NGINX_PORT to another port." >&2
    return 1
  fi
  echo "${NGINX_REQUESTED_PORT}"
}

# 获取状态输出中展示的 Nginx 端口。
resolve_status_nginx_port() {
  if [ -f "${NGINX_PORT_FILE}" ]; then
    cat "${NGINX_PORT_FILE}"
    return
  fi
  echo "${NGINX_PORT}"
}

# 判断当前脚本托管的 Nginx 是否运行中。
is_local_nginx_running() {
  local pid
  if [ ! -s "${NGINX_PID_FILE}" ]; then
    return 1
  fi
  pid="$(cat "${NGINX_PID_FILE}" 2>/dev/null || true)"
  if [ -z "${pid}" ]; then
    return 1
  fi
  is_pid_running "${pid}"
}

# 判断 Nginx 控制命令是否需要 sudo。
needs_nginx_sudo() {
  [ "${NGINX_PORT}" -lt 1024 ] && [ "$(id -u)" -ne 0 ]
}

# 执行 Nginx 控制命令。
run_nginx_control() {
  if needs_nginx_sudo; then
    if ! command -v sudo >/dev/null 2>&1; then
      echo "sudo is required to bind nginx on port ${NGINX_PORT}." >&2
      return 1
    fi
    sudo "$@"
    return
  fi
  "$@"
}

# 输出本机 Nginx 安装提示。
print_nginx_install_hint() {
  echo "nginx is not installed and cannot be installed automatically by this script." >&2
  case "$(uname -s)" in
    Darwin)
      echo "macOS: brew install nginx" >&2
      ;;
    Linux)
      echo "Debian/Ubuntu: sudo apt-get update && sudo apt-get install -y nginx" >&2
      echo "Fedora/RHEL:   sudo dnf install -y nginx" >&2
      echo "CentOS:        sudo yum install -y nginx" >&2
      ;;
    *)
      echo "Please install nginx for your operating system." >&2
      ;;
  esac
}

# 使用 root 或 sudo 执行系统安装命令。
run_with_sudo_if_needed() {
  if [ "$(id -u)" -eq 0 ]; then
    "$@"
    return
  fi
  if command -v sudo >/dev/null 2>&1; then
    sudo "$@"
    return
  fi
  echo "sudo is required to install nginx on this Linux host." >&2
  return 1
}

# 使用当前系统包管理器安装本机 Nginx。
install_local_nginx() {
  case "$(uname -s)" in
    Darwin)
      if ! command -v brew >/dev/null 2>&1; then
        print_nginx_install_hint
        return 1
      fi
      echo "nginx is missing, installing with Homebrew..." >&2
      brew install nginx
      ;;
    Linux)
      if command -v apt-get >/dev/null 2>&1; then
        echo "nginx is missing, installing with apt-get..." >&2
        run_with_sudo_if_needed apt-get update
        run_with_sudo_if_needed apt-get install -y nginx
      elif command -v dnf >/dev/null 2>&1; then
        echo "nginx is missing, installing with dnf..." >&2
        run_with_sudo_if_needed dnf install -y nginx
      elif command -v yum >/dev/null 2>&1; then
        echo "nginx is missing, installing with yum..." >&2
        run_with_sudo_if_needed yum install -y nginx
      else
        print_nginx_install_hint
        return 1
      fi
      ;;
    *)
      print_nginx_install_hint
      return 1
      ;;
  esac
}

# 确保本机已安装 Nginx。
ensure_local_nginx() {
  if command -v nginx >/dev/null 2>&1; then
    return
  fi
  install_local_nginx
  if ! command -v nginx >/dev/null 2>&1; then
    echo "nginx installation finished, but nginx is still not available in PATH." >&2
    return 1
  fi
}

# 构建前端静态资源，Nginx 模式强制使用 API 边界前缀。
build_frontend_dist_for_nginx() {
  local frontend_api_base_url
  frontend_api_base_url="$(normalize_path_prefix "${NGINX_API_PREFIX}")"
  ensure_frontend_dependencies
  EXPO_PUBLIC_API_BASE_URL="${frontend_api_base_url}" node "${ROOT_DIR}/frontend/scripts/build-web-if-needed.mjs"
}

# 渲染共享 Nginx server 配置。
render_nginx_server_config() {
  local api_prefix
  local listen_port
  local web_root
  local gateway_proxy
  mkdir -p "${NGINX_RUNTIME_DIR}/conf.d" "${NGINX_LOCAL_PREFIX}/logs" "${NGINX_LOCAL_PREFIX}/client_body_temp"

  api_prefix="$(normalize_path_prefix "${NGINX_API_PREFIX}")"
  listen_port="${NGINX_PORT}"
  web_root="${FRONTEND_DIST_DIR}"
  gateway_proxy="${NGINX_GATEWAY_PROXY:-http://127.0.0.1:9527}"

  sed \
    -e "s|\${NGINX_API_PREFIX}|$(escape_sed_replacement "${api_prefix}")|g" \
    -e "s|\${NGINX_LISTEN_PORT}|$(escape_sed_replacement "${listen_port}")|g" \
    -e "s|\${NGINX_WEB_ROOT}|$(escape_sed_replacement "${web_root}")|g" \
    -e "s|\${NGINX_GATEWAY_PROXY}|$(escape_sed_replacement "${gateway_proxy}")|g" \
    "${NGINX_SERVER_TEMPLATE_FILE}" >"${NGINX_SERVER_CONF}"
}

# 根据模板渲染本机 Nginx 主配置。
render_nginx_main_config_from_template() {
  local access_log
  local error_log
  local mime_types
  local mime_types_include
  mime_types="$(resolve_nginx_mime_types)"
  error_log="${NGINX_LOCAL_PREFIX}/logs/error.log"
  access_log="${NGINX_LOCAL_PREFIX}/logs/access.log"
  if [ -n "${mime_types}" ]; then
    mime_types_include="include ${mime_types};"
  else
    mime_types_include="# mime.types not found"
  fi

  sed \
    -e "s|\${NGINX_ERROR_LOG}|$(escape_sed_replacement "${error_log}")|g" \
    -e "s|\${NGINX_PID_FILE}|$(escape_sed_replacement "${NGINX_PID_FILE}")|g" \
    -e "s|\${NGINX_MIME_TYPES_INCLUDE}|$(escape_sed_replacement "${mime_types_include}")|g" \
    -e "s|\${NGINX_ACCESS_LOG}|$(escape_sed_replacement "${access_log}")|g" \
    -e "s|\${NGINX_SERVER_CONF}|$(escape_sed_replacement "${NGINX_SERVER_CONF}")|g" \
    "${NGINX_MAIN_TEMPLATE_FILE}" >"${NGINX_MAIN_CONF}"
}

# 校验本机 Nginx 启动后端口已进入监听状态。
verify_local_nginx_started() {
  sleep 1
  if is_tcp_port_in_use "${NGINX_PORT}"; then
    return
  fi
  echo "frontend-nginx ${ENVIRONMENT} failed to listen on port ${NGINX_PORT}." >&2
  tail -n 80 "${NGINX_LOCAL_PREFIX}/logs/error.log" >&2 || true
  return 1
}

# 启动本机 Nginx。
start_local_nginx() {
  local nginx_bin
  ensure_local_nginx
  if is_local_nginx_running; then
    echo "${SERVICE_NAME} ${ENVIRONMENT} is already running, pid=$(cat "${NGINX_PID_FILE}"), url=http://localhost:$(resolve_status_nginx_port)"
    return
  fi
  nginx_bin="$(resolve_nginx_bin)"
  NGINX_PORT="$(select_nginx_listen_port)"
  build_frontend_dist_for_nginx
  render_nginx_server_config
  render_nginx_main_config_from_template
  "${nginx_bin}" -t -e "${NGINX_LOCAL_PREFIX}/logs/error.log" -c "${NGINX_MAIN_CONF}" -p "${NGINX_LOCAL_PREFIX}" >/dev/null
  printf "%s" "${NGINX_PORT}" >"${NGINX_PORT_FILE}"
  run_nginx_control "${nginx_bin}" -e "${NGINX_LOCAL_PREFIX}/logs/error.log" -c "${NGINX_MAIN_CONF}" -p "${NGINX_LOCAL_PREFIX}"
  verify_local_nginx_started
  echo "${SERVICE_NAME} ${ENVIRONMENT} started, pid=$(cat "${NGINX_PID_FILE}"), url=http://localhost:${NGINX_PORT}, config=${NGINX_SERVER_CONF}"
}

# 停止前端 Nginx。
stop_frontend_nginx() {
  local nginx_bin
  NGINX_PORT="$(resolve_status_nginx_port)"
  if ! is_local_nginx_running; then
    rm -f "${NGINX_PORT_FILE}"
    echo "${SERVICE_NAME} ${ENVIRONMENT} is not running"
    return
  fi
  nginx_bin="$(resolve_nginx_bin)"
  run_nginx_control "${nginx_bin}" -e "${NGINX_LOCAL_PREFIX}/logs/error.log" -s stop -c "${NGINX_MAIN_CONF}" -p "${NGINX_LOCAL_PREFIX}" >/dev/null
  for _ in 1 2 3 4 5 6 7 8 9 10; do
    if ! is_local_nginx_running; then
      rm -f "${NGINX_PORT_FILE}"
      echo "${SERVICE_NAME} ${ENVIRONMENT} stopped"
      return
    fi
    sleep 1
  done
  echo "${SERVICE_NAME} ${ENVIRONMENT} did not stop within timeout. pid=$(cat "${NGINX_PID_FILE}")" >&2
  return 1
}

# 查询前端 Nginx 状态。
status_frontend_nginx() {
  local status_port
  status_port="$(resolve_status_nginx_port)"
  if is_local_nginx_running; then
    echo "${SERVICE_NAME} ${ENVIRONMENT} status: running, pid=$(cat "${NGINX_PID_FILE}")"
  else
    echo "${SERVICE_NAME} ${ENVIRONMENT} status: stopped"
  fi
  echo "${SERVICE_NAME} ${ENVIRONMENT} url=http://localhost:${status_port}, config=${NGINX_SERVER_CONF}"
}

case "${COMMAND}" in
  status)
    status_frontend_nginx
    ;;
  start)
    start_local_nginx
    ;;
  stop)
    stop_frontend_nginx
    ;;
  restart)
    stop_frontend_nginx
    start_local_nginx
    ;;
esac
