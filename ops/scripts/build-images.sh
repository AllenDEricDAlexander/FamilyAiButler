#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts
# @ClassName: build-images
# @Author: atluofu
# @CreateTime: 2026Year-05Month-21Day
# @Description: 构建并可选推送 Docker/Kubernetes 共用镜像
# @Version: 1.0
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
IMAGE_REGISTRY="${IMAGE_REGISTRY:-family-ai-butler}"
IMAGE_TAG="${IMAGE_TAG:-$(git -C "${ROOT_DIR}" rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S)}"
IMAGE_PLATFORM="${IMAGE_PLATFORM:-linux/amd64}"
PUSH_IMAGE="${PUSH_IMAGE:-false}"

BUILD_TARGETS=("$@")
if [ "${#BUILD_TARGETS[@]}" -eq 0 ]; then
  BUILD_TARGETS=("all")
fi

if [ "${PUSH_IMAGE}" = "true" ]; then
  BUILDX_OUTPUT=(--push)
else
  BUILDX_OUTPUT=(--load)
fi

# 判断当前目标是否需要构建。
should_build_target() {
  local target="$1"
  local selected
  for selected in "${BUILD_TARGETS[@]}"; do
    case "${selected}" in
      all)
        return 0
        ;;
      backend)
        case "${target}" in
          family-core|family-uaa|family-ai-qwen|family-gateway)
            return 0
            ;;
        esac
        ;;
      frontend|web)
        [ "${target}" = "family-web" ] && return 0
        ;;
      core)
        [ "${target}" = "family-core" ] && return 0
        ;;
      uaa|family-uaa|be-uaa)
        [ "${target}" = "family-uaa" ] && return 0
        ;;
      qwen|qwen-ai|ai)
        [ "${target}" = "family-ai-qwen" ] && return 0
        ;;
      gateway)
        [ "${target}" = "family-gateway" ] && return 0
        ;;
      "${target}")
        return 0
        ;;
    esac
  done
  return 1
}

# 构建前端静态资源镜像。
build_frontend_image() {
  local image="${IMAGE_REGISTRY}/family-web:${IMAGE_TAG}"
  docker buildx build \
    --platform "${IMAGE_PLATFORM}" \
    --file "${ROOT_DIR}/ops/docker-compose/Dockerfile.frontend" \
    --tag "${image}" \
    "${BUILDX_OUTPUT[@]}" \
    "${ROOT_DIR}"
  echo "built image: ${image}"
}

# 构建后端运行镜像。
build_backend_image() {
  local service="$1"
  local jar_file="$2"
  local image="${IMAGE_REGISTRY}/${service}:${IMAGE_TAG}"
  docker buildx build \
    --platform "${IMAGE_PLATFORM}" \
    --file "${ROOT_DIR}/ops/docker-compose/Dockerfile.backend" \
    --build-arg "JAR_FILE=${jar_file}" \
    --tag "${image}" \
    "${BUILDX_OUTPUT[@]}" \
    "${ROOT_DIR}/backend"
  echo "built image: ${image}"
}

NEED_BACKEND_BUILD=false
for service in family-core family-uaa family-ai-qwen family-gateway; do
  if should_build_target "${service}"; then
    NEED_BACKEND_BUILD=true
  fi
done

if [ "${NEED_BACKEND_BUILD}" = "true" ]; then
  "${ROOT_DIR}/ops/scripts/build-backend.sh" services skip-tests
fi

if should_build_target family-web; then
  build_frontend_image
fi

if should_build_target family-core; then
  build_backend_image family-core family-core/target/family-core-0.0.1-SNAPSHOT.jar
fi

if should_build_target family-uaa; then
  build_backend_image family-uaa family-uaa/uaa-core/target/uaa-core-0.0.1-SNAPSHOT.jar
fi

if should_build_target family-ai-qwen; then
  build_backend_image family-ai-qwen family-ai/qwen-ai/target/qwen-ai-0.0.1-SNAPSHOT.jar
fi

if should_build_target family-gateway; then
  build_backend_image family-gateway family-gateway/target/family-gateway-0.0.1-SNAPSHOT.jar
fi

cat <<EOF
image registry: ${IMAGE_REGISTRY}
image tag: ${IMAGE_TAG}
image platform: ${IMAGE_PLATFORM}
push image: ${PUSH_IMAGE}
EOF
