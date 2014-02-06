package me.desht.sensibletoolbox.listeners;

import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.filters.AbstractItemFilter;
import me.desht.sensibletoolbox.items.itemroutermodules.DirectionalItemRouterModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class ItemRouterModuleListener extends STBBaseListener {
	public ItemRouterModuleListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onItemRouterModuleClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		if (!(event.getInventory().getTitle().equals(DirectionalItemRouterModule.getInventoryTitle()))) {
			return;
		}
		filterItemsAllowed(event, AbstractItemFilter.class);
	}


	@EventHandler
	public void onItemRouterModuleDrag(InventoryDragEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		if (!(event.getInventory().getTitle().equals(DirectionalItemRouterModule.getInventoryTitle()))) {
			return;
		}
		for (ItemStack stack : event.getInventory()) {
			if (stack != null) {
				event.setCancelled(true);
				return;
			}
		}
		filterItemsAllowed(event, AbstractItemFilter.class);
	}

	@EventHandler
	public void onItemRouterModuleClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}
		if (!(event.getInventory().getTitle().equals(DirectionalItemRouterModule.getInventoryTitle()))) {
			return;
		}

		DirectionalItemRouterModule mod = BaseSTBItem.getItemFromItemStack(event.getPlayer().getItemInHand(), DirectionalItemRouterModule.class);
		if (mod == null) {
			// shouldn't happen - player must be holding one of these to open this inventory
			LogUtils.warning("player is not holding a directional item router module?");
			return;
		}

		mod.installFilter(null);
		for (ItemStack stack : event.getInventory()) {
			AbstractItemFilter itemFilter = BaseSTBItem.getItemFromItemStack(stack, AbstractItemFilter.class);
			if (itemFilter != null) {
				mod.installFilter(itemFilter);
			} else if (stack != null) {
				event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), stack);
			}
		}

		event.getPlayer().setItemInHand(mod.toItemStack(1));
	}
}
