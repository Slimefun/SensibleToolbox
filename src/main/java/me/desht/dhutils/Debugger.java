package me.desht.dhutils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * The {@link Debugger} provides some useful debugging tools
 * 
 * @author desht
 *
 */
public class Debugger {

    private static final String DEBUG_COLOR = ChatColor.DARK_GREEN.toString();
    private static Debugger instance;
    private int level;
    private CommandSender target;
    private String prefix = "";

    private Debugger() {
        level = 0;
    }

    @Nonnull
    public static synchronized Debugger getInstance() {
        if (instance == null) {
            instance = new Debugger();
        }

        return instance;
    }

    public void setPrefix(@Nonnull String prefix) {
        this.prefix = prefix;
    }

    @Nullable
    public CommandSender getTarget() {
        return target;
    }

    public void setTarget(@Nullable CommandSender target) {
        this.target = target;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void debug(@Nonnull String message) {
        debug(1, message);
    }

    public void debug(int msgLevel, @Nonnull String message) {
        if (msgLevel <= level && target != null) {
            target.sendMessage(DEBUG_COLOR + prefix + message);
        }
    }

}
