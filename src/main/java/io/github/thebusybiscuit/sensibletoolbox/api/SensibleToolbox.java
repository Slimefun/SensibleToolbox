package io.github.thebusybiscuit.sensibletoolbox.api;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.cscorelib2.protection.ProtectionManager;
import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.EnergyNet;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;

/**
 * Top-level collection of utility methods for Sensible Toolbox.
 */
public class SensibleToolbox {
	
    private static SensibleToolboxPlugin instance;

    /**
     * Get an instance of the running Sensible Toolbox plugin.
     *
     * @return the SensibleToolboxPlugin instance
     */
    public static SensibleToolboxPlugin getPluginInstance() {
        if (instance == null) {
            instance = (SensibleToolboxPlugin) Bukkit.getPluginManager().getPlugin("SensibleToolbox");
            
            if (instance == null || !instance.isEnabled()) {
                throw new IllegalStateException("SensibleToolbox plugin is not available!");
            }
        }
        
        return instance;
    }

    /**
     * Get the item registry instance, which handles all item registration,
     * retrieval and inspection.
     *
     * @return the STB item registry
     */
    public static ItemRegistry getItemRegistry() {
        return getPluginInstance().getItemRegistry();
    }

    /**
     * Given an item stack, get the SensibleToolbox object for that item, if
     * any.
     *
     * @param stack the item stack
     * @return the SensibleToolbox object, or null if the item is not a STB item
     * @deprecated use {@link ItemRegistry#fromItemStack(org.bukkit.inventory.ItemStack)}
     */
    @Deprecated
    public static BaseSTBItem getItemFromItemStack(ItemStack stack) {
        return getItemRegistry().fromItemStack(stack);
    }

    /**
     * Given an item stack and item type, get the SensibleToolbox object for
     * that item and type, if any.
     *
     * @param stack the item stack
     * @return the SensibleToolbox object, or null if the item is not a STB item of the given type
     * @deprecated use {@link ItemRegistry#fromItemStack(org.bukkit.inventory.ItemStack, Class)}
     */
    @Deprecated
    public static <T extends BaseSTBItem> T getItemfromItemStack(ItemStack stack, Class<T> type) {
        return getItemRegistry().fromItemStack(stack, type);
    }

    /**
     * Given a location, return the STB block at that location, if any.
     *
     * @param location the location to check
     * @return the STB block at that location, or null if there is none
     */
    public static BaseSTBBlock getBlockAt(Location location) {
        return LocationManager.getManager().get(location);
    }

    /**
     * Given a location, return the STB block at that location, if any.
     *
     * @param location the location to check
     * @param checkSign if true and the location contains a sign, then also
     *                  check the location of the block the sign is attached
     *                  to
     * @return the STB block at that location, or null if there is none
     */
    public static BaseSTBBlock getBlockAt(Location location, boolean checkSign) {
        return LocationManager.getManager().get(location, checkSign);
    }

    /**
     * Given a location, return the STB block at that location, if any.
     *
     * @param location the location to check
     * @param type the block must be an instance or a subclass of this type
     * @param checkSign if true and the location contains a sign, then also
     *                  check the location of the block the sign is attached
     *                  to
     * @return the STB block at that location, or null if there is no block of the given type
     */
    public static <T extends BaseSTBBlock> T getBlockAt(Location location, Class<T> type, boolean checkSign) {
        return LocationManager.getManager().get(location, type, checkSign);
    }

    /**
     * Given a UUID, attempt to get the player name for that UUID.  This will
     * only succeed if that player has previously connected to this server,
     * in which case the last known name for the UUID will be returned.  If
     * the player has changed their name since last logging in, this change
     * will not be reflected in the return value for this method.
     * <p/>
     * This method does not require contacting any external servers, so is
     * safe to call from the main server thread.
     *
     * @param uuid the UUID to check
     * @return the last known player name for this UUID, if any (null if name not known)
     */
    public static String getPlayerNameFromUUID(UUID uuid) {
        return getPluginInstance().getUuidTracker().getPlayerName(uuid);
    }

    /**
     * Register a new item with SensibleToolbox. The item must be a subclass
     * of {@link BaseSTBItem}.
     *
     * @param plugin the plugin doing the registration
     * @param item   an instance of the item to be registered
     * @deprecated use {@link ItemRegistry#registerItem(io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem, org.bukkit.plugin.Plugin)}
     */
    @Deprecated
    public static void registerItem(Plugin plugin, BaseSTBItem item) {
        getItemRegistry().registerItem(item, plugin);
    }

    /**
     * Register an item with Sensible Toolbox.  The item must be a subclass of
     * {@link BaseSTBItem}.
     *
     * @param plugin     the plugin doing the registration
     * @param item       an instance of the item to be registered
     * @param configNode the parent configuration node prefix controlling enablement
     * @deprecated use {@link ItemRegistry#registerItem(io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem, org.bukkit.plugin.Plugin, String)}
     */
    @Deprecated
    public static void registerItem(Plugin plugin, BaseSTBItem item, String configNode) {
        getItemRegistry().registerItem(item, plugin, configNode);
    }

    /**
     * Register an item with Sensible Toolbox.  The item must be a subclass of
     * {@link BaseSTBItem}.
     *
     * @param plugin         the plugin doing the registration
     * @param item           an instance of the item to be registered
     * @param configNode     the parent configuration node prefix controlling enablement
     * @param permissionNode the permission node prefix for registering item permissions
     * @deprecated use {@link ItemRegistry#registerItem(io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem, org.bukkit.plugin.Plugin, String, String)}
     */
    @Deprecated
    public static void registerItem(Plugin plugin, BaseSTBItem item, String configNode, String permissionNode) {
        getItemRegistry().registerItem(item, plugin, configNode, permissionNode);
    }

    /**
     * Get the friend manager object.  This object is responsible for managing
     * the trust relationships between players, primarily to support
     * Restricted access mode on STB blocks.
     *
     * @return the friend manager
     */
    public static FriendManager getFriendManager() {
        return getPluginInstance().getFriendManager();
    }

    /**
     * Get the energy net for the given block.
     *
     * @param block the block to check
     */
    public static EnergyNet getEnergyNet(Block block) {
        return getPluginInstance().getEnergyNetManager().getEnergyNet(block);
    }

	public static ProtectionManager getProtectionManager() {
		return getPluginInstance().getProtectionManager();
	}
}
