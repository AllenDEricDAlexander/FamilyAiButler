/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @FileName: FamilyLogUtil.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 家庭日志工具类文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core;

import org.slf4j.MDC;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @ClassName: FamilyLogUtil
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 家庭日志工具类
 * @Version: 1.0
 */
public final class FamilyLogUtil {

    private static final String EMPTY_VALUE = "-";
    private static final int MAX_VALUE_LENGTH = 1000;
    private static final int MAX_COLLECTION_ITEMS = 20;
    private static final String[] BIZ_LOG_FIELD_ORDER = new String[]{
            "biz", "scene", "step", "phase", "bill_type", "bill_id", "bill_no", "biz_id", "biz_uk",
            "status", "expected_status", "from_status", "to_status", "decision", "reason", "result",
            "error_code", "error_msg", "changed", "cost_ms", "msg"
    };

    private FamilyLogUtil() {
    }

    /**
     * 获取当前线程 traceId。
     *
     * @return String 返回当前 traceId
     */
    public static String traceId() {
        return MDC.get(FamilyLogMdcKeys.TRACE_ID);
    }

    /**
     * 获取当前线程 requestId。
     *
     * @return String 返回当前 requestId
     */
    public static String requestId() {
        return MDC.get(FamilyLogMdcKeys.REQUEST_ID);
    }

    /**
     * 为当前线程补齐 traceId。
     *
     * @return String 返回 traceId
     */
    public static String putTraceIdIfAbsent() {
        String traceId = traceId();
        if (isBlank(traceId)) {
            traceId = newId();
            MDC.put(FamilyLogMdcKeys.TRACE_ID, traceId);
        }
        return traceId;
    }

    /**
     * 为当前线程补齐 requestId。
     *
     * @return String 返回 requestId
     */
    public static String putRequestIdIfAbsent() {
        String requestId = requestId();
        if (isBlank(requestId)) {
            requestId = newId();
            MDC.put(FamilyLogMdcKeys.REQUEST_ID, requestId);
        }
        return requestId;
    }

    /**
     * 当值非空白时写入 MDC。
     *
     * @param key   MDC 键
     * @param value MDC 值
     */
    public static void putIfNotBlank(String key, String value) {
        if (!isBlank(key) && !isBlank(value)) {
            MDC.put(key, value);
        }
    }

    /**
     * 复制当前线程全部 MDC。
     *
     * @return Map<String, String> 返回当前线程 MDC 副本
     */
    public static Map<String, String> copyContext() {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return contextMap == null ? new LinkedHashMap<>() : new LinkedHashMap<>(contextMap);
    }

    /**
     * 复制 family 日志组件维护的 MDC。
     *
     * @return Map<String, String> 返回 family 日志 MDC 副本
     */
    public static Map<String, String> copyFamilyContext() {
        Map<String, String> familyContext = new LinkedHashMap<>();
        Map<String, String> contextMap = copyContext();
        for (String key : FamilyLogMdcKeys.familyKeys()) {
            if (!isBlank(contextMap.get(key))) {
                familyContext.put(key, contextMap.get(key));
            }
        }
        return familyContext;
    }

    /**
     * 使用给定上下文恢复 MDC。
     *
     * @param contextMap 目标 MDC 快照
     */
    public static void restore(Map<String, String> contextMap) {
        MDC.clear();
        if (contextMap != null && !contextMap.isEmpty()) {
            MDC.setContextMap(contextMap);
        }
    }

    /**
     * 将给定上下文叠加到当前线程 MDC。
     *
     * @param contextMap 待写入 MDC 上下文
     */
    public static void apply(Map<String, String> contextMap) {
        if (contextMap == null || contextMap.isEmpty()) {
            return;
        }
        contextMap.forEach(FamilyLogUtil::putIfNotBlank);
    }

