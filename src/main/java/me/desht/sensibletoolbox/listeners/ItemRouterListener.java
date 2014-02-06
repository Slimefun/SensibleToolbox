package me.desht.sensibletoolbox.listeners;

import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.ItemRouter;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.itemroutermodules.ItemRouterModule;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class ItemRouterListener extends STBBaseListener {
	public ItemRouterListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onItemRouterClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		if (!(event.getInventory().getTitle().equals(ItemRouter.getInventoryTitle()))) {
			return;
		}

		filterItemsAllowed(event, ItemRouterModule.class);
	}

	@EventHandler
	public void onItemRouterDrag(InventoryDragEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		if (!(event.getInventory().getTitle().equals(ItemRouter.getInventoryTitle()))) {
			return;
		}
		filterItemsAllowed(event, ItemRouterModule.class);
	}

	@EventHandler
	public void onItemRouterClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}
		if (!(event.getInventory().getTitle().equals(ItemRouter.getInventoryTitle()))) {
			return;
		}

		Player player = (Player) event.getPlayer();
		Object loc = STBUtil.getMetadataValue(player, ItemRouter.STB_ITEM_ROUTER);
		if (loc != null && loc instanceof Location) {
			ItemRouter rtr = LocationManager.getManager().get((Location) loc, ItemRouter.class);
			Inventory topInv = event.getView().getTopInventory();
			if (rtr != null) {
				rtr.clearModules();
				for (ItemStack stack : topInv) {
					if (stack != null) {
						BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
						if (item != null && item instanceof ItemRouterModule) {
							for (int i = 0; i < stack.getAmount(); i++) {
								rtr.insertModule((ItemRouterModule) item);
							}
						}
					}
				}
				rtr.updateBlock();
			} else {
				LogUtils.warning("can't find expected item router @ " + loc);
			}
			player.removeMetadata(ItemRouter.STB_ITEM_ROUTER, plugin);
		}
	}
}
