package io.github.thebusybiscuit.sensibletoolbox.utils;

import org.bukkit.World;

public final class SunlightLevels {

    private SunlightLevels() {}

    // lookup table: map 15-minute interval to effective sun brightness
    private static final byte[] sunLevels = new byte[96];

    static {
        // 06:00 - 06:29
        sunLevels[0] = sunLevels[1] = 14;
        // 06:30 - 17:29
        for (int i = 2; i <= 45; i++) {
            sunLevels[i] = 15;
        }
        // 17:30 - 17:59
        sunLevels[46] = sunLevels[47] = 14;
        // 18:00 - 18:29
        sunLevels[48] = sunLevels[49] = 13;
        // 18:30 - 18:44
        sunLevels[50] = 12;
        // 18:45 - 05:14
        for (int i = 51; i <= 92; i++) {
            sunLevels[i] = 0;
        }
        // 05:15 - 05:29
        sunLevels[93] = 12;
        // 05:30 - 05:59
        sunLevels[94] = sunLevels[95] = 13;
    }

    public static Byte getSunlightLevel(World w) {
        // yield 0..95 index
        int i = (int) w.getTime() / 250;
        return sunLevels[i];
    }
}
