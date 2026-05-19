#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts.lib
# @ClassName: runtime
# @Author: atluofu
# @CreateTime: 2026Year-05Month-19Day
# @Description: 运维脚本运行时通用方法
# @Version: 1.0

COMMAND="start"
ENVIRONMENT="dev"

# 解析启动命令和运行环境。
parse_command_environment() {
  local first="${1:-}"
  local second="${2:-}"

  if [ -z "${first}" ]; then
    COMMAND="start"
    ENVIRONMENT="dev"
    return
  fi

  case "${first}" in
    status|start|stop|restart)
      COMMAND="${first}"
      ENVIRONMENT="${second:-dev}"
      ;;
    dev|prod)
      ENVIRONMENT="${first}"
      COMMAND="${second:-start}"
      ;;
    *)
      echo "Unsupported command or environment: ${first}" >&2
      return 1
      ;;
  esac

  case "${COMMAND}" in
    status|start|stop|restart) ;;
    *)
      echo "Unsupported command: ${COMMAND}" >&2
      return 1
      ;;
  esac

  case "${ENVIRONMENT}" in
    dev|prod) ;;
    *)
      echo "Unsupported environment: ${ENVIRONMENT}" >&2
      return 1
      ;;
  esac
}

# 输出启动脚本参数说明。
print_runtime_usage() {
  local script_name="$1"
  echo "Usage: ${script_name} [status|start|stop|restart] [dev|prod]" >&2
  echo "       ${script_name} [dev|prod] [status|start|stop|restart]" >&2
}

# 获取运行时目录。
runtime_dir() {
  echo "${ROOT_DIR}/ops/.runtime"
}

# 获取 PID 文件路径。
pid_file_path() {
  local service_name="$1"
  local environment="$2"
  echo "$(runtime_dir)/${service_name}-${environment}.pid"
}

# 获取日志文件路径。
log_file_path() {
  local service_name="$1"
  local environment="$2"
  echo "$(runtime_dir)/${service_name}-${environment}.log"
}

# 获取 tmux 会话名称。
tmux_session_name() {
  local service_name="$1"
  local environment="$2"
  echo "family-ai-butler-${service_name}-${environment}"
}

# 判断 tmux 会话是否存在。
is_tmux_session_running() {
  local session_name="$1"
  command -v tmux >/dev/null 2>&1 && tmux has-session -t "${session_name}" >/dev/null 2>&1
}

# 判断进程是否存活。
is_pid_running() {
  local pid="$1"
  [ -n "${pid}" ] && kill -0 "${pid}" >/dev/null 2>&1
}

# 确保前端依赖已安装。
ensure_frontend_dependencies() {
  cd "${ROOT_DIR}/frontend"
  if [ ! -f "node_modules/.modules.yaml" ]; then
    corepack pnpm install
  fi
}

# 获取前端默认接口地址。
default_frontend_api_base_url() {
  local environment="$1"
  if [ "${environment}" = "prod" ]; then
    echo "http://localhost:8090"
    return
  fi
  echo "http://localhost:9527"
}

