package io.github.thebusybiscuit.sensibletoolbox.util;

import org.bukkit.World;

public class SunlightLevels {

    // lookup table: map 15-minute interval to effective sun brightness
    private static final byte[] sunLevels = new byte[96];

    static {
        sunLevels[0] = sunLevels[1] = 14; // 06:00 - 06:29
        for (int i = 2; i <= 45; i++) { // 06:30 - 17:29
            sunLevels[i] = 15;
        }
        sunLevels[46] = sunLevels[47] = 14; // 17:30 - 17:59
        sunLevels[48] = sunLevels[49] = 13; // 18:00 - 18:29
        sunLevels[50] = 12; // 18:30 - 18:44
        for (int i = 51; i <= 92; i++) { // 18:45 - 05:14
            sunLevels[i] = 0;
        }
        sunLevels[93] = 12; // 05:15 - 05:29
        sunLevels[94] = sunLevels[95] = 13; // 05:30 - 05:59
    }

    public static Byte getSunlightLevel(World w) {
        int i = (int) w.getTime() / 250; // yield 0..95 index
        return sunLevels[i];
    }
}
