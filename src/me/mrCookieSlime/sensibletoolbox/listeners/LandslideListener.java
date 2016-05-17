package me.mrCookieSlime.sensibletoolbox.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.desht.dhutils.LogUtils;
import me.desht.landslide.BlockSlideEvent;
import me.mrCookieSlime.sensibletoolbox.core.storage.LocationManager;

public class LandslideListener implements Listener {
	
    public LandslideListener(Plugin plugin) {
        try {
            Class.forName("me.desht.landslide.BlockSlideEvent");
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } catch (ClassNotFoundException e) {
            LogUtils.warning("Consider installing Landslide v1.5.0 or later for better slide protection of Checkers boards");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockSlide(BlockSlideEvent event) {
        if (LocationManager.getManager().get(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
        }
    }
}
