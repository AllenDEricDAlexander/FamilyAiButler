package top.egon.familyaibutler.family.domain.passwordview.service;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.domain.passwordview.model.valueobject.StrengthDTO;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.passwordview.service
 * @ClassName: PasswordDomainService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 密码生成和强度校验领域服务
 * @Version: 1.0
 */
@Service
public class PasswordDomainService {
    private static final Zxcvbn ZXCVBN = new Zxcvbn();
    private static final List<String> SENSITIVE_WORDS = Arrays.asList("family", "ai", "butler");
    private static final String KEYBOARD_SEQUENCE = "(q[1w]|w[2e]|e[3r]|r[4t]|t[5y]|y[6u]|u[7i]|i[8o]|o[9p]|p[0]){3,}";
    private static final int MIN_SCORE = 3;
    private static final int MIN_LENGTH = 8;
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";

    /**
     * 校验密码强度信息。
     *
     * @param password 密码
     * @return 密码强度信息
     */
    public StrengthDTO checkStrength(String password) {
        Strength measure = ZXCVBN.measure(password);
        return StrengthDTO.builder()
                .crackTimeSeconds(measure.getCrackTimeSeconds())
                .crackTimesDisplay(measure.getCrackTimesDisplay())
                .score(measure.getScore())
                .feedback(measure.getFeedback())
                .build();
    }

    /**
     * 综合验证密码强度。
     *
     * @param password 密码
     * @return 是否通过强度校验
     */
    public boolean isValidPassword(String password) {
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
     * 生成随机密码。
     *
     * @param length                密码长度
     * @param needSpecialCharacters 是否需要特殊字符
     * @param specialCharacters     特殊字符集合
     * @return 随机密码
     */
    public String generatePassword(int length, boolean needSpecialCharacters, String specialCharacters) {
        StringBuilder password = new StringBuilder(length);
        SecureRandom random = new SecureRandom();
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        String allCharacters;
        if (needSpecialCharacters) {
            password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));
            allCharacters = UPPERCASE + LOWERCASE + DIGITS + specialCharacters;
        } else {
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
            allCharacters = UPPERCASE + LOWERCASE + DIGITS;
        }
        for (int i = 4; i < length; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }
        return shuffleString(password.toString());
    }

    /**
     * 检测 4 位以上连续字符。
     *
     * @param password 密码
     * @return 是否存在连续字符
     */
    private boolean hasContinuousSequence(String password) {
        Pattern pattern = Pattern.compile("(\\d{4,})|([a-zA-Z]{4,})");
        Matcher matcher = pattern.matcher(password);
        if (matcher.find()) {
            String seq = matcher.group();
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
     * 检测 4 位以上重复字符。
     *
     * @param password 密码
     * @return 是否存在重复字符
     */
    private boolean hasRepeatedChars(String password) {
        Pattern pattern = Pattern.compile("(.)\\1{3,}");
        return pattern.matcher(password).find();
    }

    /**
     * 检测键盘混合连续模式。
     *
     * @param password 密码
     * @return 是否存在键盘连续模式
     */
    private boolean hasMixedSpatialPattern(String password) {
        return Pattern.compile(KEYBOARD_SEQUENCE, Pattern.CASE_INSENSITIVE).matcher(password).find();
    }

    /**
     * 打乱密码顺序。
     *
     * @param input 原始密码
     * @return 打乱后的密码
     */
    private String shuffleString(String input) {
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
