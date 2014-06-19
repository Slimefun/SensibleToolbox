package me.desht.sensibletoolbox.api;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents an STB block which can hold an inventory of items.
 */
public interface STBInventoryHolder extends InventoryHolder {
    /**
     * Attempt to actually insert an item into the block on the given side.
     * <p/>
     * If a non-null uuid is provided, an access rights check should be made.
     *
     * @param item    the item stack to insert (an attempt will be made to insert all items)
     * @param face    the side to insert into; BlockFace.SELF may be used if insertion is manual
     * @param sorting if true, only insert if inventory is empty or already contains the item
     * @param uuid    ID of the player who is doing the extracting (may be null)
     * @return the number of items actually inserted
     */
    public int insertItems(ItemStack item, BlockFace face, boolean sorting, UUID uuid);

    /**
     * Attempt to extract items from the block on the given side.  The size of the returned
     * item stack could be smaller than the requested amount.
     * <p/>
     * If a receiver is specified, items will be only be extracted if they will stack with the receiver,
     * and only up to the receiver's natural maximum stack size.
     * <p/>
     * If a non-null uuid is provided, an access rights check should be made.
     *
     * @param face     the side to extract from; BlockFace.SELF may be used if extraction is manual
     * @param receiver buffer to receive the items (may be null)
     * @param amount   the number of items to request
     * @param uuid     ID of the player who is doing the extracting (may be null)
     * @return the item stack extracted, or null if no item was extracted
     */
    public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid);

    /**
     * Get a view of the items which can be extracted from this inventory.  Note that
     * the returned inventory is a copied view, and modifying it will not change the
     * inventory holder's actual contents (but see {@link #updateOutputItems(org.bukkit.inventory.Inventory)})
     *
     * @return an inventory containing a copy of the items which can be extracted
     */
    public Inventory showOutputItems();

    /**
     * Update (overwite) the STB output inventory with the contents of the supplied inventory.
     *
     * @param inventory the inventory to update items from
     */
    void updateOutputItems(Inventory inventory);
}
