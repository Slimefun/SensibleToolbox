package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.items.BaseSTBBlock;

/**
 * Abstract base class for all clickable gadgets.
 */
public abstract class MonitorGadget extends Gadget {
    private final BaseSTBBlock owner;
    private boolean repaintNeeded = true;

    protected MonitorGadget(InventoryGUI gui) {
        super(gui);
        this.owner = gui.getOwningBlock();
    }

    /**
     * Repaint this monitor gadget, reflecting any changes
     * in the value being monitored.
     */
    public abstract void repaint();

    public abstract int[] getSlots();

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
