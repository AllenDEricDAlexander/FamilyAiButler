#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts
# @ClassName: k8s-apply
# @Author: atluofu
# @CreateTime: 2026Year-05Month-19Day
# @Description: 应用 Kubernetes 环境配置
# @Version: 1.0
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
source "${ROOT_DIR}/ops/scripts/lib/runtime.sh"

if ! parse_command_environment "$@"; then
  print_runtime_usage "$0"
  exit 1
fi

OVERLAY_DIR="${ROOT_DIR}/ops/k8s/overlays/${ENVIRONMENT}"
NAMESPACE="family-ai-butler"

case "${COMMAND}" in
  status)
    kubectl get all,hpa,pvc -n "${NAMESPACE}"
    ;;
  start)
    kubectl apply -k "${OVERLAY_DIR}"
    ;;
  stop)
    kubectl delete -k "${OVERLAY_DIR}" --ignore-not-found
    ;;
  restart)
    kubectl apply -k "${OVERLAY_DIR}"
    kubectl rollout restart deployment,statefulset -n "${NAMESPACE}"
    ;;
esac
