package me.desht.sensibletoolbox.items;

import me.desht.dhutils.ItemGlow;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.attributes.AttributeStorage;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.util.*;

public abstract class BaseSTBItem implements ConfigurationSerializable {
	private static final String STB_LORE_LINE = ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Sensible Toolbox item";
	private static final ChatColor DISPLAY_COLOR = ChatColor.YELLOW;
	protected static final ChatColor LORE_COLOR = ChatColor.GRAY;

	private static final Map<String, Class<? extends BaseSTBItem>> registry = new HashMap<String, Class<? extends BaseSTBItem>>();

	static {
		registerItem(new AngelicBlock());
		registerItem(new EnderLeash());
		registerItem(new RedstoneClock());
		registerItem(new BlockUpdateDetector());
	}

	public abstract Material getBaseMaterial();
	public Byte getBaseBlockData() { return null; }
	public abstract String getDisplayName();
	public abstract String[] getLore();
	public String[] getExtraLore() { return new String[0]; }
	public abstract Recipe getRecipe();
	public boolean hasGlow() { return false; }

	// override some or all of these in subclasses
	public void handleInteraction(PlayerInteractEvent event) { }
	public void handleBlockDamage(BlockDamageEvent event) { }
	public void handleEntityInteraction(PlayerInteractEntityEvent event) { }
	public void handleBlockPhysics(BlockPhysicsEvent event) { }

	public void handleBlockPlace(BlockPlaceEvent event) {
		blockPlaced(event.getBlock());
	}

	public void handleBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		block.getWorld().dropItemNaturally(block.getLocation(), toItemStack(1));
		blockRemoved(block);
		event.getPlayer().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
		block.setType(Material.AIR);
		event.setCancelled(true);
	}

	public boolean handleSignConfigure(SignChangeEvent event) { return false; }

	public void onServerTick(PersistableLocation pLoc) { }

	public static BaseSTBItem getBaseItem(ItemStack stack) {
		if (!isSTBItem(stack)) {
			return null;
		}
		String disp = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
		return getItem(disp);
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

	public ItemStack toItemStack(int amount) {
		ItemStack res = new ItemStack(getBaseMaterial(), amount);
		if (getBaseBlockData() != null) {
			res.setDurability(getBaseBlockData());
		}
		ItemMeta im = res.getItemMeta();
		im.setDisplayName(DISPLAY_COLOR + getDisplayName());
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
		String[] lore2 = getExtraLore();
		List<String> res = new ArrayList<String>(lore.length + lore2.length + 1);
		res.add(STB_LORE_LINE);
		for (String l : lore) {
			res.add(LORE_COLOR + l);
		}
		Collections.addAll(res, lore2);
		return res;
	}

	protected void blockPlaced(Block b) {
		SensibleToolboxPlugin.getInstance().getLocationManager().registerLocation(b.getLocation(), this);
	}

	protected void blockRemoved(Block b) {
		SensibleToolboxPlugin.getInstance().getLocationManager().unregisterLocation(b.getLocation(), this);
	}

	protected void blockUpdated(Block b) {
		SensibleToolboxPlugin.getInstance().getLocationManager().updateLocation(b.getLocation(), this);
	}

	public String getPlainDisplayName() {
		return ChatColor.stripColor(getDisplayName());
	}

	@Override
	public String toString() {
		return "STB item: " + getDisplayName() + " <" + getBaseMaterial() + ">";
	}

	protected static Configuration getItemAttributes(ItemStack stack) {
		AttributeStorage storage = AttributeStorage.newTarget(stack, SensibleToolboxPlugin.UNIQUE_ID);
		YamlConfiguration conf = new YamlConfiguration();
		try {
			conf.loadFromString(storage.getData(""));
			System.out.println("get item attributes:");
			for (String k : conf.getKeys(false)) { System.out.println(" " + k + " = " + conf.get(k)); }
			return conf;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return new MemoryConfiguration();
		}
	}
}
