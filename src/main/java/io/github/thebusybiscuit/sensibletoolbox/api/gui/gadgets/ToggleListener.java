package io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets;

/**
 * A callback to be executed when a {@link ToggleButton} is clicked.
 * 
 * @author desht
 */
public interface ToggleListener {

    /**
     * Called when a toggle button is clicked.
     *
     * @param newValue
     *            the proposed new value for the toggle
     * @return true if the new value should be accepted; false otherwise
     */
    boolean run(boolean newValue);
}