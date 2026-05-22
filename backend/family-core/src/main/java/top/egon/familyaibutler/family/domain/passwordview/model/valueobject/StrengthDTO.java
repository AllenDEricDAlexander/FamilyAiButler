package top.egon.familyaibutler.family.domain.passwordview.model.valueobject;

import com.nulabinc.zxcvbn.AttackTimes;
import com.nulabinc.zxcvbn.Feedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
@DocModel(name = "StrengthDTO", description = "Strength api dto")
public class StrengthDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5108785276179717760L;
    @DocField(description = "破解所需秒数")
    private AttackTimes.CrackTimeSeconds crackTimeSeconds;
    @DocField(description = "破解所需时间")
    private AttackTimes.CrackTimesDisplay crackTimesDisplay;
    @DocField(description = "密码强度", example = "3")
    private int score;
    @DocField(description = "密码强度反馈")
    private Feedback feedback;
}