    /**
     * 清理 family 日志组件维护的 MDC 键。
     */
    public static void clearFamilyKeys() {
        for (String key : FamilyLogMdcKeys.familyKeys()) {
            MDC.remove(key);
        }
    }

    /**
     * 包装 Runnable 以透传当前线程日志上下文。
     *
     * @param runnable 原始 Runnable
     * @return Runnable 返回带日志上下文的 Runnable
     */
    public static Runnable wrap(Runnable runnable) {
        Map<String, String> parentContext = copyContext();
        return () -> {
            Map<String, String> childContext = copyContext();
            try {
                restore(parentContext);
                runnable.run();
            } finally {
                restore(childContext);
            }
        };
    }

    /**
     * 包装 Callable 以透传当前线程日志上下文。
     *
     * @param callable 原始 Callable
     * @param <T>      返回值类型
     * @return Callable<T> 返回带日志上下文的 Callable
     */
    public static <T> Callable<T> wrap(Callable<T> callable) {
        Map<String, String> parentContext = copyContext();
        return () -> {
            Map<String, String> childContext = copyContext();
            try {
                restore(parentContext);
                return callable.call();
            } finally {
                restore(childContext);
            }
        };
    }

    /**
     * 包装 Supplier 以透传当前线程日志上下文。
     *
     * @param supplier 原始 Supplier
     * @param <T>      返回值类型
     * @return Supplier<T> 返回带日志上下文的 Supplier
     */
    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        Map<String, String> parentContext = copyContext();
        return () -> {
            Map<String, String> childContext = copyContext();
            try {
                restore(parentContext);
                return supplier.get();
            } finally {
                restore(childContext);
            }
        };
    }

    /**
     * 包装 Executor 以透传提交线程日志上下文。
     *
     * @param executor 原始执行器
     * @return Executor 返回带日志上下文的执行器
     */
    public static Executor decorateExecutor(Executor executor) {
        return command -> executor.execute(wrap(command));
    }

    /**
     * 创建 DEBUG 级别业务日志构建器。
     *
     * @param logger 业务类日志对象
     * @return BizLogBuilder 返回业务日志构建器
     */
    public static BizLogBuilder bizDebug(org.slf4j.Logger logger) {
        return new BizLogBuilder(logger, LogLevel.DEBUG);
    }

    /**
     * 创建 INFO 级别业务日志构建器。
     *
     * @param logger 业务类日志对象
     * @return BizLogBuilder 返回业务日志构建器
     */
    public static BizLogBuilder bizInfo(org.slf4j.Logger logger) {
        return new BizLogBuilder(logger, LogLevel.INFO);
    }

    /**
     * 创建 WARN 级别业务日志构建器。
     *
     * @param logger 业务类日志对象
     * @return BizLogBuilder 返回业务日志构建器
     */
    public static BizLogBuilder bizWarn(org.slf4j.Logger logger) {
        return new BizLogBuilder(logger, LogLevel.WARN);
    }

    /**
     * 创建 ERROR 级别业务日志构建器。
     *
     * @param logger 业务类日志对象
     * @return BizLogBuilder 返回业务日志构建器
     */
    public static BizLogBuilder bizError(org.slf4j.Logger logger) {
        return new BizLogBuilder(logger, LogLevel.ERROR);
    }

    /**
     * 按顺序读取首个非空白请求头值。
     *
     * @param headerNames 请求头名称列表
     * @param extractor   请求头读取函数
     * @return String 返回首个非空白请求头值
     */
    public static String firstNonBlank(List<String> headerNames, Function<String, String> extractor) {
        if (headerNames == null || extractor == null) {
            return null;
        }
        for (String headerName : headerNames) {
            String headerValue = extractor.apply(headerName);
            if (!isBlank(headerValue)) {
                return headerValue;
            }
        }
        return null;
    }

    /**
     * 将请求头中的 trace 值标准化为 traceId。
     *
     * @param candidate trace 相关请求头值
     * @return String 返回标准化后的 traceId
     */
    public static String normalizeTraceId(String candidate) {
        if (isBlank(candidate)) {
            return null;
        }
        if (candidate.contains("-")) {
            String[] segments = candidate.split("-");
            if (segments.length >= 4 && !isBlank(segments[1])) {
                return segments[1];
            }
        }
        return candidate;
    }

    /**
     * 生成新的日志链路标识。
     *
     * @return String 返回链路标识
     */
    public static String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 判断字符串是否为空白。
     *
     * @param value 待判断字符串
     * @return boolean 返回 true 表示为空白
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 业务日志阶段。
     */
    public enum Phase {
        START, LOAD, CHECK, DECISION, CREATE, UPDATE, DELETE, CALL, SEND, PROCESS, END
    }

    /**
     * 业务日志构建器。
     */
    public static final class BizLogBuilder {

        private final org.slf4j.Logger logger;
        private final LogLevel defaultLevel;
        private final LinkedHashMap<String, Object> fields = new LinkedHashMap<>();

        private BizLogBuilder(org.slf4j.Logger logger, LogLevel defaultLevel) {
            this.logger = logger;
            this.defaultLevel = defaultLevel;
        }

        /**
         * 设置业务域。
         *
         * @param biz 业务域
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder biz(String biz) {
            return field("biz", biz);
        }

        /**
         * 设置业务场景。
         *
         * @param scene 业务场景
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder scene(String scene) {
            return field("scene", scene);
        }

        /**
         * 设置业务步骤。
         *
         * @param step 业务步骤
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder step(String step) {
            return field("step", step);
        }

        /**
         * 设置业务阶段。
         *
         * @param phase 业务阶段
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder phase(Phase phase) {
            return phase == null ? this : phase(phase.name());
        }

        /**
         * 设置业务阶段。
         *
         * @param phase 业务阶段
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder phase(String phase) {
            return field("phase", phase);
        }

        /**
         * 设置单据信息。
         *
         * @param billType 单据类型
         * @param billId   单据 ID
         * @param billNo   单据编号
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder bill(String billType, Object billId, Object billNo) {
            field("bill_type", billType);
            field("bill_id", billId);
            field("bill_no", billNo);
            return this;
        }

        /**
         * 设置业务主键。
         *
         * @param bizId 业务主键
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder bizId(Object bizId) {
            return field("biz_id", bizId);
        }

        /**
         * 设置业务唯一键。
         *
         * @param bizUk 业务唯一键
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder bizUk(Object bizUk) {
            return field("biz_uk", bizUk);
        }

        /**
         * 设置当前状态。
         *
         * @param status 当前状态
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder status(Object status) {
            return field("status", status);
        }

        /**
         * 设置期望状态。
         *
         * @param expectedStatus 期望状态
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder expectedStatus(Object expectedStatus) {
            return field("expected_status", expectedStatus);
        }

        /**
         * 设置状态变更。
         *
         * @param fromStatus 变更前状态
         * @param toStatus   变更后状态
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder statusChange(Object fromStatus, Object toStatus) {
            field("from_status", fromStatus);
            field("to_status", toStatus);
            return this;
        }

        /**
         * 设置分支判断。
         *
         * @param decision 判断结果
         * @param reason   判断原因
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder decision(boolean decision, String reason) {
            field("decision", decision ? "YES" : "NO");
            field("reason", reason);
            return this;
        }

        /**
         * 设置分支判断。
         *
         * @param decision 判断结果
         * @param reason   判断原因
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder decision(String decision, String reason) {
            field("decision", decision);
            field("reason", reason);
            return this;
        }

        /**
         * 设置原因。
         *
         * @param reason 原因
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder reason(String reason) {
            return field("reason", reason);
        }

        /**
         * 设置变更摘要。
         *
         * @param changed 变更摘要
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder changed(String changed) {
            return field("changed", changed);
        }

        /**
         * 设置错误码。
         *
         * @param errorCode 错误码
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder errorCode(String errorCode) {
            return field("error_code", errorCode);
        }

        /**
         * 设置耗时。
         *
         * @param costMs 耗时毫秒
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder costMs(Object costMs) {
            return field("cost_ms", costMs);
        }

        /**
         * 根据开始时间设置耗时。
         *
         * @param startTimeMillis 开始时间毫秒
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder costSince(long startTimeMillis) {
            return field("cost_ms", System.currentTimeMillis() - startTimeMillis);
        }

        /**
         * 设置日志信息。
         *
         * @param msg 日志信息
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder msg(String msg) {
            return field("msg", msg);
        }

        /**
         * 设置扩展字段。
         *
         * @param key   字段名
         * @param value 字段值
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder field(String key, Object value) {
            if (isBlank(key) || value == null) {
                return this;
            }
            if (value instanceof String text && isBlank(text)) {
                return this;
            }
            fields.put(normalizeKey(key), value);
            return this;
        }

        /**
         * 批量设置扩展字段。
         *
         * @param map 扩展字段
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder fields(Map<String, ?> map) {
            if (map == null || map.isEmpty()) {
                return this;
            }
            map.forEach(this::field);
            return this;
        }

        /**
         * 设置批量单据 ID。
         *
         * @param billIds 单据 ID 集合
         * @return BizLogBuilder 返回当前构建器
         */
        public BizLogBuilder billIds(Collection<?> billIds) {
            return field("bill_ids", billIds);
        }

        /**
         * 打印开始日志。
         *
         * @param msg 日志信息
         */
        public void start(String msg) {
            field("result", "START");
            msg(msg);
            log(LogLevel.INFO, null);
        }

        /**
         * 打印成功日志。
         *
         * @param msg 日志信息
         */
        public void success(String msg) {
            field("result", "SUCCESS");
            msg(msg);
            log(defaultLevel, null);
        }

        /**
         * 打印普通业务日志。
         *
         * @param msg 日志信息
         */
        public void log(String msg) {
            msg(msg);
            log(defaultLevel, null);
        }

        /**
         * 打印业务拒绝日志。
         *
         * @param reason 拒绝原因
         */
        public void reject(String reason) {
            field("result", "REJECT");
            reason(reason);
            log(LogLevel.WARN, null);
        }

        /**
         * 打印警告日志。
         *
         * @param msg 日志信息
         */
        public void warn(String msg) {
            field("result", "WARN");
            msg(msg);
            log(LogLevel.WARN, null);
        }

        /**
         * 打印失败日志。
         *
         * @param reason 失败原因
         */
        public void fail(String reason) {
            field("result", "FAIL");
            reason(reason);
            log(LogLevel.ERROR, null);
        }

        /**
         * 打印失败日志。
         *
         * @param reason 失败原因
         * @param e      异常对象
         */
        public void fail(String reason, Throwable e) {
            field("result", "FAIL");
            reason(reason);
            if (e != null && !isBlank(e.getMessage())) {
                field("error_msg", e.getMessage());
            }
            log(LogLevel.ERROR, e);
        }

        /**
         * 输出业务日志。
         *
         * @param level 日志级别
         * @param e     异常对象
         */
        private void log(LogLevel level, Throwable e) {
            if (logger == null || level == null || !isEnabled(logger, level)) {
                return;
            }
            String logMessage = buildBizLogString(fields);
            switch (level) {
                case DEBUG -> logger.debug(logMessage);
                case INFO -> logger.info(logMessage);
                case WARN -> {
                    if (e == null) {
                        logger.warn(logMessage);
                    } else {
                        logger.warn(logMessage, e);
                    }
                }
                case ERROR -> {
                    if (e == null) {
                        logger.error(logMessage);
                    } else {
                        logger.error(logMessage, e);
                    }
                }
            }
        }
    }

    /**
     * 业务日志级别。
     */
    private enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    /**
     * 判断日志级别是否启用。
     *
     * @param logger 日志对象
     * @param level  日志级别
     * @return boolean 返回 true 表示启用
     */
    private static boolean isEnabled(org.slf4j.Logger logger, LogLevel level) {
        return switch (level) {
            case DEBUG -> logger.isDebugEnabled();
            case INFO -> logger.isInfoEnabled();
            case WARN -> logger.isWarnEnabled();
            case ERROR -> logger.isErrorEnabled();
        };
    }

    /**
     * 构建业务日志字符串。
     *
     * @param fields 日志字段
     * @return String 返回业务日志字符串
     */
    private static String buildBizLogString(LinkedHashMap<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        Set<String> writtenKeys = new HashSet<>();
        for (String key : BIZ_LOG_FIELD_ORDER) {
            if (fields.containsKey(key)) {
                appendField(builder, key, fields.get(key));
                writtenKeys.add(key);
            }
        }
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!writtenKeys.contains(entry.getKey())) {
                appendField(builder, entry.getKey(), entry.getValue());
            }
        }
        return builder.toString();
    }

    /**
     * 追加单个日志字段。
     *
     * @param builder 日志字符串
     * @param key     字段名
     * @param value   字段值
     */
    private static void appendField(StringBuilder builder, String key, Object value) {
        if (builder == null || isBlank(key) || value == null) {
            return;
        }
        String normalizedKey = normalizeKey(key);
        String formattedValue = formatValue(normalizedKey, value);
        if (isBlank(normalizedKey) || isBlank(formattedValue)) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append(normalizedKey).append('=').append(formattedValue);
    }

    /**
     * 格式化日志字段值。
     *
     * @param key   字段名
     * @param value 字段值
     * @return String 返回格式化后的字段值
     */
    private static String formatValue(String key, Object value) {
        String text = objectToString(value);
        text = normalizeValue(text);
        text = maskIfNecessary(key, text);
        text = truncate(text, MAX_VALUE_LENGTH);
        if (isBlank(text)) {
            text = EMPTY_VALUE;
        }
        if (needQuote(text)) {
            return "\"" + escapeQuote(text) + "\"";
        }
        return text;
    }

    /**
     * 将对象转为日志字符串。
     *
     * @param value 字段值
     * @return String 返回日志字符串
     */
    private static String objectToString(Object value) {
        if (value == null) {
            return EMPTY_VALUE;
        }
        if (value instanceof Collection<?> collection) {
            return collectionToString(collection);
        }
        if (value instanceof Map<?, ?> map) {
            return mapToString(map);
        }
        if (value.getClass().isArray()) {
            return arrayToString(value);
        }
        return String.valueOf(value);
    }

    /**
     * 将集合转为日志字符串。
     *
     * @param collection 集合值
     * @return String 返回日志字符串
     */
    private static String collectionToString(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return EMPTY_VALUE;
        }
        StringBuilder builder = new StringBuilder();
        int count = 0;
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext() && count < MAX_COLLECTION_ITEMS) {
            if (count > 0) {
                builder.append(',');
            }
            builder.append(String.valueOf(iterator.next()));
            count++;
        }
        if (collection.size() > MAX_COLLECTION_ITEMS) {
            builder.append("...total=").append(collection.size());
        }
        return builder.toString();
    }

    /**
     * 将数组转为日志字符串。
     *
     * @param array 数组值
     * @return String 返回日志字符串
     */
    private static String arrayToString(Object array) {
        int length = Array.getLength(array);
        if (length == 0) {
            return EMPTY_VALUE;
        }
        StringBuilder builder = new StringBuilder();
        int max = Math.min(length, MAX_COLLECTION_ITEMS);
        for (int i = 0; i < max; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(String.valueOf(Array.get(array, i)));
        }
        if (length > MAX_COLLECTION_ITEMS) {
            builder.append("...total=").append(length);
        }
        return builder.toString();
    }

    /**
     * 将 Map 转为日志字符串。
     *
     * @param map Map 值
     * @return String 返回日志字符串
     */
    private static String mapToString(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return EMPTY_VALUE;
        }
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (count >= MAX_COLLECTION_ITEMS) {
                break;
            }
            if (count > 0) {
                builder.append(',');
            }
            builder.append(String.valueOf(entry.getKey())).append(':').append(String.valueOf(entry.getValue()));
            count++;
        }
        if (map.size() > MAX_COLLECTION_ITEMS) {
            builder.append("...total=").append(map.size());
        }
        return builder.toString();
    }

    /**
     * 规范化日志字段名。
     *
     * @param key 字段名
     * @return String 返回规范化字段名
     */
    private static String normalizeKey(String key) {
        if (isBlank(key)) {
            return null;
        }
        return key.trim().replaceAll("[^a-zA-Z0-9_\\-.]", "_");
    }

    /**
     * 规范化日志字段值。
     *
     * @param value 字段值
     * @return String 返回规范化字段值
     */
    private static String normalizeValue(String value) {
        if (value == null) {
            return EMPTY_VALUE;
        }
        return value.trim().replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
    }

    /**
     * 判断字段值是否需要加引号。
     *
     * @param text 字段值
     * @return boolean 返回 true 表示需要加引号
     */
    private static boolean needQuote(String text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (Character.isWhitespace(current) || current == '=' || current == '"' || current == '\'') {
                return true;
            }
        }
        return false;
    }

    /**
     * 转义双引号。
     *
     * @param text 字段值
     * @return String 返回转义后字段值
     */
    private static String escapeQuote(String text) {
        return text == null ? EMPTY_VALUE : text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 截断超长字段。
     *
     * @param text      字段值
     * @param maxLength 最大长度
     * @return String 返回截断后字段值
     */
    private static String truncate(String text, int maxLength) {
        if (text == null || maxLength <= 0 || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...truncated";
    }

    /**
     * 按字段名执行基础脱敏。
     *
     * @param key   字段名
     * @param value 字段值
     * @return String 返回脱敏后字段值
     */
    private static String maskIfNecessary(String key, String value) {
        if (isBlank(key) || isBlank(value)) {
            return value;
        }
        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("password") || lowerKey.contains("passwd") || lowerKey.contains("pwd")
                || lowerKey.contains("token") || lowerKey.contains("secret")
                || lowerKey.contains("authorization") || lowerKey.contains("cookie")) {
            return "***";
        }
        if (lowerKey.contains("phone") || lowerKey.contains("mobile")) {
            return maskMiddle(value, 3, 4);
        }
        if (lowerKey.contains("bank_account") || lowerKey.contains("bank_card") || lowerKey.contains("card_no")
                || lowerKey.contains("id_card") || lowerKey.contains("cert_no") || lowerKey.contains("identity_no")) {
            return maskMiddle(value, 4, 4);
        }
        if (lowerKey.contains("email")) {
            return maskEmail(value);
        }
        return value;
    }

    /**
     * 中间脱敏。
     *
     * @param value        字段值
     * @param prefixLength 保留前缀长度
     * @param suffixLength 保留后缀长度
     * @return String 返回脱敏值
     */
    private static String maskMiddle(String value, int prefixLength, int suffixLength) {
        if (isBlank(value)) {
            return value;
        }
        int length = value.length();
        if (length <= prefixLength + suffixLength) {
            return "***";
        }
        return value.substring(0, prefixLength) + "****" + value.substring(length - suffixLength);
    }

    /**
     * 邮箱脱敏。
     *
     * @param value 邮箱值
     * @return String 返回脱敏值
     */
    private static String maskEmail(String value) {
        if (isBlank(value)) {
            return value;
        }
        int index = value.indexOf('@');
        if (index <= 0) {
            return "***";
        }
        return value.charAt(0) + "****" + value.substring(index);
    }
}
