package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.blocks.machines.SimpleProcessingMachine;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProgressCounter extends UpdatingGadget {
	private final int progressItemSlot;
	private final int progressCounterSlot;
	private int maxProcessingTime = 0;

	public ProgressCounter(SimpleProcessingMachine owner, int progressItemSlot, int progressCounterSlot) {
		super(owner);
		this.progressCounterSlot = progressCounterSlot;
		this.progressItemSlot = progressItemSlot;
	}

	@Override
	public int getMinimumUpdateInterval() {
		return 500; // milliseconds
	}

	public void repaint() {
		if (progressCounterSlot > 0 && progressCounterSlot < getOwner().getInventory().getSize()) {
			ItemStack stack;
			SimpleProcessingMachine owner = (SimpleProcessingMachine) getOwner();
			int progress = owner.getProgress();
			if (progress > 0 && maxProcessingTime > 0) {
				stack = new ItemStack(Material.IRON_AXE);
				short max = stack.getType().getMaxDurability();
				int dur = (max * owner.getProgress()) / maxProcessingTime;
				stack.setDurability((short)dur);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("Progress: " + owner.getProgress() + "/" + maxProcessingTime);
				stack.setItemMeta(meta);
				System.out.println("progress update : " + (max - dur) + "/" + max);
			} else {
				stack = BaseSTBMachine.BG_TEXTURE;
			}
			getOwner().getInventory().setItem(progressCounterSlot, stack);
		}
		if (progressItemSlot > 0 && progressCounterSlot < getOwner().getInventory().getSize()) {
			SimpleProcessingMachine owner = (SimpleProcessingMachine) getOwner();
			if (owner.getProcessing() != null) {
				getOwner().getInventory().setItem(progressItemSlot, owner.getProcessing());
			} else {
				getOwner().getInventory().setItem(progressItemSlot, BaseSTBMachine.BG_TEXTURE);
			}
		}
	}

	public void setInitialProgress(int processingTime) {
		maxProcessingTime = processingTime;
	}
}
