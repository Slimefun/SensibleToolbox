package io.github.thebusybiscuit.sensibletoolbox.core.enderstorage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.enderstorage.EnderStorageHolder;
import io.github.thebusybiscuit.sensibletoolbox.items.EnderBag;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.text.LogUtils;

public class EnderStorageManager implements Listener {

    public static final int MAX_ENDER_FREQUENCY = 1000;
    public static final int BAG_SIZE = 54;
    private static final String ENDER_STORAGE_DIR = "enderstorage";
    private final File storageDir;

    private final Map<Integer, GlobalEnderHolder> globalInvs = new HashMap<>();
    private final Map<UUID, Map<Integer, PlayerEnderHolder>> playerInvs = new HashMap<>();
    private final Set<EnderStorageHolder> updateNeeded = new HashSet<>();

    private static final FilenameFilter uuidFilter = (dir, name) -> MiscUtil.looksLikeUUID(name);

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
            map = new HashMap<>();
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
                Validate.isTrue(f.renameTo(newFile), "can't move " + f + " to " + newFile);
            }

            for (File f : oldDir.listFiles()) {
                try {
                    Files.delete(f.toPath());
                } catch (IOException e) {
                    LogUtils.warning("can't delete unwanted file: " + f, e);
                }
            }

            try {
                Files.delete(oldDir.toPath());
            } catch (IOException e) {
                LogUtils.warning("can't delete old bagofholding directory", e);
            }
        }
    }

    void mkdir(File dir) {
        Validate.isTrue(dir.mkdir(), "can't create directory: " + dir);
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
