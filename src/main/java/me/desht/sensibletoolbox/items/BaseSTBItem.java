package me.desht.sensibletoolbox.items;

import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseSTBItem implements ConfigurationSerializable {
	private static final String STB_LORE_LINE = ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Sensible Toolbox item";
	private static final ChatColor DISPLAY_COLOR = ChatColor.YELLOW;
	private static final ChatColor LORE_COLOR = ChatColor.GRAY;

	private static final Map<String, Class<? extends BaseSTBItem>> registry = new HashMap<String, Class<? extends BaseSTBItem>>();

	static {
		registerItem(new AngelicBlock());
		registerItem(new EnderLeash());
		registerItem(new RedstoneClock());
	}

	public abstract Material getBaseMaterial();
	public Byte getBaseBlockData() { return null; }
	public abstract String getDisplayName();
	public abstract String[] getLore();
	public abstract Recipe getRecipe();

	// override some or all of these in subclasses
	public void handleInteraction(PlayerInteractEvent event) { }
	public void handleBlockPlace(BlockPlaceEvent event) { }
	public void handleBlockDamage(BlockDamageEvent event) { }
	public void handleBlockBreak(BlockBreakEvent event) { }
	public void handleEntityInteraction(PlayerInteractEntityEvent event) { }
	public void onServerTick(PersistableLocation pLoc) { }

	// override in subclasses if necessary
	public BaseSTBItem getItem(ItemStack stack) {
		return this;
	}

	@Override
	public Map<String, Object> serialize() {
		return new HashMap<String, Object>();
	}

	private static void registerItem(BaseSTBItem item) {
		registry.put(item.getPlainDisplayName(), item.getClass());
	}

	public static BaseSTBItem getItem(String disp) {
		Class<? extends BaseSTBItem> c = registry.get(disp);
		if (c == null) {
			return null;
		}
		try {
			Constructor<? extends BaseSTBItem> cons = c.getDeclaredConstructor();
			return cons.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BaseSTBItem getBaseItem(ItemStack stack) {
		if (!isSTBItem(stack)) {
			return null;
		}
		String disp = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
		return getItem(disp);
	}

	public ItemStack toItemStack(int amount) {
		ItemStack res = new ItemStack(getBaseMaterial(), amount);
		if (getBaseBlockData() != null) {
			res.setDurability(getBaseBlockData());
		}
		ItemMeta im = res.getItemMeta();
		im.setDisplayName(DISPLAY_COLOR + getDisplayName());
		im.setLore(buildLore());
		res.setItemMeta(im);
		return res;
	}

	public static boolean isSTBItem(ItemStack stack) {
		ItemMeta im = stack.getItemMeta();
		if (im != null && im.hasLore()) {
			List<String> lore = im.getLore();
			return !lore.isEmpty() && lore.get(0).equals(STB_LORE_LINE);
		} else {
			return false;
		}
	}

	private List<String> buildLore() {
		String[] lore = getLore();
		List<String> res = new ArrayList<String>(lore.length + 1);
		res.add(STB_LORE_LINE);
		for (String l : lore) {
			res.add(LORE_COLOR + l);
		}
		return res;
	}

	protected void blockPlaced(Block b, BaseSTBItem stbItem) {
		SensibleToolboxPlugin.getInstance().getLocationManager().registerLocation(b.getLocation(), stbItem);
	}

	protected void blockRemoved(Block b, BaseSTBItem stbItem) {
		SensibleToolboxPlugin.getInstance().getLocationManager().unregisterLocation(b.getLocation(), stbItem);
	}

	public String getPlainDisplayName() {
		return ChatColor.stripColor(getDisplayName());
	}

	@Override
	public String toString() {
		return "STB item: " + getDisplayName() + " <" + getBaseMaterial() + ">";
	}

}
