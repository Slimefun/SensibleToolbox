package me.desht.sensibletoolbox.core;

import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public interface ItemRegistry {
    /**
     * Register the given item as a Sensible Toolbox item.
     *
     * @param item an instance of the item to be registered
     * @param plugin the plugin which is registering the item.
     */
    void registerItem(BaseSTBItem item, Plugin plugin);

    /**
     * Register the given item as a Sensible Toolbox item.
     * <p>
     * If the supplied configPrefix parameter is non-null, then the plugin's
     * configuration, key <em>configNode.[itemID]</em> will be checked for a
     * boolean value, which must return true for the item to be registered.
     * This can be used to allow server admin control over which items are
     * registered.  [itemID] is the return value of
     * {@link me.desht.sensibletoolbox.api.items.BaseSTBItem#getItemTypeID()}
     *
     * @param item an instance of the item to be registered
     * @param plugin the plugin which is registering the item.
     * @param configPrefix a configuration prefix
     */
    void registerItem(BaseSTBItem item, Plugin plugin, String configPrefix);

    /**
     * Register the given item as a Sensible Toolbox item.
     * <p>
     * If the supplied configPrefix parameter is non-null, then the plugin's
     * configuration, key <em>configNode.[itemID]</em> will be checked for a
     * boolean value, which must return true for the item to be registered.
     * This can be used to allow server admin control over which items are
     * registered.  [itemID] is the return value of
     * {@link me.desht.sensibletoolbox.api.items.BaseSTBItem#getItemTypeID()}
     * <p>
     * If the supplied permissionPrefix parameter is non-null, it is used as
     * the prefix to register the item's permission nodes, instead of the
     * default "stb".  See <a href="http://dev.bukkit.org/bukkit-plugins/sensible-toolbox/pages/permissions/"
     * this page</a> for more information on permissions.
     *
     * @param item an instance of the item to be registered
     * @param plugin the plugin which is registering the item.
     * @param configPrefix a configuration prefix
     * @param permissionPrefix a permission node prefix
     */
    void registerItem(BaseSTBItem item, Plugin plugin, String configPrefix, String permissionPrefix);

    /**
     * Get a set of all known STB item ID's.
     *
     * @return all know STB item ID's
     */
    Set<String> getItemIds();

    /**
     * Construct and return an STB item from a supplied ItemStack.
     *
     * @param stack the item stack
     * @return the STB item, or null if the item stack is not an STB item
     */
    BaseSTBItem fromItemStack(ItemStack stack);

    /**
     * Construct and return an STB item from a supplied ItemStack.  The item must be an instance of
     * the supplied class (or a subclass of the supplied class).
     *
     * @param stack the ItemStack
     * @param type  the required class
     * @param <T>   the parameterised type; a subclass of BaseSTBItem
     * @return the STB item, or null if the item stack is not an STB item of the desired class
     */
    <T extends BaseSTBItem> T fromItemStack(ItemStack stack, Class<T> type);

    /**
     * Construct and return an STB item.
     *
     * @param id the item ID
     * @return the STB item
     */
    BaseSTBItem getItemById(String id);

    /**
     * Construct and return an STB item.
     *
     * @param id   the item ID
     * @param conf item's frozen configuration data
     * @return the STB item
     */
    BaseSTBItem getItemById(String id, ConfigurationSection conf);

    /**
     * Check if the given item stack is an STB item.
     *
     * @param stack the item stack to check
     * @return true if this item stack is an STB item
     */
    boolean isSTBItem(ItemStack stack);

    /**
     * Check if the given item stack is an STB item of the given STB subclass
     *
     * @param stack the item stack to check
     * @param c     a subclass of BaseSTBItem
     * @return true if this item stack is an STB item of (or extending) the given class
     */
    boolean isSTBItem(ItemStack stack, Class<? extends BaseSTBItem> c);
}
