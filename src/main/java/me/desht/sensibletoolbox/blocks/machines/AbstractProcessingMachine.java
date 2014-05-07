package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.api.ProcessingMachine;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.ProgressMeter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a machine with a progress bar to indicate how much of this work cycle is done.
 */
public abstract class AbstractProcessingMachine extends BaseSTBMachine implements ProcessingMachine {
    private static final long PROGRESS_INTERVAL = 10;
    private double progress; // ticks remaining till this work cycle is done
    private int progressMeterId;
    private ItemStack processing;

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
}
