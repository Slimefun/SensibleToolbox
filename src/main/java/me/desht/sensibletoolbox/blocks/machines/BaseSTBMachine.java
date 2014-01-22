package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.blocks.machines.gui.ChargeMeter;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.io.IOException;

public abstract class BaseSTBMachine extends BaseSTBBlock implements Chargeable, STBInventoryHolder {
	public static final ItemStack INPUT_TEXTURE = new ItemStack(Material.ENDER_PORTAL);
	public static final ItemStack OUTPUT_TEXTURE = new ItemStack(Material.STATIONARY_LAVA);
	public static final ItemStack BG_TEXTURE = new ItemStack(Material.STATIONARY_WATER);
	private final Inventory inventory;
	private final ChargeMeter chargeMeter;
	private double charge;
	private RedstoneBehaviour redstoneBehaviour;
	private AccessControl accessControl;
	private boolean jammed;  // true if no space in output slots for processing result

	static {
		setDisplayName(INPUT_TEXTURE, ChatColor.YELLOW + "Input");
		setDisplayName(OUTPUT_TEXTURE, ChatColor.YELLOW + "Output");
		setDisplayName(BG_TEXTURE, " ");
	}

	protected BaseSTBMachine() {
		charge = 0;
		redstoneBehaviour = RedstoneBehaviour.IGNORE;
		accessControl = AccessControl.PUBLIC;
		inventory = createMachineInventory();
		jammed = false;
		chargeMeter = new ChargeMeter(this, getChargeIndicatorSlot());
	}

	public BaseSTBMachine(ConfigurationSection conf) {
		charge = conf.getInt("charge");
		redstoneBehaviour = RedstoneBehaviour.valueOf(conf.getString("redstoneBehaviour", "IGNORE"));
		accessControl = AccessControl.valueOf(conf.getString("accessControl", "PUBLIC"));
		inventory = createMachineInventory();
		thawSlots(conf.getString("inputSlots", ""), getInputSlots());
		thawSlots(conf.getString("outputSlots", ""), getOutputSlots());
		jammed = false;
		chargeMeter = new ChargeMeter(this, getChargeIndicatorSlot());
	}

	private static void setDisplayName(ItemStack stack, String disp) {
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(disp);
		stack.setItemMeta(meta);
	}

	public boolean isJammed() {
		return jammed;
	}

	public void setJammed(boolean jammed) {
		this.jammed = jammed;
	}

	public RedstoneBehaviour getRedstoneBehaviour() {
		return redstoneBehaviour;
	}

	public void setRedstoneBehaviour(RedstoneBehaviour redstoneBehaviour) {
		this.redstoneBehaviour = redstoneBehaviour;
	}

	public AccessControl getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(AccessControl accessControl) {
		this.accessControl = accessControl;
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("charge", charge);
		conf.set("accessControl", accessControl.toString());
		conf.set("redstoneBehaviour", redstoneBehaviour.toString());
		conf.set("inputSlots", freezeSlots(getInputSlots()));
		conf.set("outputSlots", freezeSlots(getOutputSlots()));
		return conf;
	}

	private String freezeSlots(int[] slots) {
		int invSize = slots.length + (9 - slots.length % 9);
		Inventory tmpInv = Bukkit.createInventory(null, invSize);
		for (int i = 0; i < slots.length; i++) {
			tmpInv.setItem(i, inventory.getItem(slots[i]));
		}
		return BukkitSerialization.toBase64(tmpInv);
	}

	private void thawSlots(String frozen, int[] slots) {
		if (!frozen.isEmpty() && slots.length > 0) {
			try {
				Inventory tmpInv = BukkitSerialization.fromBase64(frozen);
				for (int i = 0; i < slots.length; i++) {
					inventory.setItem(slots[i], tmpInv.getItem(i));
				}

			} catch (IOException e) {
				LogUtils.severe("can't restore inventory for " + getItemName());
			}
		}
	}

	@Override
	public double getCharge() {
		return charge;
	}

