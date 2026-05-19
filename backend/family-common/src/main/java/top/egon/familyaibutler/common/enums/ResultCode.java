package top.egon.familyaibutler.common.enums;

import lombok.Getter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.enums
 * @ClassName: ResultCode
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-18:56
 * @Description: 接口返回-业务状态码
 * 10000 ok
 * 100** auth failed
 * 200** parameter error
 * 300** 程序bug错误 PS: 此枚举不提供，此类异常需要开发或者运维接入处理，如有需要，自定义传入Result即可，编码规范-内部维护全局错误状态码文档
 * 400** 余额不足
 * @Version: 1.0
 */
@Getter
public enum ResultCode {
    SUCCESS(10000, "操作成功"),
    INVALID_USER_KEY(10001, "key不正确或过期"),
    INSUFFICIENT_PRIVILEGES(100012, "权限不足，服务请求被拒绝"),
    QPS_HAS_EXCEEDED_THE_LIMIT(10019, "使用的某个服务总QPS超限"),
    INVALID_REQUEST(10026, "账号处于被封禁状态"),
    INVALID_PARAMS(20000, "请求参数非法"),
    MISSING_REQUIRED_PARAMS(20001, "缺少必填参数"),
    ILLEGAL_REQUEST(20002, "请求协议非法"),
    UNKNOWN_ERROR(20003, "其他未知错误"),
    INVALID_PARAM(30000, "服务响应失败，请联系运维处理"),
    QUOTA_PLAN_RUN_OUT(40000, "余额耗尽"),
    SERVICE_EXPIRED(40001, "购买服务到期"),
    ABROAD_QUOTA_PLAN_RUN_OUT(40002, "海外服务余额耗尽");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
