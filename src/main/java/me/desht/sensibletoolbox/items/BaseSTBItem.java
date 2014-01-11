package me.desht.sensibletoolbox.items;

import me.desht.dhutils.ItemGlow;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.attributes.AttributeStorage;
import me.desht.sensibletoolbox.blocks.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.util.*;

public abstract class BaseSTBItem implements ConfigurationSerializable {
	public static final String STB_MULTI_BLOCK = "STB_MultiBlock_Origin";
	protected static final ChatColor LORE_COLOR = ChatColor.GRAY;
	private static final String STB_LORE_LINE = ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Sensible Toolbox item";
	private static final ChatColor DISPLAY_COLOR = ChatColor.YELLOW;
	private static final Map<String, Class<? extends BaseSTBItem>> registry = new HashMap<String, Class<? extends BaseSTBItem>>();
	private static final Map<String, String> ids = new HashMap<String, String>();

	protected BaseSTBItem() {
	}

	public static void registerItems() {
		registerItem(new AngelicBlock());
		registerItem(new EnderLeash());
		registerItem(new RedstoneClock());
		registerItem(new BlockUpdateDetector());
		registerItem(new BagOfHolding());
		registerItem(new WateringCan());
		registerItem(new MoistureChecker());
		registerItem(new AdvancedMoistureChecker());
		registerItem(new WoodCombineHoe());
		registerItem(new IronCombineHoe());
		registerItem(new GoldCombineHoe());
		registerItem(new DiamondCombineHoe());
		registerItem(new TrashCan());
		registerItem(new PaintBrush());
		registerItem(new PaintRoller());
		registerItem(new PaintCan());
	}

	private static void registerItem(BaseSTBItem item) {
		registry.put(item.getItemName(), item.getClass());
		String id = item.getItemName().replace(" ", "").toLowerCase();
		ids.put(id, item.getItemName());
	}

	public static void setupRecipes() {
		for (String key : registry.keySet()) {
			Recipe r = getItemByName(key).getRecipe();
			if (r != null) {
				Bukkit.addRecipe(r);
			}
		}
	}

	public static Set<String> getItemIds() {
		return ids.keySet();
	}

	public static BaseSTBItem getItemFromItemStack(ItemStack stack) {
		if (!isSTBItem(stack)) {
			return null;
		}
		String disp = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
		Configuration conf = BaseSTBItem.getItemAttributes(stack);
		return getItemByName((disp.split(":"))[0], conf);
	}

	public static BaseSTBItem getItemById(String id) {
		return ids.containsKey(id) ? BaseSTBItem.getItemByName(ids.get(id)) : null;
	}

	public static BaseSTBItem getItemByName(String disp) {
		return getItemByName(disp, null);
	}

