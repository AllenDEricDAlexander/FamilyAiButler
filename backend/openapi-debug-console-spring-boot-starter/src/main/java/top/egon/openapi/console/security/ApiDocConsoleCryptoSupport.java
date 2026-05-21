/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.security
 * @FileName: ApiDocConsoleCryptoSupport.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:45
 * @Description: OpenAPI 调试文档控制台加密工具文件
 * @Version: 1.0
 */
package top.egon.openapi.console.security;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.security
 * @ClassName: ApiDocConsoleCryptoSupport
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:45
 * @Description: OpenAPI 调试文档控制台加密工具
 * @Version: 1.0
 */
public final class ApiDocConsoleCryptoSupport {

    private static final int AES_GCM_IV_LENGTH = 12;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 构造方法
     */
    private ApiDocConsoleCryptoSupport() {
    }

    /**
     * 生成随机 URL 安全令牌
     *
     * @param byteLength 随机字节长度
     * @return String 返回 URL 安全令牌
     */
    public static String randomToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return base64Url(bytes);
    }

    /**
     * 生成 HmacSHA256 URL 安全摘要
     *
     * @param key   签名密钥
     * @param value 原文
     * @return String 返回 URL 安全摘要
     */
    public static String hmacSha256Base64Url(String key, String value) {
        return base64Url(hmacSha256Bytes(key.getBytes(StandardCharsets.UTF_8), value));
    }

    /**
     * 生成 HmacSHA256 十六进制摘要
     *
     * @param key   签名密钥
     * @param value 原文
     * @return String 返回十六进制摘要
     */
    public static String hmacSha256Hex(byte[] key, String value) {
        return bytesToHex(hmacSha256Bytes(key, value));
    }

    /**
     * 生成 HmacSHA256 摘要字节
     *
     * @param key   签名密钥
     * @param value 原文
     * @return byte[] 返回摘要字节
     */
    public static byte[] hmacSha256Bytes(byte[] key, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
            mac.init(keySpec);
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("接口文档平台请求签名失败", e);
        }
    }

    /**
     * 生成 SHA256 十六进制摘要
     *
     * @param value 原文
     * @return String 返回十六进制摘要
     */
    public static String sha256Hex(String value) {
        return bytesToHex(sha256Bytes(value));
    }

    /**
     * 生成 SHA256 摘要字节
     *
     * @param value 原文
     * @return byte[] 返回摘要字节
     */
    public static byte[] sha256Bytes(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不存在", e);
        }
    }

    /**
     * 加密会话令牌载荷
     *
     * @param sessionSecret 会话密钥
     * @param payload       明文载荷
     * @return String 返回加密载荷
     */
    public static String encryptPayload(String sessionSecret, String payload) {
        try {
            byte[] iv = new byte[AES_GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(sessionSecret), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return base64Url(iv) + ":" + base64Url(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("接口文档平台会话加密失败", e);
        }
    }

    /**
     * 解密会话令牌载荷
     *
     * @param sessionSecret 会话密钥
     * @param payloadValue  加密载荷
     * @return String 返回明文载荷
     */
    public static String decryptPayload(String sessionSecret, String payloadValue) {
        try {
            String[] values = payloadValue.split(":", 2);
            if (values.length != 2) {
                return "";
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey(sessionSecret), new GCMParameterSpec(128, Base64.getUrlDecoder().decode(values[0])));
            return new String(cipher.doFinal(Base64.getUrlDecoder().decode(values[1])), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 十六进制字符串转字节数组
     *
     * @param value 十六进制字符串
     * @return byte[] 返回字节数组
     */
    public static byte[] hexToBytes(String value) {
        if (value == null || value.isBlank() || value.length() % 2 != 0) {
            return new byte[0];
        }
        byte[] bytes = new byte[value.length() / 2];
        try {
            for (int i = 0; i < value.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(value.substring(i, i + 2), 16);
            }
        } catch (NumberFormatException e) {
            return new byte[0];
        }
        return bytes;
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return String 返回十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte item : bytes) {
            builder.append(String.format("%02x", item));
        }
        return builder.toString();
    }

    /**
     * 字节数组转 URL 安全 Base64
     *
     * @param bytes 字节数组
     * @return String 返回 URL 安全 Base64
     */
    public static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 常量时间字符串比较
     *
     * @param left  左侧字符串
     * @param right 右侧字符串
     * @return boolean 返回 true 表示相等
     */
    public static boolean constantEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 构造 AES 会话加密密钥
     *
     * @param sessionSecret 会话密钥
     * @return SecretKeySpec 返回 AES 密钥
     */
    private static SecretKeySpec aesKey(String sessionSecret) {
        return new SecretKeySpec(sha256Bytes(sessionSecret), "AES");
    }
}
