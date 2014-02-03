package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.api.STBMachine;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.blocks.machines.gui.ChargeMeter;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.energycells.EnergyCell;
import me.desht.sensibletoolbox.items.machineupgrades.EjectorUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.MachineUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.SpeedUpgrade;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.Range;
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

import java.io.IOException;

public abstract class BaseSTBMachine extends BaseSTBBlock implements STBMachine {
	public static final ItemStack INPUT_TEXTURE = new ItemStack(Material.ENDER_PORTAL);
	public static final ItemStack OUTPUT_TEXTURE = new ItemStack(Material.STATIONARY_LAVA);
	public static final ItemStack BG_TEXTURE = new ItemStack(Material.STATIONARY_WATER);
	private static final ItemStack ENERGY_TEXTURE = new ItemStack(Material.ENDER_PORTAL);
	public static final ItemStack UPGRADE_TEXTURE = new ItemStack(Material.ENDER_PORTAL);
	private static final long CHARGE_INTERVAL = 10;
	private final Inventory inventory;
	private final ChargeMeter chargeMeter;
	private double charge;
	private RedstoneBehaviour redstoneBehaviour;
	private AccessControl accessControl;
	private ChargeDirection chargeDirection;
	private boolean jammed;  // true if no space in output slots for processing result
	private EnergyCell installedCell;
	private double speedMultiplier;
	private double powerMultiplier;
	private BlockFace autoEjectDirection;
	private final Range slotRange;
	private boolean needToProcessUpgrades;

	static {
		setDisplayName(INPUT_TEXTURE, ChatColor.YELLOW + "Input");
		setDisplayName(OUTPUT_TEXTURE, ChatColor.YELLOW + "Output");
		setDisplayName(ENERGY_TEXTURE, ChatColor.YELLOW + "Energy Cell");
		setDisplayName(UPGRADE_TEXTURE, ChatColor.YELLOW + "Upgrades");
		setDisplayName(BG_TEXTURE, " ");
	}

	private static void setDisplayName(ItemStack stack, String disp) {
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(disp);
		stack.setItemMeta(meta);
	}

	protected BaseSTBMachine() {
		charge = 0;
		redstoneBehaviour = RedstoneBehaviour.IGNORE;
		accessControl = AccessControl.PUBLIC;
		chargeDirection = ChargeDirection.MACHINE;
		slotRange = new IntRange(0, getInventorySize() - 1);
		inventory = createMachineInventory();
		jammed = false;
		chargeMeter = new ChargeMeter(this, getChargeIndicatorSlot());
		autoEjectDirection = null;
		needToProcessUpgrades = false;
	}

	public BaseSTBMachine(ConfigurationSection conf) {
		charge = conf.getInt("charge");
		redstoneBehaviour = RedstoneBehaviour.valueOf(conf.getString("redstoneBehaviour", "IGNORE"));
		accessControl = AccessControl.valueOf(conf.getString("accessControl", "PUBLIC"));
		chargeDirection = ChargeDirection.valueOf(conf.getString("chargeDirection", "MACHINE"));
		slotRange = new IntRange(0, getInventorySize() - 1);
		inventory = createMachineInventory();
		thawSlots(conf.getString("inputSlots", ""), getInputSlots());
		thawSlots(conf.getString("outputSlots", ""), getOutputSlots());
		thawSlots(conf.getString("upgradeSlots", ""), getUpgradeSlots());
		processUpgrades();
		jammed = false;
		needToProcessUpgrades = false;
		chargeMeter = new ChargeMeter(this, getChargeIndicatorSlot());
		if (conf.contains("energyCell") && getEnergyCellSlot() >= 0) {
			EnergyCell cell = (EnergyCell) BaseSTBItem.getItemById(conf.getString("energyCell"));
			cell.setCharge(conf.getDouble("energyCellCharge", 0.0));
			inventory.setItem(getEnergyCellSlot(), cell.toItemStack(1));
			installEnergyCell(cell);
		}
	}
	public abstract int[] getInputSlots();
	public abstract int[] getOutputSlots();
	public abstract int[] getUpgradeSlots();
	public abstract int getUpgradeLabelSlot();

	@Override
	public boolean isJammed() {
		return jammed;
	}

	public void setJammed(boolean jammed) {
		this.jammed = jammed;
	}

	public double getSpeedMultiplier() {
		return speedMultiplier;
	}

	public void setSpeedMultiplier(double speedMultiplier) {
		this.speedMultiplier = speedMultiplier;
	}

	public double getPowerMultiplier() {
		return powerMultiplier;
	}

