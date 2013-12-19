package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener extends STBBaseListener {
	public BlockListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event) {
		BaseSTBItem item = plugin.getLocationManager().get(event.getBlock().getLocation());
		if (item != null) {
			item.handleBlockDamage(event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		BaseSTBItem item = BaseSTBItem.getBaseItem(event.getItemInHand());
		if (item != null) {
			item.handleBlockPlace(event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		BaseSTBItem item = plugin.getLocationManager().get(event.getBlock().getLocation());
		if (item != null) {
			item.handleBlockBreak(event);
		}
	}
}
