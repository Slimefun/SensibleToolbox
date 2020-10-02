package me.desht.dhutils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;

public final class PermissionUtils {

    private PermissionUtils() {}

    private static final String DEFAULT_MESSAGE = "You are not allowed to do that.";

    /**
     * Check if the player has the specified permission node.
     *
     * @param sender
     *            Command sender (player or console) to check
     * @param node
     *            Node to check for
     * @return true if the player has the permission node, false otherwise
     */
    public static boolean isAllowedTo(@Nullable CommandSender sender, @Nonnull String node) {
        if (sender == null) {
            // backwards compatibility - a null sender represents a console sender
            return true;
        }

        boolean allowed = sender.hasPermission(node);
        Debugger.getInstance().debug("Permission check: player=" + sender.getName() + ", node=" + node + ", allowed=" + allowed);
        return allowed;
    }

    /**
     * Throw an exception if the player does not have the specified permission.
     *
     * @param sender
     *            Command sender (player or console) to check
     * @param node
     *            Require permission node
     * @param message
     *            Error message to include in the exception
     * @throws DHUtilsException
     *             if the player does not have the node
     */
    public static void requirePerms(@Nullable CommandSender sender, @Nonnull String node, @Nonnull String message) {
        if (!isAllowedTo(sender, node)) {
            throw new DHUtilsException(message);
        }
    }

    public static void requirePerms(@Nullable CommandSender sender, @Nonnull String node) {
        requirePerms(sender, node, DEFAULT_MESSAGE);
    }

}
