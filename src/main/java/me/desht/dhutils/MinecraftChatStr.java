package me.desht.dhutils;

/*
 * character widths taken from Help's MinecraftFontWidthCalculator
 * https://github.com/tkelly910/Help
 *
 */

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.ChatColor;

public class MinecraftChatStr {

    private static final int CHAT_WIDTH = 318; // 325?
    private static final int DEF_CHAR_WIDTH = 6;

    private static class CharacterBag {

        private int[][] data = new int[255][];

        public void add(char ch, int width) {
            int[] bin = data[ch >> 8];
            if (bin == null) bin = data[ch >> 8] = new int[255];
            bin[ch & 0xFF] = width;
        }

        public int getWidth(char ch, int def) {
            int[] bin = data[ch >> 8];
            if (bin == null) return def;
            return bin[ch & 0xFF];
        }
    }

    private static final CharacterBag bag = new CharacterBag();

    static {
        final Map<Integer, String> widths = new HashMap<Integer, String>();
        widths.put(7, "@~");
        widths.put(6, "#$%&+-/0123456789=?ABCDEFGHJKLMNOPQRSTUVWXYZ\\^_abcdeghjmnopqrsuvwxyzñÑáéóúü");
        widths.put(5, "\"()*<>fk{}");
        widths.put(4, " I[]t");
        widths.put(3, "'`lí");
        widths.put(2, "!.,:;i|");
        widths.put(-6, "§"); // cancel the width of any following letter or number

        for (Map.Entry<Integer, String> e : widths.entrySet()) {
            for (int i = 0; i < e.getValue().length(); i++) {
                bag.add(e.getValue().charAt(i), e.getKey());
            }
        }
    }

    public static int getStringWidth(String s) {
        int i = 0;
        if (s != null) {
            // s = s.replaceAll("\\u00A7.", "");
            for (int j = 0; j < s.length(); j++) {
                if (s.charAt(j) >= 0) {
                    i += getCharWidth(s.charAt(j));
                }
            }
        }
        return i;
    }

    public static int getCharWidth(char c) {
        return bag.getWidth(c, 0);
    }

    public static int getCharWidth(char c, int def) {
        return bag.getWidth(c, def);
    }

    public static String uncoloredStr(String s) {
        return s != null ? s.replaceAll("\\u00A7.", "") : s;
    }

    /**
     * pads str on the right with pad (left-align)
     * 
     * @param str
     *            string to format
     * @param len
     *            spaces to pad
     * @param pad
     *            character to use when padding
     * @return str with padding appended
     */
    public static String strPadRight(String str, int len, char pad) {
        len *= DEF_CHAR_WIDTH;
        len -= getStringWidth(str);
        return str + Str.repeat(pad, len / getCharWidth(pad, DEF_CHAR_WIDTH));
    }

    public static String strPadRightChat(String str, int abslen, char pad) {
        abslen -= getStringWidth(str);
        return str + Str.repeat(pad, abslen / getCharWidth(pad, DEF_CHAR_WIDTH));
    }

    public static String strPadRightChat(String str, int abslen) {
        abslen -= getStringWidth(str);
        return str + Str.repeat(' ', abslen / getCharWidth(' ', DEF_CHAR_WIDTH));
    }

    public static String strPadRightChat(String str, char pad) {
        int width = CHAT_WIDTH - getStringWidth(str);
        return str + Str.repeat(pad, width / getCharWidth(pad, DEF_CHAR_WIDTH));
    }

    public static String strPadRightChat(String str) {
        int width = CHAT_WIDTH - getStringWidth(str);
        return str + Str.repeat(' ', width / getCharWidth(' ', DEF_CHAR_WIDTH));
    }

    /**
     * pads str on the left with pad (right-align)
     * 
     * @param str
     *            string to format
     * @param len
     *            spaces to pad
     * @param pad
     *            character to use when padding
     * @return str with padding prepended
     */
    public static String strPadLeft(String str, int len, char pad) {
        // for purposes of this function, assuming a normal char to be 6
        len *= DEF_CHAR_WIDTH;
        len -= getStringWidth(str);
        return Str.repeat(pad, len / getCharWidth(pad, DEF_CHAR_WIDTH)) + str;
    }

    public static String strPadLeftChat(String str, int abslen, char pad) {
        abslen -= getStringWidth(str);
        return Str.repeat(pad, abslen / getCharWidth(pad, DEF_CHAR_WIDTH)).concat(str);
    }

