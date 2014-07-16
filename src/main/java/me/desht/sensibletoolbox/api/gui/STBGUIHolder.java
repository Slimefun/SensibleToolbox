package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class STBGUIHolder implements InventoryHolder {
    private final BaseSTBBlock owner;

    public STBGUIHolder(BaseSTBBlock owner) {
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
