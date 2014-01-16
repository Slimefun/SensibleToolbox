package me.desht.sensibletoolbox.listeners;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class AnvilListener extends STBBaseListener {
	public AnvilListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onAnvil(InventoryClickEvent event) {
		// The whole Anvil system is horribly broken right now, so we're just going to disallow
		// any kind of anvil activity for STB items.
		if (event.getWhoClicked() instanceof Player && event.getInventory().getType() == InventoryType.ANVIL) {
//			System.out.println("anvil inventory click! " + event.getAction() + " on " + event.getRawSlot() + " - " + event.getSlotType());
			if (event.getSlotType() == InventoryType.SlotType.CRAFTING) {
				if (BaseSTBItem.isSTBItem(event.getCursor())) {
					event.setCancelled(true);
					MiscUtil.errorMessage((Player) event.getWhoClicked(), "Sensible Toolbox items don't fit in a vanilla anvil.");
				}
			} else if (event.getRawSlot() > 2 && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				if (BaseSTBItem.isSTBItem(event.getCurrentItem())) {
					event.setCancelled(true);
					MiscUtil.errorMessage((Player) event.getWhoClicked(), "Sensible Toolbox items don't fit in a vanilla anvil.");
				}
			}
		}
	}
}
