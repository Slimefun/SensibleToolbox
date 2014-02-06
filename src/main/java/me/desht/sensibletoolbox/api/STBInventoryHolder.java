package me.desht.sensibletoolbox.api;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface STBInventoryHolder extends InventoryHolder {
	/**
	 * Attempt to actually insert an item into the inventory on the given side.
	 *
	 * @param item the item to insert
	 * @param face the side to insert into; BlockFace.SELF may be used if insertion is manual
	 * @param sorting if true, only insert if inventory is empty or already contains the item
	 * @return the number of items actually inserted
	 */
	public int insertItems(ItemStack item, BlockFace face, boolean sorting);

	/**
	 * Attempt to extract items from the inventory on the given side.  The size of the returned
	 * item stack could be smaller than the requested amount.
	 * <p>
	 * If a receiver is specified, items will be only be extracted if they will stack with the receiver,
	 * and only up to the receiver's natural maximum stack size.
	 *
	 * @param face the side to extract from; BlockFace.SELF may be used if extraction is manual
	 * @param receiver buffer to receive the items (may be null)
	 * @param amount the number of items to request
	 * @return the item stack extracted, or null if no item was extracted
	 */
	public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount);

}
