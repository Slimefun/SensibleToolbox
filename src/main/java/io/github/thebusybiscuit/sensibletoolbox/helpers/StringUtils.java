package io.github.thebusybiscuit.sensibletoolbox.helpers;

import javax.annotation.Nonnull;

public class StringUtils {
    public static String repeat(@Nonnull String z, int i) {
        StringBuilder sb = new StringBuilder();
        for(int a = 0; a < i; a++) {
            sb.append(z);
        }
        return sb.toString();
    }

    public static boolean isNumeric(@Nonnull String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }
}
