package me.desht.dhutils.text;

/**
 * Programmer: Jacob Scott
 * Program Name: Str
 * Description:
 * Date: Mar 31, 2011
 */

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author jacob
 */
public class Str extends OutputStream {

    protected StringBuilder text = new StringBuilder();

    public static String argStr(String[] s) {
        return argStr(s, " ", 0);
    }

    public static String argStr(String[] s, int start) {
        return argStr(s, " ", start);
    }

    public static String argStr(String[] s, String sep) {
        return argStr(s, sep, 0);
    }

    public static String argStr(String[] s, String sep, int start) {
        StringBuilder ret = new StringBuilder();
        if (s != null) {
            for (int i = start; i < s.length; ++i) {
                ret.append(s[i]);
                if (i + 1 < s.length) {
                    ret.append(sep);
                }
            }
        }
        return ret.toString();
    }

    public static String argStr(String[] s, String sep, int start, int length) {
        StringBuilder ret = new StringBuilder();
        if (s != null) {
            for (int i = start, j = 0; i < s.length && j < length; ++i, ++j) {
                ret.append(s[i]);
                if (i + 1 < s.length) {
                    ret.append(sep);
                }
            }
        }
        return ret.toString();
    }

    public static boolean isIn(String input, String[] check) {
        input = input.trim();
        for (String c : check) {
            if (input.equalsIgnoreCase(c.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIn(String input, String check) {
        String[] comms = check.split(",");
        input = input.trim();
        for (String c : comms) {
            if (input.equalsIgnoreCase(c.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean startIsIn(String input, String check) {
        String[] comms = check.split(",");
        for (String c : comms) {
            if (input.length() >= c.length()) {
                if (input.substring(0, c.length()).equalsIgnoreCase(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean startIsIn(String input, String[] check) {
        for (String c : check) {
            if (input.length() >= c.length()) {
                if (input.substring(0, c.length()).equalsIgnoreCase(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int count(String str, String find) {
        int c = 0;
        for (int i = 0; i < str.length() - find.length(); ++i) {
            if (str.substring(i, i + find.length()).equals(find)) {
                ++c;
            }
        }
        return c;
    }

    public static int count(String str, char find) {
        int c = 0;
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == find) {
                ++c;
            }
        }
        return c;
    }

    public static int countIgnoreCase(String str, String find) {
        int c = 0;
        for (int i = 0; i < str.length() - find.length(); ++i) {
            if (str.substring(i, i + find.length()).equalsIgnoreCase(find)) {
                ++c;
            }
        }
        return c;
    }

    public static int indexOf(String[] array, String search) {
        if (array != null && array.length > 0) {
            for (int i = array.length - 1; i >= 0; --i) {
                if (array[i].equals(search)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOfIgnoreCase(String[] array, String search) {
        for (int i = array.length - 1; i >= 0; --i) {
            if (array[i].equalsIgnoreCase(search)) {
                return i;
            }
        }
        return -1;
    }

    public static String getStackStr(Exception err) {
        if (err == null) {// || err.getCause() == null) {
            return "";
        }
        Str stackoutstream = new Str();
        PrintWriter stackstream = new PrintWriter(stackoutstream);
        err.printStackTrace(stackstream);
        stackstream.flush();
        stackstream.close();
        return stackoutstream.text.toString();

    }

    /**
     * pads str on the right (space-padded) (left-align)
     * 
     * @param str
     * @param len
     * @return right-padded string
     */
    public static String padRight(String str, int len) {
        return padRight(str, len, ' ');
    }

    /**
     * pads str on the right with pad (left-align)
     * 
     * @param str
     * @param len
     * @param pad
     * @return right-padded string
     */
    public static String padRight(String str, int len, char pad) {
        StringBuilder ret = new StringBuilder(str);
        for (int i = str.length(); i < len; ++i) {
            ret.append(pad);
        }
        return ret.toString();
    }

    /**
     * pads str on the left (space-padded) (right-align)
     * 
     * @param str
     * @param len
     * @return left-padded string
     */
    public static String padLeft(String str, int len) {
        return repeat(' ', len - str.length()) + str;
    }

    /**
     * pads str on the left with pad (right-align)
     * 
     * @param str
     * @param len
     * @param pad
     * @return left-padded string
     */
    public static String padLeft(String str, int len, char pad) {
        return repeat(pad, len - str.length()) + str;
    }

    /**
     * pads str on the left & right (space-padded) (center-align)
     * 
     * @param str
     * @param len
     * @return center-aligned string
     */
    public static String padCenter(String str, int len) {
        len -= str.length();
        int prepad = len / 2;
        return repeat(' ', prepad) + str + repeat(' ', len - prepad);
    }

    /**
     * pads str on the left & right with pad (center-align)
     * 
     * @param str
     * @param len
     * @param pad
     * @return center-aligned string
     */
    public static String padCenter(String str, int len, char pad) {
        len -= str.length();
        int prepad = len / 2;
        return repeat(pad, prepad) + str + repeat(pad, len - prepad);
    }

    public static String strWordWrap(String str, int width) {
        return strWordWrap(str, width, 0, ' ');
    }

    public static String strWordWrap(String str, int width, int tab) {
        return strWordWrap(str, width, tab, ' ');
    }

    public static String strWordWrap(String str, int width, int tab, char tabChar) {
        String ret = "";
        while (str.length() > 0) {
            // find last char of first line
            if (str.length() <= width) {
                return (ret.length() > 0 ? ret + "\n" + Str.repeat(tabChar, tab) : "").concat(str);
            }
            String line1 = strTrim(str, width);
            int lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
            while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
                --lastPos;
            }
            if (lastPos == 0) {
                lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
            }
            // ret += strPadRightChat((ret.length() > 0 ? unformattedStrRepeat(tabChar, tab) : "") + str.substring(0,
            // lastPos));
            ret += (ret.length() > 0 ? "\n" + Str.repeat(tabChar, tab) : "") + str.substring(0, lastPos);
            str = str.substring(lastPos + 1);
        }
        return ret;
    }

    /**
     * right-aligns paragraphs
     * 
     * @param str
     * @param width
     * @param tab
     * @param tabChar
     * @return right-aligned string
     */
    public static String strWordWrapRight(String str, int width, int tab, char tabChar) {
        String ret = "";
        while (str.length() > 0) {
            // find last char of first line
            if (str.length() <= width) {
                return (ret.length() > 0 ? ret + "\n" : "").concat(Str.padLeft(str, width, tabChar));
            }
            String line1 = strTrim(str, width);
            int lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
            while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
                --lastPos;
            }
            if (lastPos <= 0) {
                lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
            }
            // ret += strPadLeftChat(str.substring(0, lastPos), tabChar);
            ret += (ret.length() > 0 ? "\n" : "") + Str.padLeft(str.substring(0, lastPos), width, tabChar);
            str = str.substring(lastPos + 1);
        }
        return ret;
    }

    public static String strWordWrapRight(String str, int width, int tab) {
        return strWordWrapRight(str, width, tab, ' ');
    }

    public static String repeat(char ch, int len) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            ret.append(ch);
        }
        return ret.toString();
    }

    /**
     * Returns a sequence str of the provided str count # of times
     * 
     * @param str
     * @param count
     * @return string with the input string repeated
     */
    public static String repeat(String str, int count) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            ret.append(str);
        }
        return ret.toString();
    }

    public static String strTrim(String str, int length) {
        if (str.length() > length) {
            int width = length;
            String ret = "";
            boolean lastCol = false;
            for (char c : str.toCharArray()) {
                if (c == '\u00A7') {
                    ret += c;
                    lastCol = true;
                } else {
                    if (!lastCol) {
                        if (width - 1 >= 0) {
                            width -= 1;
                            ret += c;
                        } else {
                            return ret;
                        }
                    } else {
                        ret += c;
                        lastCol = false;
                    }
                }
            }
        }
        return str;
    }

    public static String titleCase(String str) {
        StringBuilder ret = new StringBuilder();
        boolean st = true;
        for (char c : str.toLowerCase().toCharArray()) {
            if (st) {
                ret.append(Character.toTitleCase(c));
            } else {
                ret.append(c);
            }
            st = c == ' ';
        }
        return ret.toString();
    }

    @Override
    public void write(int b) throws IOException {
        text.append((char) b);
    }
} // end class Str
