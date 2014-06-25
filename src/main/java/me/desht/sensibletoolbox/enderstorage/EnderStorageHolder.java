package me.desht.sensibletoolbox.enderstorage;

import com.google.common.io.Files;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

public abstract class EnderStorageHolder implements InventoryHolder {
    private final int frequency;
    private final EnderStorageManager manager;
    private Inventory inventory;

    protected EnderStorageHolder(EnderStorageManager manager, int frequency) {
        this.frequency = frequency;
        this.manager = manager;
    }

    void loadInventory() throws IOException {
        File saveFile = getSaveFile();
        if (saveFile.exists()) {
            String encoded = new Scanner(saveFile).useDelimiter("\\A").next();
            Inventory savedInv = BukkitSerialization.fromBase64(encoded);
            inventory = Bukkit.createInventory(this, savedInv.getSize(), getInventoryTitle());
            for (int i = 0; i < savedInv.getSize(); i++) {
                inventory.setItem(i, savedInv.getItem(i));
            }
            Debugger.getInstance().debug("loaded " + this + " from " + saveFile);
        } else {
            // no saved inventory -  player must not have used the bag before
            inventory = Bukkit.createInventory(this, EnderStorageManager.BAG_SIZE, getInventoryTitle());
            saveInventory();
        }
    }

    void saveInventory() {
        final String encoded = BukkitSerialization.toBase64(getInventory());
        final File saveFile = getSaveFile();
        Bukkit.getScheduler().runTaskAsynchronously(SensibleToolboxPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    Files.write(encoded, saveFile, Charset.forName("UTF-8"));
                    Debugger.getInstance().debug("saved " + this + " to " + saveFile);
                } catch (IOException e) {
                    LogUtils.severe("Can't save ender storage " + this);
                }
            }
        });
    }

    public int getFrequency() {
        return frequency;
    }

    public EnderStorageManager getManager() {
        return manager;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public abstract File getSaveFile();
    public abstract String getInventoryTitle();
}
