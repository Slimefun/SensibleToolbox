package me.desht.sensibletoolbox.api.gui;

/**
 * Abstract base class for all gadgets.
 */
public abstract class Gadget {
    private final InventoryGUI gui;
    private boolean enabled = true;

    protected Gadget(InventoryGUI gui) {
        this.gui = gui;
    }

    /**
     * Check if this gadget responds to any clicks.
     *
     * @return true if the gadget is enabled; false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set if this gadget responds to any clicks.
     *
     * @param enabled true if this gadget should be enabled; false otherwise
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get the GUI for this gadget.
     *
     * @return this gadget's GUI object
     */
    public InventoryGUI getGUI() {
        return gui;
    }
}
