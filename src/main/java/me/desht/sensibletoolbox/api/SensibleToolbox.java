package me.desht.sensibletoolbox.api;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.inventory.ItemStack;

public class SensibleToolbox {
	public static STBItem getItemFromItemStack(ItemStack stack) {
		return BaseSTBItem.getItemFromItemStack(stack);
	}
}
