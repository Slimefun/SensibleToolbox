package me.mrCookieSlime.CSCoreLib.events.Listeners;

import java.io.File;

import me.mrCookieSlime.CSCoreLib.Configuration.Config;
import me.mrCookieSlime.CSCoreLib.Guide.Guides;
import me.mrCookieSlime.CSCoreLib.Guide.PluginGuide;
import me.mrCookieSlime.CSCoreLib.events.GuideOpenEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class GuideListener implements Listener {
	
	public GuideListener(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getItem() != null) {
				if (e.getItem().getType() == Material.ENCHANTED_BOOK) {
					if (e.getItem().getDurability() > 0) {
						if ((Guides.list().size() - 1) >= e.getItem().getDurability()) {
							Bukkit.getPluginManager().callEvent(new GuideOpenEvent(e.getPlayer(), Guides.get(e.getItem().getDurability())));
							e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BAT_TAKEOFF, (float) 0.7, 1);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		for (PluginGuide guide: Guides.list()) {
			if (guide != null) {
				if (guide.isGivenOnJoin()) {
					if (!new File("data-storage/" + guide.getPluginName() + "/Players/" + e.getPlayer().getUniqueId() + ".yml").exists()) {
						Config cfg = new Config(new File("data-storage/" + guide.getPluginName() + "/Players/" + e.getPlayer().getUniqueId() + ".yml"));
						cfg.setValue("joined", true);
						cfg.setValue("name", e.getPlayer().getName());
						
						guide.give(e.getPlayer());
					}
				}
			}
		}
	}

}
