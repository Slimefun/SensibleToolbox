package me.desht.sensibletoolbox.util;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class SoilSaturation {
	private static final String LAST_WATERED = "STB_LastWatered";
	private static final String SATURATION = "STB_Saturation";

	public static final int MAX_SATURATION = 100;

	public static long getLastWatered(Block b) {
		for (MetadataValue v : b.getMetadata(LAST_WATERED)) {
			if (v.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
				return v.asLong();
			}
		}
		return 0;
	}

	public static void setLastWatered(Block b, long lastWatered) {
		b.setMetadata(LAST_WATERED, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), lastWatered));
	}

	public static int getSaturationLevel(Block b) {
		for (MetadataValue v : b.getMetadata(SATURATION)) {
			if (v.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
				return v.asInt();
			}
		}
		return 0;
	}

	public static void setSaturationLevel(Block b, int saturationLevel) {
		b.setMetadata(SATURATION, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), saturationLevel));
	}

	public static void clear(Block b) {
		b.removeMetadata(LAST_WATERED, SensibleToolboxPlugin.getInstance());
		b.removeMetadata(SATURATION, SensibleToolboxPlugin.getInstance());
	}
}