	public static BaseSTBItem getItemByName(String disp, Configuration conf) {
		Class<? extends BaseSTBItem> c = registry.get(disp);
		if (c == null) {
			return null;
		}
		try {
			if (conf == null) {
				Constructor<? extends BaseSTBItem> cons = c.getDeclaredConstructor();
				return cons.newInstance();
			} else {
				Constructor<? extends BaseSTBItem> cons = c.getDeclaredConstructor(Configuration.class);
				return cons.newInstance(conf);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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

	protected static Configuration getItemAttributes(ItemStack stack) {
		AttributeStorage storage = AttributeStorage.newTarget(stack, SensibleToolboxPlugin.UNIQUE_ID);
		YamlConfiguration conf = new YamlConfiguration();
		try {
			String s = storage.getData("");
			if (s != null) {
				conf.loadFromString(s);
				System.out.println("get item attributes for " + stack + ":");
				for (String k : conf.getKeys(false)) { System.out.println(" " + k + " = " + conf.get(k)); }
				return conf;
			} else {
				return null;
			}
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return new MemoryConfiguration();
		}
	}

	/**
	 * Get the Bukkit Material used for this item.
	 *
	 * @return the Bukkit Material
	 */
	public abstract Material getBaseMaterial();

	/**
	 * Get the block data byte used for this item.
	 *
	 * @return the block data
	 */
	public Byte getBaseBlockData() { return null; }

	/**
	 * Get the item's displayed name.
	 *
	 * @return the item name
	 */
	public abstract String getItemName();

	/**
	 * Get any suffix to be appended to the item's displayed name.  Override this in
	 * implementing classes where you wish to represent some or all of the item's state
	 * in the display name.
	 *
	 * @return the display suffix
	 */
	public String getDisplaySuffix() { return null; }

	/**
	 * Get the base lore to display for the item.
	 *
	 * @return the item lore
	 */
	public abstract String[] getLore();

	/**
	 * Get extra lore to be appended to the base lore.  verride this in
	 * implementing classes where you wish to represent some or all of the item's state
	 * in the item lore.
	 *
	 * @return the extra item lore
	 */
	public String[] getExtraLore() { return new String[0]; }

	/**
	 * Get the recipe used to create the item.
	 *
	 * @return the recipe
	 */
	public abstract Recipe getRecipe();

	/**
	 * Check if the item should glow.  This will only work if ProtocolLib is installed.
	 *
	 * @return true if the item should glow
	 */
	public boolean hasGlow() { return false; }

	/**
	 * Called when a player interacts with a block or air while holding an STB item.
	 *
	 * @param event the interaction event.
	 */
	public void handleItemInteraction(PlayerInteractEvent event) { }

	/**
	 * Called when a player attempts to consume an STB item (which must be food or potion).
	 *
	 * @param event the consume event
	 */
	public void handleConsume(PlayerItemConsumeEvent event) { }

	/**
	 * Called when a player interacts with an entity while holding an STB item.
	 *
	 * @param event the interaction event
	 */
	public void handleEntityInteraction(PlayerInteractEntityEvent event) { }

	@Override
	public Map<String, Object> serialize() {
		return new HashMap<String, Object>();
	}

	/**
	 * Get an ItemStack from this STB item, serializing any item-specific data into the ItemStack.
	 *
	 * @param amount number of items in the stack
	 * @return the new ItemStack
	 */
	public ItemStack toItemStack(int amount) {
		ItemStack res = new ItemStack(getBaseMaterial(), amount);
		if (getBaseBlockData() != null) {
			res.setDurability(getBaseBlockData());
		}
		ItemMeta im = res.getItemMeta();
		String suffix = getDisplaySuffix() == null ? "" : ": " + getDisplaySuffix();
		im.setDisplayName(DISPLAY_COLOR + getItemName() + suffix);
		im.setLore(buildLore());
		res.setItemMeta(im);
		ItemGlow.setGlowing(res, hasGlow());

		// any serialized data from the object goes in the ItemStack attributes
		Map<String, Object> map = serialize();
		if (!map.isEmpty()) {
			YamlConfiguration conf = new YamlConfiguration();
			for (Map.Entry e : map.entrySet()) {
				conf.set((String) e.getKey(), e.getValue());
			}
			AttributeStorage storage = AttributeStorage.newTarget(res, SensibleToolboxPlugin.UNIQUE_ID);
			String data = conf.saveToString();
			storage.setData(data);
			System.out.println("serialize to itemstack:\n" + data);
			return storage.getTarget();
		} else {
			return res;
		}
	}

	private List<String> buildLore() {
		String[] lore = getLore();
		String[] lore2 = getExtraLore();
		List<String> res = new ArrayList<String>(lore.length + lore2.length + 1);
		res.add(STB_LORE_LINE);
		for (String l : lore) {
			res.add(LORE_COLOR + l);
		}
		Collections.addAll(res, lore2);
		return res;
	}

	@Override
	public String toString() {
		return "STB item: " + getItemName() + " <" + getBaseMaterial() + ">";
	}
}
