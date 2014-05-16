package me.desht.sensibletoolbox.util;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class PopupMessage {
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
