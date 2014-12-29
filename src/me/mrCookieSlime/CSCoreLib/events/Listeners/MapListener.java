package me.mrCookieSlime.CSCoreLib.events.Listeners;

import me.mrCookieSlime.CSCoreLib.general.Inventory.Maps;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;

public class MapListener implements Listener {
	
	public MapListener(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if (Maps.inv.containsKey(e.getPlayer().getUniqueId())) {
			((Player) e.getPlayer()).playSound(e.getPlayer().getLocation(), Sound.BAT_TAKEOFF, (float) 0.7, 1);
			Maps.inv.remove(e.getPlayer().getUniqueId());
		}
	}

}