	public void setPowerMultiplier(double powerMultiplier) {
		this.powerMultiplier = powerMultiplier;
	}

	public ChargeDirection getChargeDirection() {
		return chargeDirection;
	}

	public void setChargeDirection(ChargeDirection chargeDirection) {
		this.chargeDirection = chargeDirection;
	}

	@Override
	public RedstoneBehaviour getRedstoneBehaviour() {
		return redstoneBehaviour;
	}

	public void setRedstoneBehaviour(RedstoneBehaviour redstoneBehaviour) {
		this.redstoneBehaviour = redstoneBehaviour;
	}

	@Override
	public AccessControl getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(AccessControl accessControl) {
		this.accessControl = accessControl;
	}

	public void setAutoEjectDirection(BlockFace direction) {
		autoEjectDirection = direction;
	}

	public BlockFace getAutoEjectDirection() {
		return autoEjectDirection;
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("charge", charge);
		conf.set("accessControl", getAccessControl().toString());
		conf.set("redstoneBehaviour", getRedstoneBehaviour().toString());
		conf.set("chargeDirection", getChargeDirection().toString());
		conf.set("inputSlots", freezeSlots(getInputSlots()));
		conf.set("outputSlots", freezeSlots(getOutputSlots()));
		conf.set("upgradeSlots", freezeSlots(getUpgradeSlots()));
		if (installedCell != null) {
			conf.set("energyCell", installedCell.getItemID());
			conf.set("energyCellCharge", installedCell.getCharge());
		}
		return conf;
	}

	protected String freezeSlots(int... slots) {
		int invSize = STBUtil.roundUp(slots.length, 9);
		Inventory tmpInv = Bukkit.createInventory(null, invSize);
		for (int i = 0; i < slots.length; i++) {
			tmpInv.setItem(i, inventory.getItem(slots[i]));
		}
		return BukkitSerialization.toBase64(tmpInv, slots.length);
	}

