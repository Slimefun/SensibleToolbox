package me.desht.sensibletoolbox.util;

import com.dsh105.holoapi.HoloAPI;
import com.dsh105.holoapi.api.Hologram;
import com.dsh105.holoapi.api.visibility.Visibility;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class PopupMessage {
    public static void quickMessage(Player player, Location loc, List<String> message) {
        if (SensibleToolboxPlugin.getInstance().isHoloAPIenabled()
                && SensibleToolboxPlugin.getInstance().getConfig().getBoolean("holo_messages.enabled")) {
            Vector v = player.getLocation().getDirection();
            // this will place the message slightly in front of and above the location
            v.setY(0).multiply(-0.8).add(new Vector(0.5, 0.8, 0.5));
            int duration = SensibleToolboxPlugin.getInstance().getConfig().getInt("holo_messages.duration");
            Hologram h = HoloAPI.getManager().createSimpleHologram(loc.add(v), duration, message);
            h.setVisibility(new PlayerVisibility(player));
        } else {
            for (String line : message) {
                MiscUtil.statusMessage(player, line);
            }
        }
    }

    public static void quickMessage(Player player, Location loc, String... lines) {
        quickMessage(player, loc, Arrays.asList(lines));
    }

    private static class PlayerVisibility implements Visibility {
        private final Player player;

        private PlayerVisibility(Player player) {
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
