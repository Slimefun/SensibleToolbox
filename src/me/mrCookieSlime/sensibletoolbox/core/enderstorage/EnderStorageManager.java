package me.mrCookieSlime.sensibletoolbox.core.enderstorage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.desht.sensibletoolbox.dhutils.DHValidate;
import me.desht.sensibletoolbox.dhutils.LogUtils;
import me.desht.sensibletoolbox.dhutils.MiscUtil;
import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.api.enderstorage.EnderStorageHolder;
import me.mrCookieSlime.sensibletoolbox.items.EnderBag;

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EnderStorageManager implements Listener {
	
    public static final int MAX_ENDER_FREQUENCY = 1000;
    public static final int BAG_SIZE = 54;
    private static final String ENDER_STORAGE_DIR = "enderstorage";
    private final File storageDir;

    private final Map<Integer,GlobalEnderHolder> globalInvs = Maps.newHashMap();
    private final Map<UUID, Map<Integer, PlayerEnderHolder>> playerInvs = Maps.newHashMap();
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

    File getStorageDir() {
        return storageDir;
    }

    public GlobalEnderHolder getGlobalInventoryHolder(int frequency) {
        Validate.isTrue(frequency > 0 && frequency <= MAX_ENDER_FREQUENCY, "Frequency out of range: " + frequency);
        GlobalEnderHolder h = globalInvs.get(frequency);
        if (h == null) {
            h = new GlobalEnderHolder(this, frequency);
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

    public PlayerEnderHolder getPlayerInventoryHolder(OfflinePlayer player, Integer frequency) {
        Validate.isTrue(frequency > 0 && frequency <= MAX_ENDER_FREQUENCY, "Frequency out of range: " + frequency);
        Map<Integer, PlayerEnderHolder> map = playerInvs.get(player.getUniqueId());
        if (map == null) {
            map = new HashMap<Integer, PlayerEnderHolder>();
            playerInvs.put(player.getUniqueId(), map);
        }
        PlayerEnderHolder h = map.get(frequency);
        if (h == null) {
            h = new PlayerEnderHolder(this, player, frequency);
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

    void setChanged(EnderStorageHolder holder) {
        updateNeeded.add(holder);
    }

    public void tick() {
        if (!updateNeeded.isEmpty()) {
            for (EnderStorageHolder holder : updateNeeded) {
                ((STBEnderStorageHolder) holder).saveInventory();
            }
            updateNeeded.clear();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof STBEnderStorageHolder) {
            EnderStorageHolder h = (EnderStorageHolder) event.getInventory().getHolder();
            setChanged(h);
        }
    }
}
