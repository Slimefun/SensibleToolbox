package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GUIHolder implements InventoryHolder {
	private final BaseSTBBlock owner;

	public GUIHolder(BaseSTBBlock owner) {
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
