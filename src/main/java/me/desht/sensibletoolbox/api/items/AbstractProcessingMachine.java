package me.desht.sensibletoolbox.api.items;

import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.api.gui.InventoryGUI;
import me.desht.sensibletoolbox.api.gui.ProgressMeter;
import me.desht.sensibletoolbox.api.util.VanillaInventoryUtils;
import me.desht.sensibletoolbox.core.storage.LocationManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Represents a machine with a progress bar to indicate how much of this work cycle is done.
 */
public abstract class AbstractProcessingMachine extends BaseSTBMachine {
    private static final long PROGRESS_INTERVAL = 10;
    private double progress; // ticks remaining till this work cycle is done
    private int progressMeterId;
    private ItemStack processing;
    private int ejectionInterval = 1; // try to eject every tick by default

    protected AbstractProcessingMachine() {
        super();
    }

    public AbstractProcessingMachine(ConfigurationSection conf) {
        super(conf);
    }

    /**
     * Define the inventory slot where the item currently being processed can be displayed.
     *
     * @return the slot number, or -1 if no item should be displayed
     */
    public abstract int getProgressItemSlot();

    /**
     * Define the inventory slot where the progress bar should be displayed.
     *
     * @return the slot number, or -1 if no bar should be displayed
     */
    public abstract int getProgressCounterSlot();

    /**
     * Define the material used to display the progress bar.  This material must have a durability,
     * e.g. a tool or armour item.
     *
     * @return the material used to display the progress bar
     */
    public abstract Material getProgressIcon();

    /**
     * Get the ticks remaining until this work cycle is complete, or 0 if the
     * machine is not currently processing anything.
     *
     * @return the ticks remaining
     */
    public final double getProgress() {
        return progress;
    }

    /**
     * Update the machine's progress; set the ticks remaining until this work
     * cycle is complete.
     *
     * @param progress the ticks remaining until completion
     */
    public final void setProgress(double progress) {
        this.progress = Math.max(0, progress);
        getProgressMeter().repaintNeeded();
    }

    /**
     * Get the item that is currently being processed.
     *
     * @return the item being processed, or null if nothing is being processed
     */
    public final ItemStack getProcessing() {
        return processing;
    }

    /**
     * Set the item to be processed.
     *
     * @param item the item to be processed, or null to process nothing
     */
    public final void setProcessing(ItemStack item) {
        this.processing = item;
        getProgressMeter().repaintNeeded();
    }

    /**
     * Define a tooltip to be displayed on the progress counter icon.
     *
     * @return a progress message tooltip
     */
    public String getProgressMessage() {
        return "Progress: " + getProgressMeter().getProgressPercent() + "%";
    }

    /**
     * Get the progress meter gadget for this machine.
     *
     * @return a progress meter gadget, or null if no gadget has been added
     */
    protected final ProgressMeter getProgressMeter() {
        return (ProgressMeter) getGUI().getMonitor(progressMeterId);
    }

    /**
     * Get the ejection interval, in server ticks, for this machine.  Machines
     * which have difficulty ejecting items may automatically raise this interval
     * to avoid wasting CPU cycles on repeated futile attempts.
     *
     * @return the ejection interval
     */
    protected int getEjectionInterval() {
        return ejectionInterval;
    }

    private void setEjectionInterval(int ejectionInterval) {
        this.ejectionInterval = ejectionInterval;
    }

    @Override
    public void onBlockUnregistered(Location loc) {
        if (getProcessing() != null) {
            // any item being processed is lost, hard luck!
            setProcessing(null);
        }
        super.onBlockUnregistered(loc);
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = super.createGUI();

        progressMeterId = gui.addMonitor(new ProgressMeter(gui));

        return gui;
    }

    @Override
    public void onServerTick() {
        if (getTicksLived() % PROGRESS_INTERVAL == 0 && getGUI().getViewers().size() > 0) {
            getProgressMeter().doRepaint();
        }
        super.onServerTick();
    }

    /**
     * Handle auto ejecting items from the output slot(s).  This is typically called
     * by implementing subclasses at the end of their onServerTick() implementation.
     */
    protected void handleAutoEjection() {
        if (getTicksLived() % getEjectionInterval() != 0) {
            return;
        }
        boolean ejectFailed = false;
        if (getAutoEjectDirection() != null && getAutoEjectDirection() != BlockFace.SELF) {
            for (int slot : getOutputSlots()) {
                ItemStack stack = getInventoryItem(slot);
                if (stack != null) {
                    if (autoEject(stack)) {
                        stack.setAmount(stack.getAmount() - 1);
                        setInventoryItem(slot, stack);
                        setJammed(false);
                    } else {
                        ejectFailed = true;
                    }
                    break;
                }
            }
        }
        // possibly throttle back on ejection rate to reduce CPU
        // consumption on repeated attempts to re-eject the item(s)
        setEjectionInterval(ejectFailed ? 20 : 1);
    }

    private boolean autoEject(ItemStack result) {
        Location loc = getRelativeLocation(getAutoEjectDirection());
        Block target = loc.getBlock();
        ItemStack item = result.clone();
        item.setAmount(1);
        if (!target.getType().isSolid() || target.getType() == Material.WALL_SIGN) {
            // no (solid) block there - just drop the item
            Item i = loc.getWorld().dropItem(loc.add(0.5, 0.5, 0.5), item);
            i.setVelocity(new Vector(0, 0, 0));
            return true;
        } else {
            BaseSTBBlock stb = LocationManager.getManager().get(loc);
            int nInserted = stb instanceof STBInventoryHolder ?
                    ((STBInventoryHolder) stb).insertItems(item, getAutoEjectDirection().getOppositeFace(), false, getOwner()) :
                    VanillaInventoryUtils.vanillaInsertion(target, item, 1, getAutoEjectDirection().getOppositeFace(), false, getOwner());
            return nInserted > 0;
        }
    }
}
