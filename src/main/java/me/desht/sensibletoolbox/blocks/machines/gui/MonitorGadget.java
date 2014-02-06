package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.sensibletoolbox.api.STBMachine;
import org.apache.commons.lang.Validate;

public abstract class MonitorGadget {
	private final STBMachine owner;
	private boolean repaintNeeded = true;

	public abstract void repaint();
	public abstract int[] getSlots();

	protected MonitorGadget(InventoryGUI gui) {
		Validate.isTrue(gui.getOwner() instanceof STBMachine,
				"Attempt to add charge meter to non-machine block " + gui.getOwner());
		owner = (STBMachine) gui.getOwner();
	}

	public STBMachine getOwner() {
		return owner;
	}

	public void repaintNeeded() {
		repaintNeeded = true;
	}

	public void doRepaint() {
		if (repaintNeeded) {
			repaint();
			repaintNeeded = false;
		}
	}
}
