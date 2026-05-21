/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.gateway
 * @FileName: ProfileGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Profile 领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.account.gateway;

import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Profile;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.gateway
 * @ClassName: ProfileGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Profile 领域网关
 * @Version: 1.0
 */
public interface ProfileGateway {

    /**
     * 批量保存 Profile。
     *
     * @param profiles Profile 列表
     * @return 保存后的 Profile 列表
     */
    List<Profile> saveAll(List<Profile> profiles);

    /**
     * 保存单个 Profile。
     *
     * @param profile Profile 聚合
     * @return 保存后的 Profile
     */
    Profile save(Profile profile);

    /**
     * 按 Profile ID 查询 Profile。
     *
     * @param profileId Profile ID
     * @return Profile 聚合
     */
    Optional<Profile> findByProfileId(String profileId);

    /**
     * 按账号 ID 查询 Profile 列表。
     *
     * @param accountId 账号 ID
     * @return Profile 列表
     */
    List<Profile> findByAccountId(String accountId);
}
