package me.desht.sensibletoolbox.enderstorage;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.desht.dhutils.DHValidate;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.EnderBag;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EnderStorageManager implements Listener {
    public static final int MAX_ENDER_FREQUENCY = 1000;
    public static final int BAG_SIZE = 54;
    private static final String ENDER_STORAGE_DIR = "enderstorage";
    private final File storageDir;

    private final Map<Integer,GlobalHolder> globalInvs = Maps.newHashMap();
    private final Map<UUID, Map<Integer, PlayerHolder>> playerInvs = Maps.newHashMap();
    private final Set<EnderStorageHolder> updateNeeded = Sets.newHashSet();

    private static final FilenameFilter uuidFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return MiscUtil.looksLikeUUID(name);
        }
    };

    public EnderStorageManager(SensibleToolboxPlugin plugin) {
        storageDir = new File(plugin.getDataFolder(), ENDER_STORAGE_DIR);

        if (!storageDir.exists()) {
            setupStorageStructure(plugin, storageDir);
        }
    }

    public File getStorageDir() {
        return storageDir;
    }

    public GlobalHolder getGlobalInventoryHolder(int frequency) {
        GlobalHolder h = globalInvs.get(frequency);
        if (h == null) {
            h = new GlobalHolder(this, frequency);
            try {
                h.loadInventory();
                globalInvs.put(frequency, h);
            } catch (IOException e) {
                LogUtils.severe("Can't load global ender storage: " + h.getSaveFile());
                return null;
            }
        }
        return h;
    }

    public PlayerHolder getPlayerInventoryHolder(OfflinePlayer player, Integer frequency) {
        Map<Integer, PlayerHolder> map = playerInvs.get(player.getUniqueId());
        if (map == null) {
            map = new HashMap<Integer, PlayerHolder>();
            playerInvs.put(player.getUniqueId(), map);
        }
        PlayerHolder h = map.get(frequency);
        if (h == null) {
            h = new PlayerHolder(this, player, frequency);
            try {
                h.loadInventory();
                map.put(frequency, h);
            } catch (IOException e) {
                LogUtils.severe("Can't load player ender storage: " + h.getSaveFile());
                return null;
            }
        }
        return h;
    }

    public Inventory getGlobalInventory(int frequency) {
        return getGlobalInventoryHolder(frequency).getInventory();
    }

    public Inventory getPlayerInventory(OfflinePlayer player, int frequency) {
        return getPlayerInventoryHolder(player, frequency).getInventory();
    }

    private void setupStorageStructure(SensibleToolboxPlugin plugin, File storageDir) {
        mkdir(storageDir);
        File globalDir = new File(storageDir, "global");
        mkdir(globalDir);

        // migrate any old data in the bagofholding folder to personal ender channel "1"
        File oldDir = new File(plugin.getDataFolder(), EnderBag.BAG_SAVE_DIR);
        if (oldDir.exists()) {
            for (File f : oldDir.listFiles(uuidFilter)) {
                File newDir = new File(storageDir, f.getName());
                mkdir(newDir);
                File newFile = new File(newDir, "1");
                DHValidate.isTrue(f.renameTo(newFile), "can't move " + f + " to " + newFile);
            }
            for (File f : oldDir.listFiles()) {
                if (!f.delete()) {
                    LogUtils.warning("can't delete unwanted file: " + f);
                }
            }
            if (!oldDir.delete()) {
                LogUtils.warning("can't delete old bagofholding directory");
            }
        }
    }

    void mkdir(File dir) {
        DHValidate.isTrue(dir.mkdir(), "can't create directory: " + dir);
    }

    public void setChanged(EnderStorageHolder holder) {
        updateNeeded.add(holder);
    }

    public void tick() {
        if (!updateNeeded.isEmpty()) {
            for (EnderStorageHolder holder : updateNeeded) {
                holder.saveInventory();
            }
            updateNeeded.clear();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof EnderStorageHolder) {
            EnderStorageHolder h = (EnderStorageHolder) event.getInventory().getHolder();
            setChanged(h);
        }
    }
}
