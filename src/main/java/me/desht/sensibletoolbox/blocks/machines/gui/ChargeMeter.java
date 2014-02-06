package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.api.STBMachine;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ChargeMeter extends MonitorGadget {
	private final ItemStack indicator;

	public ChargeMeter(InventoryGUI gui) {
		super(gui);
		this.indicator = new ItemStack(Material.LEATHER_HELMET);
		ItemMeta meta = indicator.getItemMeta();
		meta.setDisplayName(STBUtil.getChargeString(getOwner()));
		((LeatherArmorMeta)meta).setColor(Color.YELLOW);
		indicator.setItemMeta(meta);
	}

	public void repaint() {
		Inventory inventory = getOwner().getInventory();

		ItemMeta meta = indicator.getItemMeta();
		meta.setDisplayName(STBUtil.getChargeString(getOwner()));
		indicator.setItemMeta(meta);

		short max = indicator.getType().getMaxDurability();
		double d = getOwner().getCharge() / (double) getOwner().getMaxCharge();
		short dur = (short)(max * d);
		indicator.setDurability((short)(max - dur));
		inventory.setItem(getOwner().getChargeMeterSlot(), indicator);
	}

	@Override
	public int[] getSlots() {
		return new int[] { getOwner().getChargeMeterSlot() };
	}
}
