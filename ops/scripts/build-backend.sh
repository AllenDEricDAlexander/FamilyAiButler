#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts
# @ClassName: build-backend
# @Author: atluofu
# @CreateTime: 2026Year-05Month-19Day
# @Description: 构建后端 Maven 聚合工程
# @Version: 1.0
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKEND_BUILD_MODULES=()
BACKEND_RUN_TESTS="false"
BACKEND_BUILD_FULL_REACTOR="false"

# 输出后端构建脚本参数说明。
print_backend_build_usage() {
  cat >&2 <<EOF
Usage: $0 [services|all|core|uaa|family-uaa|qwen-ai|gateway|framework|openapi|cache|codegen] [skip-tests|test]
       $0 core,uaa,gateway skip-tests

Examples:
       $0
       $0 all
       $0 services
       $0 core
       $0 uaa test
       $0 framework,openapi skip-tests
EOF
}

# 添加不重复的 Maven 模块路径。
append_backend_build_module() {
  local module="$1"
  local selected
  BACKEND_BUILD_FULL_REACTOR="false"
  if [ "${#BACKEND_BUILD_MODULES[@]}" -gt 0 ]; then
    for selected in "${BACKEND_BUILD_MODULES[@]}"; do
      if [ "${selected}" = "${module}" ]; then
        return
      fi
    done
  fi
  BACKEND_BUILD_MODULES+=("${module}")
}

# 添加本地运行和容器镜像需要的后端服务模块。
append_backend_runtime_modules() {
  append_backend_build_module "family-uaa/uaa-core"
  append_backend_build_module "family-core"
  append_backend_build_module "family-ai/qwen-ai"
  append_backend_build_module "family-gateway"
}

# 添加 framework 聚合下所有叶子模块。
append_backend_framework_modules() {
  append_backend_build_module "family-framework/family-common"
  append_backend_build_module "family-framework/family-common-web"
  append_backend_build_module "family-framework/family-common-mybatis"
  append_backend_build_module "family-framework/family-common-security"
  append_backend_build_module "family-framework/family-log/family-log-core"
  append_backend_build_module "family-framework/family-log/family-log-servlet-spring-boot-starter"
  append_backend_build_module "family-framework/family-log/family-log-webflux-spring-boot-starter"
  append_backend_build_module "family-framework/family-log/family-log-http-spring-boot-starter"
  append_backend_build_module "family-framework/family-log/family-log-dubbo-spring-boot-starter"
  append_backend_build_module "family-framework/family-log/family-log-grpc-spring-boot-starter"
}

# 规范化后端构建模块名称。
append_normalized_backend_build_module() {
  local module_name
  module_name="$(printf "%s" "$1" | tr "[:upper:]" "[:lower:]")"
  case "${module_name}" in
    all|full)
      BACKEND_BUILD_MODULES=()
      BACKEND_BUILD_FULL_REACTOR="true"
      ;;
    services|runtime|backend)
      append_backend_runtime_modules
      ;;
    core|family-core)
      append_backend_build_module "family-core"
      ;;
    uaa|uaa-core|family-uaa|be-uaa)
      append_backend_build_module "family-uaa/uaa-core"
      ;;
    uaa-facade|family-uaa-facade)
      append_backend_build_module "family-uaa/uaa-facade"
      ;;
    uaa-resource|uaa-resource-server|uaa-resource-server-spring-boot-starter)
      append_backend_build_module "family-uaa/uaa-resource-server-spring-boot-starter"
      ;;
    ai|qwen|qwen-ai|family-ai-qwen)
      append_backend_build_module "family-ai/qwen-ai"
      ;;
    gateway|family-gateway)
      append_backend_build_module "family-gateway"
      ;;
    framework|family-framework)
      append_backend_framework_modules
      ;;
    openapi|openapi-console|openapi-debug-console-spring-boot-starter)
      append_backend_build_module "openapi-debug-console-spring-boot-starter"
      ;;
    cache|family-cache|family-cache-spring-boot-starter)
      append_backend_build_module "family-cache-spring-boot-starter"
      ;;
    codegen|pg-ddd-codegen)
      append_backend_build_module "pg-ddd-codegen"
      ;;
    *)
      echo "Unsupported backend build module: $1" >&2
      return 1
      ;;
  esac
}

# 解析后端构建脚本参数。
parse_backend_build_args() {
  local arg
  local item
  for arg in "$@"; do
    case "${arg}" in
      -h|--help|help)
        print_backend_build_usage
        exit 0
        ;;
      test|--test|with-tests)
        BACKEND_RUN_TESTS="true"
        ;;
      skip-tests|--skip-tests|-DskipTests)
        BACKEND_RUN_TESTS="false"
        ;;
      *)
        IFS="," read -r -a module_items <<<"${arg}"
        for item in "${module_items[@]}"; do
          item="${item//[[:space:]]/}"
          if [ -n "${item}" ]; then
            append_normalized_backend_build_module "${item}"
          fi
        done
        ;;
    esac
  done
}

# 执行后端 Maven 构建。
run_backend_build() {
  local maven_args=("clean" "package")
  local module_csv
  if [ "${BACKEND_RUN_TESTS}" != "true" ]; then
    maven_args+=("-DskipTests")
  fi

  cd "${ROOT_DIR}/backend"
  if [ "${BACKEND_BUILD_FULL_REACTOR}" = "true" ]; then
    mvn "${maven_args[@]}"
    return
  fi

  if [ "${#BACKEND_BUILD_MODULES[@]}" -eq 0 ]; then
    append_backend_runtime_modules
  fi

  module_csv="$(IFS=,; printf "%s" "${BACKEND_BUILD_MODULES[*]}")"
  mvn -pl "${module_csv}" -am "${maven_args[@]}"
}

parse_backend_build_args "$@"
run_backend_build
