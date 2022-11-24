package io.github.thebusybiscuit.sensibletoolbox.helpers;

public class StringUtils {
    public static String repeat(String z, int i) {
        StringBuilder sb = new StringBuilder();
        for(int a = 0; a < i; a++) {
            sb.append(z);
        }
        return sb.toString();
    }

    public static boolean isNumeric(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }
}
