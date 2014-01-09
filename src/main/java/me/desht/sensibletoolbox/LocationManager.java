package me.desht.sensibletoolbox;

import me.desht.dhutils.LogUtils;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.items.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocationManager {
	private static final String SAVE_FILE = "locations.yml";

	private final SensibleToolboxPlugin plugin;

	private final Map<PersistableLocation, BaseSTBBlock> locations = new HashMap<PersistableLocation, BaseSTBBlock>();
	private boolean saveNeeded;

	public LocationManager(SensibleToolboxPlugin plugin) {
		this.plugin = plugin;
	}

	public void registerLocation(Location loc, BaseSTBBlock stbItem) {
		locations.put(new PersistableLocation(loc), stbItem);
		saveNeeded = true;
		System.out.println("registered location " + loc + " for " + stbItem);
	}

	public void unregisterLocation(Location loc, BaseSTBItem stbItem) {
		PersistableLocation pLoc = new PersistableLocation(loc);
		BaseSTBItem existing = locations.get(pLoc);
		if (existing != null) {
			if (existing.getClass() != stbItem.getClass()) {
				System.out.println("warning: class mismatch - expected " + stbItem.getClass().getName() + ", found " + existing.getClass().getName());
			}
			locations.remove(pLoc);
			saveNeeded = true;
		}
	}

	public void updateLocation(Location loc, BaseSTBBlock stbItem) {
		locations.put(new PersistableLocation(loc), stbItem);
		saveNeeded = true;
		System.out.println("updated location " + loc + " for " + stbItem);
	}

	public BaseSTBBlock get(Location loc) {
		PersistableLocation pLoc = new PersistableLocation(loc);
		if (locations.containsKey(pLoc)) {
			return locations.get(pLoc);
		} else {
			for (MetadataValue mv : loc.getBlock().getMetadata(BaseSTBItem.STB_MULTI_BLOCK)) {
				if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
					Vector vec = (Vector) mv.value();
					pLoc = new PersistableLocation(loc.getWorld(), vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
					if (locations.containsKey(pLoc)) {
						return locations.get(pLoc);
					}
				}
			}
			return null;
		}
	}

	public void save() {
		if (saveNeeded) {
			YamlConfiguration conf = new YamlConfiguration();

			for (Map.Entry<PersistableLocation, BaseSTBBlock> e : locations.entrySet()) {
				String key = String.format("%s,%d,%d,%d",
						e.getKey().getWorldName(),
						NumberConversions.floor(e.getKey().getX()),
						NumberConversions.floor(e.getKey().getY()),
						NumberConversions.floor(e.getKey().getZ()));
				conf.set(key, e.getValue());
			}
			try {
				conf.save(new File(plugin.getDataFolder(), SAVE_FILE));
				System.out.println(conf.getKeys(false).size() + " locations saved");
				saveNeeded = false;
			} catch (IOException e) {
				System.out.println("can't save " + SAVE_FILE + ": " + e.getMessage());
			}
		}
	}

	public void load() {
		YamlConfiguration conf = new YamlConfiguration();
		Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		try {
			File f = new File(plugin.getDataFolder(), SAVE_FILE);
			System.out.println("loading data from " + f);
			conf.load(f);
			System.out.println("loaded data: " + conf.getKeys(false).size() + " keys");
			for (String key : conf.getKeys(false)) {
				System.out.println("found key " + key + " = " + conf.get(key));
				String[] fields = key.split(",");
				int x = Integer.parseInt(fields[1]);
				int y = Integer.parseInt(fields[2]);
				int z = Integer.parseInt(fields[3]);
				loc.setWorld(Bukkit.getWorld(fields[0]));
				loc.setX(x);
				loc.setY(y);
				loc.setZ(z);
				BaseSTBBlock item = (BaseSTBBlock) conf.get(key);
				locations.put(new PersistableLocation(loc), item);
				Block origin = loc.getBlock();
				item.setBaseLocation(loc);
				for (Vector v : item.getBlockStructure()) {
					Block b1 = origin.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ());
					b1.setMetadata(BaseSTBItem.STB_MULTI_BLOCK, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), loc.toVector()));
				}
			}
 			saveNeeded = false;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.severe("can't load " + SAVE_FILE + ": " + e.getMessage());
		}
	}

	public void tick() {
		for (Map.Entry<PersistableLocation, BaseSTBBlock> e : locations.entrySet()) {
			e.getValue().onServerTick(e.getKey());
		}
		save();
	}
}
