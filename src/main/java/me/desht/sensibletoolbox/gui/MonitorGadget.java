package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.STBBlock;

public abstract class MonitorGadget extends Gadget {
    private final STBBlock owner;
    private boolean repaintNeeded = true;

    public abstract void repaint();

    public abstract int[] getSlots();

    protected MonitorGadget(InventoryGUI gui) {
        super(gui);
        this.owner = gui.getOwningBlock();
    }

    public STBBlock getOwner() {
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
