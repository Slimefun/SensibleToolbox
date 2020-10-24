package io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;

/**
 * Abstract base class for all clickable gadgets.
 * 
 * @author desht
 */
public abstract class ClickableGadget extends Gadget {

    private final int slot;

    protected ClickableGadget(InventoryGUI gui, int slot) {
        super(gui);
        this.slot = slot;
    }

    /**
     * Get the inventory slot that this gadget occupies.
     *
     * @return the gadget's slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Called when the gadget is clicked.
     *
     * @param event
     *            the inventory click event
     */
    public abstract void onClicked(InventoryClickEvent event);

    /**
     * Return an item stack representing this gadget's texture in the GUI.
     *
     * @return an item stack
     */
    public abstract ItemStack getTexture();

    /**
     * Update the gadget's appearance in the GUI. This should be called if a
     * gadget's value is changed programmatically, i.e. not via a player
     * action.
     */
    protected void updateGUI() {
        getGUI().getInventory().setItem(getSlot(), getTexture());
    }
}
