package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.sensibletoolbox.api.ProcessingMachine;
import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProgressMeter extends MonitorGadget {
	private final int progressItemSlot;
	private final int progressCounterSlot;
	private final Material progressIcon;
	private int maxProcessingTime = 0;

	public ProgressMeter(InventoryGUI gui) {
		super(gui);
		Validate.isTrue(getOwner() instanceof ProcessingMachine,
				"Attempt to install progress meter in non-processing machine " + getOwner());
		ProcessingMachine pm = (ProcessingMachine) getOwner();
		this.progressCounterSlot = pm.getProgressCounterSlot();
		this.progressItemSlot = pm.getProgressItemSlot();
		Validate.isTrue(progressCounterSlot > 0 || progressItemSlot > 0, "At least one counter slot and item slot must be >= 0!");
		this.progressIcon = pm.getProgressIcon();
		Validate.isTrue(progressIcon != null && progressIcon.getMaxDurability() > 0, "Material " + progressIcon + " doesn't have a durability!");
	}

	public void repaint() {
		ProcessingMachine pm = (ProcessingMachine) getOwner();
		if (progressCounterSlot > 0 && progressCounterSlot < getOwner().getInventory().getSize()) {
			ItemStack stack;
			double progress = pm.getProgress();
			if (progress > 0 && maxProcessingTime > 0) {
				stack = new ItemStack(progressIcon);
				short max = stack.getType().getMaxDurability();
				int dur = (max * (int)pm.getProgress()) / maxProcessingTime;
				stack.setDurability((short)dur);
				ItemMeta meta = stack.getItemMeta();
				int percent = (maxProcessingTime - (int)pm.getProgress()) * 100 / maxProcessingTime;
				meta.setDisplayName("Progress: " + percent + "%");
				stack.setItemMeta(meta);
			} else {
				stack = InventoryGUI.BG_TEXTURE;
			}
			getOwner().getInventory().setItem(progressCounterSlot, stack);
		}
		if (progressItemSlot > 0 && progressItemSlot < getOwner().getInventory().getSize()) {
			if (pm.getProcessing() != null) {
				getOwner().getInventory().setItem(progressItemSlot, pm.getProcessing());
			} else {
				getOwner().getInventory().setItem(progressItemSlot, InventoryGUI.BG_TEXTURE);
			}
		}
	}

	@Override
	public int[] getSlots() {
		if (progressCounterSlot > 0 && progressItemSlot > 0) {
			return new int[] { progressCounterSlot, progressItemSlot };
		} else if (progressCounterSlot > 0) {
			return new int[] { progressCounterSlot };
		} else {
			return new int[] { progressItemSlot };
		}
	}

	public void initialize(int processingTime) {
		maxProcessingTime = processingTime;
	}
}
