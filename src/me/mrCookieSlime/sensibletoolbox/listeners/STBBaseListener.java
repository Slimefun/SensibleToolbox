package me.mrCookieSlime.sensibletoolbox.listeners;

import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;

import org.bukkit.event.Listener;

public abstract class STBBaseListener implements Listener {
	
    protected SensibleToolboxPlugin plugin;

    public STBBaseListener(SensibleToolboxPlugin plugin) {
        this.plugin = plugin;
    }

//	protected void filterItemsAllowed(InventoryClickEvent event, Class<? extends BaseSTBItem> c) {
//		Inventory topInv = event.getView().getTopInventory();
//		if (event.getRawSlot() >= 0 && event.getRawSlot() < topInv.getSize() && event.getCursor().getType() != Material.AIR) {
//			STBItem item = BaseSTBItem.getItemFromItemStack(event.getCursor(), c);
//			if (item == null) {
//				event.setCancelled(true);
//			}
//		} else if (event.getRawSlot() >= topInv.getSize() && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
//			STBItem item = BaseSTBItem.getItemFromItemStack(event.getCurrentItem(), c);
//			if (item == null) {
//				event.setCancelled(true);
//			}
//		}
//	}
//
//	protected void filterItemsAllowed(InventoryDragEvent event, Class<? extends BaseSTBItem> c) {
//		Set<Integer> slots = event.getRawSlots();
//		Inventory topInv = event.getView().getTopInventory();
//		for (int slot : slots) {
//			if (slot < topInv.getSize()) {
//				if (slots.size() > 1) {
//					event.setCancelled(true);
//					break;
//				} else {
//					STBItem item = BaseSTBItem.getItemFromItemStack(event.getOldCursor(), c);
//					if (item == null) {
//						event.setCancelled(true);
//						break;
//					}
//				}
//			}
//		}
//	}
}
