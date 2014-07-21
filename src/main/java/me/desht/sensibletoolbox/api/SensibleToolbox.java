package me.desht.sensibletoolbox.api;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.core.ItemRegistry;
import me.desht.sensibletoolbox.core.STBItemRegistry;
import me.desht.sensibletoolbox.core.storage.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Top-level collection of utility methods for Sensible Toolbox.
 */
public class SensibleToolbox {
    private static SensibleToolboxPlugin instance;

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
     * Get the item registry instance, which handles all item registration
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
     * @deprecated use {@link me.desht.sensibletoolbox.core.ItemRegistry#fromItemStack(org.bukkit.inventory.ItemStack)}
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
     * @deprecated use {@link me.desht.sensibletoolbox.core.ItemRegistry#fromItemStack(org.bukkit.inventory.ItemStack, Class)}
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
     * Given a UUID, attempt to get the player name for that UUID.  This will
     * only succeed if that player has previously connected to this server,
     * in which case the last known name for the UUID will be returned.  If
     * the player has changed their name since last logging in, this change
     * will not be reflected in the return value for this method.
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
     * @deprecated use {@link me.desht.sensibletoolbox.core.ItemRegistry#registerItem(me.desht.sensibletoolbox.api.items.BaseSTBItem, org.bukkit.plugin.Plugin)}
     */
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
     * @deprecated use {@link me.desht.sensibletoolbox.core.ItemRegistry#registerItem(me.desht.sensibletoolbox.api.items.BaseSTBItem, org.bukkit.plugin.Plugin, String)}
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
     * @deprecated use {@link me.desht.sensibletoolbox.core.ItemRegistry#registerItem(me.desht.sensibletoolbox.api.items.BaseSTBItem, org.bukkit.plugin.Plugin, String, String)}
     */
    @Deprecated
    public static void registerItem(Plugin plugin, BaseSTBItem item, String configNode, String permissionNode) {
        getItemRegistry().registerItem(item, plugin, configNode, permissionNode);
    }

    /**
     * Check if the player with ID id2 is a friend of player with ID id1, i.e.
     * that id1 has added id2 as a friend.  Note that this relationship is
     * not commutative; just because id2 is a friend of id1 does not mean that
     * id1 is a friend of id2.
     *
     * @param id1 the first player's UUID
     * @param id2 the second player's UUID
     * @return true if id2 is a friend of id1; false otherwise
     */
    public static boolean isFriend(UUID id1, UUID id2) {
        return getPluginInstance().getFriendManager().isFriend(id1, id2);
    }
}
