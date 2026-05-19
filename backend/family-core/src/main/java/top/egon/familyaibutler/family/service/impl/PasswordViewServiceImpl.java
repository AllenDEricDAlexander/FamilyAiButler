package top.egon.familyaibutler.family.service.impl;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.extention.IEgonServiceImpl;
import top.egon.familyaibutler.family.domain.dto.StrengthDTO;
import top.egon.familyaibutler.family.mapper.PasswordViewMapper;
import top.egon.familyaibutler.family.po.PasswordViewPO;
import top.egon.familyaibutler.family.service.PasswordViewService;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family
 * @Author: atluofu
 * @CreateTime: 2025-08-03 09:40:09
 * @Description: (PasswordView)表服务实现类
 * @Version: 1.0
 */
@Service("passwordViewService")
public class PasswordViewServiceImpl extends IEgonServiceImpl<PasswordViewMapper, PasswordViewPO> implements PasswordViewService {

    // 密码强度检测工具
    private static final Zxcvbn ZXCVBN = new Zxcvbn();
    // 敏感词
    private static final List<String> SENSITIVE_WORDS = Arrays.asList("family", "ai", "butler");
    // 键盘连续检测
    private static final String KEYBOARD_SEQUENCE = "(q[1w]|w[2e]|e[3r]|r[4t]|t[5y]|y[6u]|u[7i]|i[8o]|o[9p]|p[0]){3,}";
    // 最小评分要求（根据业务调整，建议≥3）
    private static final int MIN_SCORE = 3;
    // 最小密码长度
    private static final int MIN_LENGTH = 8;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";


    /**
     * @description: 校验密码强度信息
     * @author: atluofu
     * @date: 2025/8/3 12:22
     * @param: password
     * @return: Strength
     **/
    public static StrengthDTO checkStrength(String password) {
        Strength measure = ZXCVBN.measure(password);
        return StrengthDTO.builder()
                .crackTimeSeconds(measure.getCrackTimeSeconds())
                .crackTimesDisplay(measure.getCrackTimesDisplay())
                .score(measure.getScore())
                .feedback(measure.getFeedback())
                .build();
    }

    /**
     * @description: 综合验证密码强度（ZXCVBN+自定义补漏）
     * @author: atluofu
     * @date: 2025/8/3 10:40
     * @param: password
     * @return: boolean
     **/
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        Strength result = ZXCVBN.measure(password, SENSITIVE_WORDS);
        if (result.getScore() < MIN_SCORE) {
            return false;
        }
        return !hasContinuousSequence(password) && !hasRepeatedChars(password) && !hasMixedSpatialPattern(password);
    }

    /**
     * @description: 检测4位以上连续字符（数字或字母）
     * @author: atluofu
     * @date: 2025/8/3 10:39
     * @param: password
     * @return: boolean
     **/
    private static boolean hasContinuousSequence(String password) {
        // 数字连续（如1234）或字母连续（如abcd）
        Pattern pattern = Pattern.compile("(\\d{4,})|([a-zA-Z]{4,})");
        Matcher matcher = pattern.matcher(password);
        if (matcher.find()) {
            String seq = matcher.group();
            // 验证是否真的连续（如1234是连续，1245不是）
            for (int i = 1; i < seq.length(); i++) {
                if (seq.charAt(i) - seq.charAt(i - 1) != 1) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @description: 检测4位以上重复字符
     * @author: atluofu
     * @date: 2025/8/3 10:38
     * @parm: password
     * @retrn: boolean
     **/
    private static boolean hasRepeatedChars(String password) {
        Pattern pattern = Pattern.compile("(.)\\1{3,}");
        return pattern.matcher(password).find();
    }

    /**
     * @description: 检测键盘混合连续模式（如q1w2e3）
     * @author: atluofu
     * @date: 2025/8/3 10:37
     * @param: password
     * @return: boolean
     **/
    private static boolean hasMixedSpatialPattern(String password) {
        return Pattern.compile(KEYBOARD_SEQUENCE, Pattern.CASE_INSENSITIVE).matcher(password).find();
    }

    /**
     * @description: 密码生成
     * @author: atluofu
     * @date: 2025/7/31 22:09
     * @param:
     * @return:
     **/
    public static String generatePassword(int length, boolean needSpecialCharacters, String realSpecialCharacters) {
        StringBuilder password = new StringBuilder(length);
        SecureRandom random = new SecureRandom();
        // 确保密码包含至少一个大写字母、小写字母、数字和特殊字符
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        String allCharacters = null;
        if (needSpecialCharacters) {
            password.append(realSpecialCharacters.charAt(random.nextInt(realSpecialCharacters.length())));
            allCharacters = UPPERCASE + LOWERCASE + DIGITS + realSpecialCharacters;
        } else {
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
            allCharacters = UPPERCASE + LOWERCASE + DIGITS;
        }
        // 随机选择剩余的字符
        for (int i = 4; i < length; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }
        return shuffleString(password.toString());
    }

    /**
     * @description: 打乱密码顺序
     * @author: atluofu
     * @date: 2025/7/31 22:08
     * @parm: a
     * @retrn: a
     **/
    private static String shuffleString(String input) {
        StringBuilder shuffled = new StringBuilder(input);
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < shuffled.length(); i++) {
            int j = random.nextInt(shuffled.length());
            char temp = shuffled.charAt(i);
            shuffled.setCharAt(i, shuffled.charAt(j));
            shuffled.setCharAt(j, temp);
        }
        return shuffled.toString();
    }
}

