package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.LightSensitive;
import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

public class LightMeter extends MonitorGadget {
	private static final ItemStack BRIGHT = new Wool(DyeColor.YELLOW).toItemStack();
	private static final ItemStack DIM = new Wool(DyeColor.ORANGE).toItemStack();
	private static final ItemStack DARK = new Wool(DyeColor.GRAY).toItemStack();
	static {
		InventoryGUI.setDisplayName(BRIGHT, "Full Efficiency");
		InventoryGUI.setDisplayName(DIM, "Reduced Efficiency");
		InventoryGUI.setDisplayName(DARK, "No Power Output");
	}

	public LightMeter(InventoryGUI gui) {
		super(gui);
		Validate.isTrue(gui.getOwningBlock() instanceof LightSensitive, "Attempt to install light meter in non-light-sensitive block!");
	}

	@Override
	public void repaint() {
		LightSensitive ls = (LightSensitive) getOwner();
		getGUI().getInventory().setItem(ls.getLightMeterSlot(), getIndicator(ls.getLightLevel()));
	}

	@Override
	public int[] getSlots() {
		return new int[] { ((LightSensitive) getOwner()).getLightMeterSlot() };
	}

	private ItemStack getIndicator(byte light) {
		if (light < 12) {
			return DARK;
		} else if (light < 15) {
			return DIM;
		} else {
			return BRIGHT;
		}
	}
}
