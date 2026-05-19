#!/usr/bin/env node
/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.scripts
 * @ClassName: normalize-tauri-icon
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: 使用前端新图标生成 Tauri 打包要求的 512x512 RGBA PNG
 * @Version: 1.0
 */
import {readFileSync, writeFileSync} from "node:fs";
import {resolve} from "node:path";
import {deflateSync, inflateSync} from "node:zlib";

const sourceIconFile = resolve(import.meta.dirname, "../apps/web/assets/icon.png");
const targetIconFile = resolve(import.meta.dirname, "../apps/desktop/src-tauri/icons/icon.png");
const targetSize = 512;
const pngSignature = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);

/**
 * 计算 CRC32。
 *
 * @param {Buffer} buffer 输入数据
 * @returns {number} CRC32 数值
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
 * @param {string} type 数据块类型
 * @param {Buffer} data 数据块内容
 * @returns {Buffer} PNG 数据块
 */
function pngChunk(type, data) {
    const typeBuffer = Buffer.from(type);
    const lengthBuffer = Buffer.alloc(4);
    const crcBuffer = Buffer.alloc(4);
    lengthBuffer.writeUInt32BE(data.length, 0);
    crcBuffer.writeUInt32BE(crc32(Buffer.concat([typeBuffer, data])), 0);
    return Buffer.concat([lengthBuffer, typeBuffer, data, crcBuffer]);
}

/**
 * 解析 PNG 数据块。
 *
 * @param {Buffer} png PNG 文件内容
 * @returns {{ ihdr: Buffer, idat: Buffer }} 关键 PNG 数据块
 */
function parsePng(png) {
    if (!png.subarray(0, pngSignature.length).equals(pngSignature)) {
        throw new Error("icon.png is not a valid PNG file");
    }

    let offset = pngSignature.length;
    let ihdr = null;
    const idatChunks = [];

    while (offset < png.length) {
        const length = png.readUInt32BE(offset);
        const type = png.subarray(offset + 4, offset + 8).toString("ascii");
        const data = png.subarray(offset + 8, offset + 8 + length);
        offset += 12 + length;

        if (type === "IHDR") {
            ihdr = data;
        } else if (type === "IDAT") {
            idatChunks.push(data);
        } else if (type === "IEND") {
            break;
        }
    }

    if (!ihdr || idatChunks.length === 0) {
        throw new Error("icon.png is missing required PNG chunks");
    }

    return {ihdr, idat: Buffer.concat(idatChunks)};
}

/**
 * 计算 Paeth 过滤器预测值。
 *
 * @param {number} left 左侧像素分量
 * @param {number} above 上方像素分量
 * @param {number} upperLeft 左上像素分量
 * @returns {number} 预测值
 */
function paeth(left, above, upperLeft) {
    const estimate = left + above - upperLeft;
    const leftDistance = Math.abs(estimate - left);
    const aboveDistance = Math.abs(estimate - above);
    const upperLeftDistance = Math.abs(estimate - upperLeft);

    if (leftDistance <= aboveDistance && leftDistance <= upperLeftDistance) {
        return left;
    }
    if (aboveDistance <= upperLeftDistance) {
        return above;
    }
    return upperLeft;
}

/**
 * 还原 PNG 过滤后的扫描行。
 *
 * @param {Buffer} compressed 压缩后的 IDAT 数据
 * @param {number} width 图片宽度
 * @param {number} height 图片高度
 * @param {number} bytesPerPixel 每像素字节数
 * @returns {Buffer[]} 已还原的扫描行
 */
function unfilterRows(compressed, width, height, bytesPerPixel) {
    const inflated = inflateSync(compressed);
    const rowLength = width * bytesPerPixel;
    const rows = [];
    let offset = 0;

    for (let rowIndex = 0; rowIndex < height; rowIndex += 1) {
        const filter = inflated[offset];
        const source = inflated.subarray(offset + 1, offset + 1 + rowLength);
        const target = Buffer.alloc(rowLength);
        const previous = rows[rowIndex - 1];
        offset += rowLength + 1;

        for (let index = 0; index < rowLength; index += 1) {
            const left = index >= bytesPerPixel ? target[index - bytesPerPixel] : 0;
            const above = previous ? previous[index] : 0;
            const upperLeft = previous && index >= bytesPerPixel ? previous[index - bytesPerPixel] : 0;

            if (filter === 0) {
                target[index] = source[index];
            } else if (filter === 1) {
                target[index] = (source[index] + left) & 0xff;
            } else if (filter === 2) {
                target[index] = (source[index] + above) & 0xff;
            } else if (filter === 3) {
                target[index] = (source[index] + Math.floor((left + above) / 2)) & 0xff;
            } else if (filter === 4) {
                target[index] = (source[index] + paeth(left, above, upperLeft)) & 0xff;
            } else {
                throw new Error(`Unsupported PNG filter type: ${filter}`);
            }
        }

        rows.push(target);
    }

    return rows;
}

