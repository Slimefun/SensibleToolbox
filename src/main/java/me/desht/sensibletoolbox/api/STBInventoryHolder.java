package me.desht.sensibletoolbox.api;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface STBInventoryHolder extends InventoryHolder {
	public void insertItem(ItemStack item, BlockFace face);
}
