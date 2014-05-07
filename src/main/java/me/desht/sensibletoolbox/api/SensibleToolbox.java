package me.desht.sensibletoolbox.api;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Top-level collection of utility methods for Sensible Toolbox.
 */
public class SensibleToolbox {
	/**
	 * Given an item stack, get the SensibleToolbox object for that item, if any.
	 *
	 * @param stack the item stack
	 * @return the SensibleToolbox object
	 */
	public static STBItem getItemFromItemStack(ItemStack stack) {
		return BaseSTBItem.getItemFromItemStack(stack);
	}

	/**
	 * Given a UUID, attempt to get the player name for that UUID.  This will only succeed if that
	 * player has previously connected to this server, and the last known name for the UUID will be
	 * returned.  If the player has changed their name since last logging in, this change will not
	 * be reflected in the return value for this method.
	 *
	 * @param uuid the UUID to check
	 * @return the last known player name for this UUID, if any (null if name not known)
	 */
	public static String getPlayerNameFromUUID(UUID uuid) {
		return SensibleToolboxPlugin.getInstance().getUuidTracker().getPlayer(uuid);
	}

    /**
     * Register a new item with SensibleToolbox. The item must be a subclass of {@link BaseSTBItem}.
     * <p>
     * Permission nodes will be registered for the item: <i>stb.{interact}.{itemId}</i> will
     * always be registered, and if the item is a subclass of BaseSTBBlock, then
     * <i>stb.{place|break|interact_block}.{itemId}</i> will also be registered.
     * <p>
     * {@code itemId} is the item's ID; the base class name of the item, lowercased.
     *
     * @param plugin the plugin doing the registration
     * @param item an instance of the item to be registered
     */
    public static void registerItem(Plugin plugin, BaseSTBItem item) {
        BaseSTBItem.registerItem(item, plugin);
    }

    /**
     * Register an item with Sensible Toolbox.  The item must be a subclass of {@link BaseSTBItem}.
     * <p>
     * If the supplied {@code configNode} is non-null, Sensible Toolbox will check the
     * boolean configuration node "{configNode}.{itemId}", and only register the
     * item if the node is true.
     * <p>
     * Permission nodes will be registered for the item: <i>stb.{interact}.{itemId}</i> will
     * always be registered, and if the item is a subclass of BaseSTBBlock, then
     * <i>stb.{place|break|interact_block}.{itemId}</i> will also be registered.
     * <p>
     * {@code itemId} is the item's ID; the base class name of the item, lowercased.
     *
     * @param plugin the plugin doing the registration
     * @param item an instance of the item to be registered
     * @param configNode the parent configuration node prefix controlling enablement
     */
    public static void registerItem(Plugin plugin, BaseSTBItem item, String configNode) {
        BaseSTBItem.registerItem(item, plugin, configNode);
    }

    /**
     * Register an item with Sensible Toolbox.  The item must be a subclass of {@link BaseSTBItem}.
     * <p>
     * If the supplied {@code configNode} is non-null, Sensible Toolbox will check the
     * boolean configuration node "{configNode}.{itemId}", and only register the
     * item if the node is true.
     * <p>
     * Permission nodes will be registered for the item: <i>{permissionNode}.{interact}.{itemId}</i>
     * will always be registered, and if the item is a subclass of BaseSTBBlock, then
     * <i>{permissionNode}.{place|break|interact_block}.{itemId}</i> will also be registered.
     * <p>
     * {@code itemId} is the item's ID; the base class name of the item, lowercased.
     *
     * @param plugin the plugin doing the registration
     * @param item an instance of the item to be registered
     * @param configNode the parent configuration node prefix controlling enablement
     * @param permissionNode the permission node prefix for registering item permissions
     */
    public static void registerItem(Plugin plugin, BaseSTBItem item, String configNode, String permissionNode) {
        BaseSTBItem.registerItem(item, plugin, configNode, permissionNode);
    }
}