# 启动托管进程。
start_managed_process() {
  local service_name="$1"
  local environment="$2"
  local workdir="$3"
  shift 3

  local pid_file
  local log_file
  local old_pid
  local session_name
  pid_file="$(pid_file_path "${service_name}" "${environment}")"
  log_file="$(log_file_path "${service_name}" "${environment}")"
  session_name="$(tmux_session_name "${service_name}" "${environment}")"
  mkdir -p "$(runtime_dir)"

  if is_tmux_session_running "${session_name}"; then
    echo "${service_name} ${environment} is already running, session=${session_name}, log=${log_file}"
    return
  fi

  if [ -f "${pid_file}" ]; then
    old_pid="$(cat "${pid_file}")"
    if is_pid_running "${old_pid}"; then
      echo "${service_name} ${environment} is already running, pid=${old_pid}, log=${log_file}"
      return
    fi
  fi

  : >"${log_file}"

  if command -v tmux >/dev/null 2>&1; then
    local command_text=""
    local arg
    for arg in "$@"; do
      command_text+=" $(printf "%q" "${arg}")"
    done
    tmux new-session -d -s "${session_name}" -c "${workdir}" "exec${command_text} >>$(printf "%q" "${log_file}") 2>&1"
    sleep 2
    if ! is_tmux_session_running "${session_name}"; then
      echo "${service_name} ${environment} failed to start. log=${log_file}" >&2
      tail -n 80 "${log_file}" >&2 || true
      return 1
    fi
    echo "${service_name} ${environment} started, session=${session_name}, log=${log_file}"
    return
  fi

  (
    cd "${workdir}"
    nohup "$@" >"${log_file}" 2>&1 &
    echo "$!" >"${pid_file}"
  )
  sleep 2

  old_pid="$(cat "${pid_file}")"
  if ! is_pid_running "${old_pid}"; then
    echo "${service_name} ${environment} failed to start. log=${log_file}" >&2
    tail -n 80 "${log_file}" >&2 || true
    return 1
  fi

  echo "${service_name} ${environment} started, pid=${old_pid}, log=${log_file}"
}

# 停止托管进程。
stop_managed_process() {
  local service_name="$1"
  local environment="$2"
  local pid_file
  local session_name
  local pid
  pid_file="$(pid_file_path "${service_name}" "${environment}")"
  session_name="$(tmux_session_name "${service_name}" "${environment}")"

  if is_tmux_session_running "${session_name}"; then
    tmux send-keys -t "${session_name}" C-c >/dev/null 2>&1 || true
    sleep 2
    if is_tmux_session_running "${session_name}"; then
      tmux kill-session -t "${session_name}" >/dev/null 2>&1 || true
    fi
    rm -f "${pid_file}"
    echo "${service_name} ${environment} stopped"
    return
  fi

  if [ ! -f "${pid_file}" ]; then
    echo "${service_name} ${environment} is not running"
    return
  fi

  pid="$(cat "${pid_file}")"
  if ! is_pid_running "${pid}"; then
    rm -f "${pid_file}"
    echo "${service_name} ${environment} is not running"
    return
  fi

  pkill -TERM -P "${pid}" >/dev/null 2>&1 || true
  kill -TERM "${pid}" >/dev/null 2>&1 || true

  for _ in 1 2 3 4 5 6 7 8 9 10; do
    if ! is_pid_running "${pid}"; then
      rm -f "${pid_file}"
      echo "${service_name} ${environment} stopped"
      return
    fi
    sleep 1
  done

  pkill -KILL -P "${pid}" >/dev/null 2>&1 || true
  kill -KILL "${pid}" >/dev/null 2>&1 || true
  rm -f "${pid_file}"
  echo "${service_name} ${environment} stopped by force"
}

# 查询托管进程状态。
status_managed_process() {
  local service_name="$1"
  local environment="$2"
  local pid_file
  local log_file
  local session_name
  local pid
  pid_file="$(pid_file_path "${service_name}" "${environment}")"
  log_file="$(log_file_path "${service_name}" "${environment}")"
  session_name="$(tmux_session_name "${service_name}" "${environment}")"

  if is_tmux_session_running "${session_name}"; then
    echo "${service_name} ${environment} status: running, session=${session_name}, log=${log_file}"
    return
  fi

  if [ ! -f "${pid_file}" ]; then
    echo "${service_name} ${environment} status: stopped"
    return
  fi

  pid="$(cat "${pid_file}")"
  if is_pid_running "${pid}"; then
    echo "${service_name} ${environment} status: running, pid=${pid}, log=${log_file}"
    return
  fi

  rm -f "${pid_file}"
  echo "${service_name} ${environment} status: stopped"
}

# 选择 Docker Compose 环境变量文件。
resolve_compose_env_file() {
  local compose_dir="$1"
  local environment="$2"
  if [ -f "${compose_dir}/.env.${environment}" ]; then
    echo ".env.${environment}"
    return
  fi
  if [ ! -f "${compose_dir}/.env" ]; then
    cp "${compose_dir}/.env.example" "${compose_dir}/.env"
  fi
  echo ".env"
}
