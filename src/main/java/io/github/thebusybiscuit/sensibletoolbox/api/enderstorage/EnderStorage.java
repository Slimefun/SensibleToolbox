package io.github.thebusybiscuit.sensibletoolbox.api.enderstorage;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;

/**
 * Top-level utility methods for interacting with ender inventories.
 * <p/>
 * Note that the STB inventory system is orthogonal to the vanilla Minecraft
 * ender storage; the inventory retrieved by the Bukkit
 * {@link org.bukkit.entity.Player#getEnderChest()} method is not connected
 * to any custom STB ender inventory.
 */
public class EnderStorage {
	
    public static final int MAX_ENDER_FREQUENCY = 1000;

    /**
     * Get the personal ender inventory for the given player and frequency.
     * Note that this inventory is <em>not</em> related to the inventory
     * returned by the Bukkit
     * {@link org.bukkit.entity.Player#getEnderChest()} method.
     *
     * @param player the player
     * @param frequency the ender frequency to use
     * @return an ender inventory
     */
    public static Inventory getEnderInventory(OfflinePlayer player, int frequency) {
        return getEnderStorageHolder(player, frequency).getInventory();
    }

    /**
     * Get the global ender inventory for the given frequency.  Note that this
     * inventory is <em>not</em> related to the inventory returned by the
     * Bukkit {@link org.bukkit.entity.Player#getEnderChest()} method.
     *
     * @param frequency the ender frequency to use
     * @return an ender inventory
     */
    public static Inventory getEnderInventory(int frequency) {
        return getEnderStorageHolder(frequency).getInventory();
    }

    /**
     * Get the personal ender inventory holder for the given player and
     * frequency.
     *
     * @param player the player
     * @param frequency the ender frequency to use
     * @return a personal ender inventory holder
     */
    public static EnderStorageHolder getEnderStorageHolder(OfflinePlayer player, int frequency) {
        return SensibleToolbox.getPluginInstance().getEnderStorageManager().getPlayerInventoryHolder(player, frequency);
    }

    /**
     * Get the global ender inventory holder for the given frequency.
     *
     * @param frequency the ender frequency to use
     * @return a global ender inventory holder
     */
    public static EnderStorageHolder getEnderStorageHolder(int frequency) {
        return SensibleToolbox.getPluginInstance().getEnderStorageManager().getGlobalInventoryHolder(frequency);
    }

}
