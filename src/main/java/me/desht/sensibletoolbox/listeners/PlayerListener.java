package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener extends STBBaseListener {
	public PlayerListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack stack = event.getPlayer().getItemInHand();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
		if (item != null) {
			item.handleInteraction(event);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		ItemStack stack = event.getPlayer().getItemInHand();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
		if (item != null) {
			item.handleEntityInteraction(event);
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		ItemStack stack = event.getPlayer().getItemInHand();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
		if (item != null) {
			item.handleConsume(event);
		}
	}
}
