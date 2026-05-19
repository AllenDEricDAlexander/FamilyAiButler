package top.egon.familyaibutler.family.domain.dto;

import com.nulabinc.zxcvbn.AttackTimes;
import com.nulabinc.zxcvbn.Feedback;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.dto
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
@Schema(name = "StrengthDTO", title = "Strength api dto")
public class StrengthDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5108785276179717760L;
    @Schema(title = "破解所需秒数", name = "crackTimeSeconds", type = "List<Double>")
    private AttackTimes.CrackTimeSeconds crackTimeSeconds;
    @Schema(title = "破解所需时间", name = "crackTimesDisplay", type = "List<String>")
    private AttackTimes.CrackTimesDisplay crackTimesDisplay;
    @Schema(title = "密码强度", name = "score", type = "int")
    private int score;
    @Schema(title = "密码强度反馈", name = "feedback", type = "Feedback")
    private Feedback feedback;
}