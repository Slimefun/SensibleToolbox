package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.api.STBMachine;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.blocks.machines.gui.*;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.energycells.EnergyCell;
import me.desht.sensibletoolbox.items.machineupgrades.EjectorUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.MachineUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.SpeedUpgrade;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseSTBMachine extends BaseSTBBlock implements STBMachine {
	private static final long CHARGE_INTERVAL = 10;
	private double charge;
	private ChargeDirection chargeDirection;
	private boolean jammed;  // true if no space in output slots for processing result
	private EnergyCell installedCell;
	private double speedMultiplier;
	private double powerMultiplier;
	private BlockFace autoEjectDirection;
	private boolean needToProcessUpgrades;
	private int chargeMeterId;
	private String frozenInput, frozenOutput; //, frozenUpgrades;
	private final List<MachineUpgrade> upgrades = new ArrayList<MachineUpgrade>();

	protected BaseSTBMachine() {
		super();
		charge = 0;
		chargeDirection = ChargeDirection.MACHINE;
		jammed = false;
		autoEjectDirection = null;
		needToProcessUpgrades = false;
	}

	public BaseSTBMachine(ConfigurationSection conf) {
		super(conf);
		charge = conf.getInt("charge");
		chargeDirection = ChargeDirection.valueOf(conf.getString("chargeDirection", "MACHINE"));
		jammed = false;
		if (conf.contains("energyCell") && getEnergyCellSlot() >= 0) {
			EnergyCell cell = (EnergyCell) BaseSTBItem.getItemById(conf.getString("energyCell"));
			cell.setCharge(conf.getDouble("energyCellCharge", 0.0));
			installEnergyCell(cell);
		}
		if (conf.contains("upgrades")) {
			for (String l : conf.getStringList("upgrades")) {
				String[] f = l.split("::", 2);
				try {
					YamlConfiguration upgConf = new YamlConfiguration();
					if (f.length > 1) {
						upgConf.loadFromString(f[1]);
					}
					MachineUpgrade upgrade = (MachineUpgrade) BaseSTBItem.getItemById(f[0], upgConf);
					upgrades.add(upgrade);
				} catch (Exception e) {
					LogUtils.warning("can't restore saved module " + f[0] + " for " + this + ": " + e.getMessage());
				}
			}
		}
		needToProcessUpgrades = true;
		frozenInput = conf.getString("inputSlots");
		frozenOutput = conf.getString("outputSlots");
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("charge", charge);
		conf.set("accessControl", getAccessControl().toString());
		conf.set("redstoneBehaviour", getRedstoneBehaviour().toString());
		conf.set("chargeDirection", getChargeDirection().toString());
		List<String> upg = new ArrayList<String>();
		for (MachineUpgrade upgrade : upgrades) {
			upg.add(upgrade.getItemID() + "::" + upgrade.freeze().saveToString());
		}
		conf.set("upgrades", upg);

		if (getGUI() != null) {
			conf.set("inputSlots", getGUI().freezeSlots(getInputSlots()));
			conf.set("outputSlots", getGUI().freezeSlots(getOutputSlots()));
		}
		if (installedCell != null) {
			conf.set("energyCell", installedCell.getItemID());
			conf.set("energyCellCharge", installedCell.getCharge());
		}
		return conf;
	}

	public abstract int[] getInputSlots();
	public abstract int[] getOutputSlots();
	public abstract int[] getUpgradeSlots();
	public abstract int getUpgradeLabelSlot();
	protected abstract void playActiveParticleEffect();

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

	@Override
	public ChargeDirection getChargeDirection() {
		return chargeDirection;
	}

	@Override
	public void setChargeDirection(ChargeDirection chargeDirection) {
		this.chargeDirection = chargeDirection;
	}

	public void setAutoEjectDirection(BlockFace direction) {
		autoEjectDirection = direction;
	}

	public BlockFace getAutoEjectDirection() {
		return autoEjectDirection;
	}

	@Override
	public double getCharge() {
		return charge;
	}

	@Override
	public void setCharge(double charge) {
		if (charge <= 0 && this.charge > 0 && getLocation() != null) {
			playOutOfChargeSound();
		}
		this.charge = Math.max(0, charge);
		getGUI().getMonitor(chargeMeterId).repaintNeeded();
		updateBlock();
	}

	protected void playStartupSound() {
		// override in subclasses
	}

	protected void playOutOfChargeSound() {
		// override in subclasses
	}

	@Override
	public Inventory getInventory() {
		return getGUI().getInventory();
	}

	private void createGUI() {
		InventoryGUI gui = new InventoryGUI(this, getInventoryGUISize(), ChatColor.DARK_BLUE + getItemName());

		gui.paintSlotSurround(getInputSlots(), InventoryGUI.INPUT_TEXTURE);
		gui.paintSlotSurround(getOutputSlots(), InventoryGUI.OUTPUT_TEXTURE);
		for (int slot : getInputSlots()) {
			gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
		}
		for (int slot : getOutputSlots()) {
			gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
		}

		int[] upgradeSlots = getUpgradeSlots();
		for (int slot : upgradeSlots) {
			gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
		}
		if (getUpgradeLabelSlot() >= 0) {
			gui.addLabel("Upgrades", getUpgradeLabelSlot());
		}
		for (int i = 0; i < upgrades.size() && i < upgradeSlots.length; i++) {
			gui.getInventory().setItem(upgradeSlots[i], upgrades.get(i).toItemStack(upgrades.get(i).getAmount()));
		}

		gui.addGadget(new RedstoneBehaviourGadget(gui), getRedstoneBehaviourSlot());
		gui.addGadget(new AccessControlGadget(gui), getAccessControlSlot());

		if (gui.containsSlot(getEnergyCellSlot())) {
			gui.setSlotType(getEnergyCellSlot(), InventoryGUI.SlotType.ITEM);
		}
		gui.addGadget(new ChargeDirectionGadget(gui), getChargeDirectionSlot());
		chargeMeterId = gui.addMonitor(new ChargeMeter(gui));
		if (installedCell != null) {
			gui.paintSlot(getEnergyCellSlot(), installedCell.toItemStack(1), true);
		}

		setGUI(gui);
	}

	public int getRedstoneBehaviourSlot() {
		return 8;  // top right
	}

	public int getAccessControlSlot() {
		return 17;  // just below top right
	}

	public int getChargeMeterSlot() {
		return 26;  // just below access control slot
	}

	public int getEnergyCellSlot() {
		return -1; // no energy cell by default
	}

	public int getChargeDirectionSlot() {
		return -1;
	}

	public int getInventoryGUISize() {
		return 54;
	}

	@Override
	public void onInteractBlock(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			getGUI().show(event.getPlayer());
			event.setCancelled(true);
		}
		super.onInteractBlock(event);
	}

	@Override
	public void setLocation(Location loc) {
		if (loc == null) {
			getGUI().ejectItems(getInputSlots());
			getGUI().ejectItems(getOutputSlots());
			getGUI().ejectItems(getUpgradeSlots());
			if (installedCell != null) {
				getGUI().ejectItems(getEnergyCellSlot());
				installedCell = null;
			}
			upgrades.clear();
		}
		super.setLocation(loc);
		if (loc != null) {
			createGUI();
			getGUI().thawSlots(frozenInput, getInputSlots());
			getGUI().thawSlots(frozenOutput, getOutputSlots());
		}
	}

	/**
	 * Find a candidate slot for item insertion; this will look for an empty slot, or a slot containing the
	 * same kind of item as the candidate item.  It will NOT check item amounts (see #insertItem() for that)
	 *
	 * @param item the candidate item to insert
	 * @param side the side being inserted from (SELF is valid option here too)
	 * @return the slot number if a slot is available, or -1 otherwise
	 */
	protected int findAvailableInputSlot(ItemStack item, BlockFace side) {
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

	@Override
	public int insertItems(ItemStack toInsert, BlockFace side, boolean sorting) {
		if (sorting) {
			return 0; // machines don't take items from sorters
		}
		int slot = findAvailableInputSlot(toInsert, side);
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
			if (Debugger.getInstance().getLevel() > 1) {
				Debugger.getInstance().debug(2, "inserted " + nInserted + " out of " +
						STBUtil.describeItemStack(toInsert) + " into " + this);
			}
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
						setJammed(false);
						updateBlock(false);
						if (Debugger.getInstance().getLevel() > 1) {
							Debugger.getInstance().debug(2, "extracted " + STBUtil.describeItemStack(result) + " from " + this);
						}
						return result;
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean onSlotClick(int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
		if (isInputSlot(slot)) {
			if (onCursor.getType() != Material.AIR && !acceptsItemType(onCursor)) {
				return false;
			}
		} else if (isOutputSlot(slot)) {
			if (onCursor.getType() != Material.AIR) {
				return false;
			} else {
				setJammed(false);
			}
		} else if (isUpgradeSlot(slot)) {
			if (onCursor.getType() != Material.AIR) {
				if (!BaseSTBItem.isSTBItem(onCursor, MachineUpgrade.class)) {
					return false;
				}
			}
			needToProcessUpgrades = true;
		} else if (slot == getEnergyCellSlot()) {
			if (onCursor.getType() != Material.AIR) {
				EnergyCell cell = BaseSTBItem.getItemFromItemStack(onCursor, EnergyCell.class);
				if (cell != null) {
					installEnergyCell(cell);
				} else {
					return false;
				}
			} else if (inSlot != null) {
				installEnergyCell(null);
			}
		}
		return true;
	}

	@Override
	public int onShiftClickInsert(int slot, ItemStack toInsert) {
		int insertionSlot = findAvailableInputSlot(toInsert, BlockFace.SELF);
		if (insertionSlot >= 0 && acceptsItemType(toInsert)) {
			ItemStack inMachine = getInventory().getItem(insertionSlot);
			if (inMachine == null) {
				getInventory().setItem(insertionSlot, toInsert);
				return toInsert.getAmount();
			} else {
				int nToInsert = Math.min(inMachine.getType().getMaxStackSize() - inMachine.getAmount(), toInsert.getAmount());
				inMachine.setAmount(inMachine.getAmount() + nToInsert);
				getInventory().setItem(insertionSlot, inMachine);
				toInsert.setAmount(toInsert.getAmount() - nToInsert);
				return nToInsert;
			}
		}
		if (getEnergyCellSlot() >= 0 && installedCell == null) {
			EnergyCell cell = BaseSTBItem.getItemFromItemStack(toInsert, EnergyCell.class);
			if (cell != null) {
				installEnergyCell(cell);
				getInventory().setItem(getEnergyCellSlot(), installedCell.toItemStack(1));
				return 1;
			}
		}
		if (getUpgradeSlots().length > 0) {
			if (BaseSTBItem.isSTBItem(toInsert, MachineUpgrade.class)) {
				int upgradeSlot = findAvailableUpgradeSlot(toInsert);
				if (upgradeSlot >= 0) {
					getInventory().setItem(upgradeSlot, toInsert);
					needToProcessUpgrades = true;
					return toInsert.getAmount();
				}
			}
		} else {
			return 0;
		}
		return 0;
	}

	@Override
	public boolean onShiftClickExtract(int slot, ItemStack toExtract) {
		// allow extraction to continue in all cases
		if (slot == getEnergyCellSlot() && toExtract != null) {
			installEnergyCell(null);
		} else if (isUpgradeSlot(slot)) {
			needToProcessUpgrades = true;
		}
		return true;
	}

	@Override
	public boolean onClickOutside() {
		return false;
	}

	private int findAvailableUpgradeSlot(ItemStack upgrade) {
		for (int slot : getUpgradeSlots()) {
			ItemStack inSlot = getInventory().getItem(slot);
			if (inSlot == null || inSlot.isSimilar(upgrade) && inSlot.getAmount() + upgrade.getAmount() <= upgrade.getType().getMaxStackSize()) {
				return slot;
			}
		}
		return -1;
	}

	private void scanUpgradeSlots() {
		upgrades.clear();
		for (int slot : getUpgradeSlots()) {
			ItemStack stack = getInventory().getItem(slot);
			if (stack != null) {
				MachineUpgrade upgrade = BaseSTBItem.getItemFromItemStack(stack, MachineUpgrade.class);
				if (upgrade == null) {
					getInventory().setItem(slot, null);
					if (getLocation() != null) {
						getLocation().getWorld().dropItemNaturally(getLocation(), stack);
					}
				} else {
					upgrade.setAmount(stack.getAmount());
					upgrades.add(upgrade);
					updateBlock(false);
				}
			}
		}
	}

	private void processUpgrades() {
		int nSpeed = 0;
		BlockFace ejectDirection = null;
		for (MachineUpgrade upgrade : upgrades) {
			if (upgrade instanceof SpeedUpgrade) {
				nSpeed += upgrade.getAmount();
			} else if (upgrade instanceof EjectorUpgrade) {
				ejectDirection = ((EjectorUpgrade) upgrade).getDirection();
			}
		}
		setSpeedMultiplier(Math.pow(1.4, nSpeed));
		setPowerMultiplier(Math.pow(1.6, nSpeed));
		setAutoEjectDirection(ejectDirection);
		Debugger.getInstance().debug("upgrades for " + this + " speed=" + getSpeedMultiplier() +
				" power=" + getPowerMultiplier() + " eject=" + getAutoEjectDirection());
	}

	public void installEnergyCell(EnergyCell cell) {
		installedCell = cell;
		Debugger.getInstance().debug("installed energy cell " + cell + " in " + this);
		updateBlock();
	}

	@Override
	public boolean shouldTick() {
		return true;
	}

	@Override
	public void onServerTick() {
		if (getTicksLived() % CHARGE_INTERVAL == 0) {
			double transferred = 0.0;
			if (installedCell != null) {
				switch (chargeDirection) {
					case MACHINE:
						transferred = transferCharge(installedCell, this);
						break;
					case CELL:
						transferred = transferCharge(this, installedCell);
						break;
				}
			}
			if (!getInventory().getViewers().isEmpty()) {
				if (transferred > 0.0) {
					getInventory().setItem(getEnergyCellSlot(), installedCell.toItemStack(1));
				}
				getGUI().getMonitor(chargeMeterId).doRepaint();
			}
		}
		if (needToProcessUpgrades) {
			if (getGUI() != null) {
				scanUpgradeSlots();
			}
			processUpgrades();
			needToProcessUpgrades = false;
		}
		super.onServerTick();
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


}
