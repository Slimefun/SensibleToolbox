package me.desht.dhutils.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Joiner;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;

/**
 * @author des
 *
 */
public abstract class AbstractCommand implements Comparable<AbstractCommand> {

    private static final Pattern quotedStringRegex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

    private final int minArgs;
    private final int maxArgs;

    private final List<CommandRecord> cmdRecs = new ArrayList<>();
    private final Map<String, CommandOptionType> options = new HashMap<>();
    private final Map<String, Object> optVals = new HashMap<>();

    private String[] usage;
    private String permissionNode;
    private boolean quotedArgs;
    private CommandRecord matchedCommand;
    private String[] matchedArgs;

    public AbstractCommand(String label) {
        this(label, 0, Integer.MAX_VALUE);
    }

    public AbstractCommand(String label, int minArgs) {
        this(label, minArgs, Integer.MAX_VALUE);
    }

    public AbstractCommand(String label, int minArgs, int maxArgs) {
        quotedArgs = false;

        setUsage("");
        addAlias(label);

        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }

    public abstract boolean execute(Plugin plugin, CommandSender sender, String[] args);

    public void addAlias(@Nonnull String label) {
        Validate.notNull(label, "The alias cannot be null");
        String[] fields = label.split(" ");
        cmdRecs.add(new CommandRecord(fields));
    }

    @ParametersAreNonnullByDefault
    public boolean matchesSubCommand(String label, String[] args) {
        return matchesSubCommand(label, args, false);
    }

    @ParametersAreNonnullByDefault
    public boolean matchesSubCommand(String label, String[] args, boolean partialOk) {
        for (CommandRecord rec : cmdRecs) {
            if (label.equalsIgnoreCase(rec.getCommand())) {
                if (!partialOk && args.length < rec.size()) {
                    continue;
                }

                int matches = getSubCommandMatches(rec, args);

                if (matches != -1 && (partialOk || matches == rec.size())) {
                    matchedCommand = rec;
                    return true;
                }
            }
        }

        return false;
    }

    @ParametersAreNonnullByDefault
    private int getSubCommandMatches(CommandRecord rec, String[] args) {
        int matches = 0;

        for (int i = 0; i < rec.size() && i < args.length; i++) {
            if (rec.getSubCommand(i).startsWith(args[i])) {
                matches++;
            }
            else {
                // match failed; try the next command record, if any
                return -1;
            }
        }

        return matches;
    }

    @ParametersAreNonnullByDefault
    public boolean matchesArgCount(String label, String[] args) {
        for (CommandRecord rec : cmdRecs) {
            if (label.equalsIgnoreCase(rec.getCommand())) {

                int nArgs;

                if (isQuotedArgs()) {
                    List<String> a = splitQuotedString(combine(args, 0));
                    nArgs = a.size() - rec.size();
                }
                else {
                    nArgs = args.length - rec.size();
                }

                Debugger.getInstance().debug(3, String.format("matchesArgCount: %s, nArgs=%d min=%d max=%d", label, nArgs, minArgs, maxArgs));
                if (nArgs >= minArgs && nArgs <= maxArgs) {
                    storeMatchedArgs(args, rec);
                    return true;
                }
            }
        }

        matchedArgs = null;
        return false;
    }

    List<CommandRecord> getCmdRecs() {
        return cmdRecs;
    }

    @Nonnull
    protected List<String> noCompletions(CommandSender sender) {
        return CommandManager.noCompletions(sender);
    }

