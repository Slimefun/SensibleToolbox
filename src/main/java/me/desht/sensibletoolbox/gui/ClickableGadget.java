package me.desht.sensibletoolbox.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ClickableGadget extends Gadget {
    protected ClickableGadget(InventoryGUI gui) {
        super(gui);
    }

    public abstract void onClicked(InventoryClickEvent event);

    public abstract ItemStack getTexture();

}
