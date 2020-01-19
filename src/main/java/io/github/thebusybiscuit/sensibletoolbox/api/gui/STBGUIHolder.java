package io.github.thebusybiscuit.sensibletoolbox.api.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;

/**
 * Represents the holder of an InventoryGUI object.  This object should not
 * normally need to be interacted with directly; its purpose is to
 * unambiguously link an inventory GUI with the STB block it was created for.
 */
public class STBGUIHolder implements InventoryHolder {
    private final BaseSTBBlock owner;

    /**
     * Constructs the holder of an InventoryGUI.  You do not need to call this
     * constructor directly; an STBGUIHolder is instantiated for every STB
     * block that is created.
     *
     * @param owner the block which owns the GUI
     */
    public STBGUIHolder(BaseSTBBlock owner) {
        this.owner = owner;
    }

    @Override
    public Inventory getInventory() {
        return getGUI().getInventory();
    }

    /**
     * Get the GUI for this holder object.
     *
     * @return an InventoryGUI object
     */
    public InventoryGUI getGUI() {
        return owner.getGUI();
    }
}
