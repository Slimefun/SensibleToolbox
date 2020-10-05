package io.github.thebusybiscuit.sensibletoolbox.listeners;

import javax.annotation.Nonnull;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;

/**
 * This {@link Listener} is responsible for any {@link WorldEvent} and loading or unloading
 * data for these {@link World Worlds}.
 * 
 * @author desht
 * 
 * @see LocationManager
 *
 */
public class WorldListener extends STBBaseListener {

    public WorldListener(@Nonnull SensibleToolboxPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        LocationManager.getManager().loadWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnLoad(WorldUnloadEvent event) {
        LocationManager.getManager().unloadWorld(event.getWorld());
    }
}
