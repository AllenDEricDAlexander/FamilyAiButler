#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts
# @ClassName: backend-local
# @Author: atluofu
# @CreateTime: 2026Year-05Month-19Day
# @Description: 本地 JDK 方式管理后端应用
# @Version: 1.0
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
source "${ROOT_DIR}/ops/scripts/lib/runtime.sh"

BACKEND_START_MODULES=(core uaa qwen-ai gateway)
BACKEND_STOP_MODULES=(gateway qwen-ai uaa core)
SELECTED_MODULES=()
COMMAND_EXIT_CODE=0

# 输出后端脚本参数说明。
print_backend_usage() {
  cat >&2 <<EOF
Usage: $0 [all|core|uaa|qwen-ai|gateway] [dev|prod] [status|start|stop|restart]
       $0 [status|start|stop|restart] [all|core|uaa|qwen-ai|gateway] [dev|prod]
       $0 [dev|prod] [all|core|uaa|qwen-ai|gateway] [status|start|stop|restart]

Examples:
       $0
       $0 all dev start
       $0 core dev restart
       $0 status qwen-ai dev
       $0 gateway prod stop
EOF
}

# 规范化后端模块名称。
normalize_backend_module() {
  local module_name
  module_name="$(printf "%s" "$1" | tr "[:upper:]" "[:lower:]")"
  case "${module_name}" in
    all)
      echo "all"
      ;;
    core|family-core)
      echo "core"
      ;;
    uaa|family-uaa)
      echo "uaa"
      ;;
    ai|qwen|qwen-ai|family-ai-qwen)
      echo "qwen-ai"
      ;;
    gateway|family-gateway)
      echo "gateway"
      ;;
    *)
      return 1
      ;;
  esac
}

# 添加不重复的模块。
append_backend_module() {
  local module="$1"
  local selected
  if [ "${module}" = "all" ]; then
    SELECTED_MODULES=("${BACKEND_START_MODULES[@]}")
    return
  fi
  if [ "${#SELECTED_MODULES[@]}" -gt 0 ]; then
    for selected in "${SELECTED_MODULES[@]}"; do
      if [ "${selected}" = "${module}" ]; then
        return
      fi
    done
  fi
  SELECTED_MODULES+=("${module}")
}

# 解析后端脚本参数。
parse_backend_args() {
  local arg
  local item
  local module
  local has_module="false"
  COMMAND="start"
  ENVIRONMENT="dev"

  for arg in "$@"; do
    case "${arg}" in
      status|start|stop|restart)
        COMMAND="${arg}"
        ;;
      dev|prod)
        ENVIRONMENT="${arg}"
        ;;
      *)
        IFS="," read -r -a module_items <<<"${arg}"
        for item in "${module_items[@]}"; do
          item="${item//[[:space:]]/}"
          if ! module="$(normalize_backend_module "${item}")"; then
            echo "Unsupported backend module, command or environment: ${item}" >&2
            return 1
          fi
          append_backend_module "${module}"
          has_module="true"
        done
        ;;
    esac
  done

  if [ "${has_module}" = "false" ]; then
    SELECTED_MODULES=("${BACKEND_START_MODULES[@]}")
  fi
}

# 判断模块是否被选中。
is_backend_module_selected() {
  local target="$1"
  local selected
  for selected in "${SELECTED_MODULES[@]}"; do
    if [ "${selected}" = "${target}" ]; then
      return 0
    fi
  done
  return 1
}

# 获取后端服务名称。
backend_service_name() {
  case "$1" in
    core)
      echo "family-core"
      ;;
    uaa)
      echo "family-uaa"
      ;;
    qwen-ai)
      echo "family-ai-qwen"
      ;;
    gateway)
      echo "family-gateway"
      ;;
  esac
}

# 获取后端 Maven 模块路径。
backend_maven_module() {
  case "$1" in
    core)
      echo "family-core"
      ;;
    uaa)
      echo "family-uaa"
      ;;
    qwen-ai)
      echo "family-ai/qwen-ai"
      ;;
    gateway)
      echo "family-gateway"
      ;;
  esac
}

# 获取后端模块监听端口。
backend_module_port() {
  case "$1" in
    core)
      echo "39090"
      ;;
    uaa)
      echo "39092"
      ;;
    qwen-ai)
      echo "39091"
      ;;
    gateway)
      echo "9527"
      ;;
  esac
}

# 获取后端模块健康检查地址。
backend_module_health_path() {
  case "$1" in
    uaa)
      echo "/"
      ;;
    *)
      echo "/actuator/health"
      ;;
  esac
}

