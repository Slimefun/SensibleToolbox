package me.desht.sensibletoolbox.api.gui;

public abstract class Gadget {
    private final InventoryGUI gui;

    public Gadget(InventoryGUI gui) {
        this.gui = gui;
    }

    public InventoryGUI getGUI() {
        return gui;
    }
}
