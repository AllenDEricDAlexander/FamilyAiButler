/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.aggregate
 * @FileName: Profile.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Profile 聚合文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.account.model.aggregate;

import top.egon.familyaibutler.uaa.domain.account.model.enums.ProfileType;

import java.util.UUID;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.aggregate
 * @ClassName: Profile
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Profile 聚合
 * @Version: 1.0
 */
public class Profile {
    private final String profileId;
    private final String accountId;
    private final ProfileType profileType;
    private String nickname;
    private String avatar;
    private String language;
    private String region;
    private boolean deleted;

    private Profile(String profileId, String accountId, ProfileType profileType, String nickname) {
        this.profileId = profileId;
        this.accountId = accountId;
        this.profileType = profileType;
        this.nickname = nickname;
        this.language = "zh-CN";
        this.region = "CN";
    }

    /**
     * 创建主 Profile。
     *
     * @param accountId 账号 ID
     * @param nickname  昵称
     * @return Profile 聚合
     */
    public static Profile createMain(String accountId, String nickname) {
        return new Profile("prof_" + UUID.randomUUID(), accountId, ProfileType.MAIN, nickname);
    }

    /**
     * 创建 Profile。
     *
     * @param accountId   账号 ID
     * @param profileType Profile 类型
     * @param nickname    昵称
     * @return Profile 聚合
     */
    public static Profile create(String accountId, ProfileType profileType, String nickname) {
        return new Profile("prof_" + UUID.randomUUID(), accountId, profileType, nickname);
    }

    /**
     * 还原 Profile 聚合。
     *
     * @param profileId   Profile ID
     * @param accountId   账号 ID
     * @param profileType Profile 类型
     * @param nickname    昵称
     * @param avatar      头像
     * @param language    语言
     * @param region      地区
     * @param deleted     删除状态
     * @return Profile 聚合
     */
    public static Profile restore(String profileId, String accountId, ProfileType profileType, String nickname,
                                  String avatar, String language, String region, boolean deleted) {
        Profile profile = new Profile(profileId, accountId, profileType, nickname);
        profile.avatar = avatar;
        profile.language = language;
        profile.region = region;
        profile.deleted = deleted;
        return profile;
    }

    /**
     * 修改 Profile 基础资料。
     *
     * @param nickname 昵称
     * @param avatar   头像
     * @param language 语言
     * @param region   地区
     */
    public void updateBasicInfo(String nickname, String avatar, String language, String region) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.language = language;
        this.region = region;
    }

    /**
     * 删除 Profile。
     */
    public void delete() {
        this.deleted = true;
    }

    /**
     * 获取 Profile ID。
     *
     * @return Profile ID
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * 获取账号 ID。
     *
     * @return 账号 ID
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * 获取 Profile 类型。
     *
     * @return Profile 类型
     */
    public ProfileType getProfileType() {
        return profileType;
    }

    /**
     * 获取昵称。
     *
     * @return 昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 获取头像。
     *
     * @return 头像
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * 获取语言。
     *
     * @return 语言
     */
    public String getLanguage() {
        return language;
    }

    /**
     * 获取地区。
     *
     * @return 地区
     */
    public String getRegion() {
        return region;
    }

    /**
     * 判断 Profile 是否已删除。
     *
     * @return true 表示已删除
     */
    public boolean isDeleted() {
        return deleted;
    }
}
