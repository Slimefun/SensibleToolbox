package me.desht.sensibletoolbox.api.gui;

public abstract class Gadget {
    private final InventoryGUI gui;
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Gadget(InventoryGUI gui) {
        this.gui = gui;
    }

    public InventoryGUI getGUI() {
        return gui;
    }
}
