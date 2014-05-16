package me.desht.sensibletoolbox.util;

import com.dsh105.holoapi.HoloAPI;
import com.dsh105.holoapi.api.Hologram;
import com.dsh105.holoapi.api.visibility.Visibility;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.List;

public class HoloMessage {
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
