package io.github.thebusybiscuit.sensibletoolbox.api.gui;

public enum SlotType {

    /**
     * A background slot. This slot type is never interactable.
     */
    BACKGROUND,

    /**
     * A slot which holds an item which might be placed or removed
     * from the GUI.
     */
    ITEM,

    /**
     * A slot which holds an interactable control of some kind
     * (e.g. a button, a label, a monitor for some value...)
     */
    GADGET
}