package io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets;

/**
 * A callback to be executed when a {@link NumericGadget} is clicked.
 * 
 * @author desht
 */
@FunctionalInterface
public interface NumericListener {

    /**
     * Called when the value of the numeric gadget is to be changed.
     *
     * @param newValue
     *            the proposed new value
     * @return true if the new value should be accepted; false otherwise
     */
    boolean run(int newValue);

}