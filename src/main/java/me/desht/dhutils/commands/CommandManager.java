package me.desht.dhutils.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Joiner;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;

public class CommandManager {

    private final List<AbstractCommand> cmdList = new ArrayList<>();
    private Plugin plugin;

    public CommandManager(@Nonnull Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommand(@Nonnull AbstractCommand cmd) {
        Validate.notNull(cmd, "Command cannot be null!");

        Debugger.getInstance().debug(2, "register command: " + cmd.getClass().getName());
        cmdList.add(cmd);
    }

    private boolean dispatch(CommandSender sender, String cmdName, String label, String[] args) {
        boolean res = true;

        List<AbstractCommand> possibleMatches = getPossibleMatches(cmdName, args, true);
        String desc = plugin.getDescription().getFullName();

        if (possibleMatches.size() == 1) {
            // good - a unique match
            AbstractCommand cmd = possibleMatches.get(0);

            if (cmd.matchesArgCount(cmdName, args)) {
                if (cmd.getPermissionNode() != null && !sender.hasPermission(cmd.getPermissionNode())) {
                    throw new DHUtilsException("You are not allowed to do that.");
                }

                res = cmd.execute(plugin, sender, cmd.getMatchedArgs());
            } else {
                cmd.showUsage(sender, label);
            }
        } else if (possibleMatches.isEmpty()) {
            // no match
            String s = cmdList.size() == 1 ? "" : "s";
            MiscUtil.errorMessage(sender, cmdList.size() + " possible matching command" + s + " in " + desc + ":");

            for (AbstractCommand cmd : MiscUtil.asSortedList(cmdList)) {
                cmd.showUsage(sender, label, "\u2022 ");
            }
        } else {
            // multiple possible matches
            MiscUtil.errorMessage(sender, possibleMatches.size() + " possible matching commands in " + desc + ":");
            for (AbstractCommand cmd : MiscUtil.asSortedList(possibleMatches)) {
                cmd.showUsage(sender, label, "\u2022 ");
            }
        }

        return res;
    }

    public boolean dispatch(CommandSender sender, Command command, String label, String[] args) {
        try {
            return dispatch(sender, command.getName(), label, args);
        } catch (DHUtilsException e) {
            MiscUtil.errorMessage(sender, e.getMessage());
            return true;
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        Debugger.getInstance().debug("tab complete: sender=" + sender.getName() + ", cmd=" + command.getName() + ", label=" + label + ", args=[" + Joiner.on(",").join(args) + "]");

        List<AbstractCommand> possibleMatches = getPossibleMatches(command.getName(), args, true);

        if (possibleMatches.isEmpty()) {
            return noCompletions(sender);
        } else if (possibleMatches.size() == 1 && args.length > possibleMatches.get(0).getMatchedCommand().size()) {
            // tab completion to be done by the command itself
            Debugger.getInstance().debug("tab complete: pass to command: " + possibleMatches.get(0).getMatchedCommand());
            int from = possibleMatches.get(0).getMatchedCommand().size();

            try {
                return possibleMatches.get(0).onTabComplete(plugin, sender, subRange(args, from));
            } catch (DHUtilsException e) {
                MiscUtil.errorMessage(sender, e.getMessage());
                return noCompletions(sender);
            }
        } else {
            // tab completion done here; try to fill in the subcommand
            Set<String> completions = new HashSet<>();

            for (AbstractCommand cmd : possibleMatches) {
                if (cmd.getPermissionNode() != null && !sender.hasPermission(cmd.getPermissionNode())) {
                    continue;
                }

                Debugger.getInstance().debug(2, "add completion: " + cmd);
                CommandRecord rec = cmd.getMatchedCommand();

                if (rec.size() >= args.length) {
                    completions.add(cmd.getMatchedCommand().getSubCommand(args.length - 1));
                }
                // completions.add(cmd.getMatchedCommand().lastSubCommand());
            }

            return MiscUtil.asSortedList(completions);
        }
    }

    @Nonnull
    private String[] subRange(@Nonnull String[] a, int from) {
        String[] res = new String[a.length - from];
        System.arraycopy(a, from, res, 0, res.length);
        return res;
    }

    @Nonnull
    private List<AbstractCommand> getPossibleMatches(String cmdName, String[] args, boolean partialOk) {
        List<AbstractCommand> possibleMatches = new ArrayList<>();

        for (AbstractCommand cmd : cmdList) {
            if (cmd.matchesSubCommand(cmdName, args, partialOk)) {
                possibleMatches.add(cmd);
            }
        }

        Debugger.getInstance().debug("found " + possibleMatches.size() + " possible matches for " + cmdName);

        return possibleMatches;
    }

    static List<String> noCompletions(CommandSender sender) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
        }

        return Collections.emptyList();
    }
}
