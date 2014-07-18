package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.core.STBFriendManager;
import me.desht.sensibletoolbox.core.storage.LocationManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class SaveCommand extends AbstractCommand {
    public SaveCommand() {
        super("stb save", 0, 0);
        setPermissionNode("stb.commands.save");
        setUsage("/<command> save");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        SensibleToolboxPlugin sPlugin = (SensibleToolboxPlugin) plugin;
        LocationManager.getManager().save();
        ((STBFriendManager)sPlugin.getFriendManager()).save();
        MiscUtil.statusMessage(sender, "STB persisted data saved");
        return true;
    }
}
