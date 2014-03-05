package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.dhutils.ItemNames;
import me.desht.sensibletoolbox.api.FilterType;
import me.desht.sensibletoolbox.api.Filtering;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.blocks.ItemRouter;
import me.desht.sensibletoolbox.gui.FilterTypeGadget;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.ToggleButton;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.Filter;
import me.desht.sensibletoolbox.util.STBUtil;
import me.desht.sensibletoolbox.util.VanillaInventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import java.util.Arrays;
import java.util.List;

public abstract class DirectionalItemRouterModule extends ItemRouterModule implements Filtering {
	private static final String LIST_ITEM = ChatColor.LIGHT_PURPLE + "\u2022 " + ChatColor.AQUA;
	private static final ItemStack WHITE_BUTTON = InventoryGUI.makeTexture(new Wool(DyeColor.WHITE), ChatColor.UNDERLINE + "Whitelist");
	private static final ItemStack BLACK_BUTTON = InventoryGUI.makeTexture(new Wool(DyeColor.BLACK), ChatColor.UNDERLINE + "Blacklist");
	private static final ItemStack OFF_BUTTON = InventoryGUI.makeTexture(
			STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.LIGHT_BLUE), ChatColor.UNDERLINE + "Termination OFF",
			"Subsequent modules will", "process items even if this", "module processes items."
	);
	private static final ItemStack ON_BUTTON = InventoryGUI.makeTexture(
			new Wool(DyeColor.ORANGE), ChatColor.UNDERLINE + "Termination ON",
			"If this module processes an item,", "the processing sequence stops."
	);
	private final Filter filter;
	private BlockFace direction;
	private boolean terminator;
	private InventoryGUI gui;
	private final int[] filterSlots = { 1, 2, 3, 10, 11, 12, 19, 20, 21 };

	public abstract boolean execute();

	public DirectionalItemRouterModule() {
		filter = new Filter();  // default filter: blacklist, no items
		setDirection(BlockFace.SELF);
	}

	public DirectionalItemRouterModule(ConfigurationSection conf) {
		super(conf);
		setDirection(BlockFace.valueOf(conf.getString("direction")));
		setTerminator(conf.getBoolean("terminator", false));
		if (conf.contains("filtered")) {
			boolean isWhite = conf.getBoolean("filterWhitelist", true);
			FilterType filterType = FilterType.valueOf(conf.getString("filterType", "MATERIAL"));
			@SuppressWarnings("unchecked")
			List<ItemStack> l = (List<ItemStack>) conf.getList("filtered");
			filter = Filter.fromItemList(isWhite, l, filterType);
		} else {
			filter = new Filter();
		}
	}

	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("direction", getDirection().toString());
		conf.set("terminator", isTerminator());
		if (filter != null) {
			conf.set("filtered", filter.listFiltered());
			conf.set("filterWhitelist", filter.isWhiteList());
			conf.set("filterType", filter.getFilterType().toString());
		}
		return conf;
	}

	@Override
	public String[] getExtraLore() {
		if (filter == null) {
			return new String[0];
		} else {
			String[] lore = new String[(filter.size() + 1) / 2 + 2];
			String what = filter.isWhiteList() ? "white-listed" : "black-listed";
			String s = filter.size() == 1 ? "" : "s";
			lore[0] = ChatColor.GOLD.toString() + filter.size() + " item" + s + " " + what;
			if (isTerminator()) {
				lore[0] += ", " + ChatColor.BOLD + "Terminating";
			}
			lore[1] = ChatColor.GOLD + filter.getFilterType().getLabel();
			int i = 2;
			for (ItemStack stack : filter.listFiltered()) {
				int n = i / 2 + 1;
				String name = ItemNames.lookup(stack);
				lore[n] = lore[n] == null ? LIST_ITEM + name : lore[n] + " " + LIST_ITEM + name;
				i++;
			}
			return lore;
		}
	}

	@Override
	public String getDisplaySuffix() {
		return direction != null && direction != BlockFace.SELF ? direction.toString() : null;
	}

	public BlockFace getDirection() {
		return direction;
	}

	public void setDirection(BlockFace direction) {
		this.direction = direction;
	}

	public Filter getFilter() {
		return filter;
	}

	public boolean isTerminator() {
		return terminator;
	}

	public void setTerminator(boolean terminator) {
		this.terminator = terminator;
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			// set module direction based on clicked block face
			setDirection(event.getBlockFace().getOppositeFace());
			event.getPlayer().setItemInHand(toItemStack(event.getPlayer().getItemInHand().getAmount()));
			event.setCancelled(true);
		} else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemRouter rtr = event.getClickedBlock() == null ? null : LocationManager.getManager().get(event.getClickedBlock().getLocation(), ItemRouter.class);
			if (event.getClickedBlock() == null || (rtr == null && !STBUtil.isInteractive(event.getClickedBlock().getType()))) {
				// open module configuration GUI
				gui = createGUI(event.getPlayer());
				gui.show(event.getPlayer());
				event.setCancelled(true);
			}
		}
	}

	private InventoryGUI createGUI(Player player) {
		InventoryGUI gui = new InventoryGUI(player, this, 27, ChatColor.DARK_RED + "Module Configuration");

		gui.addGadget(new ToggleButton(gui, getFilter().isWhiteList(), WHITE_BUTTON, BLACK_BUTTON, new ToggleButton.ToggleListener() {
			@Override
			public boolean run(int slot, boolean newValue) {
				if (getFilter() != null) {
					getFilter().setWhiteList(newValue);
					return true;
				} else {
					return false;
				}
			}
		}), 8);
		gui.addGadget(new FilterTypeGadget(gui), 17);
		gui.addGadget(new ToggleButton(gui, isTerminator(), ON_BUTTON, OFF_BUTTON, new ToggleButton.ToggleListener() {
			@Override
			public boolean run(int slot, boolean newValue) {
				setTerminator(newValue);
				return true;
			}
		}), 26);

		gui.addLabel("Filtered Items", 0, null);
		for (int slot : filterSlots) {
			gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
		}
		populateFilterInventory(gui.getInventory());

		return gui;
	}

	private void populateFilterInventory(Inventory inv) {
		int n = 0;
		for (ItemStack stack : filter.listFiltered()) {
			inv.setItem(filterSlots[n], stack);
			if (++n >= filterSlots.length) {
				break;
			}
		}
	}

	protected String[] makeDirectionalLore(String... lore) {
		String[] newLore = Arrays.copyOf(lore, lore.length + 1);
		newLore[lore.length] = "L-click Block: " + ChatColor.RESET + " Set direction";
		return newLore;
	}

	@Override
	public boolean onSlotClick(int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
		if (onCursor.getType() == Material.AIR) {
			gui.getInventory().setItem(slot, null);
		} else {
			ItemStack stack = onCursor.clone();
			stack.setAmount(1);
			gui.getInventory().setItem(slot, stack);
		}
		return false;
	}

	@Override
	public boolean onPlayerInventoryClick(int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
		return true;
	}

	@Override
	public int onShiftClickInsert(int slot, ItemStack toInsert) {
		return 0;
	}

	@Override
	public boolean onShiftClickExtract(int slot, ItemStack toExtract) {
		return false;
	}

	@Override
	public boolean onClickOutside() {
		return false;
	}

	public void onGUIClosed() {
		Player player = gui.getPrimaryPlayer();
		if (player != null) {
			filter.clear();
			for (int slot : filterSlots) {
				ItemStack stack = gui.getInventory().getItem(slot);
				if (stack != null) {
					filter.addItem(stack);
				}
			}
			player.setItemInHand(toItemStack(player.getItemInHand().getAmount()));
		}
	}

	protected boolean doPull(BlockFace from) {
		ItemStack inBuffer = getOwner().getBufferItem();
		if (inBuffer != null && inBuffer.getAmount() >= inBuffer.getType().getMaxStackSize()) {
			return false;
		}
		int nToPull = getOwner().getStackSize();
		Block b = getOwner().getLocation().getBlock();
		Block target = b.getRelative(from);
		STBBlock stb = LocationManager.getManager().get(target.getLocation());
		ItemStack pulled;
		if (stb instanceof STBInventoryHolder) {
			pulled = ((STBInventoryHolder)stb).extractItems(from.getOppositeFace(), inBuffer, nToPull);
		} else {
			// possible vanilla inventory holder
			pulled = VanillaInventoryUtils.pullFromInventory(target, nToPull, inBuffer, getFilter());
		}
		if (pulled != null) {
			if (stb != null) {
				stb.updateBlock(false);
			}
			getOwner().setBufferItem(inBuffer == null ? pulled : inBuffer);
			return true;
		}
		return false;
	}

	protected boolean vanillaInsertion(Block target, int amount, BlockFace side) {
		ItemStack buffer = getOwner().getBufferItem();
		int nInserted = VanillaInventoryUtils.vanillaInsertion(target, buffer, amount, side, false);
		if (nInserted == 0) {
			// no insertion happened
			return false;
		} else {
			// some or all items were inserted, buffer size has been adjusted accordingly
			getOwner().setBufferItem(buffer.getAmount() == 0 ? null : buffer);
			return true;
		}
	}
}
