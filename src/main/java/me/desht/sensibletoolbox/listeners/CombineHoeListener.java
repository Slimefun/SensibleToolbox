package me.desht.sensibletoolbox.listeners;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.CombineHoe;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CombineHoeListener extends STBBaseListener {
	public CombineHoeListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onAddItemToCombineHoe(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getWhoClicked();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(player.getItemInHand());
		if (!(item instanceof CombineHoe)) {
			return;
		}
		CombineHoe hoe = (CombineHoe) item;
		System.out.println("inventory click event: " + event.getAction() + " " + event.getClick() + " " + event.getCursor() + " -> " + event.getCurrentItem() + event.getSlot());
		if (event.getInventory().getTitle().equals(hoe.getInventoryTitle())) {
			ItemStack cursor = event.getCursor();
			ItemStack current = event.getCurrentItem();
			InventoryAction action = event.getAction();
			System.out.println("click in " + event.getInventory().getTitle() + " slot = " + event.getSlot() + ", raw slot = " + event.getRawSlot() + " cursor = " + cursor.getType());
			Inventory topInv = event.getView().getTopInventory();
			if (event.getRawSlot() < 9) {
				// in the hoe's 9-slot inventory
				System.out.println("upper inv! size = " + topInv.getSize() + " type = " + topInv.getType());
				if (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE || action == InventoryAction.PLACE_SOME || action == InventoryAction.SWAP_WITH_CURSOR) {
					if (STBUtil.getCropType(cursor.getType()) == null) {
						event.setCancelled(true);
					} else if (!verifyUnique(topInv, cursor, event.getRawSlot())) {
						event.setCancelled(true);
					}
				}
			} else {
				// in the player's main inventory
				if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
					if (STBUtil.getCropType(current.getType()) == null) {
						event.setCancelled(true);
					} else if (!verifyUnique(topInv, current, -1)) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onDragItemToCombineHoe(InventoryDragEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getWhoClicked();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(player.getItemInHand());
		if (!(item instanceof CombineHoe)) {
			return;
		}
		CombineHoe hoe = (CombineHoe) item;
		if (event.getInventory().getTitle().equals(hoe.getInventoryTitle())) {
			for (int slot : event.getRawSlots()) {
				if (slot < 9 && STBUtil.getCropType(event.getOldCursor().getType()) == null) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onCombineHoeClosed(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getPlayer();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(player.getItemInHand());
		if (!(item instanceof CombineHoe)) {
			return;
		}
		CombineHoe hoe = (CombineHoe) item;
		if (event.getInventory().getTitle().equals(hoe.getInventoryTitle())) {
			Inventory topInv = event.getView().getTopInventory();
			Material seedType = null;
			int count = 0;
			String err = null;
			for (int i = 0; i < topInv.getSize(); i++) {
				ItemStack topInvItem = topInv.getItem(i);
				if (topInvItem != null) {
					if (seedType != null && seedType != topInvItem.getType()) {
						player.getWorld().dropItemNaturally(player.getLocation(), topInvItem);
						err = "Mixed items in the seed bag??";
					} else if (STBUtil.getCropType(topInvItem.getType()) == null) {
						player.getWorld().dropItemNaturally(player.getLocation(), topInvItem);
						err = "Non-seed items in the seed bag??";
					} else {
						seedType = topInvItem.getType();
						count += topInvItem.getAmount();
					}
				}
			}
			if (err != null) {
				MiscUtil.errorMessage(player, err);
			}
			hoe.setSeedBagContents(seedType, count);
			player.setItemInHand(hoe.toItemStack(1));
		}
	}

	private boolean verifyUnique(Inventory inv, ItemStack stack, int exclude) {
		for (int i = 0; i < inv.getSize(); i++) {
			if (i != exclude && inv.getItem(i) != null && inv.getItem(i).getType() != stack.getType()) {
				return false;
			}
		}
		return true;
	}
}
