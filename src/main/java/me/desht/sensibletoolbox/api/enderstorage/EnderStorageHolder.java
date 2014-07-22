package me.desht.sensibletoolbox.api.enderstorage;

import me.desht.sensibletoolbox.api.STBInventoryHolder;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents an STB ender inventory holder.
 */
public interface EnderStorageHolder extends STBInventoryHolder {
    /**
     * Get the ender frequency of this ender holder.
     *
     * @return the ender frequency
     */
    int getFrequency();

    /**
     * Check if this ender holder is global or personal.
     *
     * @return true if the holder is global; false if it is personal
     */
    boolean isGlobal();

    Inventory getInventory();

    int insertItems(ItemStack item, BlockFace face, boolean sorting, UUID uuid);

    ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid);

    Inventory showOutputItems(UUID uuid);

    void updateOutputItems(UUID uuid, Inventory inventory);

    /**
     * Mark this ender holder as having been changed and in need of saving.
     * This method should be called after you modify an ender inventory via
     * Bukkit inventory API to ensure any changes get persisted.
     */
    void setChanged();
}