/**
 * 解码 PNG 为 RGBA 像素数据。
 *
 * @param {string} file PNG 文件路径
 * @returns {{ width: number, height: number, pixels: Buffer }} RGBA 图像数据
 */
function decodeRgba(file) {
    const png = readFileSync(file);
    const {ihdr, idat} = parsePng(png);
    const width = ihdr.readUInt32BE(0);
    const height = ihdr.readUInt32BE(4);
    const bitDepth = ihdr[8];
    const colorType = ihdr[9];
    const interlace = ihdr[12];

    if (bitDepth !== 8 || interlace !== 0) {
        throw new Error("icon.png must be an 8-bit non-interlaced PNG");
    }

    if (colorType !== 2 && colorType !== 6) {
        throw new Error(`icon.png must be RGB or RGBA, got color type ${colorType}`);
    }

    const bytesPerPixel = colorType === 6 ? 4 : 3;
    const rows = unfilterRows(idat, width, height, bytesPerPixel);
    const pixels = Buffer.alloc(width * height * 4);

    for (let y = 0; y < height; y += 1) {
        const row = rows[y];
        for (let pixel = 0; pixel < width; pixel += 1) {
            const sourceOffset = pixel * bytesPerPixel;
            const targetOffset = (y * width + pixel) * 4;
            pixels[targetOffset] = row[sourceOffset];
            pixels[targetOffset + 1] = row[sourceOffset + 1];
            pixels[targetOffset + 2] = row[sourceOffset + 2];
            pixels[targetOffset + 3] = colorType === 6 ? row[sourceOffset + 3] : 255;
        }
    }

    return {width, height, pixels};
}

/**
 * 缩放 RGBA 图像。
 *
 * @param {Buffer} pixels 原始 RGBA 像素
 * @param {number} width 原始宽度
 * @param {number} height 原始高度
 * @param {number} size 目标边长
 * @returns {Buffer} 缩放后的 RGBA 像素
 */
function resizeRgba(pixels, width, height, size) {
    if (width === size && height === size) {
        return pixels;
    }

    if (width !== height) {
        throw new Error("icon.png must be square");
    }

    const resized = Buffer.alloc(size * size * 4);
    const scale = (width - 1) / (size - 1);

    for (let y = 0; y < size; y += 1) {
        const sourceY = y * scale;
        const y0 = Math.floor(sourceY);
        const y1 = Math.min(y0 + 1, height - 1);
        const dy = sourceY - y0;

        for (let x = 0; x < size; x += 1) {
            const sourceX = x * scale;
            const x0 = Math.floor(sourceX);
            const x1 = Math.min(x0 + 1, width - 1);
            const dx = sourceX - x0;
            const targetOffset = (y * size + x) * 4;

            for (let channel = 0; channel < 4; channel += 1) {
                const topLeft = pixels[(y0 * width + x0) * 4 + channel];
                const topRight = pixels[(y0 * width + x1) * 4 + channel];
                const bottomLeft = pixels[(y1 * width + x0) * 4 + channel];
                const bottomRight = pixels[(y1 * width + x1) * 4 + channel];
                const top = topLeft * (1 - dx) + topRight * dx;
                const bottom = bottomLeft * (1 - dx) + bottomRight * dx;
                resized[targetOffset + channel] = Math.round(top * (1 - dy) + bottom * dy);
            }
        }
    }

    return resized;
}

/**
 * 写入 RGBA PNG 文件。
 *
 * @param {string} file PNG 文件路径
 * @param {Buffer} pixels RGBA 像素
 * @param {number} width 图片宽度
 * @param {number} height 图片高度
 */
function writeRgbaPng(file, pixels, width, height) {
    const rawRows = [];
    for (let y = 0; y < height; y += 1) {
        rawRows.push(Buffer.from([0]));
        rawRows.push(pixels.subarray(y * width * 4, (y + 1) * width * 4));
    }

    const ihdr = Buffer.alloc(13);
    ihdr.writeUInt32BE(width, 0);
    ihdr.writeUInt32BE(height, 4);
    ihdr[8] = 8;
    ihdr[9] = 6;
    ihdr[10] = 0;
    ihdr[11] = 0;
    ihdr[12] = 0;

    writeFileSync(
        file,
        Buffer.concat([
            pngSignature,
            pngChunk("IHDR", ihdr),
            pngChunk("IDAT", deflateSync(Buffer.concat(rawRows), {level: 9})),
            pngChunk("IEND", Buffer.alloc(0))
        ])
    );
}

/**
 * 使用前端新图标生成 Tauri 标准图标。
 */
function normalizeIcon() {
    const {width, height, pixels} = decodeRgba(sourceIconFile);
    writeRgbaPng(targetIconFile, resizeRgba(pixels, width, height, targetSize), targetSize, targetSize);
}

normalizeIcon();
