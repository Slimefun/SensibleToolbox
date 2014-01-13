package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener extends STBBaseListener {
	public WorldListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		plugin.getLocationManager().worldLoaded(event.getWorld());
	}

	@EventHandler
	public void onWorldUnLoad(WorldUnloadEvent event) {
		plugin.getLocationManager().worldUnloaded(event.getWorld());
	}

//	@EventHandler(priority = EventPriority.MONITOR)
//	public void onChunkUnload(ChunkUnloadEvent event) {
//		System.out.println("chunk unload: " + event.getChunk().toString() + " cancelled = " + event.isCancelled());
//	}
//
//	@EventHandler(priority = EventPriority.MONITOR)
//	public void onChunkLoad(ChunkLoadEvent event) {
//		System.out.println("chunk load: " + event.getChunk().toString());
//	}
}
