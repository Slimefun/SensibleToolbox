package me.desht.sensibletoolbox.listeners;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.CombineHoe;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.*;
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
		if (!(event.getInventory().getTitle().equals(CombineHoe.getInventoryTitle()))) {
			return;
		}
		Player player = (Player) event.getWhoClicked();
		CombineHoe hoe = BaseSTBItem.getItemFromItemStack(player.getItemInHand(), CombineHoe.class);
		if (hoe != null) {
			ItemStack cursor = event.getCursor();
			ItemStack current = event.getCurrentItem();
			InventoryAction action = event.getAction();
			Inventory topInv = event.getView().getTopInventory();
			if (event.getRawSlot() < 9) {
				// in the hoe's 9-slot inventory
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
		if (!(event.getInventory().getTitle().equals(CombineHoe.getInventoryTitle()))) {
			return;
		}
		Player player = (Player) event.getWhoClicked();
		CombineHoe hoe = BaseSTBItem.getItemFromItemStack(player.getItemInHand(), CombineHoe.class);
		if (hoe != null) {
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
		if (!(event.getInventory().getTitle().equals(CombineHoe.getInventoryTitle()))) {
			return;
		}
		Player player = (Player) event.getPlayer();
		CombineHoe hoe = BaseSTBItem.getItemFromItemStack(player.getItemInHand(), CombineHoe.class);
		if (hoe != null) {
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
			hoe.setSeedAmount(count);
			hoe.setSeedType(seedType);
			player.setItemInHand(hoe.toItemStack());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void leavesBroken(BlockBreakEvent event) {
		Block b = event.getBlock();
		if (b.getType() == Material.LEAVES || b.getType() == Material.LEAVES_2) {
			Player player = event.getPlayer();
			ItemStack stack = event.getPlayer().getItemInHand();
			CombineHoe hoe = BaseSTBItem.getItemFromItemStack(stack, CombineHoe.class);
			if (hoe != null) {
				hoe.harvestLayer(player, b);
				if (!player.isSneaking()) {
					hoe.harvestLayer(player, b.getRelative(BlockFace.UP));
					hoe.harvestLayer(player, b.getRelative(BlockFace.DOWN));
				}
				hoe.damageHeldItem(player, (short) 1);
			}
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
