package me.desht.sensibletoolbox.listeners;

import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.Floodlight;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.HashSet;
import java.util.Set;

public class FloodlightListener extends STBBaseListener {
	private final Set<Floodlight> lights = new HashSet<Floodlight>();

	public FloodlightListener(SensibleToolboxPlugin plugin) {
		super(plugin);
		System.out.println("listener created!");
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getEntity() instanceof Monster) {
			int range = Floodlight.INTERDICTION_RANGE * Floodlight.INTERDICTION_RANGE;
			for (Floodlight light : lights) {
				Location loc = event.getLocation();
				if (loc.getWorld().equals(light.getLocation().getWorld()) && loc.distanceSquared(light.getLocation()) < range) {
					Debugger.getInstance().debug(2, light + " prevents spawn of " + event.getEntity().getType());
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		Item item = event.getEntity();
		for (MetadataValue mv : item.getMetadata(Floodlight.STB_FLOODLIGHT_FLAME)) {
			if (mv.getOwningPlugin() == plugin) {
				Floodlight light = (Floodlight) mv.value();
//				light.addFlame();
				break;
			}
		}
	}

	public void registerFloodlight(Floodlight light) {
		System.out.println("register light @ " + light.getLocation());
		lights.add(light);
	}

	public void unregisterFloodlight(Floodlight light) {
		System.out.println("unregister light @ " + light.getLocation());
		lights.remove(light);
	}
}
