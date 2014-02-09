package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.AccessControl;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AccessControlGadget extends ClickableGadget {
	private AccessControl accessControl;

	public AccessControlGadget(InventoryGUI owner) {
		super(owner);
		accessControl = owner.getOwningBlock().getAccessControl();
	}

	@Override
	public void onClicked(InventoryClickEvent event) {
		int n = (accessControl.ordinal() + 1) % AccessControl.values().length;
		accessControl = AccessControl.values()[n];
		event.setCurrentItem(accessControl.getTexture());
		getGUI().getOwningBlock().setAccessControl(accessControl);
	}

	@Override
	public ItemStack getTexture() {
		return accessControl.getTexture();
	}
}