	@Override
	public void setCharge(double charge) {
		this.charge = charge;
		chargeMeter.scheduleRepaint();
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	private Inventory createMachineInventory() {
		Inventory inv = Bukkit.createInventory(this, getInventorySize(), ChatColor.DARK_BLUE + getItemName());

		paintSlots(inv, getInputSlots(), INPUT_TEXTURE);
		paintSlots(inv, getOutputSlots(), OUTPUT_TEXTURE);
		for (int i = 0; i < inv.getSize(); i++) {
			paintSlot(inv, i, BG_TEXTURE, false);
		}
		for (int slot : getInputSlots()) {
			inv.setItem(slot, new ItemStack(Material.AIR));
		}
		for (int slot : getOutputSlots()) {
			inv.setItem(slot, new ItemStack(Material.AIR));
		}

		if (getRedstoneBehaviourSlot() >= 0 && getRedstoneBehaviourSlot() < inv.getSize()) {
			inv.setItem(getRedstoneBehaviourSlot(), redstoneBehaviour.getTexture());
		}
		if (getAccessControlSlot() >= 0 && getAccessControlSlot() < inv.getSize()) {
			inv.setItem(getAccessControlSlot(), accessControl.getTexture());
		}
		return inv;
	}

	private void paintSlots(Inventory inv, int[] slots, ItemStack colour) {
		for (int slot : slots) {
			int row = slot / 9, col = slot % 9;
			for (int i = row - 1; i <= row + 1; i++) {
				for (int j = col - 1; j <= col + 1; j++) {
					paintSlot(inv, i, j, colour, false);
				}
			}
		}
	}

	protected int getRedstoneBehaviourSlot() {
		return 8;  // top right
	}

	protected int getAccessControlSlot() {
		return 17;  // just below top right
	}

	protected int getChargeIndicatorSlot() {
		return 26;
	}

	protected int getInventorySize() {
		return 54;
	}


	private void paintSlot(Inventory inv, int row, int col, ItemStack item, boolean overwrite) {
		paintSlot(inv, row * 9 + col, item, overwrite);
	}

	private void paintSlot(Inventory inv, int slot, ItemStack item, boolean overwrite) {
		if (slot < inv.getSize()) {
			if (inv.getItem(slot) == null || overwrite) {
				inv.setItem(slot, item);
			}
		}
	}

//	@Override
//	public void onChunkUnload() {
//		inventory = null;
//	}

	@Override
	public void onInteractBlock(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Inventory inv = getInventory();
			chargeMeter.doRepaint();
			System.out.println("inventory currently has " + inv.getViewers().size() + " viewers");
			event.getPlayer().openInventory(inv);
			event.setCancelled(true);
		} else {
			super.onInteractBlock(event);
		}
	}

	@Override
	public void setLocation(Location loc) {
		if (loc == null) {
			for (int slot : getInputSlots()) {
				ItemStack stack = inventory.getItem(slot);
				if (stack != null) {
					getLocation().getWorld().dropItemNaturally(getLocation(), stack);
					inventory.setItem(slot, null);
				}
			}
			for (int slot : getOutputSlots()) {
				ItemStack stack = inventory.getItem(slot);
				if (stack != null) {
					getLocation().getWorld().dropItemNaturally(getLocation(), stack);
					inventory.setItem(slot, null);
				}
			}
		}
		super.setLocation(loc);
	}

	@Override
	public int findSlotForItemInsertion(ItemStack item, BlockFace side) {
		for (int slot : getInputSlots()) {
			ItemStack inSlot = getInventory().getItem(slot);
			if (inSlot == null) {
				return slot;
			} else if (inSlot.isSimilar(item) && inSlot.getAmount() + item.getAmount() <= item.getType().getMaxStackSize() ) {
				return slot;
			}
		}
		return -1;
	}

	protected int findOutputSlot(ItemStack item) {
		for (int slot : getOutputSlots()) {
			ItemStack outSlot = getInventory().getItem(slot);
			if (outSlot == null) {
				return slot;
			} else if (outSlot.isSimilar(item) && outSlot.getAmount() + item.getAmount() <= item.getType().getMaxStackSize() ) {
				return slot;
			}
		}
		return -1;
	}

	/**
	 * Check if the given item would be accepted as input to this machine.  By default, every item type
	 * is accepted, but this can be overridden in subclasses.
	 *
	 * @param item the item to check
	 * @return true if the item is accepted, false otherwise
	 */
	public boolean acceptsItemType(ItemStack item) {
		return true;
	}

	public boolean isInputSlot(int slot) {
		for (int s1 : getInputSlots()) {
			if (s1 == slot) {
				return true;
			}
		}
		return false;
	}

