/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.core
 * @FileName: ApiDocConsoleDocumentRenderer.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:45
 * @Description: OpenAPI 调试文档控制台导出渲染器文件
 * @Version: 1.0
 */
package top.egon.openapi.console.core;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.core
 * @ClassName: ApiDocConsoleDocumentRenderer
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:45
 * @Description: OpenAPI 调试文档控制台导出渲染器
 * @Version: 1.0
 */
public class ApiDocConsoleDocumentRenderer {

    private static final int PDF_PAGE_WIDTH = 595;

    private static final int PDF_PAGE_HEIGHT = 842;

    private static final int PDF_LINE_LIMIT = 50;

    private static final int PDF_LINE_LENGTH = 88;

    /**
     * 渲染 Markdown 文档
     *
     * @param spec OpenAPI JSON
     * @return String 返回 Markdown 文档
     */
    public String renderMarkdown(JsonNode spec) {
        StringBuilder builder = new StringBuilder();
        JsonNode info = spec.path("info");
        builder.append("# ").append(text(info.path("title"), "OpenAPI Document")).append("\n\n");
        builder.append("- Version: ").append(text(info.path("version"), "v1")).append("\n");
        builder.append("- Description: ").append(text(info.path("description"), "")).append("\n\n");
        appendServers(builder, spec.path("servers"));
        appendPaths(builder, spec.path("paths"));
        return builder.toString();
    }

    /**
     * 渲染 PDF 文档
     *
     * @param markdown Markdown 文档
     * @return byte[] 返回 PDF 字节
     */
    public byte[] renderPdf(String markdown) {
        List<List<String>> pages = paginate(markdown);
        int totalObjects = 5 + pages.size() * 2;
        List<Integer> offsets = new ArrayList<>();
        for (int index = 0; index <= totalObjects; index++) {
            offsets.add(0);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(output, "%PDF-1.4\n");
        writeObject(output, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        writeObject(output, offsets, 2, "<< /Type /Pages /Kids [" + pageKids(pages.size()) + "] /Count " + pages.size() + " >>");
        writeObject(output, offsets, 3, "<< /Type /Font /Subtype /Type0 /BaseFont /STSong-Light /Encoding /UniGB-UCS2-H /DescendantFonts [4 0 R] >>");
        writeObject(output, offsets, 4, "<< /Type /Font /Subtype /CIDFontType0 /BaseFont /STSong-Light /CIDSystemInfo << /Registry (Adobe) /Ordering (GB1) /Supplement 5 >> /FontDescriptor 5 0 R >>");
        writeObject(output, offsets, 5, "<< /Type /FontDescriptor /FontName /STSong-Light /Flags 6 /FontBBox [0 -200 1000 900] /ItalicAngle 0 /Ascent 880 /Descent -120 /CapHeight 700 /StemV 80 >>");
        for (int index = 0; index < pages.size(); index++) {
            int pageObject = 6 + index * 2;
            int contentObject = pageObject + 1;
            String content = pdfContent(pages.get(index));
            writeObject(output, offsets, pageObject, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PDF_PAGE_WIDTH + " " + PDF_PAGE_HEIGHT + "] /Resources << /Font << /F1 3 0 R >> >> /Contents " + contentObject + " 0 R >>");
            writeStreamObject(output, offsets, contentObject, content);
        }
        int startXref = output.size();
        write(output, "xref\n0 " + (totalObjects + 1) + "\n");
        write(output, "0000000000 65535 f \n");
        for (int index = 1; index <= totalObjects; index++) {
            write(output, String.format("%010d 00000 n \n", offsets.get(index)));
        }
        write(output, "trailer\n<< /Size " + (totalObjects + 1) + " /Root 1 0 R >>\nstartxref\n" + startXref + "\n%%EOF");
        return output.toByteArray();
    }

    /**
     * 追加服务地址
     *
     * @param builder Markdown 构造器
     * @param servers OpenAPI servers 节点
     */
    private void appendServers(StringBuilder builder, JsonNode servers) {
        if (!servers.isArray() || servers.isEmpty()) {
            return;
        }
        builder.append("## Servers\n\n");
        for (JsonNode server : servers) {
            builder.append("- ").append(text(server.path("url"), "")).append("\n");
        }
        builder.append("\n");
    }

    /**
     * 追加接口路径
     *
     * @param builder Markdown 构造器
     * @param paths   OpenAPI paths 节点
     */
    private void appendPaths(StringBuilder builder, JsonNode paths) {
        if (!paths.isObject()) {
            return;
        }
        builder.append("## APIs\n\n");
        paths.fields().forEachRemaining(pathEntry -> pathEntry.getValue().fields().forEachRemaining(methodEntry -> {
            String method = methodEntry.getKey().toUpperCase();
            JsonNode operation = methodEntry.getValue();
            builder.append("### ").append(method).append(" ").append(pathEntry.getKey()).append("\n\n");
            builder.append(text(operation.path("summary"), text(operation.path("operationId"), ""))).append("\n\n");
            appendParameters(builder, operation.path("parameters"));
            appendRequestBody(builder, operation.path("requestBody"));
            appendResponses(builder, operation.path("responses"));
        }));
    }

    /**
     * 追加请求参数
     *
     * @param builder    Markdown 构造器
     * @param parameters OpenAPI parameters 节点
     */
    private void appendParameters(StringBuilder builder, JsonNode parameters) {
        if (!parameters.isArray() || parameters.isEmpty()) {
            return;
        }
        builder.append("#### Parameters\n\n| Name | In | Required | Description |\n| --- | --- | --- | --- |\n");
        for (JsonNode parameter : parameters) {
            builder.append("| ")
                    .append(text(parameter.path("name"), ""))
                    .append(" | ")
                    .append(text(parameter.path("in"), ""))
                    .append(" | ")
                    .append(parameter.path("required").asBoolean(false))
                    .append(" | ")
                    .append(text(parameter.path("description"), ""))
                    .append(" |\n");
        }
        builder.append("\n");
    }

    /**
     * 追加请求体
     *
     * @param builder     Markdown 构造器
     * @param requestBody OpenAPI requestBody 节点
     */
    private void appendRequestBody(StringBuilder builder, JsonNode requestBody) {
        JsonNode content = requestBody.path("content");
        if (!content.isObject() || content.isEmpty()) {
            return;
        }
        builder.append("#### Request Body\n\n");
        content.fields().forEachRemaining(entry -> builder.append("- ").append(entry.getKey()).append("\n"));
        builder.append("\n");
    }

    /**
     * 追加响应信息
     *
     * @param builder   Markdown 构造器
     * @param responses OpenAPI responses 节点
     */
    private void appendResponses(StringBuilder builder, JsonNode responses) {
        if (!responses.isObject() || responses.isEmpty()) {
            return;
        }
        builder.append("#### Responses\n\n| Status | Description |\n| --- | --- |\n");
        responses.fields().forEachRemaining(entry -> builder.append("| ")
                .append(entry.getKey())
                .append(" | ")
                .append(text(entry.getValue().path("description"), ""))
                .append(" |\n"));
        builder.append("\n");
    }

    /**
     * 读取文本节点
     *
     * @param node         JSON 节点
     * @param defaultValue 默认值
     * @return String 返回文本
     */
    private String text(JsonNode node, String defaultValue) {
        return node == null || node.isMissingNode() || node.isNull() ? defaultValue : node.asText(defaultValue);
    }

    /**
     * 对 Markdown 分页
     *
     * @param markdown Markdown 文档
     * @return List<List<String>> 返回分页行
     */
    private List<List<String>> paginate(String markdown) {
        List<String> lines = new ArrayList<>();
        Arrays.stream(markdown.split("\\R", -1)).forEach(line -> wrapLine(line, lines));
        if (lines.isEmpty()) {
            lines.add("");
        }
        List<List<String>> pages = new ArrayList<>();
        for (int index = 0; index < lines.size(); index += PDF_LINE_LIMIT) {
            pages.add(lines.subList(index, Math.min(index + PDF_LINE_LIMIT, lines.size())));
        }
        return pages;
    }

    /**
     * 包装长行
     *
     * @param line  原始行
     * @param lines 目标行集合
     */
    private void wrapLine(String line, List<String> lines) {
        String value = line.replace("\t", "    ");
        if (value.isEmpty()) {
            lines.add("");
            return;
        }
        for (int index = 0; index < value.length(); index += PDF_LINE_LENGTH) {
            lines.add(value.substring(index, Math.min(index + PDF_LINE_LENGTH, value.length())));
        }
    }

    /**
     * 生成页面对象列表
     *
     * @param pageCount 页数
     * @return String 返回 PDF 页面对象引用
     */
    private String pageKids(int pageCount) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < pageCount; index++) {
            builder.append(6 + index * 2).append(" 0 R ");
        }
        return builder.toString();
    }

