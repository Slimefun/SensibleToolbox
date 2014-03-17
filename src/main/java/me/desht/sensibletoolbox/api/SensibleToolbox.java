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
	 * Register a new item with SensibleToolbox.  The item must be a subclass of BaseSTBItem, and
	 * the registration must be done from your plugin's onEnable().
	 *
	 * @param item the new item to register
	 */
	public static void registerItem(BaseSTBItem item, Plugin plugin) {
		BaseSTBItem.registerItem(item, plugin);
	}

	/**
	 * Given a UUID, attempt to get the player name for that UUID.  This will only succeed if that
	 * player has previously connected to this server, and the last known name for the UUID will be
	 * returned.  If the player has changed their name since last logging in, this change will not
	 * be reflected in the return value for this method.
	 *
	 * @param uuid the UUID to check
	 * @return the last known player name for this UUID, if any (may be null if name not known)
	 */
	public static String getPlayerNameFromUUID(UUID uuid) {
		return SensibleToolboxPlugin.getInstance().getUuidTracker().getPlayer(uuid);
	}
}
