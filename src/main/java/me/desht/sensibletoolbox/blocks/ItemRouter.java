package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.itemroutermodules.DirectionalItemRouterModule;
import me.desht.sensibletoolbox.items.itemroutermodules.ItemRouterModule;
import me.desht.sensibletoolbox.items.itemroutermodules.SpeedModule;
import me.desht.sensibletoolbox.items.itemroutermodules.StackModule;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemRouter extends BaseSTBBlock implements STBInventoryHolder {
	private static final MaterialData md = new MaterialData(Material.STAINED_CLAY, DyeColor.BLUE.getWoolData());
	public static final String STB_ITEM_ROUTER = "STB_Item_Router";
	private final List<ItemRouterModule> modules = new ArrayList<ItemRouterModule>();
	private ItemStack bufferItem;
	private int stackSize;
	private int tickRate;

	public ItemRouter() {
		setStackSize(1);
		setTickRate(20);
	}

	public ItemRouter(ConfigurationSection conf) {
		super(conf);
		setStackSize(1);
		setTickRate(20);
		for (String l : conf.getStringList("modules")) {
			String[] f = l.split("::", 2);
			try {
				YamlConfiguration modConf = new YamlConfiguration();
				if (f.length > 1) {
					modConf.loadFromString(f[1]);
				}
				ItemRouterModule mod = (ItemRouterModule) BaseSTBItem.getItemById(f[0], modConf);
				insertModule(mod);
			} catch (Exception e) {
				LogUtils.warning("can't restore saved module " + f[0] + " for " + this + ": " + e.getMessage());
			}
		}
		try {
			if (conf.contains("buffer")) {
				Inventory inv = BukkitSerialization.fromBase64(conf.getString("buffer"));
				setBufferItem(inv.getItem(0));
			}
		} catch (IOException e) {
			LogUtils.warning("item router @ " + MiscUtil.formatLocation(getLocation()) +
					": can't restore buffer item: " + e.getMessage());
		}
	}

	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		List<String> mods = new ArrayList<String>();
		for (ItemRouterModule module : modules) {
			mods.add(module.getItemID() + "::" + module.freeze().saveToString());
		}
		conf.set("modules", mods);
		Inventory inv = Bukkit.createInventory(null, 9);
		inv.setItem(0, getBufferItem());
		conf.set("buffer", BukkitSerialization.toBase64(inv, 1));
		return conf;
	}

	public static String getInventoryTitle() {
		return ChatColor.GOLD + "Item Router: Module Management";
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Item Router";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Routes items.  Insert one or", "more Routing Modules.", "R-click: open module GUI"};
	}

	@Override
	public String[] getExtraLore() {
		if (modules.isEmpty()) {
			return new String[0];
		} else {
			String[] lore = new String[modules.size()];
			for (int i = 0; i < modules.size(); i++) {
				lore[i] = ChatColor.GREEN + modules.get(i).getItemName();
				if (modules.get(i) instanceof DirectionalItemRouterModule) {
					lore[i] += ": " + ((DirectionalItemRouterModule) modules.get(i)).getDirection();
				}
			}
			return lore;
		}
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(4));
		recipe.shape("RFR", "FLF", "RFR");
		recipe.setIngredient('R', Material.REDSTONE);
		recipe.setIngredient('F', Material.IRON_FENCE);
		recipe.setIngredient('L', Material.LEVER);
		return recipe;
	}

	public int getStackSize() {
		return stackSize;
	}

	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}

	private void setTickRate(int tickRate) {
		if (tickRate >= 5) {
			this.tickRate = tickRate;
		}
	}

	public int getTickRate() {
		return tickRate;
	}

	@Override
	public void onInteractBlock(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
			Inventory inv = Bukkit.createInventory(event.getPlayer(), 9, getInventoryTitle());
			populateGUI(inv);
			event.getPlayer().openInventory(inv);
			event.getPlayer().setMetadata(STB_ITEM_ROUTER,
					new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), getLocation()));
			event.setCancelled(true);
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == Material.AIR) {
			MiscUtil.alertMessage(event.getPlayer(), "Item router buffer: " + ChatColor.GOLD + STBUtil.describeItemStack(getBufferItem()));
			if (event.getPlayer().isSneaking() && getBufferItem() != null) {
				getLocation().getWorld().dropItemNaturally(getLocation(), getBufferItem());
				setBufferItem(null);
				updateBlock();
				event.getPlayer().playSound(getLocation(), Sound.CHICKEN_EGG_POP, 1.0f, 1.0f);
			}
		} else {
			super.onInteractBlock(event);
		}
	}

	@Override
	public void setLocation(Location loc) {
		if (loc == null && getBufferItem() != null) {
			getLocation().getWorld().dropItemNaturally(getLocation(), getBufferItem());
			setBufferItem(null);
		}
		super.setLocation(loc);
	}

	@Override
	public boolean shouldTick() {
		return true;
	}

	@Override
	public void onServerTick() {
		boolean update = false;
		if (getTicksLived() % getTickRate() == 0) {
			for (ItemRouterModule module : modules) {
				if (module.execute()) {
					update = true;
				}
			}
		}
		if (update) {
			updateBlock(false);
			playParticles();
		}
		super.onServerTick();
	}

	private void playParticles() {
		if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
			Location loc = getLocation().add(0.5, 0.5, 0.5);
			ParticleEffect.WITCH_MAGIC.play(loc, 0.2f, 0.2f, 0.2f, 0, 10);
		}
	}

	private void populateGUI(Inventory inv) {
		int slot = 0;
		for (ItemRouterModule module : modules) {
			inv.setItem(slot, module.toItemStack(1));
			slot++;
		}
	}

	public void clearModules() {
		modules.clear();
		setStackSize(1);
		setTickRate(20);
	}

	public void insertModule(ItemRouterModule module) {
		module.setOwner(this);
		if (module instanceof DirectionalItemRouterModule) {
			DirectionalItemRouterModule dm = (DirectionalItemRouterModule) module;
			if (dm.getDirection() == null) {
				dm.setDirection(BlockFace.SELF);
			}
		} else if (module instanceof StackModule) {
			setStackSize(64);
		} else if (module instanceof SpeedModule) {
			setTickRate(getTickRate() - 5);
		}
		modules.add(module);
	}

	public ItemStack getBufferItem() {
		return bufferItem == null ? null : bufferItem.clone();
	}

	public void setBufferItem(ItemStack bufferItem) {
		this.bufferItem = bufferItem;
	}

	public int reduceBuffer(int amount) {
		if (bufferItem != null && amount > 0) {
			amount = Math.min(amount, bufferItem.getAmount());
			bufferItem.setAmount(bufferItem.getAmount() - amount);
			if (bufferItem.getAmount() <= 0) {
				bufferItem = null;
			}
			return amount;
		} else {
			return 0;
		}
	}

	public ItemRouterModule[] getInstalledModules() {
		return modules.toArray(new ItemRouterModule[modules.size()]);
	}

	@Override
	public int insertItems(ItemStack item, BlockFace face, boolean sorting) {
		// item routers don't care about sorters - they will take items from them happily
		if (bufferItem == null) {
			bufferItem = item.clone();
			return item.getAmount();
		} else if (item.isSimilar(bufferItem)) {
			int nInserted = Math.min(item.getAmount(), item.getType().getMaxStackSize() - bufferItem.getAmount());
			bufferItem.setAmount(bufferItem.getAmount() + nInserted);
			return nInserted;
		} else {
			return 0;
		}
	}

	@Override
	public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount) {
		if (bufferItem == null) {
			return null;
		} else if (receiver == null) {
			ItemStack returned = bufferItem.clone();
			int nExtracted = Math.min(amount, bufferItem.getAmount());
			returned.setAmount(nExtracted);
			bufferItem.setAmount(bufferItem.getAmount() - nExtracted);
			if (bufferItem.getAmount() <= 0) {
				bufferItem = null;
			}
			return returned;
		} else if (receiver.isSimilar(bufferItem)) {
			int nExtracted = Math.min(amount, bufferItem.getAmount());
			nExtracted = Math.min(nExtracted, receiver.getType().getMaxStackSize() - receiver.getAmount());
			receiver.setAmount(receiver.getAmount() + nExtracted);
			return receiver;
		} else {
			return null;
		}
	}

	@Override
	public Inventory getInventory() {
		return null;
	}
}