    /**
     * 生成 PDF 页面内容
     *
     * @param lines 页面文本行
     * @return String 返回 PDF 内容流
     */
    private String pdfContent(List<String> lines) {
        StringBuilder builder = new StringBuilder("BT /F1 10 Tf 50 800 Td 14 TL\n");
        for (String line : lines) {
            builder.append("<").append(hex(line)).append("> Tj T*\n");
        }
        builder.append("ET");
        return builder.toString();
    }

    /**
     * 转换为 UTF-16BE 十六进制文本
     *
     * @param value 原文
     * @return String 返回十六进制文本
     */
    private String hex(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_16BE);
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte item : bytes) {
            builder.append(String.format("%02X", item));
        }
        return builder.toString();
    }

    /**
     * 写入 PDF 对象
     *
     * @param output       输出流
     * @param offsets      偏移集合
     * @param objectNumber 对象编号
     * @param body         对象内容
     */
    private void writeObject(ByteArrayOutputStream output, List<Integer> offsets, int objectNumber, String body) {
        offsets.set(objectNumber, output.size());
        write(output, objectNumber + " 0 obj\n" + body + "\nendobj\n");
    }

    /**
     * 写入 PDF 流对象
     *
     * @param output       输出流
     * @param offsets      偏移集合
     * @param objectNumber 对象编号
     * @param content      内容流
     */
    private void writeStreamObject(ByteArrayOutputStream output, List<Integer> offsets, int objectNumber, String content) {
        offsets.set(objectNumber, output.size());
        byte[] bytes = content.getBytes(StandardCharsets.ISO_8859_1);
        write(output, objectNumber + " 0 obj\n<< /Length " + bytes.length + " >>\nstream\n");
        output.writeBytes(bytes);
        write(output, "\nendstream\nendobj\n");
    }

    /**
     * 写入 PDF 字符串
     *
     * @param output 输出流
     * @param value  字符串
     */
    private void write(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.ISO_8859_1));
    }
}
