/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject
 * @FileName: AccessTokenPO.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:20
 * @Description: 访问令牌数据对象文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject
 * @ClassName: AccessTokenPO
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:20
 * @Description: 访问令牌数据对象
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@With
@Accessors(chain = true)
@Builder
@TableName("uaa_access_token")
public class AccessTokenPO {
    @TableId(value = "access_token", type = IdType.INPUT)
    private String accessTokenHash;
    private String accountId;
    private String profileId;
    private String clientId;
    private String sessionId;
    private String deviceId;
    private Long authVersion;
    private Long entitlementVersion;
    private String riskLevel;
    private Date expiresAt;
    @TableLogic
    private Boolean deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    @Version
    private Integer version;
}
