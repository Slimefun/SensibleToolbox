package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

public class ChargeMeter extends UpdatingGadget {
	private static final ItemStack BLACK = makeStack(DyeColor.BLACK);
	private static final ItemStack RED = makeStack(DyeColor.RED);
	private static final ItemStack ORANGE = makeStack(DyeColor.ORANGE);
	private static final ItemStack YELLOW = makeStack(DyeColor.YELLOW);
	private static final ItemStack GREEN = makeStack(DyeColor.LIME);

	private static ItemStack makeStack(DyeColor dc) {
		Wool w = new Wool(dc);
		ItemStack stack = w.toItemStack(1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(" ");
		stack.setItemMeta(meta);
		return stack;
	}

	private final int slot;

	public ChargeMeter(BaseSTBMachine owner, int slot) {
		super(owner);
		this.slot = slot;
	}

	@Override
	public int getMinimumUpdateInterval() {
		return 500; // milliseconds
	}

	public void repaint() {
		Inventory inventory = getOwner().getInventory();
//		int slot = row >= 0 ? row * 9 : inventory.getSize() - 9;
//		if (slot > inventory.getSize()) { slot = inventory.getSize() - 9; }

//		int n = (int)((getOwner().getCharge() / getOwner().getMaxCharge()) * 8);
//		if (n > 8) { n = 8; }

		ItemStack indicator = new ItemStack(Material.LEATHER_HELMET);
		ItemMeta meta = indicator.getItemMeta();
		meta.setDisplayName(BaseSTBItem.getChargeString(getOwner()));
		indicator.setItemMeta(meta);

		short max = indicator.getType().getMaxDurability();
		double d = getOwner().getCharge() / (double) getOwner().getMaxCharge();
		short dur = (short)(max * d);
		indicator.setDurability((short)(max - dur));
		inventory.setItem(slot, indicator);

//		slot++;
//		for (int i = 0; i < 8; i++) {
//			if (i >= n) {
//				inventory.setItem(slot + i, BLACK);
//			} else if (i < 2) {
//				inventory.setItem(slot + i, RED);
//			} else if (i < 4) {
//				inventory.setItem(slot + i, ORANGE);
//			} else if (i < 6) {
//				inventory.setItem(slot + i, YELLOW);
//			} else {
//				inventory.setItem(slot + i, GREEN);
//			}
//		}
	}
}
