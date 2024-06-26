package potatowoong.potatomallback.global.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MaskingUtils {

    /**
     * 마스킹 처리
     *
     * @param word 원본 단어
     * @return 마스킹된 단어
     */
    public static String masking(final String word) {
        if (StringUtils.isBlank(word)) {
            return "";
        }

        final int nameLength = word.length();
        StringBuilder maskedName = new StringBuilder();

        maskedName.append(word.charAt(0));
        maskedName.append("*".repeat(Math.max(0, nameLength - 2)));

        if (nameLength > 1) {
            maskedName.append(word.charAt(nameLength - 1));
        }

        return maskedName.toString();
    }
}
