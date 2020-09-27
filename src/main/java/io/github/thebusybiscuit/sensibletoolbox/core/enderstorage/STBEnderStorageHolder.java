package io.github.thebusybiscuit.sensibletoolbox.core.enderstorage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.Files;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.enderstorage.EnderStorageHolder;
import io.github.thebusybiscuit.sensibletoolbox.util.BukkitSerialization;
import io.github.thebusybiscuit.sensibletoolbox.util.VanillaInventoryUtils;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;

public abstract class STBEnderStorageHolder implements EnderStorageHolder {

    private final int frequency;
    private final EnderStorageManager manager;
    private Inventory inventory;

    protected STBEnderStorageHolder(@Nonnull EnderStorageManager manager, int frequency) {
        this.frequency = frequency;
        this.manager = manager;
    }

    @SuppressWarnings("resource")
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
        }
        else {
            // no saved inventory - player must not have used the bag before
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
                    File dir = saveFile.getParentFile();
                    if (!dir.exists()) {
                        getManager().mkdir(dir);
                    }
                    Files.write(encoded, saveFile, Charset.forName("UTF-8"));
                    Debugger.getInstance().debug("saved " + this + " to " + saveFile);
                }
                catch (IOException e) {
                    LogUtils.severe("Can't save ender storage " + this + ": " + e.getMessage());
                }
            }
        });
    }

    @Override
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

    @Override
    public int insertItems(ItemStack item, BlockFace face, boolean sorting, UUID uuid) {
        int nInserted = VanillaInventoryUtils.vanillaInsertion(getInventory(), item, item.getAmount(), face, sorting);
        if (nInserted > 0) {
            setChanged();
        }
        return nInserted;
    }

    @Override
    public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid) {
        ItemStack stack = VanillaInventoryUtils.pullFromInventory(getInventory(), amount, receiver, null);
        if (stack != null) {
            setChanged();
        }
        return stack;
    }

    @Override
    public Inventory showOutputItems(UUID uuid) {
        Inventory res = Bukkit.createInventory(this, getInventory().getSize());
        res.setContents(getInventory().getContents());
        return res;
    }

    @Override
    public void updateOutputItems(UUID uuid, Inventory inventory) {
        Inventory target = getInventory();
        target.setContents(inventory.getContents());
        setChanged();
    }

    @Override
    public void setChanged() {
        getManager().setChanged(this);
    }

    public abstract File getSaveFile();

    public abstract String getInventoryTitle();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return frequency == ((STBEnderStorageHolder) o).frequency;
    }

    @Override
    public int hashCode() {
        return frequency;
    }
}