	protected void thawSlots(String frozen, int... slots) {
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
		chargeMeter.repaintNeeded();
		if (charge <= 0 && getLocation() != null) {
			getLocation().getWorld().playSound(getLocation(), Sound.BLAZE_DEATH, 1.0f, 0.25f);
		}
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	private Inventory createMachineInventory() {
		Inventory inv = Bukkit.createInventory(this, getInventorySize(), ChatColor.DARK_BLUE + getItemName());

		for (int slot = 0; slot < inv.getSize(); slot++) {
			inv.setItem(slot, BG_TEXTURE);
		}
		paintSlotSurround(inv, getInputSlots(), INPUT_TEXTURE);
		paintSlotSurround(inv, getOutputSlots(), OUTPUT_TEXTURE);
		for (int slot : getInputSlots()) {
			inv.setItem(slot, null);
		}
		for (int slot : getOutputSlots()) {
			inv.setItem(slot, null);
		}
		for (int slot : getUpgradeSlots()) {
			inv.setItem(slot, null);
		}
		paintSlot(inv, getUpgradeLabelSlot(), UPGRADE_TEXTURE, true);
		paintSlot(inv, getRedstoneBehaviourSlot(), getRedstoneBehaviour().getTexture(), true);
		paintSlot(inv, getAccessControlSlot(), getAccessControl().getTexture(), true);
		paintSlot(inv, getEnergyCellSlot(), null, true);
		paintSlot(inv, getChargeDirectionSlot(), getChargeDirection().getTexture(), true);
		return inv;
	}

	private void paintSlotSurround(Inventory inv, int[] slots, ItemStack colour) {
		for (int slot : slots) {
			int row = slot / 9, col = slot % 9;
			for (int i = row - 1; i <= row + 1; i++) {
				for (int j = col - 1; j <= col + 1; j++) {
					paintSlot(inv, i, j, colour, true);
				}
			}
		}
	}

	private void paintSlot(Inventory inv, int row, int col, ItemStack item, boolean overwrite) {
		paintSlot(inv, row * 9 + col, item, overwrite);
	}

	private void paintSlot(Inventory inv, int slot, ItemStack item, boolean overwrite) {
		if (slotRange.containsInteger(slot)) {
			if (overwrite || inv.getItem(slot) == null) {
				inv.setItem(slot, item);
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
		return 26;  // just below access control slot
	}

	protected int getEnergyCellSlot() {
		return -1; // no energy cell by default
	}

	protected int getChargeDirectionSlot() {
		return -1;
	}

	protected int getInventorySize() {
		return 54;
	}

	@Override
	public void onInteractBlock(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (getInventory().getViewers().isEmpty()) {
				// no one's already looking at this inventory/gui, so ensure it's up to date
				System.out.println("refresh inventory/gui for " + this);
				chargeMeter.doRepaint();
				if (installedCell != null) {
					getInventory().setItem(getEnergyCellSlot(), installedCell.toItemStack(1));
				}
			}
			event.getPlayer().openInventory(getInventory());
			event.setCancelled(true);
		}
		super.onInteractBlock(event);
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
			if (installedCell != null) {
				getLocation().getWorld().dropItemNaturally(getLocation(), installedCell.toItemStack(1));
				installedCell = null;
			}
		}
		super.setLocation(loc);
	}

	/**
	 * Find a candidate slot for item insertion; this will look for an empty slot, or a slot containing the
	 * same kind of item as the candidate item.  It will NOT check item amounts (see #insertItem() for that)
	 *
	 * @param item the candidate item to insert
	 * @param side the side being inserted from (SELF is valid option here too)
	 * @return the slot number if a slot is available, or -1 otherwise
	 */
	protected int findSlotForItemInsertion(ItemStack item, BlockFace side) {
		for (int slot : getInputSlots()) {
			ItemStack inSlot = getInventory().getItem(slot);
			if (inSlot == null || inSlot.isSimilar(item)) {
				return slot;
			} else if (inSlot.isSimilar(item)) {
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

	@Override
	public boolean acceptsItemType(ItemStack item) {
		return true;
	}

	@Override
	public boolean isInputSlot(int slot) {
		return isSlotIn(slot, getInputSlots());
	}

	@Override
	public boolean isOutputSlot(int slot) {
		return isSlotIn(slot, getOutputSlots());
	}

	@Override
	public boolean isUpgradeSlot(int slot) {
		return isSlotIn(slot, getUpgradeSlots());
	}

	private boolean isSlotIn(int slot, int[] slots) {
		for (int s1 : slots) {
			if (s1 == slot) {
				return true;
			}
		}
		return false;
	}

//	}

	@Override
	public int insertItems(ItemStack toInsert, BlockFace side) {
		int slot = findSlotForItemInsertion(toInsert, side);
		int nInserted = 0;
		if (slot >= 0 && acceptsItemType(toInsert)) {
			ItemStack inMachine = getInventory().getItem(slot);
			if (inMachine == null) {
				nInserted = toInsert.getAmount();
				getInventory().setItem(slot, toInsert);
			} else {
				nInserted = Math.min(toInsert.getAmount(), inMachine.getType().getMaxStackSize() - inMachine.getAmount());
				if (nInserted > 0) {
					inMachine.setAmount(inMachine.getAmount() + nInserted);
					getInventory().setItem(slot, inMachine);
					updateBlock(false);
				}
			}
			System.out.println("inserted " + nInserted + " out of " + STBUtil.describeItemStack(toInsert) + " into " + this);
		}
		return nInserted;
	}

	@Override
	public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount) {
		int[] slots = getOutputSlots();
		int max = slots == null ? getInventory().getSize() : slots.length;
		for (int i = 0; i < max; i++) {
			int slot = slots == null ? i : slots[i];
			ItemStack stack = getInventory().getItem(slot);
			if (stack != null) {
				if (receiver == null || stack.isSimilar(receiver)) {
					int toTake = Math.min(amount, stack.getAmount());
					if (receiver != null) {
						toTake = Math.min(toTake, receiver.getType().getMaxStackSize() - receiver.getAmount());
					}
					if (toTake > 0) {
						ItemStack result  = stack.clone();
						result.setAmount(toTake);
						if (receiver != null) {
							receiver.setAmount(receiver.getAmount() + toTake);
						}
						stack.setAmount(stack.getAmount() - toTake);
						getInventory().setItem(slot, stack.getAmount() > 0 ? stack : null);
						updateBlock(false);
						System.out.println("extracted " + STBUtil.describeItemStack(result) + " from " + this);

						return result;
					}
				}
			}
		}
		return null;
	}

	public void handleInventoryClick(InventoryClickEvent event) {
		int slot = event.getRawSlot();
		boolean input = isInputSlot(slot);
		boolean output = isOutputSlot(slot);
		boolean upgrade = isUpgradeSlot(slot);
		if (slot < inventory.getSize() && !input && !output && !upgrade && slot != getEnergyCellSlot()) {
			event.setCancelled(true);
		}
		if (input) {
			if (event.getCursor().getType() != Material.AIR && !acceptsItemType(event.getCursor())) {
				System.out.println("cancel place into input slot " + slot);
				event.setCancelled(true);
			}
		} else if (output) {
			if (event.getCursor().getType() != Material.AIR) {
				System.out.println("cancel place into output slot " + slot);
				event.setCancelled(true);
			}
		} else if (upgrade) {
			if (event.getCursor().getType() != Material.AIR) {
				MachineUpgrade mu = BaseSTBItem.getItemFromItemStack(event.getCursor(), MachineUpgrade.class);
				if (mu == null) {
					event.setCancelled(true);
				}
			}
			needToProcessUpgrades = true;
		} else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getRawSlot() > inventory.getSize()) {
			// trying to shift-click item from player inventory into the machine
			handleShiftClickIntoMachine(event);
		} else if (slot == getEnergyCellSlot()) {
			if (event.getCursor().getType() != Material.AIR) {
				EnergyCell cell = BaseSTBItem.getItemFromItemStack(event.getCursor(), EnergyCell.class);
				if (cell != null) {
					System.out.println("put cell in machine");
					installEnergyCell(cell);
				} else {
					event.setCancelled(true);
				}
			} else if (event.getCurrentItem() != null) {
				installEnergyCell(null);
			}
		}

		if (slot == getRedstoneBehaviourSlot()) {
			int n = (redstoneBehaviour.ordinal() + 1) % RedstoneBehaviour.values().length;
			setRedstoneBehaviour(RedstoneBehaviour.values()[n]);
			event.setCurrentItem(redstoneBehaviour.getTexture());
			updateBlock(false);
		} else if (slot == getAccessControlSlot()) {
			int n = (accessControl.ordinal() + 1) % AccessControl.values().length;
			setAccessControl(AccessControl.values()[n]);
			event.setCurrentItem(accessControl.getTexture());
			updateBlock(false);
		} else if (slot == getChargeDirectionSlot()) {
			int n = (chargeDirection.ordinal() + 1) % ChargeDirection.values().length;
			setChargeDirection(ChargeDirection.values()[n]);
			event.setCurrentItem(chargeDirection.getTexture());
			updateBlock(false);
		} else if (!event.isCancelled()) {
			// something must have been inserted or extracted
			if (output) {
				setJammed(false);
			}
			updateBlock(false);
		}
	}

	private void handleShiftClickIntoMachine(InventoryClickEvent event) {
		event.setCancelled(true);
		ItemStack currentItem = event.getCurrentItem();
		int insertionSlot = findSlotForItemInsertion(currentItem, BlockFace.SELF);
		if (insertionSlot >= 0 && acceptsItemType(currentItem)) {
			ItemStack inMachine = inventory.getItem(insertionSlot);
			if (inMachine == null) {
				inventory.setItem(insertionSlot, currentItem);
				event.setCurrentItem(null);
			} else {
				int toInsert = Math.min(inMachine.getType().getMaxStackSize() - inMachine.getAmount(), currentItem.getAmount());
				inMachine.setAmount(inMachine.getAmount() + toInsert);
				inventory.setItem(insertionSlot, inMachine);
				currentItem.setAmount(currentItem.getAmount() - toInsert);
				event.setCurrentItem(currentItem.getAmount() > 0 ? currentItem : null);
			}
		} else if (getEnergyCellSlot() >= 0 && installedCell == null) {
			// maybe shift-clicking an energy cell?
			EnergyCell cell = BaseSTBItem.getItemFromItemStack(currentItem, EnergyCell.class);
			if (cell != null) {
				System.out.println("shift click energy cell into machine");
				installEnergyCell(cell);
				event.setCurrentItem(null);
				inventory.setItem(getEnergyCellSlot(), installedCell.toItemStack(1));
			}
		} else if (getUpgradeSlots().length > 0) {
			// shift-clicking an upgrade?
			MachineUpgrade mu = BaseSTBItem.getItemFromItemStack(currentItem, MachineUpgrade.class);
			if (mu != null) {
				int upgradeSlot = findUpgradeSlot(currentItem);
				if (upgradeSlot >= 0) {
					inventory.setItem(upgradeSlot, currentItem);
					event.setCurrentItem(null);
					needToProcessUpgrades = true;
				}
			}
		}
	}

	private int findUpgradeSlot(ItemStack upgrade) {
		for (int slot : getUpgradeSlots()) {
			ItemStack inSlot = inventory.getItem(slot);
			if (inSlot == null || inSlot.isSimilar(upgrade) && inSlot.getAmount() + upgrade.getAmount() <= upgrade.getType().getMaxStackSize()) {
				return slot;
			}
		}
		return -1;
	}

	private void processUpgrades() {
		int nSpeed = 0;
		BlockFace ejectDirection = null;
		for (int slot : getUpgradeSlots()) {
			ItemStack stack = getInventory().getItem(slot);
			if (stack != null) {
				MachineUpgrade mu = BaseSTBItem.getItemFromItemStack(stack, MachineUpgrade.class);
				if (mu == null) {
					getInventory().setItem(slot, null);
					getLocation().getWorld().dropItemNaturally(getLocation(), stack);
				} else {
					if (mu instanceof SpeedUpgrade) {
						nSpeed += stack.getAmount();
					} else if (mu instanceof EjectorUpgrade) {
						ejectDirection = ((EjectorUpgrade) mu).getDirection();
					}
				}
			}
		}
		setSpeedMultiplier(Math.pow(1.4, nSpeed));
		setPowerMultiplier(Math.pow(1.6, nSpeed));
		setAutoEjectDirection(ejectDirection);
		System.out.println("upgrades for " + this + " speed=" + getSpeedMultiplier() + " power=" + getPowerMultiplier() + " eject=" + getAutoEjectDirection());
	}

	public void installEnergyCell(EnergyCell cell) {
		installedCell = cell;
		System.out.println("installed energy cell: " + cell);
		updateBlock();
	}

	public void handleInventoryDrag(InventoryDragEvent event) {
		boolean inMachine = false;
		for (int slot : event.getRawSlots()) {
			if (slot < inventory.getSize()) {
				inMachine = true;
				break;
			}
		}
		if (inMachine && event.getRawSlots().size() != 1) {
			event.setCancelled(true);
			return;
		}
		if (!inMachine) {
			return;
		}
		int slot = (event.getRawSlots().toArray(new Integer[1]))[0];
		if (slot == getEnergyCellSlot()) {
			EnergyCell cell = BaseSTBItem.getItemFromItemStack(event.getOldCursor(), EnergyCell.class);
			if (cell != null) {
				installEnergyCell(cell);
			} else {
				event.setCancelled(true);
			}
		} else if (isUpgradeSlot(slot)) {
			MachineUpgrade upgrade = BaseSTBItem.getItemFromItemStack(event.getOldCursor(), MachineUpgrade.class);
			if (upgrade != null) {
				needToProcessUpgrades = true;
			} else {
				event.setCancelled(true);
			}
		} else if (!isInputSlot(slot) || !acceptsItemType(event.getOldCursor())) {
			event.setCancelled(true);
		}
	}

	public void handleInventoryInsertion(InventoryMoveItemEvent event) {
		event.setCancelled(true);
	}

	public void handleInventoryExtraction(InventoryMoveItemEvent event) {
		// TODO : pull from the output slots
		event.setCancelled(true);
	}

	@Override
	public void onServerTick() {
		if (getLocation().getWorld().getFullTime() % CHARGE_INTERVAL == 0) {
			double transferred = 0.0;
			if (installedCell != null) {
				switch (chargeDirection) {
					case MACHINE: transferred = transferCharge(installedCell, this); break;
					case CELL: transferred = transferCharge(this, installedCell); break;
				}
			}
			if (!getInventory().getViewers().isEmpty()) {
				if (transferred > 0.0) {
					getInventory().setItem(getEnergyCellSlot(), installedCell.toItemStack(1));
				}
				chargeMeter.doRepaint();
			}
		}
		if (needToProcessUpgrades) {
			processUpgrades();
			needToProcessUpgrades = false;
		}
	}

	private double transferCharge(Chargeable from, Chargeable to) {
		if (to.getCharge() >= to.getMaxCharge() || from.getCharge() == 0) {
			return 0;
		}
		double toTransfer = Math.min(from.getChargeRate() * CHARGE_INTERVAL, to.getMaxCharge() - to.getCharge());
		toTransfer = Math.min(to.getChargeRate() * CHARGE_INTERVAL, toTransfer);
		toTransfer = Math.min(from.getCharge(), toTransfer);
		to.setCharge(to.getCharge() + toTransfer);
		from.setCharge(from.getCharge() - toTransfer);
//		System.out.println("transfer " + toTransfer + " charge from " + from + " to " + to + " from now=" + from.getCharge() + " to now=" + to.getCharge());
		return toTransfer;
	}

	protected boolean isActive() {
		switch (redstoneBehaviour) {
			case IGNORE: return true;
			case HIGH: return getLocation().getBlock().isBlockIndirectlyPowered();
			case LOW: return !getLocation().getBlock().isBlockIndirectlyPowered();
			default: return false;
		}
	}
}
