package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.api.ProcessingMachine;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.ProgressMeter;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.VanillaInventoryUtils;
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
public abstract class AbstractProcessingMachine extends BaseSTBMachine implements ProcessingMachine {
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

    public abstract int getProgressItemSlot();

    public abstract int getProgressCounterSlot();

    public abstract Material getProgressIcon();

    @Override
    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = Math.max(0, progress);
        getProgressMeter().repaintNeeded();
    }

    @Override
    public ItemStack getProcessing() {
        return processing;
    }

    public void setProcessing(ItemStack processing) {
        this.processing = processing;
        getProgressMeter().repaintNeeded();
    }

    protected ProgressMeter getProgressMeter() {
        return (ProgressMeter) getGUI().getMonitor(progressMeterId);
    }

    protected int getEjectionInterval() {
        return ejectionInterval;
    }

    private void setEjectionInterval(int ejectionInterval) {
        this.ejectionInterval = ejectionInterval;
    }

    @Override
    public void setLocation(Location loc) {
        if (loc == null && getProcessing() != null) {
            // any item being processed is lost, hard luck!
            setProcessing(null);
        }
        super.setLocation(loc);
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = super.createGUI();

        progressMeterId = gui.addMonitor(new ProgressMeter(gui));

        return gui;
    }

    @Override
    public void onServerTick() {
        if (getTicksLived() % PROGRESS_INTERVAL == 0 && isRedstoneActive() && getGUI().getViewers().size() > 0) {
            getProgressMeter().doRepaint();
        }
        super.onServerTick();
    }

    @Override
    public String getProgressMessage() {
        return "Progress: " + getProgressMeter().getProgressPercent() + "%";
    }

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

    /**
     * Attempt to auto-eject one item from an output slot.
     *
     * @param result the item to eject
     * @return true if an item was ejected, false otherwise
     */
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
            STBBlock stb = LocationManager.getManager().get(loc);
            int nInserted = stb instanceof STBInventoryHolder ?
                    ((STBInventoryHolder) stb).insertItems(item, getAutoEjectDirection().getOppositeFace(), false, getOwner()) :
                    VanillaInventoryUtils.vanillaInsertion(target, item, 1, getAutoEjectDirection().getOppositeFace(), false, getOwner());
            return nInserted > 0;
        }
    }
}
