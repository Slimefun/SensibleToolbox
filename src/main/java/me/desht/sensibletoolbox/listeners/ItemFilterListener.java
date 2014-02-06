package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.filters.AbstractItemFilter;
//import me.desht.sensibletoolbox.items.filters.ItemFilter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemFilterListener extends STBBaseListener {
	public ItemFilterListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void filterGUIClicked(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		if (!(event.getInventory().getTitle().equals(AbstractItemFilter.getInventoryTitle()))) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		AbstractItemFilter filter = BaseSTBItem.getItemFromItemStack(player.getItemInHand(), AbstractItemFilter.class);

		if (filter != null) {
			Inventory topInv = event.getView().getTopInventory();
			if (event.getRawSlot() >= 0 && event.getRawSlot() < topInv.getSize()) {
				// clicking in the filters GUI
				switch (event.getClick()) {
					case LEFT: case RIGHT:
						guiClicked(topInv, event.getRawSlot(), event.getCursor());
						break;
				}
				event.setCancelled(true);
			} else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void filterGUIDragged(InventoryDragEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		if (!(event.getInventory().getTitle().equals(AbstractItemFilter.getInventoryTitle()))) {
			return;
		}

		Inventory topInv = event.getView().getTopInventory();

		if (onlyBottomInventoryDragged(event)) {
			return;
		}

		event.setCancelled(true);
		if (event.getRawSlots().size() == 1) {
			// same as left clicking the slot
			Integer[] slot = event.getRawSlots().toArray(new Integer[1]);
			if (slot[0] < topInv.getSize()) {
				guiClicked(topInv, slot[0], event.getOldCursor());
			}
		}
	}

	private void guiClicked(Inventory inv, Integer slot, ItemStack cursor) {
		// copy the item in cursor into the inv, but don't place it
		if (cursor != null) {
			ItemStack stack = new ItemStack(cursor.getType(), 1, cursor.getDurability());
			stack.setAmount(1);
			inv.setItem(slot, stack);
		} else {
			inv.setItem(slot, null);
		}
	}

	private boolean onlyBottomInventoryDragged(InventoryDragEvent event) {
		Inventory topInv = event.getView().getTopInventory();
		for (int slot : event.getRawSlots()) {
			if (slot < topInv.getSize()) {
				return false;
			}
		}
		return true;
	}

	@EventHandler
	public void filterGUIClosed(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}
		if (!(event.getInventory().getTitle().equals(AbstractItemFilter.getInventoryTitle()))) {
			return;
		}

		Player player = (Player) event.getPlayer();
		AbstractItemFilter filter = BaseSTBItem.getItemFromItemStack(player.getItemInHand(), AbstractItemFilter.class);

		filter.clear();
		if (filter != null) {
			Inventory topInv = event.getView().getTopInventory();
			for (int i = 0; i < topInv.getSize(); i++) {
				ItemStack topInvItem = topInv.getItem(i);
				if (topInvItem != null) {
					filter.addFilteredItem(topInvItem);
				}
			}
		}
		player.setItemInHand(filter.toItemStack(player.getItemInHand().getAmount()));
	}
}