    private void storeMatchedArgs(String[] args, CommandRecord rec) {
        String[] tmpResult = new String[args.length - rec.size()];

        for (int i = rec.size(); i < args.length; i++) {
            tmpResult[i - rec.size()] = args[i];
        }

        String[] tmpArgs;
        if (isQuotedArgs()) {
            tmpArgs = splitQuotedString(combine(tmpResult, 0)).toArray(new String[0]);
        }
        else {
            tmpArgs = tmpResult;
        }

        // extract any command-line options that were specified
        List<String> l = new ArrayList<>(tmpArgs.length);
        optVals.clear();

        for (int i = 0; i < tmpArgs.length; i++) {
            String opt;

            if (tmpArgs[i].length() < 2 || !tmpArgs[i].startsWith("-")) {
                opt = "";
            }
            else {
                opt = tmpArgs[i].substring(1);
            }

            if (options.containsKey(opt)) {
                try {
                    switch (options.get(opt)) {
                    case BOOLEAN:
                        optVals.put(opt, true);
                        break;
                    case STRING:
                        i++;
                        optVals.put(opt, tmpArgs[i]);
                        break;
                    case INT:
                        i++;
                        optVals.put(opt, Integer.parseInt(tmpArgs[i]));
                        break;
                    case DOUBLE:
                        i++;
                        optVals.put(opt, Double.parseDouble(tmpArgs[i]));
                        break;
                    default:
                        throw new IllegalStateException("unexpected option type for " + tmpArgs[i]);
                    }
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    throw new DHUtilsException("Missing value for option '" + tmpArgs[i - 1] + "'");
                }
                catch (Exception e) {
                    throw new DHUtilsException("Invalid value for option '" + tmpArgs[i - 1] + "'");
                }
            }
            else {
                l.add(tmpArgs[i]);
            }
        }

        matchedArgs = l.toArray(new String[0]);
    }

    protected void showUsage(CommandSender sender, String alias, String prefix) {
        if (usage.length == 0) {
            return;
        }

        String indent;
        if (prefix == null || prefix.isEmpty()) {
            indent = "";
        }
        else {
            int l = prefix.length();
            indent = sender instanceof Player ? StringUtils.repeat(" ", l + 2) : StringUtils.repeat(" ", l);
        }

        for (int i = 0; i < usage.length; i++) {
            String s = alias == null ? usage[i] : usage[i].replace("<command>", alias);
            MiscUtil.errorMessage(sender, (i == 0 ? prefix : indent) + s);
        }
    }

    protected void showUsage(CommandSender sender, String alias) {
        showUsage(sender, alias, "Usage: ");
    }

    protected void showUsage(CommandSender sender) {
        showUsage(sender, getMatchedCommand().getCommand());
    }

    CommandRecord getMatchedCommand() {
        return matchedCommand;
    }

    protected String[] getMatchedArgs() {
        return matchedArgs;
    }

    protected void setPermissionNode(String node) {
        this.permissionNode = node;
    }

    protected void setUsage(String usage) {
        this.usage = new String[] { usage };
    }

    protected void setUsage(String[] usage) {
        this.usage = usage;
    }

    protected String[] getUsage() {
        return usage;
    }

    protected void setOptions(String... optSpec) {
        for (String opt : optSpec) {
            String[] parts = opt.split(":");

            if (parts.length == 1) {
                options.put(parts[0], CommandOptionType.BOOLEAN);
            }
            else if (parts[1].startsWith("i")) {
                options.put(parts[0], CommandOptionType.INT);
            }
            else if (parts[1].startsWith("d")) {
                options.put(parts[0], CommandOptionType.DOUBLE);
            }
            else if (parts[1].startsWith("s")) {
                options.put(parts[0], CommandOptionType.STRING);
            }
        }
    }

    protected String getPermissionNode() {
        return permissionNode;
    }

    public boolean isQuotedArgs() {
        return quotedArgs;
    }

    protected boolean hasOption(String opt) {
        return optVals.containsKey(opt);
    }

    protected Object getOption(String opt) {
        return optVals.get(opt);
    }

    protected int getIntOption(String opt) {
        return getIntOption(opt, 0);
    }

    protected int getIntOption(String opt, int def) {
        if (!optVals.containsKey(opt)) return def;
        return (Integer) optVals.get(opt);
    }

    protected String getStringOption(String opt) {
        return getStringOption(opt, null);
    }

    protected String getStringOption(String opt, String def) {
        if (!optVals.containsKey(opt)) return def;
        return (String) optVals.get(opt);
    }

    protected double getDoubleOption(String opt) {
        return getDoubleOption(opt, 0.0);
    }

    protected double getDoubleOption(String opt, double def) {
        if (!optVals.containsKey(opt)) return def;
        return (Double) optVals.get(opt);
    }

    protected boolean getBooleanOption(String opt) {
        return getBooleanOption(opt, false);
    }

