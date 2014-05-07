package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.STBBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class STBGUIHolder implements InventoryHolder {
    private final STBBlock owner;

    public STBGUIHolder(STBBlock owner) {
        this.owner = owner;
    }

    @Override
    public Inventory getInventory() {
        return getGUI().getInventory();
    }

    public InventoryGUI getGUI() {
        return owner.getGUI();
    }
}
