package io.github.thebusybiscuit.sensibletoolbox.helpers;

public class WordUtils {
    public static String wrap(String s, int w) {
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
