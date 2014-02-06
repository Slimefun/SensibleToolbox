package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InventoryGUI {
	// some handy stock textures
	public static final ItemStack INPUT_TEXTURE = new ItemStack(Material.ENDER_PORTAL);
	public static final ItemStack OUTPUT_TEXTURE = new ItemStack(Material.STATIONARY_LAVA);
	public static final ItemStack BG_TEXTURE = new ItemStack(Material.STATIONARY_WATER);
	public static final ItemStack ENERGY_TEXTURE = new ItemStack(Material.ENDER_PORTAL);
	public static final ItemStack UPGRADE_TEXTURE = new ItemStack(Material.ENDER_PORTAL);
	static {
		setDisplayName(INPUT_TEXTURE, ChatColor.YELLOW + "Input");
		setDisplayName(OUTPUT_TEXTURE, ChatColor.YELLOW + "Output");
		setDisplayName(ENERGY_TEXTURE, ChatColor.YELLOW + "Energy Cell");
		setDisplayName(UPGRADE_TEXTURE, ChatColor.YELLOW + "Upgrades");
		setDisplayName(BG_TEXTURE, " ");
	}
	private final Inventory inventory;
	private final BaseSTBBlock owner;
	private final ClickableGadget[] gadgets;
	private final SlotType[] slotTypes;
	private final IntRange slotRange;
	private final List<MonitorGadget> monitors = new ArrayList<MonitorGadget>();

	public InventoryGUI(BaseSTBBlock owner, int size, String title) {
		this.owner = owner;
		this.inventory = Bukkit.createInventory(owner.getGuiHolder(), size, title);
		this.gadgets = new ClickableGadget[size];
		this.slotRange = new IntRange(0, size - 1);
		this.slotTypes = new SlotType[size];

		for (int slot = 0; slot < size; slot++) {
			setSlotType(slot, SlotType.BACKGROUND);
		}
	}

	private static void setDisplayName(ItemStack stack, String disp) {
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

	public int addMonitor(MonitorGadget gadget) {
		Validate.isTrue(gadget.getSlots().length > 0, "Gadget has no slots!");
		monitors.add(gadget);
		for (int slot : gadget.getSlots()) {
			setSlotType(slot, SlotType.GADGET);
		}
		return monitors.size() - 1;
	}

	public MonitorGadget getMonitor(int monitorId) {
		return monitors.get(monitorId);
	}

	public BaseSTBBlock getOwner() {
		return owner;
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
			System.out.println("refresh inventory/gui for " + this);
			for (MonitorGadget monitor : monitors) {
				monitor.doRepaint();
			}
		}
		player.openInventory(inventory);
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
					break;
				default:
					break;
			}
		} else if (event.getRawSlot() > 0) {
			// clicking inside the player's inventory
			if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				int nInserted = owner.onShiftClickInsert(event.getRawSlot(), event.getCurrentItem());
				if (nInserted > 0) {
					ItemStack stack = event.getCurrentItem();
					stack.setAmount(stack.getAmount() - nInserted);
					event.setCurrentItem(stack.getAmount() > 0 ? stack: null);
				}
			} else {
				shouldCancel = !owner.onPlayerInventoryClick(event.getSlot(), event.getClick(), event.getCurrentItem(), event.getCursor());
			}
		} else {
			// clicking outside the inventory entirely
			shouldCancel = !owner.onClickOutside();
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
				shouldCancel = !owner.onSlotClick(slot, ClickType.LEFT, inventory.getItem(slot), event.getOldCursor());
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
				return owner.onShiftClickExtract(event.getRawSlot(), event.getCurrentItem());
			case PLACE_ONE: case PLACE_ALL: case PLACE_SOME: case SWAP_WITH_CURSOR:
			case PICKUP_ALL: case PICKUP_HALF: case PICKUP_ONE: case PICKUP_SOME:
				return owner.onSlotClick(event.getRawSlot(), event.getClick(), event.getCurrentItem(), event.getCursor());
			default:
				return false;
		}
	}

	public void receiveEvent(InventoryCloseEvent event) {
		owner.onGUIClosed(event);
		System.out.println("passed inventory close event to " + owner);
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
		if (!frozen.isEmpty() && slots.length > 0) {
			try {
				Inventory tmpInv = BukkitSerialization.fromBase64(frozen);
				for (int i = 0; i < slots.length; i++) {
					inventory.setItem(slots[i], tmpInv.getItem(i));
				}

			} catch (IOException e) {
				LogUtils.severe("can't restore inventory for " + getOwner().getItemName());
			}
		}
	}

	public void ejectItems(int... slots) {
		Location loc = getOwner().getLocation();
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
		public void onGUIClosed(InventoryCloseEvent event);
	}

	public enum SlotType {
		BACKGROUND,
//		LABEL,
		ITEM,
		GADGET
	}
}