    public static String strPadLeftChat(String str, int abslen) {
        abslen -= getStringWidth(str);
        return Str.repeat(' ', abslen / getCharWidth(' ', DEF_CHAR_WIDTH)).concat(str);
    }

    public static String strPadLeftChat(String str, char pad) {
        int width = CHAT_WIDTH - getStringWidth(str);
        return Str.repeat(pad, width / getCharWidth(pad, DEF_CHAR_WIDTH)).concat(str);
    }

    public static String strPadLeftChat(String str) {
        int width = CHAT_WIDTH - getStringWidth(str);
        return Str.repeat(' ', width / getCharWidth(' ', DEF_CHAR_WIDTH)).concat(str);
    }

    /**
     * pads str on the left & right with pad (center-align)
     * 
     * @param str
     *            string to format
     * @param len
     *            spaces to pad
     * @param pad
     *            character to use when padding
     * @return str centered with pad
     */
    public static String strPadCenter(String str, int len, char pad) {
        // for purposes of this function, assuming a normal char to be 6
        len *= DEF_CHAR_WIDTH;
        len -= getStringWidth(str);
        int padwid = getCharWidth(pad, DEF_CHAR_WIDTH);
        int prepad = (len / padwid) / 2;
        len -= prepad * padwid;
        return Str.repeat(pad, prepad) + str + Str.repeat(pad, len / padwid);
    }

    public static String strPadCenterChat(String str, int abslen, char pad) {
        // int width = 325;
        abslen -= getStringWidth(str);
        int padwid = getCharWidth(pad, DEF_CHAR_WIDTH);
        int prepad = (abslen / padwid) / 2;
        abslen -= prepad * padwid;
        return Str.repeat(pad, prepad) + str + Str.repeat(pad, abslen / padwid);
    }

    public static String strPadCenterChat(String str, char pad) {
        int width = 325 - getStringWidth(str);
        int padwid = getCharWidth(pad, DEF_CHAR_WIDTH);
        int prepad = (width / padwid) / 2;
        width -= prepad * padwid;
        return Str.repeat(pad, prepad) + str + Str.repeat(pad, width / padwid);
    }

    public static int strLen(String str) {
        if (!str.contains("\u00A7")) {
            return str.length();
        }
        // just searching for \u00A7.
        return str.replaceAll("\\u00A7.", "").length();
    }

    public static String strTrim(String str, int length) {
        if (uncoloredStr(str).length() > length) {
            int width = length;
            String ret = "";
            boolean lastCol = false;
            for (char c : str.toCharArray()) {
                if (c == '\u00A7') {
                    ret += c;
                    lastCol = true;
                }
                else {
                    if (!lastCol) {
                        if (width - 1 >= 0) {
                            width -= 1;
                            ret += c;
                        }
                        else {
                            return ret;
                        }
                    }
                    else {
                        ret += c;
                        lastCol = false;
                    }
                }
            }
        }
        return str;
    }

    public static String strChatTrim(String str) {
        return strChatTrim(str, CHAT_WIDTH);
    }

    public static String strChatTrim(String str, int absLen) {
        int width = getStringWidth(str);
        if (width > absLen) {
            width = absLen;
            String ret = "";
            boolean lastCol = false;
            for (char c : str.toCharArray()) {
                if (c == '\u00A7') {
                    ret += c;
                    lastCol = true;
                }
                else {
                    if (!lastCol) {
                        int w = getCharWidth(c);
                        if (width - w >= 0) {
                            width -= w;
                            ret += c;
                        }
                        else {
                            return ret;
                        }
                    }
                    else {
                        ret += c;
                        lastCol = false;
                    }
                }
            }
        }
        return str;
    }

    public static String strWordWrap(String str) {
        return strWordWrap(str, 0, ' ');
    }

    public static String strWordWrap(String str, int tab) {
        return strWordWrap(str, tab, ' ');
    }