    protected boolean getBooleanOption(String opt, boolean def) {
        if (!optVals.containsKey(opt)) return def;
        return (Boolean) optVals.get(opt);
    }

    public void setQuotedArgs(boolean usesQuotedArgs) {
        this.quotedArgs = usesQuotedArgs;
    }

    protected void notFromConsole(CommandSender sender) throws DHUtilsException {
        if (!(sender instanceof Player)) {
            throw new DHUtilsException("This command can't be run from the console.");
        }
    }

    protected String combine(String[] args, int idx) {
        return combine(args, idx, args.length - 1);
    }

    protected String combine(String[] args, int idx1, int idx2) {
        StringBuilder result = new StringBuilder();
        for (int i = idx1; i <= idx2 && i < args.length; i++) {
            result.append(args[i]);
            if (i < idx2) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    /**
     * Return a list of possible completions for the command for the given arguments.
     * Override this in subclasses.
     *
     * @param sender
     *            the player doing the tab completion
     * @param args
     *            the argument list
     * @return a list of possible completions
     */
    public List<String> onTabComplete(Plugin plugin, CommandSender sender, String[] args) {
        return CommandManager.noCompletions(sender);
    }

    /**
     * Given a collection of String and a String prefix, return a List of those collection members
     * which start with the prefix. The sender is used for notification purposes if no members are
     * matched, and may be null.
     *
     * @param sender
     * @param c
     * @param prefix
     * @return
     */
    protected List<String> filterPrefix(CommandSender sender, Collection<String> c, String prefix) {
        List<String> res = new ArrayList<>();

        for (String s : c) {
            if (prefix == null || prefix.isEmpty() || s.toLowerCase().startsWith(prefix.toLowerCase())) {
                res.add(s);
            }
        }

        return getResult(res, sender, true);
    }

    /**
     * Given a list of Strings, return the list, possibly sorted. If a sender is supplied, notify the
     * sender if the list is empty.
     *
     * @param res
     * @param sender
     * @param sorted
     * @return
     */
    protected List<String> getResult(List<String> res, CommandSender sender, boolean sorted) {
        if (res.isEmpty()) {
            return sender == null ? Collections.emptyList() : CommandManager.noCompletions(sender);
        }

        return sorted ? MiscUtil.asSortedList(res) : res;
    }

    protected List<String> getEnumCompletions(CommandSender sender, Class<? extends Enum<?>> c, String prefix) {
        List<String> res = new ArrayList<>();

        for (Object o1 : c.getEnumConstants()) {
            res.add(o1.toString());
        }

        return filterPrefix(sender, res, prefix);
    }

    protected List<String> getConfigCompletions(CommandSender sender, ConfigurationSection config, String prefix) {
        List<String> res = new ArrayList<>();

        for (String k : config.getKeys(true)) {
            if (!config.isConfigurationSection(k)) {
                res.add(k);
            }
        }

        return filterPrefix(sender, res, prefix);
    }

    protected List<String> getConfigValueCompletions(CommandSender sender, String key, Object obj, String desc, String prefix) {
        List<String> res = new ArrayList<>();

        if (obj instanceof Enum<?>) {
            MiscUtil.alertMessage(sender, key + ":" + desc);

            for (Object o1 : obj.getClass().getEnumConstants()) {
                res.add(o1.toString());
            }
        }
        else if (obj instanceof Boolean) {
            MiscUtil.alertMessage(sender, key + ":" + desc);
            res.add("true");
            res.add("false");
        }
        else {
            MiscUtil.alertMessage(sender, key + " = <" + obj.getClass().getSimpleName() + ">" + desc);
        }

        return filterPrefix(sender, res, prefix);
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
    @Nonnull
    private static List<String> splitQuotedString(@Nonnull String s) {
        List<String> matchList = new ArrayList<>();
        Matcher regexMatcher = quotedStringRegex.matcher(s);

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

    @Override
    public int compareTo(AbstractCommand other) {
        List<CommandRecord> recs = getCmdRecs();
        List<CommandRecord> recs2 = other.getCmdRecs();
        return recs.toString().compareTo(recs2.toString());
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(",").join(cmdRecs) + "]";
    }
}
