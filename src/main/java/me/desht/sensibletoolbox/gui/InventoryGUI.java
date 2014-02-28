package me.desht.sensibletoolbox.gui;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class InventoryGUI {
	// some handy stock textures
	public static final ItemStack INPUT_TEXTURE = new ItemStack(Material.ENDER_PORTAL);
	public static final ItemStack OUTPUT_TEXTURE = new ItemStack(Material.STATIONARY_LAVA);
	public static final ItemStack BG_TEXTURE = new ItemStack(Material.STATIONARY_WATER);
	static {
		setDisplayName(INPUT_TEXTURE, ChatColor.AQUA + "Input");
		setDisplayName(OUTPUT_TEXTURE, ChatColor.AQUA + "Output");
		setDisplayName(BG_TEXTURE, " ");
	}

	private static final String STB_OPEN_GUI = "STB_Open_GUI";
	private final Inventory inventory;
	private final InventoryGUIListener listener;
	private final ClickableGadget[] gadgets;
	private final SlotType[] slotTypes;
	private final IntRange slotRange;
	private final List<MonitorGadget> monitors = new ArrayList<MonitorGadget>();
	private WeakReference<Player> player;

	public InventoryGUI(InventoryGUIListener listener, int size, String title) {
		this(null, listener, size, title);
	}

	public InventoryGUI(Player player, InventoryGUIListener listener, int size, String title) {
		this.listener = listener;
		this.inventory = player == null ?
				Bukkit.createInventory(((BaseSTBBlock) listener).getGuiHolder(), size, title) :
				Bukkit.createInventory(player, size, title);
		this.gadgets = new ClickableGadget[size];
		this.slotRange = new IntRange(0, size - 1);
		this.slotTypes = new SlotType[size];

		for (int slot = 0; slot < size; slot++) {
			setSlotType(slot, SlotType.BACKGROUND);
		}
	}

	public static List<String> makeLore(String... lore) {
		List<String> res = new ArrayList<String>();
		for (String s : lore) {
			res.add(ChatColor.GRAY + s);
		}
		return res;
	}

	public static InventoryGUI getOpenGUI(Player player) {
		for (MetadataValue mv : player.getMetadata(STB_OPEN_GUI)) {
			if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
				return (InventoryGUI) mv.value();
			}
		}
		return null;
	}

	public static void setOpenGUI(Player player, InventoryGUI gui) {
		if (gui != null) {
			gui.setPrimaryPlayer(player);
			player.setMetadata(STB_OPEN_GUI, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), gui));
		} else {
			player.removeMetadata(STB_OPEN_GUI, SensibleToolboxPlugin.getInstance());
		}
	}

	public static ItemStack makeTexture(MaterialData material, String title, String... lore ) {
		ItemStack res = material.toItemStack();
		ItemMeta meta = res.getItemMeta();
		meta.setDisplayName(title);
		if (lore.length > 0) {
			meta.setLore(makeLore(lore));
		}
		res.setItemMeta(meta);
		return res;
	}

	public static void setDisplayName(ItemStack stack, String disp) {
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(disp);
		stack.setItemMeta(meta);
	}

	public void addGadget(ClickableGadget gadget, int slot) {
		if (containsSlot(slot)) {
			inventory.setItem(slot, gadget.getTexture());
			gadgets[slot] = gadget;
			setSlotType(slot, SlotType.GADGET);
		}
	}

	public void addLabel(String label, int slot, ItemStack texture, String... lore) {
		ItemStack stack = texture == null  ? new ItemStack(Material.ENDER_PORTAL) : texture;
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + label);
		if (lore.length > 0) {
			meta.setLore(makeLore(lore));
		}
		stack.setItemMeta(meta);
		setSlotType(slot, SlotType.BACKGROUND);
		inventory.setItem(slot, stack);
	}

	public int addMonitor(MonitorGadget gadget) {
		Validate.isTrue(gadget.getSlots().length > 0, "Gadget has no slots!");
		monitors.add(gadget);
		for (int slot : gadget.getSlots()) {
			setSlotType(slot, SlotType.GADGET);
		}
		return monitors.size() - 1;
	}

	public Player getPrimaryPlayer() {
		if (player == null || player.get() == null) {
			if (getViewers().isEmpty()) {
				return null;
			} else {
				player = new WeakReference<Player>((Player) getViewers().get(0));
			}
		}
		return player.get();
	}

	private void setPrimaryPlayer(Player player) {
		if (player == null) {
			this.player = null;
		} else if (this.player == null || this.player.get() == null) {
			this.player = new WeakReference<Player>(player);
		}
	}

	public MonitorGadget getMonitor(int monitorId) {
		return monitors.get(monitorId);
	}

	public BaseSTBBlock getOwningBlock() {
		if (listener instanceof BaseSTBBlock) {
			return (BaseSTBBlock) listener;
		}
		throw new IllegalStateException("attempt to get STB block for non-block listener");
	}

	public BaseSTBItem getOwningItem() {
		if (listener instanceof BaseSTBItem) {
			return (BaseSTBItem) listener;
		}
		throw new IllegalStateException("attempt to get STB item for non-item listener");
	}

	public Inventory getInventory() {
		return inventory;
	}

	public boolean containsSlot(int slot) {
		return slotRange.containsInteger(slot);
	}

	public void show(Player player) {
		// TODO ownership/permission validation
		if (inventory.getViewers().isEmpty()) {
			// no one's already looking at this inventory/gui, so ensure it's up to date
			Debugger.getInstance().debug("refreshing GUI inventory of " + getOwningItem());
			for (MonitorGadget monitor : monitors) {
				monitor.doRepaint();
			}
		}
		Debugger.getInstance().debug(player.getName() + " opened GUI for " + getOwningItem());
		setOpenGUI(player, this);
		player.openInventory(inventory);
	}

	public void hide(Player player) {
		Debugger.getInstance().debug(player.getName() + ": hide GUI");
		setOpenGUI(player, null);
		player.closeInventory();
	}

	public List<HumanEntity> getViewers() {
		return inventory.getViewers();
	}

	public void receiveEvent(InventoryClickEvent event) {
		boolean shouldCancel = true;
		if (containsSlot(event.getRawSlot())) {
			// clicking inside the GUI
			switch (getSlotType(event.getRawSlot())) {
				case GADGET:
					if (gadgets[event.getRawSlot()] != null) {
						gadgets[event.getRawSlot()].onClicked(event);
					}
					break;
				case ITEM:
					shouldCancel = !processGUIInventoryAction(event);
					Debugger.getInstance().debug("handled click for " + event.getWhoClicked().getName() + " in item slot " + event.getRawSlot() + " of " + getOwningItem() + ": cancelled = " + shouldCancel);
					break;
				default:
					break;
			}
		} else if (event.getRawSlot() > 0) {
			// clicking inside the player's inventory
			if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				int nInserted = listener.onShiftClickInsert(event.getRawSlot(), event.getCurrentItem());
				if (nInserted > 0) {
					ItemStack stack = event.getCurrentItem();
					stack.setAmount(stack.getAmount() - nInserted);
					event.setCurrentItem(stack.getAmount() > 0 ? stack: null);
				}
			} else {
				shouldCancel = !listener.onPlayerInventoryClick(event.getSlot(), event.getClick(), event.getCurrentItem(), event.getCursor());
			}
		} else {
			// clicking outside the inventory entirely
			shouldCancel = !listener.onClickOutside();
		}
		if (shouldCancel) {
			event.setCancelled(true);
		}
	}

	public void receiveEvent(InventoryDragEvent event) {
		boolean inGUI = false;
		boolean shouldCancel = true;
		for (int slot : event.getRawSlots()) {
			if (containsSlot(slot)) {
				inGUI = true;
			}
		}
		if (inGUI) {
			// we only allow drags with a single slot involved, and we fake that as a left-click on the slot
			if (event.getRawSlots().size() == 1) {
				int slot = (event.getRawSlots().toArray(new Integer[1]))[0];
				shouldCancel = !listener.onSlotClick(slot, ClickType.LEFT, inventory.getItem(slot), event.getOldCursor());
			}
		} else {
			// drag is purely in the player's inventory; allow it
			shouldCancel = false;
		}
		if (shouldCancel) {
			event.setCancelled(true);
		}
	}

	private boolean processGUIInventoryAction(InventoryClickEvent event) {
		switch (event.getAction()) {
			case MOVE_TO_OTHER_INVENTORY:
				return listener.onShiftClickExtract(event.getRawSlot(), event.getCurrentItem());
			case PLACE_ONE: case PLACE_ALL: case PLACE_SOME: case SWAP_WITH_CURSOR:
			case PICKUP_ALL: case PICKUP_HALF: case PICKUP_ONE: case PICKUP_SOME:
				return listener.onSlotClick(event.getRawSlot(), event.getClick(), event.getCurrentItem(), event.getCursor());
			default:
				return false;
		}
	}

	public void receiveEvent(InventoryCloseEvent event) {
		Debugger.getInstance().debug("received GUI close event for " + event.getPlayer().getName());
		listener.onGUIClosed();
		if (event.getPlayer() instanceof Player) {
			setOpenGUI((Player) event.getPlayer(), null);
			Player p = getPrimaryPlayer();
			if (p != null && p.getUniqueId().equals(event.getPlayer().getUniqueId())) {
				setPrimaryPlayer(null);
			}
		}
		Debugger.getInstance().debug(event.getPlayer().getName() + " closed GUI for " + getOwningItem());
	}

	public SlotType getSlotType(int slot) {
		return slotTypes[slot];
	}

	public void setSlotType(int slot, SlotType type) {
		slotTypes[slot] = type;
		switch (type) {
			case BACKGROUND:
				paintSlot(slot, BG_TEXTURE, true); break;
			case ITEM:
				paintSlot(slot, null, true); break;
		}
	}

	public void paintSlotSurround(int[] slots, ItemStack texture) {
		for (int slot : slots) {
			int row = slot / 9, col = slot % 9;
			for (int i = row - 1; i <= row + 1; i++) {
				for (int j = col - 1; j <= col + 1; j++) {
					paintSlot(i, j, texture, true);
				}
			}
		}
	}

	public void paintSlot(int row, int col, ItemStack texture, boolean overwrite) {
		paintSlot(row * 9 + col, texture, overwrite);
	}

	public void paintSlot(int slot, ItemStack texture, boolean overwrite) {
		if (slotRange.containsInteger(slot)) {
			if (overwrite || inventory.getItem(slot) == null) {
				inventory.setItem(slot, texture);
			}
		}
	}

	public String freezeSlots(int... slots) {
		int invSize = STBUtil.roundUp(slots.length, 9);
		Inventory tmpInv = Bukkit.createInventory(null, invSize);
		for (int i = 0; i < slots.length; i++) {
			tmpInv.setItem(i, inventory.getItem(slots[i]));
		}
		return BukkitSerialization.toBase64(tmpInv, slots.length);
	}

	public void thawSlots(String frozen, int... slots) {
		if (frozen != null && !frozen.isEmpty() && slots.length > 0) {
			try {
				Inventory tmpInv = BukkitSerialization.fromBase64(frozen);
				for (int i = 0; i < slots.length; i++) {
					inventory.setItem(slots[i], tmpInv.getItem(i));
				}

			} catch (IOException e) {
				LogUtils.severe("can't restore inventory for " + getOwningItem().getItemName());
			}
		}
	}

	public void ejectItems(int... slots) {
		Location loc = getOwningBlock().getLocation();
		for (int slot : slots) {
			ItemStack stack = inventory.getItem(slot);
			if (stack != null) {
				loc.getWorld().dropItemNaturally(loc, stack);
				inventory.setItem(slot, null);
			}
		}
	}

	public interface InventoryGUIListener {
		public boolean onSlotClick(int slot, ClickType click, ItemStack inSlot, ItemStack onCursor);
		public boolean onPlayerInventoryClick(int slot, ClickType click, ItemStack inSlot, ItemStack onCursor);
		public int onShiftClickInsert(int slot, ItemStack toInsert);
		public boolean onShiftClickExtract(int slot, ItemStack toExtract);
		public boolean onClickOutside();
		public void onGUIClosed();
	}

	public enum SlotType {
		BACKGROUND,
		ITEM,
		GADGET
	}
}
