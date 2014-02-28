package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.STBMachine;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import org.apache.commons.lang.Validate;

public abstract class MonitorGadget extends Gadget {
	private final BaseSTBBlock owner;
	private boolean repaintNeeded = true;

	public abstract void repaint();
	public abstract int[] getSlots();

	protected MonitorGadget(InventoryGUI gui) {
		super(gui);
		this.owner = gui.getOwningBlock();
	}

	public BaseSTBBlock getOwner() {
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