	public boolean isOutputSlot(int slot) {
		for (int s1 : getOutputSlots()) {
			if (s1 == slot) {
				return true;
			}
		}
		return false;
	}

	public void handleInventoryClick(InventoryClickEvent event) {
		int slot = event.getRawSlot();
		boolean input = isInputSlot(slot);
		boolean output = isOutputSlot(slot);
		if (slot < inventory.getSize() && !input && !output) {
			System.out.println("cancel non I/O slot " + slot);
			event.setCancelled(true);
		}
		if (input) {
			if (event.getCursor().getType() != Material.AIR) {
				if (!acceptsItemType(event.getCursor())) {
					System.out.println("cancel place into input slot " + slot);
					event.setCancelled(true);
				}
			}
		} else if (output) {
			if (event.getCursor().getType() != Material.AIR) {
				System.out.println("cancel place into output slot " + slot);
				event.setCancelled(true);
			}
		} else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getRawSlot() > inventory.getSize()) {
			// try to shift-click item into input slot
			event.setCancelled(true);
			System.out.println("cancel shift-click from slot " + slot);
			int insertionSlot = findSlotForItemInsertion(event.getCurrentItem(), BlockFace.SELF);
			if (insertionSlot >= 0 && acceptsItemType(event.getCurrentItem())) {
				System.out.println(" - but insert into input slot " + insertionSlot);
				ItemStack stack = inventory.getItem(insertionSlot);
				if (stack == null) {
					inventory.setItem(insertionSlot, event.getCurrentItem());
				} else {
					stack.setAmount(stack.getAmount() + event.getCurrentItem().getAmount());
					inventory.setItem(insertionSlot, stack);
				}
				event.setCurrentItem(null);
			}
		}

		if (event.getSlot() == getRedstoneBehaviourSlot()) {
			int n = (redstoneBehaviour.ordinal() + 1) % RedstoneBehaviour.values().length;
			redstoneBehaviour = RedstoneBehaviour.values()[n];
			event.setCurrentItem(redstoneBehaviour.getTexture());
			updateBlock(false);
		} else if (event.getSlot() == getAccessControlSlot()) {
			int n = (accessControl.ordinal() + 1) % AccessControl.values().length;
			accessControl = AccessControl.values()[n];
			event.setCurrentItem(accessControl.getTexture());
			updateBlock(false);
		} else if (!event.isCancelled()) {
			// something must have been inserted or extracted
			if (isOutputSlot(event.getRawSlot())) {
				setJammed(false);
			}
			updateBlock(false);
		}
	}

	public void handleInventoryDrag(InventoryDragEvent event) {
		for (int slot : event.getRawSlots()) {
			if (slot < inventory.getSize()) {
				event.setCancelled(true);
				break;
			}
		}
	}

	public void handleInventoryInsertion(InventoryMoveItemEvent event) {
		event.setCancelled(true);
	}

	public void handleInventoryExtraction(InventoryMoveItemEvent event) {
		// TODO : pull from the output slots
		event.setCancelled(true);
	}

	protected boolean isActive() {
		switch (redstoneBehaviour) {
			case IGNORE: return true;
			case HIGH: return getLocation().getBlock().isBlockIndirectlyPowered();
			case LOW: return !getLocation().getBlock().isBlockIndirectlyPowered();
			default: return false;
		}
	}

	public enum RedstoneBehaviour {
		IGNORE(Material.SULPHUR, "Ignore Redstone"),
		HIGH(Material.REDSTONE, "Require Signal"),
		LOW(Material.GLOWSTONE_DUST, "Require No Signal");
		private final Material material;
		private final String label;

		RedstoneBehaviour(Material mat, String label) {
			this.material = mat;
			this.label = label;
		}

		public ItemStack getTexture() {
			ItemStack res = new ItemStack(material);
			ItemMeta meta = res.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + label);
			res.setItemMeta(meta);
			return res;
		}
	}

	public enum AccessControl {
		PUBLIC(DyeColor.GREEN, "Public Access"),
		PRIVATE(DyeColor.RED, "Owner Only Access");
		private final String label;
		private final DyeColor color;

		AccessControl(DyeColor color, String label) {
			this.color = color;
			this.label = label;
		}

		public ItemStack getTexture() {
			ItemStack res = new Wool(color).toItemStack(1);
			ItemMeta meta = res.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + label);
			res.setItemMeta(meta);
			return res;
		}
	}
}
