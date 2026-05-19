#!/usr/bin/env bash
# @BelongsProject: FamilyAiButler
# @BelongsPackage: ops.scripts
# @ClassName: desktop-tauri-build
# @Author: atluofu
# @CreateTime: 2026Year-05Month-19Day
# @Description: 一键打包 Tauri 桌面客户端
# @Version: 1.0
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

cd "${ROOT_DIR}/frontend"
if [ ! -f "node_modules/.modules.yaml" ]; then
  corepack pnpm install
fi

BUILD_SCRIPT="build:desktop"
for arg in "$@"; do
  case "${arg}" in
    --skip-web)
      export SKIP_WEB_BUILD=1
      ;;
    --dmg)
      BUILD_SCRIPT="build:desktop:dmg"
      ;;
    --all)
      BUILD_SCRIPT="build:desktop:all"
      ;;
    --no-bundle)
      BUILD_SCRIPT="build:desktop:no-bundle"
      ;;
    *)
      echo "Unsupported argument: ${arg}" >&2
      echo "Usage: $0 [--skip-web] [--dmg|--all|--no-bundle]" >&2
      exit 1
      ;;
  esac
done

EXPO_PUBLIC_API_BASE_URL="${EXPO_PUBLIC_API_BASE_URL:-http://localhost:9527}" corepack pnpm "${BUILD_SCRIPT}"
