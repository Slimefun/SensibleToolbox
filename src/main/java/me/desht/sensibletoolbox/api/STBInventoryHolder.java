package me.desht.sensibletoolbox.api;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface STBInventoryHolder extends InventoryHolder {
	/**
	 * Check if an item can be inserted into the inventory on the given side.
	 *
	 * @param item the item to check
	 * @param face the side to insert into; BlockFace.SELF may be used if insertion is manual
	 * @return true if the item could be inserted, false otherwise
	 */
	public int findSlotForItemInsertion(ItemStack item, BlockFace face);

	/**
	 * Attempt to insert an item into the inventory on the given side.
	 *
	 * @param item the item to insert
	 * @param face the side to insert into; BlockFace.SELF may be used if insertion is manual
	 * @return true if insertion was successful, false otherwise
	 */
	public boolean insertItem(ItemStack item, BlockFace face);

	/**
	 * Attempt to extract an item from the inventory on the given side.
	 *
	 * @param face the side to extract from; BlockFace.SELF may be used if extraction is manual
	 * @return the item extracted, or null if no item was extracted
	 */
	public ItemStack extractItem(BlockFace face);

	public int[] getInputSlots();
	public int[] getOutputSlots();
}
