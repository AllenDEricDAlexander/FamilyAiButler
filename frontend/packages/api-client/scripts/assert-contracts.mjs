#!/usr/bin/env node
/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.packages.api-client.scripts
 * @ClassName: assert-contracts
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: family-core 前端接口契约校验
 * @Version: 1.0
 */
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";
import assert from "node:assert/strict";

const currentDir = dirname(fileURLToPath(import.meta.url));
const source = readFileSync(resolve(currentDir, "../src/index.ts"), "utf8");

const expectedFragments = [
  "familyCoreContracts",
  "/base/category/list",
  "/base/category/type/list",
  "/base/password/password/list/{pageNum}/{pageSize}",
  "/base/password/password/add",
  "/base/password/generate/{passwordLength}",
  "/base/password/checkStrength/{password}",
  "/base/password/checkValid/{password}"
];

for (const fragment of expectedFragments) {
  assert.ok(source.includes(fragment), `Missing frontend contract fragment: ${fragment}`);
}

console.log("family-core frontend contracts ok");