# 判断后端健康检查 HTTP 状态是否表示服务已可响应。
is_backend_health_http_code_success() {
  local module="$1"
  local http_code="$2"
  case "${http_code}" in
    2*|3*)
      return 0
      ;;
    401|403)
      [ "${module}" = "uaa" ]
      return
      ;;
    *)
      return 1
      ;;
  esac
}

# 准备后端模块启动所需的本地 Maven 依赖。
prepare_backend_module_dependencies() {
  local service_name="$1"
  local maven_module="$2"
  local dependency_log_file
  dependency_log_file="$(runtime_dir)/${service_name}-${ENVIRONMENT}-maven-dependencies.log"
  mkdir -p "$(runtime_dir)"

  echo "${service_name} ${ENVIRONMENT} preparing local Maven dependencies, log=${dependency_log_file}"
  if [ "${BACKEND_MAVEN_VERBOSE:-false}" = "true" ]; then
    if ! (cd "${ROOT_DIR}/backend" && mvn -pl "${maven_module}" -am install -DskipTests -Dspring-boot.repackage.skip=true 2>&1 | tee "${dependency_log_file}"); then
      echo "${service_name} ${ENVIRONMENT} local Maven dependencies failed, log=${dependency_log_file}" >&2
      tail -n "${BACKEND_MAVEN_LOG_TAIL_LINES:-80}" "${dependency_log_file}" >&2 || true
      return 1
    fi
  elif ! (cd "${ROOT_DIR}/backend" && mvn -pl "${maven_module}" -am install -DskipTests -Dspring-boot.repackage.skip=true >"${dependency_log_file}" 2>&1); then
    echo "${service_name} ${ENVIRONMENT} local Maven dependencies failed, log=${dependency_log_file}" >&2
    tail -n "${BACKEND_MAVEN_LOG_TAIL_LINES:-80}" "${dependency_log_file}" >&2 || true
    return 1
  fi
  echo "${service_name} ${ENVIRONMENT} local Maven dependencies ready"
}

# 查询占用端口的进程。
find_backend_port_pid() {
  local port="$1"
  if ! command -v lsof >/dev/null 2>&1; then
    return 1
  fi
  lsof -nP -tiTCP:"${port}" -sTCP:LISTEN 2>/dev/null | head -n 1
}

# 等待后端健康检查可用。
wait_backend_health() {
  local module="$1"
  local service_name
  local port
  local health_path
  local timeout
  local waited=0
  local http_code
  local log_file
  service_name="$(backend_service_name "${module}")"
  port="$(backend_module_port "${module}")"
  health_path="$(backend_module_health_path "${module}")"
  log_file="$(log_file_path "${service_name}" "${ENVIRONMENT}")"
  timeout="${BACKEND_HEALTH_TIMEOUT_SECONDS:-90}"

  if [ "${BACKEND_WAIT_HEALTH:-true}" != "true" ] || ! command -v curl >/dev/null 2>&1; then
    return
  fi

  while [ "${waited}" -lt "${timeout}" ]; do
    http_code="$(curl -sS -o /dev/null -w "%{http_code}" --max-time 2 "http://127.0.0.1:${port}${health_path}" 2>/dev/null || true)"
    if is_backend_health_http_code_success "${module}" "${http_code}"; then
      echo "${service_name} ${ENVIRONMENT} health check passed, port=${port}, path=${health_path}, http_code=${http_code}"
      return
    fi
    waited=$((waited + 2))
    sleep 2
  done

  echo "${service_name} ${ENVIRONMENT} health check timeout, port=${port}, path=${health_path}, last_http_code=${http_code}, log=${log_file}" >&2
  tail -n "${BACKEND_HEALTH_LOG_TAIL_LINES:-80}" "${log_file}" >&2 || true
  return 1
}

