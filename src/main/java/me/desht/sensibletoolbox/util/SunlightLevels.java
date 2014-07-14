package me.desht.sensibletoolbox.util;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Map;
import java.util.UUID;

public class SunlightLevels implements Runnable {
    private final Map<UUID,Byte> levels = Maps.newHashMap();

    @Override
    public void run() {
        for (World w : Bukkit.getWorlds()) {
            levels.put(w.getUID(), calculateSunlightStrength(w.getTime()));
        }
    }

    public Byte getSunlightLevel(World w) {
        return levels.get(w.getUID());
    }

    private byte calculateSunlightStrength(long time) {
        if (time < 500) {
            return 14;  // 06:00 - 06:30
        } else if (time < 11500) {
            return 15;  // 06:30 - 17:30
        } else if (time < 12000) {
            return 14;  // 17:30 - 18:00
        } else if (time < 12500) {
            return 13;  // 18:00 - 18.30
        } else if (time < 12750) {
            return 12;  // 18:30 - 18:45
        } else if (time < 23250) {
            return 0;   // 18.30 - 05:15
        } else if (time < 23500) {
            return 12;   // 05:15 - 05:30
        } else {
            return 13;  // 05:30 - 06:00
        }
    }
}
