package me.desht.sensibletoolbox.storage;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.api.STBItem;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.metadata.FixedMetadataValue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LocationManager {
    private static LocationManager instance;

    private final Set<String> deferredBlocks = new HashSet<String>();
    private final PreparedStatement queryStmt;
    private final PreparedStatement queryTypeStmt;
    private long lastSave;
    private int saveInterval;  // ms
    private long totalTicks;
    private long totalTime;
    private final SensibleToolboxPlugin plugin;
    private final DBStorage dbStorage;
    private final Thread updaterTask;

    // tracks those blocks which need to do something on a server tick
    private final Map<String, Set<BaseSTBBlock>> tickers = new HashMap<String, Set<BaseSTBBlock>>();
    // indexes all loaded blocks by world and (frozen) location
    private final Map<String, Map<String, BaseSTBBlock>> blockIndex = new HashMap<String, Map<String, BaseSTBBlock>>();
    // tracks the pending updates by (frozen) location since the last save was done
    private final Map<String, UpdateRecord> pendingUpdates = new HashMap<String, UpdateRecord>();
    // blocking queue is used to pass actual updates over to the DB writer thread
    private final BlockingQueue<UpdateRecord> updateQueue = new LinkedBlockingQueue<UpdateRecord>();

    private LocationManager(SensibleToolboxPlugin plugin) throws SQLException {
        this.plugin = plugin;
        saveInterval = plugin.getConfig().getInt("save_interval", 30) * 1000;
        lastSave = System.currentTimeMillis();
        try {
            dbStorage = new DBStorage();
            dbStorage.getConnection().setAutoCommit(false);
            queryStmt = dbStorage.getConnection().prepareStatement("SELECT * FROM " + DBStorage.makeTableName("blocks") + " WHERE world_id = ?");
            queryTypeStmt = dbStorage.getConnection().prepareStatement("SELECT * FROM " + DBStorage.makeTableName("blocks") + " WHERE world_id = ? and type = ?");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Unable to initialise DB storage: " + e.getMessage());
        }
        updaterTask = new Thread(new DBUpdaterTask(this));
        updaterTask.start();
    }

    public static synchronized LocationManager getManager() {
        if (instance == null) {
            try {
                instance = new LocationManager(SensibleToolboxPlugin.getInstance());
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return instance;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    DBStorage getDbStorage() {
        return dbStorage;
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

    private Map<String, BaseSTBBlock> getWorldIndex(World w) {
        if (!blockIndex.containsKey(w.getName())) {
            blockIndex.put(w.getName(), new HashMap<String, BaseSTBBlock>());
        }
        return blockIndex.get(w.getName());
    }

    public void registerLocation(Location loc, BaseSTBBlock stb, boolean isPlacing) {
        STBBlock stb2 = get(loc);
        if (stb2 != null) {
            LogUtils.warning("Attempt to register duplicate STB block " + stb + " @ " + loc + " - existing block " + stb2);
            return;
        }

        stb.setLocation(loc);
        loc.getBlock().setMetadata(BaseSTBBlock.STB_BLOCK, new FixedMetadataValue(plugin, stb));
        String locStr = MiscUtil.formatLocation(loc);
        getWorldIndex(loc.getWorld()).put(locStr, stb);

        if (isPlacing) {
            addPendingDBOperation(loc, locStr, stb, UpdateRecord.Operation.INSERT);
        }

        if (stb.getTickRate() > 0) {
            addTicker(stb);
        }

        Debugger.getInstance().debug("Registered " + stb + " @ " + loc);
    }

    public void updateLocation(Location loc, BaseSTBBlock stb) {
        addPendingDBOperation(loc, MiscUtil.formatLocation(loc), stb, UpdateRecord.Operation.UPDATE);
    }

    public void unregisterLocation(Location loc, BaseSTBBlock stb) {
        if (stb != null) {
            if (stb.getTickRate() > 0) {
                removeTicker(stb);
            }
            stb.setLocation(null);
            loc.getBlock().removeMetadata(BaseSTBBlock.STB_BLOCK, plugin);
            String locStr = MiscUtil.formatLocation(loc);
            addPendingDBOperation(loc, locStr, stb, UpdateRecord.Operation.DELETE);
            getWorldIndex(loc.getWorld()).remove(locStr);
            Debugger.getInstance().debug("Unregistered " + stb + " @ " + loc);
        } else {
            LogUtils.warning("Attempt to unregister non-existent STB block @ " + loc);
        }
    }

    /**
     * Move an existing STB block to a new location.  Note that this method doesn't do any
     * redrawing of blocks.
     *
     * @param oldLoc the STB block's old location
     * @param newLoc the STB block's new location
     */
    public void moveBlock(BaseSTBBlock stb, Location oldLoc, Location newLoc) {
        oldLoc.getBlock().removeMetadata(BaseSTBBlock.STB_BLOCK, plugin);
        String locStr = MiscUtil.formatLocation(oldLoc);
        addPendingDBOperation(oldLoc, locStr, stb, UpdateRecord.Operation.DELETE);
        getWorldIndex(oldLoc.getWorld()).remove(locStr);

        stb.moveTo(oldLoc, newLoc);

        newLoc.getBlock().setMetadata(BaseSTBBlock.STB_BLOCK, new FixedMetadataValue(plugin, stb));
        locStr = MiscUtil.formatLocation(newLoc);
        addPendingDBOperation(newLoc, locStr, stb, UpdateRecord.Operation.INSERT);
        getWorldIndex(newLoc.getWorld()).put(locStr, stb);

        Debugger.getInstance().debug("moved " + stb + " from " + oldLoc + " to " + newLoc);
    }

    private void addPendingDBOperation(Location loc, String locStr, BaseSTBBlock stb, UpdateRecord.Operation op) {
        UpdateRecord existingRec = pendingUpdates.get(locStr);
        switch (op) {
            case INSERT:
                if (existingRec == null) {
                    // brand new insertion
                    pendingUpdates.put(locStr, new UpdateRecord(UpdateRecord.Operation.INSERT, loc, stb.getItemTypeID(), stb));
                } else if (existingRec.getOp() == UpdateRecord.Operation.DELETE) {
                    // re-inserting where a block was just deleted
                    pendingUpdates.put(locStr, new UpdateRecord(UpdateRecord.Operation.UPDATE, loc, stb.getItemTypeID(), stb));
                }
                break;
            case UPDATE:
                if (existingRec == null || existingRec.getOp() != UpdateRecord.Operation.INSERT) {
                    pendingUpdates.put(locStr, new UpdateRecord(UpdateRecord.Operation.UPDATE, loc, stb.getItemTypeID(), stb));
                }
                break;
            case DELETE:
                if (existingRec != null && existingRec.getOp() == UpdateRecord.Operation.INSERT) {
                    // remove a recent insertion
                    pendingUpdates.remove(locStr);
                } else {
                    pendingUpdates.put(locStr, new UpdateRecord(UpdateRecord.Operation.DELETE, loc, stb.getItemTypeID(), stb));
                }
                break;
            default:
                throw new IllegalArgumentException("Unexpected operation: " + op);
        }
    }


    /**
     * Get the STB block at the given location.
     *
     * @param loc the location to check at
     * @return the STB block item at the given location, or null if no matching item
     */
    public BaseSTBBlock get(Location loc) {
        BaseSTBBlock stb = (BaseSTBBlock) STBUtil.getMetadataValue(loc.getBlock(), BaseSTBBlock.STB_BLOCK);
        if (stb != null) {
            return stb;
        } else {
            // perhaps it's part of a multi-block structure
            return (BaseSTBBlock) STBUtil.getMetadataValue(loc.getBlock(), BaseSTBBlock.STB_MULTI_BLOCK);
        }
    }

    /**
     * Get the STB block of the given type at the given location.
     *
     * @param loc  the location to check at
     * @param type the type of STB block required
     * @param <T>  a subclass of BaseSTBBlock
     * @return the STB block item at the given location, or null if no matching item
     */
    public <T extends BaseSTBBlock> T get(Location loc, Class<T> type) {
        STBBlock stbBlock = get(loc);
        if (stbBlock != null && type.isAssignableFrom(stbBlock.getClass())) {
            return type.cast(stbBlock);
        } else {
            return null;
        }
    }

    /**
     * Get all the STB blocks in the given chunk
     *
     * @param chunk the chunk to check
     * @return an array of STB block objects
     */
    public List<STBBlock> get(Chunk chunk) {
        List<STBBlock> res = new ArrayList<STBBlock>();
        for (STBBlock stb : listBlocks(chunk.getWorld(), false)) {
            PersistableLocation pLoc = stb.getPersistableLocation();
            if ((int) pLoc.getX() >> 4 == chunk.getX() && (int) pLoc.getZ() >> 4 == chunk.getZ()) {
                res.add(stb);
            }
        }
        return res;
    }

    public void tick() {
        long now = System.nanoTime();
        for (World w : Bukkit.getWorlds()) {
            if (tickers.containsKey(w.getName())) {
                for (BaseSTBBlock stb : tickers.get(w.getName())) {
                    PersistableLocation pLoc = stb.getPersistableLocation();
                    int x = (int) pLoc.getX(), z = (int) pLoc.getZ();
                    if (w.isChunkLoaded(x >> 4, z >> 4)) {
                        stb.tick();
                        if (stb.getTicksLived() % stb.getTickRate() == 0) {
                            stb.onServerTick();
                        }
                    }
                }
            }
        }
        totalTicks++;
        totalTime += System.nanoTime() - now;
//		System.out.println("tickers took " + (System.nanoTime() - now) + " ns");
        if (System.currentTimeMillis() - lastSave > saveInterval) {
            save();
        }
    }

    public void save() {
        // send any pending updates over to the DB updater thread via a BlockingQueue
        if (!pendingUpdates.isEmpty()) {
            for (Map.Entry<String, UpdateRecord> rec : pendingUpdates.entrySet()) {
                updateQueue.add(rec.getValue());
            }
            updateQueue.add(UpdateRecord.commitRecord());
            pendingUpdates.clear();
        }
        lastSave = System.currentTimeMillis();
    }

    public void loadFromDatabase(World world, String wantedType) throws SQLException {
        ResultSet rs;
        if (wantedType == null) {
            queryStmt.setString(1, world.getUID().toString());
            rs = queryStmt.executeQuery();
        } else {
            queryTypeStmt.setString(1, world.getUID().toString());
            queryTypeStmt.setString(2, wantedType);
            rs = queryTypeStmt.executeQuery();
        }
        while (rs.next()) {
            String type = rs.getString(5);
            if (deferredBlocks.contains(type) && !type.equals(wantedType)) {
                continue;
            }
            int x = rs.getInt(2);
            int y = rs.getInt(3);
            int z = rs.getInt(4);
            String data = rs.getString(6);
            try {
                YamlConfiguration conf = new YamlConfiguration();
                conf.loadFromString(data);
                STBItem stbItem = BaseSTBItem.getItemById(type, conf);
                if (stbItem != null) {
                    Location loc = new Location(world, x, y, z);
                    if (stbItem instanceof STBBlock) {
                        registerLocation(loc, (BaseSTBBlock) stbItem, false);
                    } else {
                        LogUtils.severe("STB item " + type + " @ " + loc + " is not a block!");
                    }
                } else {
                    // defer it - should hopefully be registered by another plugin later
                    Debugger.getInstance().debug("deferring load for unrecognised block type '" + type + "'");
                    deferBlockLoad(type);
                }
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
                LogUtils.severe(String.format("Can't load STB block at %s,%d,%d,%d: %s", world.getName(), x, y, z, e.getMessage()));
            }
        }
    }

    public void load() throws SQLException {
        for (World w : Bukkit.getWorlds()) {
            loadFromDatabase(w, null);
        }
    }

    private void deferBlockLoad(String typeID) {
        deferredBlocks.add(typeID);
    }

    /**
     * Load all blocks for the given block type.  Called when a block is registered after the
     * initial DB load is done.
     *
     * @param type the block type
     * @throws SQLException if there is a problem loading from the DB
     */
    public void loadDeferredBlocks(String type) throws SQLException {
        if (deferredBlocks.contains(type)) {
            for (World world : Bukkit.getWorlds()) {
                loadFromDatabase(world, type);
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
        save();

        Map<String, BaseSTBBlock> map = blockIndex.get(world.getName());
        if (map != null) {
            map.clear();
            blockIndex.remove(world.getName());
        }
    }

    /**
     * The given world has just become loaded.
     *
     * @param world the world that has been loaded
     */
    public void worldLoaded(World world) {
        if (!blockIndex.containsKey(world.getName())) {
            try {
                loadFromDatabase(world, null);
            } catch (SQLException e) {
                e.printStackTrace();
                LogUtils.severe("can't load STB data for world " + world.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Get an array of all STB blocks for the given world.
     *
     * @param world  the world to query
     * @param sorted if true, the array is sorted by block type
     * @return an array of STB block objects
     */
    public BaseSTBBlock[] listBlocks(World world, boolean sorted) {
        Collection<BaseSTBBlock> list = getWorldIndex(world).values();
        return sorted ?
                MiscUtil.asSortedList(list).toArray(new BaseSTBBlock[list.size()]) :
                list.toArray(new BaseSTBBlock[list.size()]);
    }

    /**
     * Get the average time in nanoseconds that the plugin has spent ticking tickable blocks
     * since the plugin started up.
     *
     * @return the average time spent ticking blocks
     */
    public long getAverageTimePerTick() {
        return totalTime / totalTicks;
    }

    /**
     * Set the save interval; any changes will be written to the persisted DB this often.
     *
     * @param saveInterval the save interval, in seconds.
     */
    public void setSaveInterval(int saveInterval) {
        this.saveInterval = saveInterval * 1000;
    }

    /**
     * Shut down the location manager after ensuring all pending changes are written to the DB,
     * and the DB thread has exited.  This may block the main thread for a short time, but should only
     * be called when the plugin is being disabled.
     */
    public void shutdown() {
        updateQueue.add(UpdateRecord.finishingRecord());
        try {
            // 5 seconds is hopefully enough for the DB thread to finish its work
            updaterTask.join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            dbStorage.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    UpdateRecord getUpdateRecord() throws InterruptedException {
        return updateQueue.take();
    }
}
