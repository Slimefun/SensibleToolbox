package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.DHValidate;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.FriendManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class UnfriendCommand extends STBAbstractCommand {
    public UnfriendCommand() {
        super("stb unfriend", 1);
        setPermissionNode("stb.commands.unfriend");
        setUsage("/<command> unfriend <player-name-or-id>");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        FriendManager fm = ((SensibleToolboxPlugin) plugin).getFriendManager();

        Player target = getTargetPlayer(sender, getStringOption("p"));

        UUID id = getID(args[0]);
        DHValidate.notNull(id, "Unknown player: " + args[0]);
        fm.removeFriend(target.getUniqueId(), id);
        MiscUtil.statusMessage(sender, target.getName() + " is no longer friends with " + args[0]);

        return true;
    }
}
