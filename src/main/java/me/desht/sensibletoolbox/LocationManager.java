package me.desht.sensibletoolbox;

import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.util.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.metadata.MetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocationManager {
	private static final String SAVE_FILE = "locations.yml";
	private final SensibleToolboxPlugin plugin;
	private final Map<String, Map<BlockPosition, BaseSTBBlock>> loaded = new HashMap<String, Map<BlockPosition, BaseSTBBlock>>();
	private final Map<String, Map<BlockPosition, BaseSTBBlock>> unloaded = new HashMap<String, Map<BlockPosition, BaseSTBBlock>>();
	private boolean saveNeeded;

	public LocationManager(SensibleToolboxPlugin plugin) {
		this.plugin = plugin;
	}

	public void registerLocation(Location loc, BaseSTBBlock stbItem) {
		String worldName = loc.getWorld().getName();
		if (!loaded.containsKey(worldName)) {
			loaded.put(worldName, new HashMap<BlockPosition, BaseSTBBlock>());
		}
		loaded.get(worldName).put(new BlockPosition(loc), stbItem);
		saveNeeded = true;
		System.out.println("registered location " + loc + " for " + stbItem);
	}

	public void unregisterLocation(Location loc, BaseSTBBlock stbItem) {
		String worldName = loc.getWorld().getName();
		if (loaded.containsKey(worldName)) {
			Map<BlockPosition, BaseSTBBlock> m = loaded.get(worldName);
			BlockPosition pos = new BlockPosition(loc);
			BaseSTBBlock existing = m.get(pos);
			if (existing != null) {
				if (existing.getClass() != stbItem.getClass()) {
					System.out.println("warning: class mismatch - expected " + stbItem.getClass().getName() + ", found " + existing.getClass().getName());
				}
				m.remove(pos);
				saveNeeded = true;
			}
		}
	}

	public void updateLocation(Location loc, BaseSTBBlock stbItem) {
		String worldName = loc.getWorld().getName();
		if (loaded.containsKey(worldName)) {
			loaded.get(worldName).put(new BlockPosition(loc), stbItem);
			saveNeeded = true;
			System.out.println("updated location " + loc + " for " + stbItem);
		}
	}
	/**
	 * Get the STB block at the given location.
	 *
	 * @param loc the location to check at
	 * @return the STB block item at the given location, or null if no matching item
	 */
	public BaseSTBBlock get(Location loc) {
		String worldName = loc.getWorld().getName();
		BlockPosition pos = new BlockPosition(loc);
		if (loaded.containsKey(worldName)) {
			Map<BlockPosition, BaseSTBBlock> m = loaded.get(worldName);
			if (m.containsKey(pos)) {
				return m.get(pos);
			} else {
				// maybe it's part of a multi-block structure?
				for (MetadataValue mv : loc.getBlock().getMetadata(BaseSTBBlock.STB_MULTI_BLOCK)) {
					if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
						BlockPosition pos2 = (BlockPosition) mv.value();
						if (m.containsKey(pos2)) {
							return m.get(pos2);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get the STB block of the given type at the given location.
	 *
	 * @param loc the location to check at
	 * @param type the type of STB block required
	 * @param <T> a subclass of BaseSTBBlock
	 * @return the STB block item at the given location, or null if no matching item
	 */
	public <T extends BaseSTBBlock> T get(Location loc, Class<T> type) {
		BaseSTBBlock stbBlock = get(loc);
		if (stbBlock != null && type.isAssignableFrom(stbBlock.getClass())) {
			return type.cast(stbBlock);
		} else {
			return null;
		}
	}

	public void save() {
		if (saveNeeded) {
			YamlConfiguration conf = new YamlConfiguration();

			for (String worldName : loaded.keySet()) {

				for (Map.Entry<BlockPosition, BaseSTBBlock> e : loaded.get(worldName).entrySet()) {
					String key = String.format("%s,%d,%d,%d", worldName, e.getKey().getX(), e.getKey().getY(), e.getKey().getZ());
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
				String worldName = fields[0];
				int x = Integer.parseInt(fields[1]);
				int y = Integer.parseInt(fields[2]);
				int z = Integer.parseInt(fields[3]);
				World w = Bukkit.getWorld(worldName);
				BaseSTBBlock item = (BaseSTBBlock) conf.get(key);
				if (w != null) {
					if (!loaded.containsKey(worldName)) {
						loaded.put(worldName, new HashMap<BlockPosition, BaseSTBBlock>());
					}
					loc.setWorld(w);
					loc.setX(x);
					loc.setY(y);
					loc.setZ(z);
					item.setBaseLocation(loc);
					loaded.get(worldName).put(new BlockPosition(loc), item);
				} else {
					// this world's not loaded (yet)
					if (!unloaded.containsKey(worldName)) {
						unloaded.put(worldName, new HashMap<BlockPosition, BaseSTBBlock>());
					}
					unloaded.get(worldName).put(new BlockPosition(x, y, z), item);
				}
			}
 			saveNeeded = false;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.severe("can't load " + SAVE_FILE + ": " + e.getMessage());
		}
	}

	/**
	 * The given world has just become unloaded; take any STB blocks that were in it off the active list,
	 * and add them to the unloaded list for that world.
	 *
	 * @param world the world that has been unloaded
	 */
	public void worldUnloaded(World world) {
		String worldName = world.getName();
		unloaded.put(worldName, loaded.get(worldName));
		loaded.remove(worldName);
	}

	/**
	 * The given world has just become loaded; set up any STB blocks that are on the unloaded list for it.
	 *
	 * @param world the world that has been loaded
	 */
	public void worldLoaded(World world) {
		String worldName = world.getName();
		loaded.put(worldName, unloaded.get(worldName));
		for (Map.Entry<BlockPosition, BaseSTBBlock> e : loaded.get(worldName).entrySet()) {
			Location loc = new Location(world, e.getKey().getX(), e.getKey().getY(), e.getKey().getZ());
			e.getValue().setBaseLocation(loc);
		}
		unloaded.remove(worldName);
	}

	public void tick() {
		Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		for (String worldName : loaded.keySet()) {
			Map<BlockPosition,BaseSTBBlock> m = loaded.get(worldName);
			World w = Bukkit.getWorld(worldName);
			if (w == null) {
				// shouldn't happen...
				continue;
			}
			loc.setWorld(w);
			for (Map.Entry<BlockPosition, BaseSTBBlock> e : m.entrySet()) {
				BlockPosition pos = e.getKey();
				if (w.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
					loc.setX(pos.getX());
					loc.setY(pos.getY());
					loc.setZ(pos.getZ());
					e.getValue().onServerTick(loc);
				}
			}
		}
		save();
	}

	public BaseSTBBlock[] listItems(World w, boolean sorted) {
		Map<BlockPosition,BaseSTBBlock> map = loaded.get(w.getName());
		if (map != null) {
			return sorted ?
					MiscUtil.asSortedList(map.values()).toArray(new BaseSTBBlock[map.size()]) :
					map.values().toArray(new BaseSTBBlock[map.size()]);
		} else {
			return new BaseSTBBlock[0];
		}
	}
}
