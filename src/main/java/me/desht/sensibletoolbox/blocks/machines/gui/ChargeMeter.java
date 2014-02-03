package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Wool;

public class ChargeMeter extends UpdatingGadget {
	private final int slot;
	private final ItemStack indicator;

	public ChargeMeter(BaseSTBMachine owner, int slot) {
		super(owner);
		this.slot = slot;
		this.indicator = new ItemStack(Material.LEATHER_HELMET);
		ItemMeta meta = indicator.getItemMeta();
		((LeatherArmorMeta)meta).setColor(Color.YELLOW);
		indicator.setItemMeta(meta);
	}

	@Override
	public int getMinimumUpdateInterval() {
		return 500; // milliseconds
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
		inventory.setItem(slot, indicator);
	}
}
