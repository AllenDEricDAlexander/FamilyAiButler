#!/usr/bin/env node
/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.scripts
 * @ClassName: generate-tauri-icon
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: 生成 Tauri 桌面端默认 PNG 图标
 * @Version: 1.0
 */
import { mkdirSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { deflateSync } from "node:zlib";

const outputFile = resolve(import.meta.dirname, "../apps/desktop/src-tauri/icons/icon.png");
const size = 512;
const pixels = Buffer.alloc(size * size * 4);

/**
 * 写入单个像素。
 *
 * @param x 横向坐标
 * @param y 纵向坐标
 * @param color RGBA 颜色
 */
function setPixel(x, y, color) {
  const offset = (y * size + x) * 4;
  pixels[offset] = color[0];
  pixels[offset + 1] = color[1];
  pixels[offset + 2] = color[2];
  pixels[offset + 3] = color[3];
}

/**
 * 绘制矩形。
 *
 * @param left 左坐标
 * @param top 上坐标
 * @param width 宽度
 * @param height 高度
 * @param color RGBA 颜色
 */
function fillRect(left, top, width, height, color) {
  for (let y = top; y < top + height; y += 1) {
    for (let x = left; x < left + width; x += 1) {
      setPixel(x, y, color);
    }
  }
}

/**
 * 计算 CRC32。
 *
 * @param buffer 输入数据
 * @returns CRC32 数值
 */
function crc32(buffer) {
  let crc = 0xffffffff;
  for (const byte of buffer) {
    crc ^= byte;
    for (let index = 0; index < 8; index += 1) {
      crc = (crc >>> 1) ^ (0xedb88320 & -(crc & 1));
    }
  }
  return (crc ^ 0xffffffff) >>> 0;
}

/**
 * 生成 PNG 数据块。
 *
 * @param type 数据块类型
 * @param data 数据块内容
 * @returns PNG 数据块
 */
function pngChunk(type, data) {
  const typeBuffer = Buffer.from(type);
  const lengthBuffer = Buffer.alloc(4);
  const crcBuffer = Buffer.alloc(4);
  lengthBuffer.writeUInt32BE(data.length, 0);
  crcBuffer.writeUInt32BE(crc32(Buffer.concat([typeBuffer, data])), 0);
  return Buffer.concat([lengthBuffer, typeBuffer, data, crcBuffer]);
}

for (let y = 0; y < size; y += 1) {
  for (let x = 0; x < size; x += 1) {
    const distance = Math.hypot(x - size / 2, y - size / 2) / (size / 2);
    const shade = Math.max(0, 1 - distance * 0.35);
    setPixel(x, y, [Math.round(31 * shade), Math.round(79 * shade), Math.round(143 * shade), 255]);
  }
}

fillRect(112, 96, 288, 72, [246, 203, 82, 255]);
fillRect(112, 96, 78, 320, [246, 203, 82, 255]);
fillRect(112, 228, 238, 66, [246, 203, 82, 255]);

const rawRows = [];
for (let y = 0; y < size; y += 1) {
  rawRows.push(Buffer.from([0]));
  rawRows.push(pixels.subarray(y * size * 4, (y + 1) * size * 4));
}

const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(size, 0);
ihdr.writeUInt32BE(size, 4);
ihdr[8] = 8;
ihdr[9] = 6;
ihdr[10] = 0;
ihdr[11] = 0;
ihdr[12] = 0;

mkdirSync(dirname(outputFile), { recursive: true });
writeFileSync(
  outputFile,
  Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    pngChunk("IHDR", ihdr),
    pngChunk("IDAT", deflateSync(Buffer.concat(rawRows), { level: 9 })),
    pngChunk("IEND", Buffer.alloc(0))
  ])
);

console.log(`generated ${outputFile}`);
