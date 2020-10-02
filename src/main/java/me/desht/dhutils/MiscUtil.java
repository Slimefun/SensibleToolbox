package me.desht.dhutils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class MiscUtil {

    private static final Map<String, String> prevColors = new HashMap<>();

    public static final String STATUS_COLOUR = ChatColor.AQUA.toString();
    public static final String ERROR_COLOUR = ChatColor.RED.toString();
    public static final String ALERT_COLOUR = ChatColor.YELLOW.toString();
    public static final String GENERAL_COLOUR = ChatColor.WHITE.toString();

    private static boolean coloredConsole = true;

    public static void setColoredConsole(boolean colored) {
        coloredConsole = colored;
    }

    public static void errorMessage(CommandSender sender, String string) {
        setPrevColor(sender.getName(), ERROR_COLOUR);
        message(sender, ERROR_COLOUR + string, Level.WARNING);
        prevColors.remove(sender.getName());
    }

    public static void statusMessage(CommandSender sender, String string) {
        setPrevColor(sender.getName(), STATUS_COLOUR);
        message(sender, STATUS_COLOUR + string, Level.INFO);
        prevColors.remove(sender.getName());
    }

    public static void alertMessage(CommandSender sender, String string) {
        setPrevColor(sender.getName(), ALERT_COLOUR);
        message(sender, ALERT_COLOUR + string, Level.INFO);
        prevColors.remove(sender.getName());
    }

    public static void generalMessage(CommandSender sender, String string) {
        setPrevColor(sender.getName(), GENERAL_COLOUR);
        message(sender, GENERAL_COLOUR + string, Level.INFO);
        prevColors.remove(sender.getName());
    }

    private static void setPrevColor(String name, String color) {
        prevColors.put(name, color);
    }

    private static String getPrevColor(String name) {
        String color = prevColors.get(name);
        return color == null ? GENERAL_COLOUR : color;
    }

    public static void rawMessage(CommandSender sender, String string) {
        boolean strip = sender instanceof ConsoleCommandSender && !coloredConsole;
        for (String line : string.split("\\n")) {
            if (strip) {
                sender.sendMessage(ChatColor.stripColor(line));
            }
            else {
                sender.sendMessage(line);
            }
        }
    }

    private static void message(CommandSender sender, String string, Level level) {
        boolean strip = sender instanceof ConsoleCommandSender && !coloredConsole;
        for (String line : string.split("\\n")) {
            if (strip) {
                LogUtils.log(level, ChatColor.stripColor(parseColorSpec(sender, line)));
            }
            else {
                sender.sendMessage(parseColorSpec(sender, line));
            }
        }
    }

    public static String formatLocation(Location loc) {
        return String.format("%d,%d,%d,%s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
    }

    public static Location parseLocation(String arglist) {
        return parseLocation(arglist, null);
    }

    public static Location parseLocation(String arglist, CommandSender sender) {
        String s = sender instanceof Player ? "" : ",worldname";
        String[] args = arglist.split(",");

        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);
            World w = (sender instanceof Player) ? ((Player) sender).getWorld() : findWorld(args[3]);
            return new Location(w, x, y, z);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("You must specify all of x,y,z" + s + ".");
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in " + arglist);
        }
    }

    private static final Pattern colorPat = Pattern.compile("(?<!&)&(?=[0-9a-fA-Fk-oK-OrR])");

    public static String parseColorSpec(CommandSender sender, String spec) {
        String who = sender == null ? "*" : sender.getName();
        String res = colorPat.matcher(spec).replaceAll("\u00A7");
        return res.replace("&-", getPrevColor(who)).replace("&&", "&");
    }

    /**
     * Find the given world by name.
     *
     * @param worldName
     *            name of the world to find
     * @return the World object representing the world name
     * @throws IllegalArgumentException
     *             if the given world cannot be found
     */
    public static World findWorld(String worldName) {
        World w = Bukkit.getServer().getWorld(worldName);
        if (w != null) {
            return w;
        }
        else {
            throw new IllegalArgumentException("World " + worldName + " was not found on the server.");
        }
    }

    /**
     * Split the given string, but ensure single & double quoted sections of the string are
     * kept together.
     * <p>
     * E.g. the String 'one "two three" four' will be split into [ "one", "two three", "four" ]
     *
     * @param s
     *            the String to split
     * @return a List of items
     */
    public static List<String> splitQuotedString(String s) {
        List<String> matchList = new ArrayList<>();

        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(s);

        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            }
            else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            }
            else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }

        return matchList;
    }

    /**
     * Return the given collection (of Comparable items) as a sorted list.
     *
     * @param c
     *            the collection to sort
     * @return a list of the sorted items in the collection
     */
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public static boolean looksLikeUUID(String s) {
        return s.length() == 36 && s.charAt(8) == '-' && s.charAt(13) == '-' && s.charAt(18) == '-' && s.charAt(23) == '-';
    }

    /**
     * Truncates a decimal to a given number of places.
     *
     * @param x the decimal to truncate
     * @param places the number of places to truncate
     * @return x truncated to places places
     */
    @Nonnull
    public static double truncateDecimal(double x, int places) {
        if (x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(places, RoundingMode.FLOOR).doubleValue();
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(places, RoundingMode.CEILING).doubleValue();
        }
    }
}
