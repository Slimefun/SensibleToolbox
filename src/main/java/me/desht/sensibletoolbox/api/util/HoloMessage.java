package me.desht.sensibletoolbox.api.util;

import com.dsh105.holoapi.HoloAPI;
import com.dsh105.holoapi.api.Hologram;
import com.dsh105.holoapi.api.visibility.Visibility;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Display a temporary popup message to a player via a hologram.  This
 * requires the HoloAPI plugin to function.
 */
public class HoloMessage {
    /**
     * Display a quick popup message to the given player.  The message
     * duration is dependent on the message length and the plugin
     * configuration (item <em>holo_messages.duration_per_line</em>).
     *
     * @param player the player to show the message to
     * @param loc the base location at which to show the message text
     * @param message the message text
     */
    public static void quickMessage(Player player, Location loc, List<String> message) {
        Vector v = player.getLocation().getDirection();
        // this will place the message slightly in front of and above the location
        v.setY(0).multiply(-0.8).add(new Vector(0.5, 0.8, 0.5));
        double duration = SensibleToolboxPlugin.getInstance().getConfig().getDouble("holo_messages.duration_per_line") * message.size();
        Hologram h = HoloAPI.getManager().createSimpleHologram(loc.add(v), (int)duration, message);
        h.setVisibility(new PlayerVisibility(player));
    }

    private static class PlayerVisibility implements Visibility {
        private final Player player;

        PlayerVisibility(Player player) {
            this.player = player;
        }

        @Override
        public boolean isVisibleTo(Player player, String s) {
            return player.equals(this.player);
        }

        @Override
        public String getSaveKey() {
            return null;
        }

        @Override
        public LinkedHashMap<String, Object> getDataToSave() {
            return null;
        }
    }
}
