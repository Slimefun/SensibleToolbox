package io.github.thebusybiscuit.sensibletoolbox.api.energy;

/**
 * Represents an energy net; a collection of chargeable blocks connected by
 * cabling.
 */
public interface EnergyNet {

    void findSourcesAndSinks();

    /**
     * Get the energy net ID for this energy net.
     *
     * @return the energy net ID
     */
    int getNetID();

    /**
     * Get the number of cables in this energy net.
     *
     * @return the number of cables
     */
    int getCableCount();

    /**
     * Get the number of energy sources in this energy net.
     *
     * @return the number of energy sources
     */
    int getSourceCount();

    /**
     * Get the number of energy sinks in this energy net.
     *
     * @return the number of energy sinks
     */
    int getSinkCount();

    /**
     * Get the instantaneous energy demand per tick. This is requested
     * energy, not necessarily supplied.
     *
     * @return the demand
     */
    double getDemand();

    /**
     * Get the instantaneous energy supply per tick. This is available
     * energy, not necessarily used.
     *
     * @return the supply
     */
    double getSupply();
}
