package io.github.thebusybiscuit.sensibletoolbox.api.enderstorage;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.sensibletoolbox.api.STBInventoryHolder;

import java.util.UUID;

/**
 * Represents an STB ender inventory holder.
 * 
 * @author desht
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
     * This method should be called if you modify an ender inventory via
     * the Bukkit inventory API to ensure any changes get persisted. It is
     * automatically called when items are inserted/extracted via direct
     * player manipulation or via inventory manipulation methods in
     * {@link io.github.thebusybiscuit.sensibletoolbox.api.STBInventoryHolder}.
     */
    void setChanged();
}
