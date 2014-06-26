package me.desht.sensibletoolbox.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ClickableGadget extends Gadget {
    private final int slot;

    protected ClickableGadget(InventoryGUI gui, int slot) {
        super(gui);
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public abstract void onClicked(InventoryClickEvent event);

    public abstract ItemStack getTexture();

    protected void updateGUI() {
        getGUI().getInventory().setItem(getSlot(), getTexture());
    }
}
