package top.egon.familyaibutler.family.domain.passwordview.model.valueobject;

import com.nulabinc.zxcvbn.AttackTimes;
import com.nulabinc.zxcvbn.Feedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.io.Serial;
import java.io.Serializable;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.passwordview.model.valueobject
 * @ClassName: StrengthDTO
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-03Day-12:23
 * @Description: StrengthDTO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@With
@Accessors(chain = true)
@Builder
@EqualsAndHashCode
@DocModel(name = "FamilyPasswordStrengthDTO", description = "密码强度评估结果传输对象")
public class StrengthDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5108785276179717760L;

    /**
     * 破解所需秒数。
     */
    @DocField(description = "破解所需秒数", required = false, example = "{\"onlineNoThrottling10perSecond\":1000000}")
    private AttackTimes.CrackTimeSeconds crackTimeSeconds;

    /**
     * 破解所需时间。
     */
    @DocField(description = "破解所需时间", required = false, example = "{\"onlineNoThrottling10perSecond\":\"11 days\"}")
    private AttackTimes.CrackTimesDisplay crackTimesDisplay;

    /**
     * 密码强度评分。
     */
    @DocField(description = "密码强度评分", required = true, example = "3")
    private int score;

    /**
     * 密码强度反馈。
     */
    @DocField(description = "密码强度反馈", required = false, example = "{\"warning\":\"\",\"suggestions\":[]}")
    private Feedback feedback;
}
