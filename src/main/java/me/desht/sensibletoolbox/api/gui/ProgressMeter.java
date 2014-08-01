package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.items.AbstractProcessingMachine;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.core.gui.STBInventoryGUI;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A progress meter gadget.  The GUI that this is added to must be owned by an
 * {@link me.desht.sensibletoolbox.api.items.AbstractProcessingMachine}.
 */
public class ProgressMeter extends MonitorGadget {
    private final Material progressIcon;
    private final AbstractProcessingMachine machine;
    private int maxProcessingTime = 0;

    /**
     * Constructs a new progress meter.
     *
     * @param gui the GUI which holds this progress meter
     */
    public ProgressMeter(InventoryGUI gui) {
        super(gui);
        Validate.isTrue(getGUI().getOwningBlock() instanceof AbstractProcessingMachine,
                "Attempt to install progress meter in non-processing machine " + getGUI().getOwningBlock());
        machine = (AbstractProcessingMachine) getGUI().getOwningBlock();
        Validate.isTrue(machine.getProgressCounterSlot() > 0 || machine.getProgressItemSlot() > 0, "At least one of counter slot and item slot must be >= 0!");
        this.progressIcon = machine.getProgressIcon();
        Validate.isTrue(progressIcon != null && progressIcon.getMaxDurability() > 0, "Material " + progressIcon + " doesn't have a durability!");
    }

    @Override
    public void repaint() {
        if (machine.getProgressCounterSlot() > 0 && machine.getProgressCounterSlot() < getGUI().getInventory().getSize()) {
            ItemStack stack;
            double progress = machine.getProgress();
            if (progress > 0 && maxProcessingTime > 0) {
                stack = new ItemStack(progressIcon);
                STBUtil.levelToDurability(stack, (int) (maxProcessingTime - progress), maxProcessingTime);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(machine.getProgressMessage());
                stack.setItemMeta(meta);
            } else {
                stack = STBInventoryGUI.BG_TEXTURE;
            }
            getGUI().getInventory().setItem(machine.getProgressCounterSlot(), stack);
        }
        if (machine.getProgressItemSlot() > 0 && machine.getProgressItemSlot() < getGUI().getInventory().getSize()) {
            if (machine.getProcessing() != null) {
                getGUI().getInventory().setItem(machine.getProgressItemSlot(), machine.getProcessing());
            } else {
                getGUI().getInventory().setItem(machine.getProgressItemSlot(), STBInventoryGUI.BG_TEXTURE);
            }
        }
    }

    @Override
    public int[] getSlots() {
        if (machine.getProgressCounterSlot() > 0 && machine.getProgressItemSlot() > 0) {
            return new int[]{machine.getProgressCounterSlot(), machine.getProgressItemSlot()};
        } else if (machine.getProgressCounterSlot() > 0) {
            return new int[]{machine.getProgressCounterSlot()};
        } else {
            return new int[]{machine.getProgressItemSlot()};
        }
    }

    /**
     * Get the current progress as a percentage of the maximum processing
     * time.
     *
     * @return the current progress
     */
    public int getProgressPercent() {
        return (maxProcessingTime - (int) machine.getProgress()) * 100 / maxProcessingTime;
    }

    /**
     * Set the maximum processing time for the gadget.  Until this is set, the
     * gadget will not display a value.
     *
     * @param processingTime the maximum processing time
     */
    public void setMaxProgress(int processingTime) {
        maxProcessingTime = processingTime;
    }
}
