package me.desht.sensibletoolbox.gui;

public abstract class Gadget {
    private final InventoryGUI gui;

    public Gadget(InventoryGUI gui) {
        this.gui = gui;
    }

    public InventoryGUI getGUI() {
        return gui;
    }
}