# 启动单个后端模块。
start_backend_module() {
  local module="$1"
  local service_name
  local maven_module
  local port
  local port_pid
  local java_tool_options
  service_name="$(backend_service_name "${module}")"
  maven_module="$(backend_maven_module "${module}")"
  port="$(backend_module_port "${module}")"
  port_pid="$(find_backend_port_pid "${port}" || true)"
  if [ -n "${JAVA_TOOL_OPTIONS:-}" ]; then
    java_tool_options="${JAVA_TOOL_OPTIONS} -DJM.LOG.PATH=${ROOT_DIR}/ops/.runtime/logs/nacos"
  else
    java_tool_options="-DJM.LOG.PATH=${ROOT_DIR}/ops/.runtime/logs/nacos"
  fi

  if [ -n "${port_pid}" ]; then
    echo "${service_name} ${ENVIRONMENT} is already running, pid=${port_pid}, port=${port}"
    return
  fi

  if ! prepare_backend_module_dependencies "${service_name}" "${maven_module}"; then
    return 1
  fi
  start_managed_process "${service_name}" "${ENVIRONMENT}" "${ROOT_DIR}/backend" \
    env "SPRING_PROFILES_ACTIVE=${ENVIRONMENT}" "FAMILY_AI_BUTLER_ENV=${ENVIRONMENT}" \
    "FAMILY_AI_BUTLER_LOG_PATH=${ROOT_DIR}/ops/.runtime/logs" "JAVA_TOOL_OPTIONS=${java_tool_options}" \
    mvn -pl "${maven_module}" spring-boot:run "-Dspring-boot.run.profiles=${ENVIRONMENT}"
  wait_backend_health "${module}"
}

# 停止单个后端模块。
stop_backend_module() {
  local module="$1"
  local service_name
  local port
  local port_pid
  service_name="$(backend_service_name "${module}")"
  port="$(backend_module_port "${module}")"

  stop_managed_process "${service_name}" "${ENVIRONMENT}"
  port_pid="$(find_backend_port_pid "${port}" || true)"
  if [ -z "${port_pid}" ]; then
    return
  fi

  echo "${service_name} ${ENVIRONMENT} still listens on port=${port}, stopping pid=${port_pid}"
  kill -TERM "${port_pid}" >/dev/null 2>&1 || true
  sleep 2
  if kill -0 "${port_pid}" >/dev/null 2>&1; then
    kill -KILL "${port_pid}" >/dev/null 2>&1 || true
  fi
}

# 查询单个后端模块状态。
status_backend_module() {
  local module="$1"
  local service_name
  local port
  local port_pid
  local pid_file
  local log_file
  local session_name
  local pid
  service_name="$(backend_service_name "${module}")"
  port="$(backend_module_port "${module}")"
  pid_file="$(pid_file_path "${service_name}" "${ENVIRONMENT}")"
  log_file="$(log_file_path "${service_name}" "${ENVIRONMENT}")"
  session_name="$(tmux_session_name "${service_name}" "${ENVIRONMENT}")"

  if is_tmux_session_running "${session_name}"; then
    echo "${service_name} ${ENVIRONMENT} status: running, session=${session_name}, port=${port}, log=${log_file}"
    return
  fi

  if [ -f "${pid_file}" ]; then
    pid="$(cat "${pid_file}")"
    if is_pid_running "${pid}"; then
      echo "${service_name} ${ENVIRONMENT} status: running, pid=${pid}, port=${port}, log=${log_file}"
      return
    fi
    rm -f "${pid_file}"
  fi

  port_pid="$(find_backend_port_pid "${port}" || true)"
  if [ -n "${port_pid}" ]; then
    echo "${service_name} ${ENVIRONMENT} status: running, pid=${port_pid}, port=${port}, source=external"
    return
  fi
  echo "${service_name} ${ENVIRONMENT} status: stopped, port=${port}"
}

# 按启动顺序执行后端模块命令。
run_backend_modules_by_start_order() {
  local module
  for module in "${BACKEND_START_MODULES[@]}"; do
    if ! is_backend_module_selected "${module}"; then
      continue
    fi
    if ! "$1" "${module}"; then
      COMMAND_EXIT_CODE=1
    fi
  done
}

# 按停止顺序执行后端模块命令。
run_backend_modules_by_stop_order() {
  local module
  for module in "${BACKEND_STOP_MODULES[@]}"; do
    if ! is_backend_module_selected "${module}"; then
      continue
    fi
    if ! "$1" "${module}"; then
      COMMAND_EXIT_CODE=1
    fi
  done
}

if ! parse_backend_args "$@"; then
  print_backend_usage
  exit 1
fi

case "${COMMAND}" in
  status)
    run_backend_modules_by_start_order status_backend_module
    ;;
  start)
    run_backend_modules_by_start_order start_backend_module
    ;;
  stop)
    run_backend_modules_by_stop_order stop_backend_module
    ;;
  restart)
    run_backend_modules_by_stop_order stop_backend_module
    run_backend_modules_by_start_order start_backend_module
    ;;
esac

exit "${COMMAND_EXIT_CODE}"
