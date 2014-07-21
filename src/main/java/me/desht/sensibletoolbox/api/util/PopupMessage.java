package me.desht.sensibletoolbox.api.util;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Display a popup message to the player via one or more messaging systems.
 * At this time only HoloAPI is supported.  If no supported message system
 * is available, the message will be displayed to the player via the chat
 * interface.
 */
public class PopupMessage {
    /**
     * Display a quick popup message to the given player.
     *
     * @param player the player to show the message to
     * @param loc the base location at which to show the message text
     * @param message the message text
     */
    public static void quickMessage(Player player, Location loc, List<String> message) {
        if (SensibleToolboxPlugin.getInstance().isHoloAPIenabled()
                && SensibleToolboxPlugin.getInstance().getConfig().getBoolean("holo_messages.enabled")) {
            HoloMessage.quickMessage(player, loc, message);
        } else {
            for (String line : message) {
                MiscUtil.statusMessage(player, line);
            }
        }
    }

    public static void quickMessage(Player player, Location loc, String... lines) {
        quickMessage(player, loc, Arrays.asList(lines));
    }
}
