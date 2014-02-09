package me.desht.sensibletoolbox.storage;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.metadata.MetadataValue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class LocationManager {
	private static final long MIN_SAVE_INTERVAL = 30000;  // 10 sec
	private static final FilenameFilter ymlFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".yml");
		}
	};
	private static LocationManager instance;
	private final STBWorldMap worldMap;
	private final Map<String, Set<ChunkCoords>> dirtyChunks;
	private final File saveDir;
	private final Set<String> unloadedWorlds = new HashSet<String>();
	private final Map<String, Map<PersistableLocation,ConfigurationSection>> deferredBlocks =
			new HashMap<String, Map<PersistableLocation, ConfigurationSection>>();
	private boolean saveNeeded = false;
	private long lastSave;
	private final Map<String,Set<BaseSTBBlock>> tickers = new HashMap<String,Set<BaseSTBBlock>>();

	private LocationManager() {
		lastSave = System.currentTimeMillis();
		worldMap = new STBWorldMap();
		dirtyChunks = new HashMap<String, Set<ChunkCoords>>();
		saveDir = new File(SensibleToolboxPlugin.getInstance().getDataFolder(), "blocks");
		if (!saveDir.exists()) {
			if (!saveDir.mkdir()) {
				LogUtils.severe("can't create directory" + saveDir);
			}
		}
	}

	public static synchronized LocationManager getManager() {
		if (instance == null) {
			instance = new LocationManager();
		}
		return instance;
	}

	@SuppressWarnings("CloneDoesntCallSuperClone")
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void addTicker(BaseSTBBlock stb) {
		Location l = stb.getLocation();
		World w = l.getWorld();
		if (!tickers.containsKey(w.getName())) {
			tickers.put(w.getName(), new HashSet<BaseSTBBlock>());
		}
		Debugger.getInstance().debug(2, "add ticking block " + stb + " in world: " + w.getName());
		tickers.get(w.getName()).add(stb);
	}

	private void removeTicker(BaseSTBBlock stb) {
		Location l = stb.getLocation();
		World w = l.getWorld();
		if (!tickers.containsKey(w.getName())) {
			tickers.put(w.getName(), new HashSet<BaseSTBBlock>());
		}
		Debugger.getInstance().debug(2, "remove ticking block " + stb + " in world: " + w.getName());
		tickers.get(w.getName()).remove(stb);
	}

	public void registerLocation(Location loc, BaseSTBBlock stb) {
		BaseSTBBlock stb2 = get(loc);
		if (stb2 == null) {
			String worldName = loc.getWorld().getName();
			Chunk chunk = loc.getChunk();
			BlockPosition pos = new BlockPosition(loc);
			stb.setLocation(loc);
			worldMap.get(worldName).get(chunk).put(pos, stb);
			markChunkDirty(worldName, pos);
			if (stb.shouldTick()) {
				addTicker(stb);
			}
			Debugger.getInstance().debug("Registered " + stb + " @ " + loc);
		} else {
			LogUtils.warning("Attempt to register duplicate STB block " + stb + " @ " + loc + " - existing block " + stb2);
		}
	}

	public void updateLocation(Location loc) {
//		System.out.println("location " + loc + " updated");
		String worldName = loc.getWorld().getName();
		BlockPosition pos = new BlockPosition(loc);
		markChunkDirty(worldName, pos);
	}

	public void unregisterLocation(Location loc) {
		BaseSTBBlock stb = get(loc);
		if (stb != null) {
			if (stb.shouldTick()) {
				removeTicker(stb);
			}
			stb.setLocation(null);
			String worldName = loc.getWorld().getName();
			BlockPosition pos = new BlockPosition(loc);
			Chunk chunk = loc.getChunk();
			worldMap.get(worldName).get(chunk).remove(pos);
			markChunkDirty(worldName, pos);
			Debugger.getInstance().debug("Unregistered " + stb + " @ " + loc);
		} else {
			LogUtils.warning("Attempt to unregister non-existent STB block @ " + loc);
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
		Chunk chunk = loc.getChunk();
		BlockPosition pos = new BlockPosition(loc);

		BaseSTBBlock stb = worldMap.get(worldName).get(chunk).get(pos);
		if (stb == null) {
			List<MetadataValue> l = loc.getBlock().getMetadata(BaseSTBBlock.STB_MULTI_BLOCK);
			for (MetadataValue mv : l) {
				if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
					BlockPosition pos2 = (BlockPosition) mv.value();
					return worldMap.get(worldName).get(chunk).get(pos2);
				}
			}
			return null;
		} else {
			return stb;
		}
	}

	/**
	 * Get all the STB blocks in the given chunk
	 *
	 * @param chunk the chunk to check
	 * @return an array of STB block objects
	 */
	public BaseSTBBlock[] get(Chunk chunk) {
		String worldName = chunk.getWorld().getName();
		STBBlockMap stbm = worldMap.get(worldName).get(chunk);
		List<? extends BaseSTBBlock> l = stbm.list();
		return l.toArray(new BaseSTBBlock[l.size()]);
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

	public void tick() {
		for (World w : Bukkit.getWorlds()) {
			if (tickers.containsKey(w.getName())) {
				for (BaseSTBBlock stb : tickers.get(w.getName())) {
					stb.onServerTick();
				}
			}
		}
		if (System.currentTimeMillis() - lastSave > MIN_SAVE_INTERVAL) {
			save();
		}
	}

	public void save() {
		if (!saveNeeded) {
			return;
		}

		Debugger.getInstance().debug("saving " + dirtyChunks.size() + " chunks");
		for (Map.Entry<String, Set<ChunkCoords>> entry : dirtyChunks.entrySet()) {
			for (ChunkCoords cc : entry.getValue()) {
				YamlConfiguration conf = worldMap.get(entry.getKey()).get(cc).freeze();
				File dir = getWorldFolder(entry.getKey());
				File f = new File(dir, "C_" + cc.getX() + "_" + cc.getZ() + ".yml");
				try {
					if (conf.getKeys(false).size() == 0) {
						Debugger.getInstance().debug("deleting empty chunk file " + f);
						if (!f.delete()) {
							LogUtils.warning("can't delete " + f);
						}
					} else {
						Debugger.getInstance().debug(2, "saving " + conf.getKeys(false).size() + " objects to " + f);
						conf.save(f);
					}
				} catch (IOException e) {
					LogUtils.severe("can't save data to " + f);
				}
			}
			entry.getValue().clear();
		}
		lastSave = System.currentTimeMillis();
		saveNeeded = false;
	}

	public void load() {
		for (File worldDir : saveDir.listFiles()) {
			String worldName = worldDir.getName();
			World world = Bukkit.getWorld(worldName);
			if (world != null) {
				load(world);
			} else {
				// defer loading
				unloadedWorlds.add(worldName);
			}
		}
	}

	public void load(World world) {
		String worldName = world.getName();
		File worldDir = new File(saveDir, worldName);
		if (worldDir.exists()) {
			for (File saveFile : worldDir.listFiles(ymlFilter)) {
				try {
					YamlConfiguration conf = new YamlConfiguration();
					conf.load(saveFile);
					for (String k : conf.getKeys(false)) {
						ConfigurationSection cs = conf.getConfigurationSection(k);
						BlockPosition pos = BlockPosition.fromString(k);
						Location loc = new Location(world, pos.getX(), pos.getY(), pos.getZ());
						String type = cs.getString("TYPE");
						BaseSTBItem tmpl = BaseSTBItem.getItemById(type, cs);
						if (tmpl != null) {
							if (tmpl instanceof BaseSTBBlock) {
								registerLocation(loc, (BaseSTBBlock) tmpl);
							} else {
								LogUtils.severe("STB item " + type + " @ " + loc + " is not a block!");
							}
						} else {
							// defer it - should hopefully be registered by another plugin later
							deferBlockLoad(world, pos, cs);
						}
					}
					Debugger.getInstance().debug("loaded " + conf.getKeys(false).size() + " objects from " + saveFile);
				} catch (Exception e) {
					LogUtils.severe("can't load " + saveFile + ": " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		dirtyChunks.get(worldName).clear();
		saveNeeded = false;
	}

	private void deferBlockLoad(World world, BlockPosition pos, ConfigurationSection cs) {
		String type = cs.getString("TYPE");
		if (!deferredBlocks.containsKey(type)) {
			deferredBlocks.put(type, new HashMap<PersistableLocation, ConfigurationSection>());
		}
		PersistableLocation pl = new PersistableLocation(world, pos.getX(), pos.getY(), pos.getZ());
		Debugger.getInstance().debug(2, "block loading for " + type + " @ " + pl + " deferred - no registration for it yet");
		deferredBlocks.get(type).put(pl, cs);
	}

	public void loadDeferredBlock(String type) {
		Map<PersistableLocation, ConfigurationSection> map = deferredBlocks.get(type);
		if (map != null) {
			Debugger.getInstance().debug("loading " + map.size() + " deferred blocks of type: " + type);
			for (Map.Entry<PersistableLocation, ConfigurationSection> entry : map.entrySet()) {
				if (entry.getKey().isWorldAvailable()) {
					BaseSTBItem stb = BaseSTBItem.getItemById(type, entry.getValue());
					registerLocation(entry.getKey().getLocation(), (BaseSTBBlock) stb);
				}
			}
			deferredBlocks.remove(type);
		}
	}

	/**
	 * The given world has just become unloaded..
	 *
	 * @param world the world that has been unloaded
	 */
	public void worldUnloaded(World world) {
		String worldName = world.getName();
		worldMap.remove(worldName);
		unloadedWorlds.add(worldName);
	}

	/**
	 * The given world has just become loaded.
	 *
	 * @param world the world that has been loaded
	 */
	public void worldLoaded(World world) {
		load(world);
		unloadedWorlds.remove(world.getName());
	}

	private void markChunkDirty(String worldName, BlockPosition pos) {
		if (!dirtyChunks.containsKey(worldName)) {
			dirtyChunks.put(worldName, new HashSet<ChunkCoords>());
		}
		dirtyChunks.get(worldName).add(new ChunkCoords(pos));
		saveNeeded = true;
	}

	private File getWorldFolder(String worldName) {
		File f = new File(saveDir, worldName);
		if (!f.exists()) {
			if (!f.mkdir()) {
				LogUtils.severe("can't create directory " + f);
			}
 		}
		return f;
	}

	public BaseSTBBlock[] listBlocks(World world, boolean sorted) {
		STBChunkMap stcm = worldMap.get(world.getName());
		List<? extends BaseSTBBlock> stb = stcm.list();
		return sorted ?
				MiscUtil.asSortedList(stb).toArray(new BaseSTBBlock[stb.size()]) :
				stb.toArray(new BaseSTBBlock[stb.size()]);
	}
}
