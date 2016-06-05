package me.mrCookieSlime.sensibletoolbox.api.gui;

import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBBlock;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Implements a GUI system using custom inventory windows.
 */
public interface InventoryGUI {
    /**
     * Get the type of the given slot.
     *
     * @param slot the slot to check
     * @return the slot type
     */
    public SlotType getSlotType(int slot);

    /**
     * Change the type of the given slot.
     *
     * @param slot the slot to change
     * @param type the new slot type
     */
    public void setSlotType(int slot, SlotType type);

    /**
     * Add a clickable gadget to this GUI.  The gadget will define its
     * position in the GUI.
     *
     * @param gadget the gadget to add
     */
    public void addGadget(ClickableGadget gadget);

    /**
     * Get the clickable gadget in the given slot.
     *
     * @param slot the slot to check
     * @return the clickable gadget, or null if there is no gadget there
     */
    public ClickableGadget getGadget(int slot);

    /**
     * Add an informational label to this GUI.
     * <p>
     * If a null texture is passed, a default texture defined in the
     * SensibleToolbox plugin configuration ("gui.texture.label") is used.
     *
     * @param label the label text
     * @param slot the inventory slot to place the label in
     * @param texture the texture for the label, may be null
     * @param lore the label tooltip; extended information can be defined here
     */
    public void addLabel(String label, int slot, ItemStack texture, String... lore);

    /**
     * Add a monitor gadget to this GUI.  The gadget defines its own
     * position in the GUI.
     *
     * @param gadget the gadget to add
     * @return a unique integer identifying the monitor gadget
     */
    public int addMonitor(MonitorGadget gadget);

    /**
     * Get the monitor gadget for the given ID.
     *
     * @param monitorId a unique identifier, as returned
     *                  by {@link #addMonitor(MonitorGadget)}
     * @return a monitor gadget, or null if no such gadget exists
     */
    public MonitorGadget getMonitor(int monitorId);

    /**
     * Get the item in the given inventory slot.  The slot must have
     * previously been marked as an item slot with
     * {@link #setSlotType(int, me.desht.sensibletoolbox.api.gui.InventoryGUI.SlotType)}
     *
     * @param slot the slot to check
     * @return the item in that slot, may be null
     */
    public ItemStack getItem(int slot);

    /**
     * Change the item in the given inventory slot.  The slot must have
     * previously been marked as an item slot with
     * {@link #setSlotType(int, me.desht.sensibletoolbox.api.gui.InventoryGUI.SlotType)}
     *
     * @param slot the slot to update
     * @param stack the new item to place in the slot
     */
    public void setItem(int slot, ItemStack stack);

    /**
     * Get the STB block which owns this GUI.
     *
     * @return the owning STB block
     * @throws java.lang.IllegalStateException if the owner is not an STB block
     */
    public BaseSTBBlock getOwningBlock();

    /**
     * Get the STB item which owns this GUI.
     *
     * @return the owning STB item
     * @throws java.lang.IllegalStateException if the owner is not an STB item
     */
    public BaseSTBItem getOwningItem();

    /**
     * Get the Bukkit inventory backing this GUI.  Care should be taken when
     * working with the raw inventory.
     *
     * @return the Bukkit inventory
     */
    public Inventory getInventory();

    /**
     * Show this GUI to the given player.
     *
     * @param player the player to show the GUI to
     */
    public void show(Player player);

    /**
     * Hide this GUI from the given player (pop it down)
     *
     * @param player the player to hide the GUI from
     */
    public void hide(Player player);

    /**
     * Get a list of players who currently have this GUI open.
     *
     * @return a list of HumanEntity
     */
    public List<HumanEntity> getViewers();

    /**
     * Paint the slots surrounding the given list of slots.
     *
     * @param slots an array of slots
     * @param texture an item stack to use as a slot texture
     */
    public void paintSlotSurround(int[] slots, ItemStack texture);

    /**
     * Paint a slot in the GUI with the given texture.
     *
     * @param slot the slot to paint
     * @param texture an item stack to use as a slot texture
     * @param overwrite if false, don't paint the slot unless it's empty
     */
    public void paintSlot(int slot, ItemStack texture, boolean overwrite);

    /**
     * Freeze any items in the given slots into a string representation.  This
     * can later be passed to {@link #thawSlots(String, int...)}.
     *
     * @param slots an array of slots to freeze
     * @return a string representation of the items in the given slots
     */
    public String freezeSlots(int... slots);

    /**
     * Thaw a frozen item representation into the given slots.  This frozen
     * representation would have been created by {@link #freezeSlots(int...)}
     *
     * @param frozen a frozen string representing some items
     * @param slots the slots to thaw those items into
     */
    public void thawSlots(String frozen, int... slots);

    /**
     * Eject any items in the given slots, dropping them on the ground at or
     * near the GUI's owning block or player.
     *
     * @param slots the slots to eject items from
     */
    public void ejectItems(int... slots);

    public enum SlotType {
        /**
         * A background slot.  This slot type is never interactable.
         */
        BACKGROUND,
        /**
         * A slot which holds an item which might be placed or removed
         * from the GUI.
         */
        ITEM,
        /**
         * A slot which holds an interactable control of some kind
         * (e.g. a button, a label, a monitor for some value...)
         */
        GADGET
    }
}
