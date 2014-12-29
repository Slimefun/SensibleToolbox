package me.mrCookieSlime.sensibletoolbox.commands;

import java.util.UUID;

import me.desht.sensibletoolbox.dhutils.MiscUtil;
import me.desht.sensibletoolbox.dhutils.PermissionUtils;
import me.desht.sensibletoolbox.dhutils.commands.AbstractCommand;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class STBAbstractCommand extends AbstractCommand {
    public STBAbstractCommand(String label) {
        super(label);
    }

    public STBAbstractCommand(String label, int minArgs) {
        super(label, minArgs);
    }

    public STBAbstractCommand(String label, int minArgs, int maxArgs) {
        super(label, minArgs, maxArgs);
    }

    protected UUID getID(String s) {
        if (MiscUtil.looksLikeUUID(s)) {
            return UUID.fromString(s);
        } else {
            @SuppressWarnings("deprecation")
            Player p = Bukkit.getPlayer(s);
            return p == null ? null : p.getUniqueId();
        }
    }

    @SuppressWarnings("deprecation")
	protected Player getTargetPlayer(CommandSender sender, String playerNameOrID) {
        if (playerNameOrID == null) {
            notFromConsole(sender);
            return (Player) sender;
        } else {
            PermissionUtils.requirePerms(sender, "stb.friends.other");
            //noinspection deprecation
            return MiscUtil.looksLikeUUID(playerNameOrID) ? Bukkit.getPlayer(UUID.fromString(playerNameOrID)) : Bukkit.getPlayer(playerNameOrID);
        }
    }
}
