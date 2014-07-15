package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.ProcessingMachine;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProgressMeter extends MonitorGadget {
    private final Material progressIcon;
    private final ProcessingMachine machine;
    private int maxProcessingTime = 0;

    public ProgressMeter(InventoryGUI gui) {
        super(gui);
        Validate.isTrue(getGUI().getOwningBlock() instanceof ProcessingMachine,
                "Attempt to install progress meter in non-processing machine " + getGUI().getOwningBlock());
        machine = (ProcessingMachine) getGUI().getOwningBlock();
        Validate.isTrue(machine.getProgressCounterSlot() > 0 || machine.getProgressItemSlot() > 0, "At least one of counter slot and item slot must be >= 0!");
        this.progressIcon = machine.getProgressIcon();
        Validate.isTrue(progressIcon != null && progressIcon.getMaxDurability() > 0, "Material " + progressIcon + " doesn't have a durability!");
    }

    public void repaint() {
        if (machine.getProgressCounterSlot() > 0 && machine.getProgressCounterSlot() < getGUI().getInventory().getSize()) {
            ItemStack stack;
            double progress = machine.getProgress();
            if (progress > 0 && maxProcessingTime > 0) {
                stack = new ItemStack(progressIcon);
                short max = stack.getType().getMaxDurability();
                int dur = (max * (int) machine.getProgress()) / maxProcessingTime;
                stack.setDurability((short) dur);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(machine.getProgressMessage());
                stack.setItemMeta(meta);
            } else {
                stack = InventoryGUI.BG_TEXTURE;
            }
            getGUI().getInventory().setItem(machine.getProgressCounterSlot(), stack);
        }
        if (machine.getProgressItemSlot() > 0 && machine.getProgressItemSlot() < getGUI().getInventory().getSize()) {
            if (machine.getProcessing() != null) {
                getGUI().getInventory().setItem(machine.getProgressItemSlot(), machine.getProcessing());
            } else {
                getGUI().getInventory().setItem(machine.getProgressItemSlot(), InventoryGUI.BG_TEXTURE);
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

    public int getProgressPercent() {
        return (maxProcessingTime - (int) machine.getProgress()) * 100 / maxProcessingTime;
    }

    public void setMaxProgress(int processingTime) {
        maxProcessingTime = processingTime;
    }
}
