package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.api.STBMachine;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.energynet.EnergyNet;
import me.desht.sensibletoolbox.energynet.EnergyNetManager;
import me.desht.sensibletoolbox.gui.*;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.energycells.EnergyCell;
import me.desht.sensibletoolbox.items.machineupgrades.EjectorUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.MachineUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.RegulatorUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.SpeedUpgrade;
import me.desht.sensibletoolbox.recipes.CustomRecipeManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class BaseSTBMachine extends BaseSTBBlock implements STBMachine {
    private double charge;
    private ChargeDirection chargeDirection;
    private boolean jammed;  // true if no space in output slots for processing result
    private EnergyCell installedCell;
    private double speedMultiplier;
    private double powerMultiplier;
    private BlockFace autoEjectDirection;
    private boolean needToProcessUpgrades;
    private int chargeMeterId;
    private final String frozenInput;
    private final String frozenOutput;
    private final List<MachineUpgrade> upgrades = new ArrayList<MachineUpgrade>();
    private final Map<BlockFace, EnergyNet> energyNets = new HashMap<BlockFace, EnergyNet>();
    private int regulatorAmount;

    protected BaseSTBMachine() {
        super();
        charge = 0;
        chargeDirection = ChargeDirection.MACHINE;
        jammed = false;
        autoEjectDirection = null;
        needToProcessUpgrades = false;
        frozenInput = frozenOutput = null;
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
                String[] f = l.split("::", 3);
                try {
                    YamlConfiguration upgConf = new YamlConfiguration();
                    int amount = Integer.parseInt(f[1]);
                    if (f.length > 2) {
                        upgConf.loadFromString(f[2]);
                    }
                    MachineUpgrade upgrade = (MachineUpgrade) BaseSTBItem.getItemById(f[0], upgConf);
                    upgrade.setAmount(amount);
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
            upg.add(upgrade.getItemTypeID() + "::" + upgrade.getAmount() + "::" + upgrade.freeze().saveToString());
        }
        conf.set("upgrades", upg);

        if (getGUI() != null) {
            conf.set("inputSlots", getGUI().freezeSlots(getInputSlots()));
            conf.set("outputSlots", getGUI().freezeSlots(getOutputSlots()));
        }
        if (installedCell != null) {
            conf.set("energyCell", installedCell.getItemTypeID());
            conf.set("energyCellCharge", installedCell.getCharge());
        }
        return conf;
    }

    public abstract int[] getInputSlots();

    public abstract int[] getOutputSlots();

    public abstract int[] getUpgradeSlots();

    public abstract int getUpgradeLabelSlot();

    protected abstract void playActiveParticleEffect();

    public void addCustomRecipes(CustomRecipeManager crm) {
        // override in subclasses
    }

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
        updateBlock(false);
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
        if (charge == this.charge) {
            return;
        }
        if (charge <= 0 && this.charge > 0 && getLocation() != null) {
            playOutOfChargeSound();
        }
        this.charge = Math.min(getMaxCharge(), Math.max(0, charge));
        if (getGUI() != null && chargeMeterId >= 0) {
            getGUI().getMonitor(chargeMeterId).repaintNeeded();
        }
        updateBlock(false);
    }

    @Override
    public String[] getExtraLore() {
        return getMaxCharge() > 0 ? new String[]{STBUtil.getChargeString(this)} : new String[0];
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

    public ItemStack getInventoryItem(int slot) {
        return getInventory().getItem(slot);
    }

    public void setInventoryItem(int slot, ItemStack item) {
        Validate.isTrue(getGUI().getSlotType(slot) == InventoryGUI.SlotType.ITEM, "Attempt to insert item into non-item slot");
        getInventory().setItem(slot, item != null && item.getAmount() > 0 ? item : null);
        updateBlock(false);
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = new InventoryGUI(this, getInventoryGUISize(), ChatColor.DARK_BLUE + getItemName());

        gui.paintSlotSurround(getInputSlots(), InventoryGUI.INPUT_TEXTURE);
        gui.paintSlotSurround(getOutputSlots(), InventoryGUI.OUTPUT_TEXTURE);
        for (int slot : getInputSlots()) {
            gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
        }
        gui.thawSlots(frozenInput, getInputSlots());
        for (int slot : getOutputSlots()) {
            gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
        }
        gui.thawSlots(frozenOutput, getOutputSlots());

        int[] upgradeSlots = getUpgradeSlots();
        for (int slot : upgradeSlots) {
            gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
        }
        if (getUpgradeLabelSlot() >= 0) {
            gui.addLabel("Upgrades", getUpgradeLabelSlot(), null);
        }
        for (int i = 0; i < upgrades.size() && i < upgradeSlots.length; i++) {
            gui.getInventory().setItem(upgradeSlots[i], upgrades.get(i).toItemStack(upgrades.get(i).getAmount()));
        }

        gui.addGadget(new RedstoneBehaviourGadget(gui, getRedstoneBehaviourSlot()));
        gui.addGadget(new AccessControlGadget(gui, getAccessControlSlot()));

        if (gui.containsSlot(getEnergyCellSlot())) {
            gui.setSlotType(getEnergyCellSlot(), InventoryGUI.SlotType.ITEM);
        }
        gui.addGadget(new ChargeDirectionGadget(gui, getChargeDirectionSlot()));
        chargeMeterId = getMaxCharge() > 0 ? gui.addMonitor(new ChargeMeter(gui)) : -1;
        if (installedCell != null) {
            gui.paintSlot(getEnergyCellSlot(), installedCell.toItemStack(), true);
        }
        return gui;
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

    public void setRegulatorAmount(int regulatorAmount) {
        this.regulatorAmount = regulatorAmount;
    }

    public int getRegulatorAmount() {
        return regulatorAmount;
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
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
            setGUI(null);
            EnergyNetManager.onMachineRemoved(this);
        }
        super.setLocation(loc);
        if (loc != null) {
            EnergyNetManager.onMachinePlaced(this);
        }
    }


    /**
     * Find a candidate slot for item insertion; this will look for an empty slot, or a slot containing the
     * same kind of item as the candidate item.  It will NOT check item amounts (see #insertItem() for that)
     *
     * @param item the candidate item to insert
     * @param side the side being inserted from (SELF is a valid option here too)
     * @return the slot number if a slot is available, or -1 otherwise
     */
    protected int findAvailableInputSlot(ItemStack item, BlockFace side) {
        for (int slot : getInputSlots()) {
            ItemStack inSlot = getInventoryItem(slot);
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
            ItemStack outSlot = getInventoryItem(slot);
            if (outSlot == null) {
                return slot;
            } else if (outSlot.isSimilar(item) && outSlot.getAmount() + item.getAmount() <= item.getType().getMaxStackSize()) {
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
    public int insertItems(ItemStack toInsert, BlockFace side, boolean sorting, UUID uuid) {
        if (!hasAccessRights(uuid)) {
            return 0;
        }
        if (sorting) {
            return 0; // machines don't take items from sorters
        }
        int slot = findAvailableInputSlot(toInsert, side);
        int nInserted = 0;
        if (slot >= 0 && acceptsItemType(toInsert)) {
            ItemStack inMachine = getInventoryItem(slot);
            if (inMachine == null) {
                nInserted = toInsert.getAmount();
                setInventoryItem(slot, toInsert);
            } else {
                nInserted = Math.min(toInsert.getAmount(), inMachine.getType().getMaxStackSize() - inMachine.getAmount());
                if (nInserted > 0) {
                    inMachine.setAmount(inMachine.getAmount() + nInserted);
                    setInventoryItem(slot, inMachine);
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
    public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid) {
        if (!hasAccessRights(uuid)) {
            return null;
        }
        int[] slots = getOutputSlots();
        int max = slots == null ? getInventory().getSize() : slots.length;
        for (int i = 0; i < max; i++) {
            int slot = slots == null ? i : slots[i];
            ItemStack stack = getInventoryItem(slot);
            if (stack != null) {
                if (receiver == null || stack.isSimilar(receiver)) {
                    int toTake = Math.min(amount, stack.getAmount());
                    if (receiver != null) {
                        toTake = Math.min(toTake, receiver.getType().getMaxStackSize() - receiver.getAmount());
                    }
                    if (toTake > 0) {
                        ItemStack result = stack.clone();
                        result.setAmount(toTake);
                        if (receiver != null) {
                            receiver.setAmount(receiver.getAmount() + toTake);
                        }
                        stack.setAmount(stack.getAmount() - toTake);
                        setInventoryItem(slot, stack);
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
    public Inventory showOutputItems() {
        if (getOutputSlots() == null) {
            Inventory inv = Bukkit.createInventory(this, getInventory().getSize());
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, getInventoryItem(i));
            }
            return inv;
        } else {
            Inventory inv = Bukkit.createInventory(this, STBUtil.roundUp(getOutputSlots().length, 9));
            int i = 0;
            for (int slot : getOutputSlots()) {
                inv.setItem(i++, getInventoryItem(slot));
            }
            return inv;
        }
    }

    @Override
    public void updateOutputItems(Inventory inventory) {
        if (getOutputSlots() == null) {
            for (int i = 0; i < getInventory().getSize() && i < inventory.getSize(); i++) {
                setInventoryItem(i, inventory.getItem(i));
            }
        } else {
            int i = 0;
            for (int slot : getOutputSlots()) {
                setInventoryItem(slot, inventory.getItem(i++));
            }
        }
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
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
                if (!isValidUpgrade(player, BaseSTBItem.getItemFromItemStack(onCursor))) {
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
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        BaseSTBItem item = BaseSTBItem.getItemFromItemStack(toInsert);

        if (getUpgradeSlots().length > 0 && isValidUpgrade(player, item)) {
            int upgradeSlot = findAvailableUpgradeSlot(toInsert);
            if (upgradeSlot >= 0) {
                if (getInventoryItem(upgradeSlot) != null) {
                    toInsert.setAmount(toInsert.getAmount() + getInventoryItem(upgradeSlot).getAmount());
                }
                setInventoryItem(upgradeSlot, toInsert);
                needToProcessUpgrades = true;
                return toInsert.getAmount();
            }
        }

        if (item instanceof EnergyCell && getEnergyCellSlot() >= 0 && installedCell == null) {
            installEnergyCell((EnergyCell) item);
            setInventoryItem(getEnergyCellSlot(), installedCell.toItemStack());
            return 1;
        }

        int insertionSlot = findAvailableInputSlot(toInsert, BlockFace.SELF);
        if (insertionSlot >= 0 && acceptsItemType(toInsert)) {
            ItemStack inMachine = getInventoryItem(insertionSlot);
            if (inMachine == null) {
                // insert the whole stack
                setInventoryItem(insertionSlot, toInsert);
                return toInsert.getAmount();
            } else {
                // insert as much as possible
                int nToInsert = Math.min(inMachine.getMaxStackSize() - inMachine.getAmount(), toInsert.getAmount());
                inMachine.setAmount(inMachine.getAmount() + nToInsert);
                setInventoryItem(insertionSlot, inMachine);
                return nToInsert;
            }
        }

        return 0;
    }

    private boolean isValidUpgrade(HumanEntity player, BaseSTBItem item) {
        if (!(item instanceof MachineUpgrade)) {
            return false;
        }
        if (item instanceof EjectorUpgrade && ((EjectorUpgrade) item).getDirection() == BlockFace.SELF) {
            STBUtil.complain((Player) player, "Ejector upgrade must have a direction configured.");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        // allow extraction to continue in all cases
        if (slot == getEnergyCellSlot() && toExtract != null) {
            installEnergyCell(null);
        } else if (isUpgradeSlot(slot)) {
            needToProcessUpgrades = true;
        } else if (isOutputSlot(slot)) {
            setJammed(false);
        }
        return true;
    }

    @Override
    public boolean onClickOutside(HumanEntity player) {
        return false;
    }

    private int findAvailableUpgradeSlot(ItemStack upgrade) {
        for (int slot : getUpgradeSlots()) {
            ItemStack inSlot = getInventoryItem(slot);
            if (inSlot == null || inSlot.isSimilar(upgrade) && inSlot.getAmount() + upgrade.getAmount() <= upgrade.getType().getMaxStackSize()) {
                return slot;
            }
        }
        return -1;
    }

    private void scanUpgradeSlots() {
        upgrades.clear();
        for (int slot : getUpgradeSlots()) {
            ItemStack stack = getInventoryItem(slot);
            if (stack != null) {
                MachineUpgrade upgrade = BaseSTBItem.getItemFromItemStack(stack, MachineUpgrade.class);
                if (upgrade == null) {
                    setInventoryItem(slot, null);
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
        int nRegulator = 0;
        for (MachineUpgrade upgrade : upgrades) {
            if (upgrade instanceof SpeedUpgrade) {
                nSpeed += upgrade.getAmount();
            } else if (upgrade instanceof EjectorUpgrade) {
                ejectDirection = ((EjectorUpgrade) upgrade).getDirection();
            } else if (upgrade instanceof RegulatorUpgrade) {
                nRegulator += upgrade.getAmount();
            }
        }
        setRegulatorAmount(nRegulator);
        setSpeedMultiplier(Math.pow(1.4, nSpeed));
        setPowerMultiplier(Math.pow(1.6, nSpeed));
        setPowerMultiplier(Math.max(getPowerMultiplier() - nRegulator * 0.1, 1.0));
        setAutoEjectDirection(ejectDirection);
        Debugger.getInstance().debug("upgrades for " + this + " speed=" + getSpeedMultiplier() +
                " power=" + getPowerMultiplier() + " eject=" + getAutoEjectDirection());
    }

    public void installEnergyCell(EnergyCell cell) {
        installedCell = cell;
        Debugger.getInstance().debug("installed energy cell " + cell + " in " + this);
        updateBlock(false);
    }

    @Override
    public int getTickRate() {
        return 1;
    }

    @Override
    public void onServerTick() {
        if (getTicksLived() % EnergyNetManager.getTickRate() == 0) {
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
                    setInventoryItem(getEnergyCellSlot(), installedCell.toItemStack());
                }
                if (chargeMeterId >= 0) {
                    getGUI().getMonitor(chargeMeterId).doRepaint();
                }
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
        double toTransfer = Math.min(from.getChargeRate() * EnergyNetManager.getTickRate(), to.getMaxCharge() - to.getCharge());
        toTransfer = Math.min(to.getChargeRate() * EnergyNetManager.getTickRate(), toTransfer);
        toTransfer = Math.min(from.getCharge(), toTransfer);
        to.setCharge(to.getCharge() + toTransfer);
        from.setCharge(from.getCharge() - toTransfer);
//		System.out.println("transfer " + toTransfer + " charge from " + from + " to " + to + " from now=" + from.getCharge() + " to now=" + to.getCharge());
        return toTransfer;
    }

    @Override
    public void attachToEnergyNet(EnergyNet net, BlockFace face) {
        Debugger.getInstance().debug(this + ": attach to enet " + net.getNetID());
        energyNets.put(face, net);
    }

    @Override
    public void detachFromEnergyNet(EnergyNet net) {
        Iterator<Map.Entry<BlockFace, EnergyNet>> iter = energyNets.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BlockFace, EnergyNet> entry = iter.next();
            if (entry.getValue().getNetID() == net.getNetID()) {
                iter.remove();
                Debugger.getInstance().debug(this + ": detached from enet #" + net.getNetID());
            }
        }
    }

    @Override
    public EnergyNet[] getAttachedEnergyNets() {
        Set<EnergyNet> nets = new HashSet<EnergyNet>();
        nets.addAll(energyNets.values());
        return nets.toArray(new EnergyNet[nets.size()]);
    }

    @Override
    public List<BlockFace> getFacesForNet(EnergyNet net) {
        List<BlockFace> res = new ArrayList<BlockFace>();
        for (Map.Entry<BlockFace, EnergyNet> entry : energyNets.entrySet()) {
            if (entry.getValue().getNetID() == net.getNetID()) {
                res.add(entry.getKey());
            }
        }
        return res;
    }
}
