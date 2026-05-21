package top.egon.familyaibutler.family.domain.passwordview.gateway;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.passwordview.gateway
 * @ClassName: PasswordViewQueryGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码复杂查询网关
 * @Version: 1.0
 */
public interface PasswordViewQueryGateway {

    /**
     * 执行复杂查询。
     *
     * @param query 查询对象
     * @return 查询结果
     */
    List<?> query(Object query);
}
