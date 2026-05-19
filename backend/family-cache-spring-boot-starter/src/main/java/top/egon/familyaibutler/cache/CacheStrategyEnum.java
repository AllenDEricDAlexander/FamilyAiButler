package top.egon.familyaibutler.cache;

import lombok.Getter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.cache
 * @ClassName: CacheStrategyEnum
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-31Day-19:30
 * @Description: 缓存操作策略枚举
 * 定义缓存的常见操作类型，用于标识缓存操作意图
 * @Version: 1.0
 */
@Getter
public enum CacheStrategyEnum {

    /**
     * 缓存删除策略：移除指定键的缓存
     * 适用场景：当数据被删除或失效时，清除对应的缓存项
     */
    EVICT("evict", "删除指定键的缓存"),

    /**
     * 缓存更新策略：更新或新增指定键的缓存
     * 适用场景：当数据被修改或新增时，同步更新缓存内容
     */
    UPDATE("update", "更新或新增指定键的缓存"),

    /**
     * 缓存清空策略：清除所有缓存（通常指特定命名空间或全量缓存）
     * 适用场景：批量数据更新、缓存一致性维护等需要全量清理的场景
     */
    CLEAR("clear", "清空缓存（全量或指定范围）");

    /**
     * 策略编码：用于序列化和传输（如缓存同步消息中标识操作类型）
     */
    private final String code;

    /**
     * 策略描述：说明该策略的具体用途
     */
    private final String description;

    CacheStrategyEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举实例
     * 用于从缓存消息中解析操作类型
     *
     * @param code 策略编码
     * @return 对应的枚举实例，若未匹配则返回null
     */
    public static CacheStrategyEnum getByCode(String code) {
        for (CacheStrategyEnum strategy : values()) {
            if (strategy.code.equalsIgnoreCase(code)) {
                return strategy;
            }
        }
        return null;
    }
}