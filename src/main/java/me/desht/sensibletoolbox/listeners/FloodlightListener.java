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

	public void registerFloodlight(Floodlight light) {
		Debugger.getInstance().debug("register light @ " + light.getLocation());
		lights.add(light);
	}

	public void unregisterFloodlight(Floodlight light) {
		Debugger.getInstance().debug("unregister light @ " + light.getLocation());
		lights.remove(light);
	}
}