    public static String strWordWrap(String str, int tab, char tabChar) {
        String ret = "";
        while (str.length() > 0) {
            // find last char of first line
            if (getStringWidth(str) <= CHAT_WIDTH) {
                return (ret.length() > 0 ? ret + "\n" + lastStrColor(ret) + Str.repeat(tabChar, tab) : "").concat(str);
            }
            String line1 = strChatTrim(str);
            int lastPos = line1.length() - (ret.length() > 0 ? tab + 1 : 1);
            while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
                --lastPos;
            }
            if (lastPos == 0) {
                lastPos = line1.length() - (ret.length() > 0 ? tab + 1 : 1);
            }
            // ret += strPadRightChat((ret.length() > 0 ? unformattedStrRepeat(tabChar, tab) : "") + str.substring(0,
            // lastPos));

            ret += (ret.length() > 0 ? "\n" + Str.repeat(tabChar, tab) + lastStrColor(ret) : "") + str.substring(0, lastPos);
            str = str.substring(lastPos + 1);
        }
        return ret;
    }

    public static String strWordWrapRight(String str, int tab) {
        return strWordWrapRight(str, tab, ' ');
    }

    /**
     * right-aligns paragraphs
     * 
     * @param str
     * @param tab
     * @param tabChar
     * @return right-aligned string (padded to the left)
     */
    public static String strWordWrapRight(String str, int tab, char tabChar) {
        String ret = "";
        while (str.length() > 0) {
            // find last char of first line
            if (getStringWidth(str) <= CHAT_WIDTH) {
                return (ret.length() > 0 ? ret + "\n" + lastStrColor(ret) : "").concat(strPadLeftChat(str, tabChar));
            }
            String line1 = strChatTrim(str);
            int lastPos = line1.length() - (ret.length() > 0 ? tab + 1 : 1);
            while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
                --lastPos;
            }
            if (lastPos == 0) {
                lastPos = line1.length() - (ret.length() > 0 ? tab + 1 : 1);
            }
            // ret += strPadLeftChat(str.substring(0, lastPos), tabChar);
            ret += (ret.length() > 0 ? "\n" + lastStrColor(ret) : "") + strPadLeftChat(str.substring(0, lastPos), tabChar);
            str = str.substring(lastPos + 1);
        }
        return ret;
    }

    /**
     * will left-align the start of the string until sepChar, then right-align the remaining paragraph
     * 
     * @param str
     * @param tab
     * @param tabChar
     * @param sepChar
     * @return left-aligned string (padded right)
     */
    public static String strWordWrapRight(String str, int tab, char tabChar, char sepChar) {
        String ret = "";
        String line1 = strChatTrim(str);
        // first run the first left & right align
        if (line1.contains("" + sepChar)) {
            int lastPos = line1.length() - (ret.length() > 0 ? tab + 1 : 1);
            int sepPos = line1.indexOf(sepChar) + 1;
            while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
                --lastPos;
            }
            if (lastPos == 0) {
                lastPos = line1.length() - (ret.length() > 0 ? tab + 1 : 1);
            }
            else if (sepPos > lastPos) {
                lastPos = sepPos;
            }
            ret += str.substring(0, sepPos);
            ret += strPadLeftChat(str.substring(sepPos, lastPos), CHAT_WIDTH - getStringWidth(ret));
            str = str.substring(lastPos + 1);
        }
        while (str.length() > 0) {
            // find last char of first line
            if (getStringWidth(str) <= CHAT_WIDTH) {
                return (ret.length() > 0 ? ret + "\n" + lastStrColor(ret) : "").concat(strPadLeftChat(str, tabChar));
            }
            line1 = strChatTrim(str);
            int lastPos = line1.length() - (ret.length() > 0 ? tab + 1 : 1);
            while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
                --lastPos;
            }
            if (lastPos == 0) {
                lastPos = line1.length() - (ret.length() > 0 ? tab + 1 : 1);
            }
            // ret += strPadLeftChat(str.substring(0, lastPos), tabChar);
            ret += (ret.length() > 0 ? "\n" + lastStrColor(ret) : "") + strPadLeftChat(str.substring(0, lastPos), tabChar);
            str = str.substring(lastPos + 1);
        }
        return ret;
    }

    /**
     * will left-align the start of the string until sepChar, then right-align the remaining paragraph
     * 
     * @param str
     * @param width
     * @param tab
     * @param tabChar
     * @param sepChar
     * @return right-aligned string (padded left)
     */
    public static String strWordWrapRight(String str, int width, int tab, char tabChar, char sepChar) {
        String ret = "";
        String line1 = strTrim(str, width);
        // first run the first left & right align
        if (line1.contains("" + sepChar)) {
            int lastPos = line1.length() - (ret.length() > 0 ? tab + 1 : 1);
            int sepPos = line1.indexOf(sepChar) + 1;
            while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
                --lastPos;
            }
            if (lastPos == 0) {
                lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
            }
            else if (sepPos > lastPos) {
                lastPos = sepPos;
            }
            ret += str.substring(0, sepPos);
            ret += strPadLeftChat(str.substring(sepPos, lastPos), width - strLen(ret));
            str = str.substring(lastPos + 1);
        }
        while (str.length() > 0) {
            // find last char of first line
            if (strLen(str) <= width) {
                return (ret.length() > 0 ? ret + "\n" + lastStrColor(ret) : "").concat(Str.padLeft(str, width, tabChar));
            }
            line1 = strChatTrim(str);
            int lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
            while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
                --lastPos;
            }
            if (lastPos == 0) {
                lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
            }
            // ret += strPadLeftChat(str.substring(0, lastPos), tabChar);
            ret += (ret.length() > 0 ? "\n" + lastStrColor(ret) : "") + Str.padLeft(str.substring(0, lastPos), width, tabChar);
            str = str.substring(lastPos + 1);
        }
        return ret;
    }

    public static String lastStrColor(String str) {
        int i = str.lastIndexOf('\u00A7');
        if (i >= 0 && i + 1 < str.length()) {
            return str.substring(i, i + 2);
        }
        return "\u00A7F";// white
    }

    private static boolean containsAlignTag(String str, String tag) {
        int pos = str.indexOf("<" + tag);
        if (pos >= 0) {
            return str.length() > pos + ("<" + tag).length() && (str.charAt(pos + ("<" + tag).length()) == '>' || str.charAt(pos + ("<" + tag).length() + 1) == '>');
        }
        return false;
    }

    private static boolean containsAlignTag(LinkedList<String> input, String tag) {
        for (String l : input) {
            if (containsAlignTag(l, tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * UNTESTED: DON'T USE YET
     */
    public static String alignTags(String input, boolean minecraftChatFormat) {
        for (String fm : new String[] { "l", "r", "c" }) {
            while (containsAlignTag(input, fm)) {
                char repl = ' ';
                if (input.matches("^.*<" + fm + ".>.*$")) {
                    repl = input.substring(input.indexOf("<" + fm) + 2).charAt(0);
                    input = input.replaceFirst("<" + fm + ".>", "<" + fm + ">");
                }

                if (fm.equals("l")) {
                    if (minecraftChatFormat) {
                        input = strPadRight(input.substring(0, input.indexOf("<" + fm + ">")), input.indexOf("<" + fm + ">"), repl) + input.substring(input.indexOf("<" + fm + ">") + 3);
                    }
                    else {
                        input = Str.padRight(input.substring(0, input.indexOf("<" + fm + ">")), input.indexOf("<" + fm + ">"), repl) + input.substring(input.indexOf("<" + fm + ">") + 3);
                    }
                }
                else if (fm.equals("c")) {
                    if (minecraftChatFormat) {
                        input = strPadCenter(input.substring(0, input.indexOf("<" + fm + ">")), input.indexOf("<" + fm + ">"), repl) + input.substring(input.indexOf("<" + fm + ">") + 3);
                    }
                    else {
                        input = Str.padCenter(input.substring(0, input.indexOf("<" + fm + ">")), input.indexOf("<" + fm + ">"), repl) + input.substring(input.indexOf("<" + fm + ">") + 3);
                    }
                }
                else {
                    if (minecraftChatFormat) {
                        input = strPadLeft(input.substring(0, input.indexOf("<" + fm + ">")), input.indexOf("<" + fm + ">"), repl) + input.substring(input.indexOf("<" + fm + ">") + 3);
                    }
                    else {
                        input = Str.padLeft(input.substring(0, input.indexOf("<" + fm + ">")), input.indexOf("<" + fm + ">"), repl) + input.substring(input.indexOf("<" + fm + ">") + 3);
                    }
                }
            }
        }
        return input;
    }

    public static LinkedList<String> alignTags(LinkedList<String> input, boolean minecraftChatFormat) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        char[] repl = new char[input.size()];
        for (String fm : new String[] { "l", "r", "c" }) {
            while (containsAlignTag(input, fm)) {
                for (int i = 0; i < input.size(); ++i) {
                    if (input.get(i).matches("^.*<" + fm + ".>.*$")) {// || input.get(1).matches("^.*<r.>.*$")) {
                        repl[i] = input.get(i).substring(input.get(i).indexOf("<" + fm) + 2).charAt(0); // ,
                                                                                                        // input.get(1).indexOf(">")
                        input.set(i, input.get(i).replaceFirst("<" + fm + ".>", "<" + fm + ">"));
                    }
                    else {
                        repl[i] = ' ';
                    }
                }
                int maxPos = 0;
                for (int i = 0; i < input.size(); ++i) {
                    if (input.get(i).indexOf("<" + fm + ">") > maxPos) {
                        maxPos = input.get(i).indexOf("<" + fm + ">");
                    }
                }

                LinkedList<String> newinput = new LinkedList<String>();
                for (int i = 0; i < input.size(); ++i) {
                    String line = input.get(i);

                    if (line.indexOf("<" + fm + ">") != -1) {
                        if (fm.equals("l")) {
                            if (minecraftChatFormat) {
                                newinput.add(strPadRight(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl[i]) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                            else {
                                newinput.add(Str.padRight(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl[i]) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                        }
                        else if (fm.equals("c")) {
                            if (minecraftChatFormat) {
                                newinput.add(strPadCenter(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl[i]) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                            else {
                                newinput.add(Str.padCenter(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl[i]) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                        }
                        else {
                            if (minecraftChatFormat) {
                                newinput.add(strPadLeft(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl[i]) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                            else {
                                newinput.add(Str.padLeft(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl[i]) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                        }
                    }
                    else {
                        newinput.add(line);
                    }
                }
                input = newinput;
            }
        }
        return input;
    }

    public static String getChatColor(String col) {
        String def = ChatColor.WHITE.toString();
        if (col == null || col.length() == 0) {
            return def;
        }
        else if (col.length() >= 2 && col.startsWith("\u00A7")) {
            return col.substring(0, 2);
        }
        col = col.toLowerCase().trim();
        /*
         * # &0 is black
         * # &1 is dark blue
         * # &2 is dark green
         * # &3 is dark sky blue
         * # &4 is red
         * # &5 is magenta
         * # &6 is gold or amber
         * # &7 is light grey
         * # &8 is dark grey
         * # &9 is medium blue
         * # &2 is light green
         * # &b is cyan
         * # &c is orange-red
         * # &d is pink
         * # &e is yellow
         * # &f is white
         */
        if (col.equalsIgnoreCase("black")) {
            return ChatColor.BLACK.toString();
        }
        else if (col.equals("blue") || col.equals("dark blue")) {
            return ChatColor.DARK_BLUE.toString();
        }
        else if (col.equals("green") || col.equals("dark green")) {
            return ChatColor.DARK_GREEN.toString();
        }
        else if (col.equals("sky blue") || col.equals("dark sky blue") || col.equals("aqua")) {
            return ChatColor.DARK_AQUA.toString();
        }
        else if (col.equals("red") || col.equals("dark red")) {
            return ChatColor.DARK_RED.toString();
        }
        else if (col.equals("magenta") || col.equals("purple")) {
            return ChatColor.DARK_PURPLE.toString();
        }
        else if (col.equals("gold") || col.equals("amber") || col.equals("dark yellow")) {
            return ChatColor.GOLD.toString();
        }
        else if (col.equals("light gray") || col.equals("light grey")) {
            return ChatColor.GRAY.toString();
        }
        else if (col.equals("dark gray") || col.equals("dark grey") || col.equals("gray") || col.equals("grey")) {
            return ChatColor.DARK_GRAY.toString();
        }
        else if (col.equals("medium blue")) {
            return ChatColor.BLUE.toString();
        }
        else if (col.equals("light green") || col.equals("lime") || col.equals("lime green")) {
            return ChatColor.GREEN.toString();
        }
        else if (col.equals("cyan") || col.equals("light blue")) {
            return ChatColor.AQUA.toString();
        }
        else if (col.equals("orange") || col.equals("orange-red") || col.equals("red-orange")) {
            return ChatColor.RED.toString();
        }
        else if (col.equals("pink") || col.equals("light red") || col.equals("light purple")) {
            return ChatColor.LIGHT_PURPLE.toString();
        }
        else if (col.equals("yellow")) {
            return ChatColor.YELLOW.toString();
        }
        else if (col.equals("white")) {
            return ChatColor.WHITE.toString();
        }
        else {
            return def;
        }
    }
}
