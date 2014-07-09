package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ExamineCommand extends AbstractCommand {
    public ExamineCommand() {
        super("stb examine", 0, 0);
        setUsage("/stb examine");
        setPermissionNode("stb.commands.examine");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        notFromConsole(sender);
        Player player = (Player) sender;
        STBUtil.dumpItemStack(player.getItemInHand());
        return true;
    }
}
