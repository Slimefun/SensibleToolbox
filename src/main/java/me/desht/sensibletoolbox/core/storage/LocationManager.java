package me.desht.sensibletoolbox.core.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.SensibleToolbox;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.api.util.STBUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.Sign;

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
    private final DBStorage dbStorage;
    private final Thread updaterTask;
    private static final BlockAccess blockAccess = new BlockAccess();

    // tracks those blocks (on a per-world basis) which need to do something on a server tick
    private final Map<UUID, Set<BaseSTBBlock>> allTickers = new HashMap<UUID, Set<BaseSTBBlock>>();
    // indexes all loaded blocks by world and (frozen) location
    private final Map<UUID, Map<String, BaseSTBBlock>> blockIndex = new HashMap<UUID, Map<String, BaseSTBBlock>>();
    // tracks the pending updates by (frozen) location since the last save was done
    private final Map<String, UpdateRecord> pendingUpdates = new HashMap<String, UpdateRecord>();
    // a blocking queue is used to pass actual updates over to the DB writer thread
    private final BlockingQueue<UpdateRecord> updateQueue = new LinkedBlockingQueue<UpdateRecord>();

    private LocationManager(SensibleToolboxPlugin plugin) throws SQLException {
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
        Location loc = stb.getLocation();
        World w = loc.getWorld();
        Set<BaseSTBBlock> tickerSet = allTickers.get(w.getUID());
        if (tickerSet == null) {
            tickerSet = Sets.newHashSet();
            allTickers.put(w.getUID(), tickerSet);
        }
        tickerSet.add(stb);
        Debugger.getInstance().debug(2, "Added ticking block " + stb);
    }

    private Map<String, BaseSTBBlock> getWorldIndex(World w) {
        Map<String,BaseSTBBlock> index = blockIndex.get(w.getUID());
        if (index == null) {
            index = Maps.newHashMap();
            blockIndex.put(w.getUID(), index);
        }
        return index;
    }

    public void registerLocation(Location loc, BaseSTBBlock stb, boolean isPlacing) {
        BaseSTBBlock stb2 = get(loc);
        if (stb2 != null) {
            LogUtils.warning("Attempt to register duplicate STB block " + stb + " @ " + loc + " - existing block " + stb2);
            return;
        }

        stb.setLocation(blockAccess, loc);

        String locStr = MiscUtil.formatLocation(loc);
        getWorldIndex(loc.getWorld()).put(locStr, stb);
        stb.preRegister(blockAccess, loc, isPlacing);

        if (isPlacing) {
            addPendingDBOperation(loc, locStr, UpdateRecord.Operation.INSERT);
        }

        if (stb.getTickRate() > 0) {
            addTicker(stb);
        }

        Debugger.getInstance().debug("Registered " + stb + " @ " + loc);
    }

    public void updateLocation(Location loc) {
        addPendingDBOperation(loc, MiscUtil.formatLocation(loc), UpdateRecord.Operation.UPDATE);
    }

    public void unregisterLocation(Location loc, BaseSTBBlock stb) {
        if (stb != null) {
            stb.onBlockUnregistered(loc);
            String locStr = MiscUtil.formatLocation(loc);
            addPendingDBOperation(loc, locStr, UpdateRecord.Operation.DELETE);
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

        // TODO: translate multi-block structures

        String locStr = MiscUtil.formatLocation(oldLoc);
        addPendingDBOperation(oldLoc, locStr, UpdateRecord.Operation.DELETE);
        getWorldIndex(oldLoc.getWorld()).remove(locStr);

        stb.moveTo(blockAccess, oldLoc, newLoc);

        locStr = MiscUtil.formatLocation(newLoc);
        addPendingDBOperation(newLoc, locStr, UpdateRecord.Operation.INSERT);
        getWorldIndex(newLoc.getWorld()).put(locStr, stb);

        Debugger.getInstance().debug("moved " + stb + " from " + oldLoc + " to " + newLoc);
    }

    private void addPendingDBOperation(Location loc, String locStr, UpdateRecord.Operation op) {
        UpdateRecord existingRec = pendingUpdates.get(locStr);
        switch (op) {
            case INSERT:
                if (existingRec == null) {
                    // brand new insertion
                    pendingUpdates.put(locStr, new UpdateRecord(UpdateRecord.Operation.INSERT, loc));
                } else if (existingRec.getOp() == UpdateRecord.Operation.DELETE) {
                    // re-inserting where a block was just deleted
                    pendingUpdates.put(locStr, new UpdateRecord(UpdateRecord.Operation.UPDATE, loc));
                }
                break;
            case UPDATE:
                if (existingRec == null || existingRec.getOp() != UpdateRecord.Operation.INSERT) {
                    pendingUpdates.put(locStr, new UpdateRecord(UpdateRecord.Operation.UPDATE, loc));
                }
                break;
            case DELETE:
                if (existingRec != null && existingRec.getOp() == UpdateRecord.Operation.INSERT) {
                    // remove a recent insertion
                    pendingUpdates.remove(locStr);
                } else {
                    pendingUpdates.put(locStr, new UpdateRecord(UpdateRecord.Operation.DELETE, loc));
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
     * @return the STB block at the given location, or null if no matching item
     */
    public BaseSTBBlock get(Location loc) {
        return get(loc, false);
    }

    /**
     * Get the STB block at the given location, or if the location contains a
     * sign, possibly at the location the sign is attached to.
     *
     * @param loc the location to check at
     * @param checkSigns if true, and the location contains a sign, check at
     *                   the location that the sign is attached to
     * @return the STB block at the given location, or null if no matching item
     */
    public BaseSTBBlock get(Location loc, boolean checkSigns) {
        Block b = loc.getBlock();
        if (checkSigns && (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST)) {
            Sign sign = (Sign) b.getState().getData();
            b = b.getRelative(sign.getAttachedFace());
        }
        BaseSTBBlock stb = (BaseSTBBlock) STBUtil.getMetadataValue(b, BaseSTBBlock.STB_BLOCK);
        if (stb != null) {
            return stb;
        } else {
            // perhaps it's part of a multi-block structure
            return (BaseSTBBlock) STBUtil.getMetadataValue(b, BaseSTBBlock.STB_MULTI_BLOCK);
        }
    }

    /**
     * Get the STB block of the given type at the given location.
     *
     * @param loc  the location to check at
     * @param type the type of STB block required
     * @param <T>  a subclass of BaseSTBBlock
     * @return the STB block at the given location, or null if no matching item
     */
    public <T extends BaseSTBBlock> T get(Location loc, Class<T> type) {
        return get(loc, type, false);
    }

    /**
     * Get the STB block of the given type at the given location.
     *
     * @param loc  the location to check at
     * @param type the type of STB block required
     * @param <T>  a subclass of BaseSTBBlock
     * @param checkSigns if true, and the location contains a sign, check at
     *                   the location that the sign is attached to
     * @return the STB block at the given location, or null if no matching item
     */
    public <T extends BaseSTBBlock> T get(Location loc, Class<T> type, boolean checkSigns) {
        BaseSTBBlock stbBlock = get(loc, checkSigns);
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
    public List<BaseSTBBlock> get(Chunk chunk) {
        List<BaseSTBBlock> res = new ArrayList<BaseSTBBlock>();
        for (BaseSTBBlock stb : listBlocks(chunk.getWorld(), false)) {
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
            Set<BaseSTBBlock> tickerSet = allTickers.get(w.getUID());
            if (tickerSet != null) {
                Iterator<BaseSTBBlock> iter = tickerSet.iterator();
                while (iter.hasNext()) {
                    BaseSTBBlock stb = iter.next();
                    if (stb.isPendingRemoval()) {
                        Debugger.getInstance().debug("Removing block " + stb + " from tickers list");
                        iter.remove();
                    } else {
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
            // TODO: may want to do this over a few ticks to reduce the risk of lag spikes
            for (UpdateRecord rec : pendingUpdates.values()) {
                BaseSTBBlock stb = get(rec.getLocation());
                if (stb != null) {
                    rec.setType(stb.getItemTypeID());
                    rec.setData(stb.freeze().saveToString());
                } else {
                    Validate.isTrue(rec.getOp() == UpdateRecord.Operation.DELETE, "Found null STB block @ " + rec.getLocation() + " with op = " + rec.getOp());
                }
                updateQueue.add(rec);
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
                BaseSTBItem stbItem = SensibleToolbox.getItemRegistry().getItemById(type, conf);
                if (stbItem != null) {
                    Location loc = new Location(world, x, y, z);
                    if (stbItem instanceof BaseSTBBlock) {
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

        Map<String, BaseSTBBlock> map = blockIndex.get(world.getUID());
        if (map != null) {
            map.clear();
            blockIndex.remove(world.getUID());
        }
    }

    /**
     * The given world has just become loaded.
     *
     * @param world the world that has been loaded
     */
    public void worldLoaded(World world) {
        if (!blockIndex.containsKey(world.getUID())) {
            try {
                loadFromDatabase(world, null);
            } catch (SQLException e) {
                e.printStackTrace();
                LogUtils.severe("can't load STB data for world " + world.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Get a list of all STB blocks for the given world.
     *
     * @param world  the world to query
     * @param sorted if true, the array is sorted by block type
     * @return an array of STB block objects
     */
    public List<BaseSTBBlock> listBlocks(World world, boolean sorted) {
        Collection<BaseSTBBlock> list = getWorldIndex(world).values();
        return sorted ? MiscUtil.asSortedList(list) : Lists.newArrayList(list);
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

    public static class BlockAccess {
        // this is a little naughty, but it lets us call public methods
        // in BaseSTBBlock which we don't want everyone else to call
        private BlockAccess() { }
    }
}
