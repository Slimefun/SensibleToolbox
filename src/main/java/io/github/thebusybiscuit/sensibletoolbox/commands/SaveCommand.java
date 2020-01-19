package io.github.thebusybiscuit.sensibletoolbox.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.core.STBFriendManager;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

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
