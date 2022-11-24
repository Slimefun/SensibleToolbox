package io.github.thebusybiscuit.sensibletoolbox.helpers;

import javax.annotation.Nonnull;

public class WordUtils {
    public static String wrap(@Nonnull String s, int w) {
        StringBuilder sb = new StringBuilder();

        char[] ch = s.toCharArray();
        for(int i = 0; i < ch.length; i++) {
            int a = i % w;
            sb.append(ch[i]);
            if(a == w-1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
