package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.storage.LocationManager;
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
        LocationManager.getManager().worldLoaded(event.getWorld());
    }

    @EventHandler
    public void onWorldUnLoad(WorldUnloadEvent event) {
        LocationManager.getManager().worldUnloaded(event.getWorld());
    }

//	@EventHandler(priority = EventPriority.MONITOR)
//	public void onChunkUnload(ChunkUnloadEvent event) {
//		for (BaseSTBBlock stb : LocationManager.getManager().get(event.getChunk())) {
//			stb.onChunkUnload();
//		}
////		System.out.println("chunk unload: " + event.getChunk().toString() + " cancelled = " + event.isCancelled());
//	}
//
//	@EventHandler(priority = EventPriority.MONITOR)
//	public void onChunkLoad(ChunkLoadEvent event) {
//		for (BaseSTBBlock stb : LocationManager.getManager().get(event.getChunk())) {
//			stb.onChunkLoad();
//		}
////		System.out.println("chunk load: " + event.getChunk().toString());
//	}
}
