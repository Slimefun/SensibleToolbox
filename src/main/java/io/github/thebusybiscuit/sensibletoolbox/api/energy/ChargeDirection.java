package io.github.thebusybiscuit.sensibletoolbox.api.energy;

/**
 * Represents the direction of charging selected for this machine.
 * 
 * @author desht
 */
public enum ChargeDirection {

    /**
     * Energy will flow from an installed energy cell to the machine.
     */
    MACHINE,

    /**
     * Energy will flow from the machine to an installed energy cell.
     */
    CELL;

}