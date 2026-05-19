package top.egon.familyaibutler.common.exception;


import lombok.Getter;
import lombok.ToString;

/**
 * @BelongsProject: top\egon\familyaibutler\common\exception\BusinessExceptionEnum.java
 * @BelongsPackage: top.egon.familyaibutler.common.exception
 * @Author: atluofu
 * @CreateTime: 2025/8/1  22:09
 * @Description: 全局常见异常枚举
 * @Version: 1.0
 */
@ToString
@Getter
public enum BusinessExceptionEnum {

    MEMBER_MOBILE_EXIST("手机号已注册"),
    MEMBER_MOBILE_NOT_EXIST("请先获取短信验证码"),
    MEMBER_MOBILE_CODE_ERROR("短信验证码错误"),

    CONFIRM_ORDER_TICKET_COUNT_ERROR("余票不足"),
    CONFIRM_ORDER_EXCEPTION("服务器忙，请稍候重试"),
    CONFIRM_ORDER_LOCK_FAIL("当前抢票人数过多，请稍候重试"),
    CONFIRM_ORDER_FLOW_EXCEPTION("当前抢票人数太多了，请稍候重试"),
    CONFIRM_ORDER_SK_TOKEN_FAIL("当前抢票人数过多，请5秒后重试"),
    ;

    private final String desc;

    BusinessExceptionEnum(String desc) {
        this.desc = desc;
    }
}
