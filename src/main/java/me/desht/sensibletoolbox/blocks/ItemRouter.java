package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.gui.AccessControlGadget;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.RedstoneBehaviourGadget;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.itemroutermodules.DirectionalItemRouterModule;
import me.desht.sensibletoolbox.items.itemroutermodules.ItemRouterModule;
import me.desht.sensibletoolbox.items.itemroutermodules.SpeedModule;
import me.desht.sensibletoolbox.items.itemroutermodules.StackModule;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import me.desht.sensibletoolbox.util.STBUtil;
import me.desht.sensibletoolbox.util.VanillaInventoryUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemRouter extends BaseSTBBlock implements STBInventoryHolder {
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.BLUE);
	public static final String STB_ITEM_ROUTER = "STB_Item_Router";
	public static final int MOD_SLOT_START = 27;
	public static final int MOD_SLOT_END = 36;
	private static final int BUFFER_DISPLAY_SLOT = 1;
	private final List<ItemRouterModule> modules = new ArrayList<ItemRouterModule>();
	private ItemStack bufferItem;
	private int stackSize;
	private int tickRate;
	private boolean needToProcessModules = false;
	private final List<BlockFace> neighbours = new ArrayList<BlockFace>();

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
			mods.add(module.getItemTypeID() + "::" + module.freeze().saveToString());
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
		return new String[] {
				"Routes items.  Insert one or",
				"more Routing Modules to activate.",
				"R-click block:" + ChatColor.RESET + " configure router"
		};
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
		this.stackSize = Math.min(stackSize, 64);
	}

	private void setTickRate(int tickRate) {
		this.tickRate = Math.max(tickRate, 5);
	}

	public int getTickRate() {
		return tickRate;
	}

	@Override
	public void onInteractBlock(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
			updateBufferIndicator(true);
			getGUI().show(event.getPlayer());
			event.setCancelled(true);
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (event.getPlayer().getItemInHand().getType() == Material.AIR) {
				MiscUtil.alertMessage(event.getPlayer(), "Item router buffer: " + ChatColor.GOLD + STBUtil.describeItemStack(getBufferItem()));
				if (event.getPlayer().isSneaking() && getBufferItem() != null) {
					Block b = getLocation().getBlock().getRelative(event.getBlockFace());
					getLocation().getWorld().dropItemNaturally(b.getLocation(), getBufferItem());
					setBufferItem(null);
					updateBlock(false);
					event.getPlayer().playSound(getLocation(), Sound.CHICKEN_EGG_POP, 1.0f, 1.0f);
				}
				event.setCancelled(true);
			}
		}
		super.onInteractBlock(event);
	}

	@Override
	protected InventoryGUI createGUI() {
		InventoryGUI gui = new InventoryGUI(this, 36, ChatColor.DARK_RED + getItemName());
		gui.addGadget(new RedstoneBehaviourGadget(gui), 8);
		gui.addGadget(new AccessControlGadget(gui), 17);
		for (int slot = MOD_SLOT_START; slot < MOD_SLOT_END; slot++) {
			gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
		}
		gui.addLabel("Item Buffer", 0, null, "Shift-left-click the Item Router", "with an empty hand", "to eject items from buffer");
		gui.addLabel("Item Router Modules", 18, null, "Insert one or more modules below", "Modules are processed in order,", "left to right");
		int slot = MOD_SLOT_START;
		for (ItemRouterModule module : modules) {
			gui.getInventory().setItem(slot, module.toItemStack(module.getAmount()));
			slot++;
		}
		return gui;
	}

	@Override
	public void setLocation(Location loc) {
		if (loc == null) {
			if (getBufferItem() != null) {
				getLocation().getWorld().dropItemNaturally(getLocation(), getBufferItem());
				setBufferItem(null);
			}
			for (int modSlot = MOD_SLOT_START; modSlot < MOD_SLOT_END; modSlot++) {
				ItemStack stack = getGUI().getInventory().getItem(modSlot);
				if (stack != null) {
					getLocation().getWorld().dropItemNaturally(getLocation(), stack);
				}
			}
			clearModules();
		}
		super.setLocation(loc);
		if (loc != null) {
			// defer this so we can be sure all neighbouring STB blocks are actually created first
			Bukkit.getScheduler().runTask(SensibleToolboxPlugin.getInstance(), new Runnable() {
				@Override
				public void run() {
					findNeighbourInventories();
				}
			});
		}
	}

	@Override
	public boolean shouldTick() {
		return true;
	}

	@Override
	public void onServerTick() {
		boolean didSomeWork = false;
		if (needToProcessModules) {
			processModules();
			needToProcessModules = false;
		}
		if (isRedstoneActive() && getTicksLived() % getTickRate() == 0) {
			for (ItemRouterModule module : modules) {
				if (module instanceof DirectionalItemRouterModule) {
					DirectionalItemRouterModule dmod = (DirectionalItemRouterModule) module;
					if (dmod.execute()) {
						didSomeWork = true;
						if (dmod.isTerminator()) {
							break;
						}
					}
				}

			}
		}
		if (didSomeWork) {
			updateBlock(false);
			playParticles();
		}
		super.onServerTick();
	}

	@Override
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Bukkit.getScheduler().runTask(SensibleToolboxPlugin.getInstance(), new Runnable() {
			@Override
			public void run() {
				findNeighbourInventories();
			}
		});
	}

	private void findNeighbourInventories() {
		neighbours.clear();
		Location loc = getLocation();
		if (loc == null) {
			return;
		}
		Block b = loc.getBlock();
		for (BlockFace face : STBUtil.directFaces) {
			Block b1 = b.getRelative(face);
			BaseSTBBlock stb = LocationManager.getManager().get(b1.getLocation());
			if (stb instanceof STBInventoryHolder) {
				neighbours.add(face);
			} else if (VanillaInventoryUtils.isVanillaInventory(b1)) {
				neighbours.add(face);
			}
		}
	}

	private void playParticles() {
		if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
			Location loc = getLocation().add(0.5, 0.5, 0.5);
			ParticleEffect.WITCH_MAGIC.play(loc, 0.2f, 0.2f, 0.2f, 0, 10);
		}
	}

	private void clearModules() {
		modules.clear();
		setStackSize(1);
		setTickRate(20);
	}

	private void insertModule(ItemRouterModule module) {
		module.setOwner(this);
		if (module instanceof DirectionalItemRouterModule) {
			DirectionalItemRouterModule dm = (DirectionalItemRouterModule) module;
			if (dm.getDirection() == null) {
				dm.setDirection(BlockFace.SELF);
			}
		} else if (module instanceof StackModule) {
			setStackSize(getStackSize() * (int) Math.pow(2, module.getAmount()));
		} else if (module instanceof SpeedModule) {
			setTickRate(getTickRate() - 5 * module.getAmount());
		}
		modules.add(module);
	}

	public ItemStack getBufferItem() {
		return bufferItem == null ? null : bufferItem.clone();
	}

	private void updateBufferIndicator(boolean force) {
		if (getGUI() != null && (getGUI().getViewers().size() > 0 || force)) {
			if (bufferItem == null) {
				getGUI().getInventory().setItem(BUFFER_DISPLAY_SLOT, null);
			} else {
				getGUI().getInventory().setItem(BUFFER_DISPLAY_SLOT, bufferItem);
			}
		}
	}

	public void setBufferItem(ItemStack bufferItem) {
		this.bufferItem = bufferItem;
		updateBufferIndicator(false);
	}

	public void setBufferAmount(int newAmount) {
		if (newAmount == bufferItem.getAmount()) {
			return;
		}
		if (newAmount <= 0) {
			setBufferItem(null);
		} else {
			bufferItem.setAmount(newAmount);
			updateBufferIndicator(false);
		}
	}

	public int reduceBuffer(int amount) {
		if (bufferItem != null && amount > 0) {
			amount = Math.min(amount, bufferItem.getAmount());
			setBufferAmount(bufferItem.getAmount() - amount);
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
			setBufferItem(item.clone());
			return item.getAmount();
		} else if (item.isSimilar(bufferItem)) {
			int nInserted = Math.min(item.getAmount(), item.getType().getMaxStackSize() - bufferItem.getAmount());
			setBufferAmount(bufferItem.getAmount() + nInserted);
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
			setBufferAmount(bufferItem.getAmount() - nExtracted);
			return returned;
		} else if (receiver.isSimilar(bufferItem)) {
			int nExtracted = Math.min(amount, bufferItem.getAmount());
			nExtracted = Math.min(nExtracted, receiver.getMaxStackSize() - receiver.getAmount());
			receiver.setAmount(receiver.getAmount() + nExtracted);
			setBufferAmount(bufferItem.getAmount() - nExtracted);
			return receiver;
		} else {
			return null;
		}
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

	@Override
	public boolean onSlotClick(int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
		if (onCursor.getType() == Material.AIR || BaseSTBItem.getItemFromItemStack(onCursor, ItemRouterModule.class) != null) {
			needToProcessModules = true;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onPlayerInventoryClick(int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
		return true;
	}

	@Override
	public int onShiftClickInsert(int slot, ItemStack toInsert) {
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(toInsert, ItemRouterModule.class);
		if (item == null) {
			return 0;
		}
		int nInserted = 0;
		for (int modSlot = MOD_SLOT_START; modSlot < MOD_SLOT_END; modSlot++) {
			ItemStack mod = getGUI().getInventory().getItem(modSlot);
			if (mod == null) {
				getGUI().getInventory().setItem(modSlot, toInsert);
				nInserted = toInsert.getAmount();
			} else if (mod.isSimilar(toInsert)) {
				nInserted = mod.getType().getMaxStackSize() - mod.getAmount();
				nInserted = Math.min(toInsert.getAmount(), nInserted);
				mod.setAmount(mod.getAmount() + nInserted);
				getGUI().getInventory().setItem(modSlot, mod);
			}
			if (nInserted > 0) {
				break;
			}
		}
		if (nInserted > 0) {
			needToProcessModules = true;
		}
		return nInserted;
	}

	@Override
	public boolean onShiftClickExtract(int slot, ItemStack toExtract) {
		return true;
	}

	@Override
	public boolean onClickOutside() {
		return false;
	}

	@Override
	public void onGUIClosed() {
		// no action needed here
	}

	private void processModules() {
		clearModules();
		Map<ItemStack,Integer> mods = new LinkedHashMap<ItemStack, Integer>();
		for (int modSlot = MOD_SLOT_START; modSlot < MOD_SLOT_END; modSlot++) {
			ItemStack stack = getGUI().getInventory().getItem(modSlot);
			if (stack != null) {
				if (!mods.containsKey(stack)) {
					mods.put(stack, stack.getAmount());
				} else {
					mods.put(stack, mods.get(stack) + stack.getAmount());
				}
			}
		}
		for (Map.Entry<ItemStack,Integer> entry : mods.entrySet()) {
			ItemRouterModule mod = BaseSTBItem.getItemFromItemStack(entry.getKey(), ItemRouterModule.class);
			mod.setAmount(entry.getValue());
			insertModule(mod);
		}
		Debugger.getInstance().debug("re-processed modules for " + this + " tick-rate=" + getTickRate() + " stack-size=" + getStackSize());
		updateBlock(false);
	}

	public List<BlockFace> getNeighbours() {
		return neighbours;
	}
}
