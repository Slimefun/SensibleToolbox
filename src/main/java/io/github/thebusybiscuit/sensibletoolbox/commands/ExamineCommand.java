package io.github.thebusybiscuit.sensibletoolbox.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.dhutils.text.MessagePager;

public class ExamineCommand extends AbstractCommand {

    public ExamineCommand() {
        super("stb examine", 0, 0);
        setUsage("/stb examine");
        setPermissionNode("stb.commands.examine");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MiscUtil.errorMessage(sender, "This command can't be run from the console.");
            return true;
        }

        Player player = (Player) sender;
        MessagePager pager = MessagePager.getPager(sender).clear();
        pager.add(STBUtil.dumpItemStack(player.getInventory().getItemInMainHand()));
        pager.showPage();
        return true;
    }
}
