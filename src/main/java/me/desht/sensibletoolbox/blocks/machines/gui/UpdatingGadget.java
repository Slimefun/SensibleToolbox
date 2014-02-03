package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;

public abstract class UpdatingGadget {
	private final BaseSTBMachine owner;
	private boolean repaintNeeded = true;
	private static final long MIN_UPDATE_INTERVAL = 1000; // milliseconds

	public abstract int getMinimumUpdateInterval();
	public abstract void repaint();

	public UpdatingGadget(BaseSTBMachine owner) {
		this.owner = owner;
	}

	public BaseSTBMachine getOwner() {
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
