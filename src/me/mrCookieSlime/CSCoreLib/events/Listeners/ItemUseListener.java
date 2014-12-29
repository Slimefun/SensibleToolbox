package me.mrCookieSlime.CSCoreLib.events.Listeners;

import me.mrCookieSlime.CSCoreLib.events.ItemUseEvent;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class ItemUseListener implements Listener {
	
	public ItemUseListener(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getItem() != null) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block b = null;
				if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					b = e.getClickedBlock();
				}
				Bukkit.getPluginManager().callEvent(new ItemUseEvent(e.getPlayer(), e.getItem(), b));
			}
		}
	}

}
