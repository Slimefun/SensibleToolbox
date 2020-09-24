package io.github.thebusybiscuit.sensibletoolbox.commands;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

public class DebugCommand extends AbstractCommand {

    public DebugCommand() {
        super("stb debug", 0, 1);
        setPermissionNode("stb.commands.debug");
        setUsage("/<command> debug [<level>]");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        if (args.length >= 1) {
            try {
                int level = Integer.parseInt(args[0]);
                Validate.isTrue(level >= 0, "Debug level must be >= 0");
                setDebugLevel(sender, level);
            }
            catch (IllegalArgumentException e) {
                throw new DHUtilsException(e.getMessage());
            }
        }
        else {
            if (Debugger.getInstance().getLevel() > 0) {
                setDebugLevel(sender, 0);
            }
            else {
                setDebugLevel(sender, 1);
            }
        }
        return true;
    }

    private void setDebugLevel(CommandSender sender, int level) {
        Debugger.getInstance().setLevel(level);
        Debugger.getInstance().setTarget(level == 0 ? null : sender);
        if (level > 0) {
            MiscUtil.statusMessage(sender, "Debugger enabled (level " + level + ")");
        }
        else {
            MiscUtil.statusMessage(sender, "Debugger disabled");
        }
    }
}
