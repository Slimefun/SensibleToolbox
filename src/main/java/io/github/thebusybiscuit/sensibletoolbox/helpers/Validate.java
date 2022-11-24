package io.github.thebusybiscuit.sensibletoolbox.helpers;

import java.io.IOException;

public class Validate {
    public static void isTrue(boolean b, String s) {
        if(!b) {
            throwException(new IOException(s));
        }
    }

    public static void notNull(Object o, String s) {
        if(o == null) {
            throwException(new IOException(s));
        }
    }

    public static void noNullElements(Object[] objs, String s) {
        if(objs == null) {
            throwException(new IOException("Argument is null!"));
        }
        for(int i = 0; i < objs.length; i++) {
            if(objs[i] == null) {
                throwException(new IOException(s));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwException(Throwable exception, Object dummy) throws T{
        throw (T) exception;
    }

    public static void throwException(Throwable exception) {
        Validate.<RuntimeException>throwException(exception, null);
    }
}
