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

cd "${ROOT_DIR}/backend"
mvn clean package -DskipTests
