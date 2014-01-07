package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;
import org.bukkit.material.Sign;

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
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(event.getItemInHand());
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

	@EventHandler(ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		final Block b = event.getBlock();
		Sign sign = (Sign) b.getState().getData();
		Block attachedTo = b.getRelative(sign.getAttachedFace());
		BaseSTBItem item = plugin.getLocationManager().get(attachedTo.getLocation());
		if (item != null) {
			boolean ret = item.handleSignConfigure(event);
			if (ret) {
				// pop the sign off next tick; it's done its job
				Bukkit.getScheduler().runTask(plugin, new Runnable() {
					@Override
					public void run() {
						b.setType(Material.AIR);
						b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.SIGN));
					}
				});
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {

		Block block = event.getBlock();
		BaseSTBItem item = plugin.getLocationManager().get(block.getLocation());
		if (item != null) {
			item.handleBlockPhysics(event);
		} else {
			if (block.getType() == Material.LEVER) {
				Lever l = (Lever) block.getState().getData();
				item = plugin.getLocationManager().get(block.getRelative(l.getAttachedFace()).getLocation());
				if (item != null) {
					event.setCancelled(true);
				}
			}
		}
	}
}
