package io.github.thebusybiscuit.sensibletoolbox.api.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an STB item or block which listens for actions from its GUI.
 */
public interface InventoryGUIListener {
    /**
     * Called when a slot in an inventory GUI is clicked by a player.
     *
     * @param player   the player who clicked
     * @param slot     the raw inventory slot which was clicked
     * @param click    the click type
     * @param inSlot   the item currently in the clicked slot
     * @param onCursor the item on the player's cursor
     * @return true if the click should go ahead, false if it should be cancelled
     */
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor);

    /**
     * Called when a slot in a player inventory is clicked by a player while
     * an inventory GUI is shown.
     *
     * @param player   the player who clicked
     * @param slot     the raw inventory slot which was clicked
     * @param click    the click type
     * @param inSlot   the item currently in the clicked slot
     * @param onCursor the item on the player's cursor
     * @return true if the click should go ahead, false if it should be cancelled
     */
    public boolean onPlayerInventoryClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor);

    /**
     * Called when an attempt is made to shift-click an item into an inventory
     * GUI.  The implementor is responsible for actually putting items into
     * the appropriate slot(s) of the inventory GUI, but the stack being
     * clicked will be automatically adjusted, based on this method's return
     * value.
     *
     * @param player   the player who clicked
     * @param slot     the slot that was shift-clicked
     * @param toInsert the item stack to be inserted
     * @return the number of items from the stack that were actually inserted
     */
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert);

    /**
     * Called when an attempt is made to shift-click an item out of an
     * inventory GUI.
     *
     * @param player    the player who clicked
     * @param slot      the slot in the inventory GUI that was shift-clicked
     * @param toExtract the number of items being extracted
     * @return true if items should be extracted, false otherwise
     */
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract);

    /**
     * Called when a player clicks outside the inventory window.
     *
     * @param player player who clicked
     * @return true if the click should go ahead, false to cancel the event
     */
    public boolean onClickOutside(HumanEntity player);

    /**
     * Called when an inventory GUI window is opened.
     *
     * @param player player who opened the window
     */
    public void onGUIOpened(HumanEntity player);

    /**
     * Called when an inventory GUI window is closed.
     *
     * @param player player who closed the window
     */
    public void onGUIClosed(HumanEntity player);
}
