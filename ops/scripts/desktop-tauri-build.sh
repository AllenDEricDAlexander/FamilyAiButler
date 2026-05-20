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
DESKTOP_DIR="${ROOT_DIR}/frontend/apps/desktop"
TAURI_DIR="${DESKTOP_DIR}/src-tauri"
BUILD_TARGET="dmg"

cd "${ROOT_DIR}/frontend"
if [ ! -f "node_modules/.modules.yaml" ]; then
  corepack pnpm install
fi

# 输出 Tauri 桌面端打包脚本参数说明。
print_usage() {
  echo "Usage: $0 [--skip-web] [dmg|app|exe|apk|all|no-bundle]" >&2
  echo "       $0 [--skip-web] [--dmg|--app|--exe|--apk|--all|--no-bundle]" >&2
}

# 规范化 Tauri 打包所需的 RGBA PNG 图标。
ensure_tauri_icon() {
  node "${ROOT_DIR}/frontend/scripts/normalize-tauri-icon.mjs" >/dev/null
}

# 清理 Tauri 应用 crate 缓存，确保静态前端资源重新嵌入。
clean_tauri_application_cache() {
  if ! command -v cargo >/dev/null 2>&1; then
    return
  fi
  (
    cd "${TAURI_DIR}"
    cargo clean -p family-ai-butler-desktop >/dev/null
  )
}

# 执行 Tauri CLI 命令。
run_tauri() {
  ensure_tauri_icon
  clean_tauri_application_cache
  EXPO_PUBLIC_API_BASE_URL="${EXPO_PUBLIC_API_BASE_URL:-http://localhost/api}" \
    corepack pnpm --filter @family-ai-butler/desktop exec tauri "$@"
}

# 清理上次 dmg 打包失败遗留的临时磁盘镜像和目录。
cleanup_macos_bundle_artifacts() {
  local bundle_dir="${TAURI_DIR}/target/release/bundle"
  local device
  local devices

  if command -v hdiutil >/dev/null 2>&1; then
    devices="$(hdiutil info | awk -v bundle_dir="${bundle_dir}" '
      $1 == "image-path" && index($3, bundle_dir) == 1 { matched = 1; next }
      matched == 1 && $1 ~ /^\/dev\/disk/ { print $1; matched = 0 }
    ')"
    for device in ${devices}; do
      hdiutil detach "${device}" >/dev/null 2>&1 || true
    done
  fi

  rm -rf "${bundle_dir}/dmg"
  rm -f "${bundle_dir}/macos"/rw.*.dmg
}

# 执行 macOS Tauri 打包并在失败后清理临时磁盘镜像。
run_macos_tauri_build() {
  cleanup_macos_bundle_artifacts
  if run_tauri "$@"; then
    return
  fi
  cleanup_macos_bundle_artifacts
  return 1
}

# 打包 macOS dmg 安装包。
build_macos_dmg() {
  if [ "$(uname -s)" != "Darwin" ]; then
    echo "macOS dmg can only be built on macOS." >&2
    return 1
  fi
  run_macos_tauri_build build --bundles dmg
}

# 打包 macOS app 应用包。
build_macos_app() {
  if [ "$(uname -s)" != "Darwin" ]; then
    echo "macOS app can only be built on macOS." >&2
    return 1
  fi
  run_macos_tauri_build build --bundles app
}

# 打包 Windows exe 安装包。
build_windows_exe() {
  case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*)
      run_tauri build --bundles nsis
      ;;
    *)
      echo "Windows exe requires running this script on Windows with the Tauri Windows bundler toolchain." >&2
      echo "Current OS: $(uname -s)" >&2
      return 1
      ;;
  esac
}

# 打包 Android apk 安装包。
build_android_apk() {
  if [ ! -d "${TAURI_DIR}/gen/android" ]; then
    echo "Android apk requires a Tauri Android project, but ${TAURI_DIR}/gen/android does not exist." >&2
    echo "Initialize it first from ${DESKTOP_DIR}: corepack pnpm exec tauri android init" >&2
    return 1
  fi
  run_tauri android build --apk
}

# 跳过安装包阶段，只验证 Rust/Tauri 编译。
build_without_bundle() {
  run_tauri build --no-bundle
}

# 尽可能打包当前工程已支持的全部客户端制品。
build_all_targets() {
  local failed=0

  build_macos_dmg || failed=1
  build_windows_exe || failed=1
  build_android_apk || failed=1

  if [ "${failed}" -ne 0 ]; then
    echo "One or more requested artifacts were not built. See the messages above." >&2
    return 1
  fi
}

for arg in "$@"; do
  case "${arg}" in
    --skip-web)
      export SKIP_WEB_BUILD=1
      ;;
    dmg|--dmg)
      BUILD_TARGET="dmg"
      ;;
    app|--app)
      BUILD_TARGET="app"
      ;;
    exe|--exe|windows|--windows)
      BUILD_TARGET="exe"
      ;;
    apk|--apk|android|--android)
      BUILD_TARGET="apk"
      ;;
    all|--all)
      BUILD_TARGET="all"
      ;;
    no-bundle|--no-bundle)
      BUILD_TARGET="no-bundle"
      ;;
    -h|--help)
      print_usage
      exit 0
      ;;
    *)
      echo "Unsupported argument: ${arg}" >&2
      print_usage
      exit 1
      ;;
  esac
done

case "${BUILD_TARGET}" in
  dmg)
    build_macos_dmg
    ;;
  app)
    build_macos_app
    ;;
  exe)
    build_windows_exe
    ;;
  apk)
    build_android_apk
    ;;
  all)
    build_all_targets
    ;;
  no-bundle)
    build_without_bundle
    ;;
esac
