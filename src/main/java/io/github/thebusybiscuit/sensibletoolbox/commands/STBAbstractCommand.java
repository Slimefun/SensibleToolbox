package io.github.thebusybiscuit.sensibletoolbox.commands;

import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

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

    @Nullable
    protected UUID getID(String s) {
        if (MiscUtil.looksLikeUUID(s)) {
            return UUID.fromString(s);
        } else {
            Player p = Bukkit.getPlayer(s);
            return p == null ? null : p.getUniqueId();
        }
    }

    protected Player getTargetPlayer(CommandSender sender, @Nullable String playerNameOrID) {
        if (playerNameOrID == null) {
            if (sender instanceof Player) {
                return (Player) sender;
            } else {
                throw new DHUtilsException("This command can't be run from the console.");
            }
        } else if (!sender.hasPermission("stb.friends.other")) {
            throw new DHUtilsException("You are not allowed to do that.");
        } else {
            return MiscUtil.looksLikeUUID(playerNameOrID) ? Bukkit.getPlayer(UUID.fromString(playerNameOrID)) : Bukkit.getPlayer(playerNameOrID);
        }
    }
}
