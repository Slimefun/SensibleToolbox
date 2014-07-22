package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.items.BaseSTBItem;

/**
 * Abstract base class for all clickable gadgets.
 */
public abstract class MonitorGadget extends Gadget {
    private final BaseSTBItem owner;
    private boolean repaintNeeded = true;

    protected MonitorGadget(InventoryGUI gui) {
        super(gui);
        this.owner = gui.getOwningItem();
    }

    /**
     * Force an immediate repaint of the monitor gadget.
     */
    public abstract void repaint();

    /**
     * Get the slot or slots that this monitor occupies.
     *
     * @return the slots occupied by this monitor
     */
    public abstract int[] getSlots();

    /**
     * Get the STB item or block which this monitor is attached to.
     *
     * @return the owning STB item or block
     */
    public BaseSTBItem getOwner() {
        return owner;
    }

    /**
     * Note that this monitor needs a repaint.  This method should be called
     * by the owning STB item/block whenever the quantity being monitored
     * changes.
     */
    public void repaintNeeded() {
        repaintNeeded = true;
    }

    /**
     * Repaint the monitor if necessary.
     */
    public void doRepaint() {
        if (repaintNeeded) {
            repaint();
            repaintNeeded = false;
        }
    }
}
