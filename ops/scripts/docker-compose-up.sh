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

# 读取 env 文件中的单个配置值。
read_compose_env_file_value() {
  local key="$1"
  local line
  if [ ! -f "${ENV_FILE}" ]; then
    return
  fi
  while IFS= read -r line || [ -n "${line}" ]; do
    case "${line}" in
      "${key}="*)
        printf "%s" "${line#*=}"
        return
        ;;
    esac
  done <"${ENV_FILE}"
}

# 获取 Compose 插值配置值，优先使用当前 shell 环境。
compose_env_value() {
  local key="$1"
  local shell_value="${!key:-}"
  if [ -n "${shell_value}" ]; then
    printf "%s" "${shell_value}"
    return
  fi
  read_compose_env_file_value "${key}"
}

# 判断是否启用了 backend profile。
is_backend_compose_profile_enabled() {
  local profiles
  profiles="$(compose_env_value "COMPOSE_PROFILES")"
  case ",${profiles}," in
    *,backend,*) return 0 ;;
    *) return 1 ;;
  esac
}

# 校验后端容器镜像需要复制的 jar 是否已存在。
verify_backend_compose_jars() {
  local jar_file
  local missing="false"
  for jar_file in \
    "family-uaa/uaa-core/target/uaa-core-0.0.1-SNAPSHOT.jar" \
    "family-core/target/family-core-0.0.1-SNAPSHOT.jar" \
    "family-ai/qwen-ai/target/qwen-ai-0.0.1-SNAPSHOT.jar" \
    "family-gateway/target/family-gateway-0.0.1-SNAPSHOT.jar"; do
    if [ ! -f "${ROOT_DIR}/backend/${jar_file}" ]; then
      echo "backend jar is missing: backend/${jar_file}" >&2
      missing="true"
    fi
  done
  [ "${missing}" = "false" ]
}

# 为 backend profile 预构建服务 jar，并修正容器内服务访问地址。
prepare_backend_compose_profile() {
  local profiles
  if ! is_backend_compose_profile_enabled; then
    return
  fi
  profiles="$(compose_env_value "COMPOSE_PROFILES")"
  export COMPOSE_PROFILES="${profiles}"
  export NGINX_GATEWAY_PROXY="${NGINX_GATEWAY_PROXY:-http://family-gateway:9527}"
  export UAA_AUTHORIZATION_BASE_URL="${UAA_AUTHORIZATION_BASE_URL:-http://family-uaa:39092}"
  "${ROOT_DIR}/ops/scripts/build-backend.sh" services skip-tests
  verify_backend_compose_jars
}

cd "${COMPOSE_DIR}"

case "${COMMAND}" in
  status)
    docker compose --env-file "${ENV_FILE}" ps
    ;;
  start)
    prepare_backend_compose_profile
    docker compose --env-file "${ENV_FILE}" up -d --build
    ;;
  stop)
    docker compose --env-file "${ENV_FILE}" down
    ;;
  restart)
    docker compose --env-file "${ENV_FILE}" down
    prepare_backend_compose_profile
    docker compose --env-file "${ENV_FILE}" up -d --build
    ;;
esac
