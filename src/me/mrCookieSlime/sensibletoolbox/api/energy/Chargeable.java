package me.mrCookieSlime.sensibletoolbox.api.energy;

/**
 * Represents an STB item or block which can hold energy, meansured in units
 * of SCU: Sensible Charge Units.
 */
public interface Chargeable {
    /**
     * Get the current SCU level for this item
     *
     * @return the current SCU level
     */
    public double getCharge();

    /**
     * Change the current SCU level for this item
     *
     * @param charge the new SCU level
     */
    public void setCharge(double charge);

    /**
     * Get the maximum SCU level for this item
     *
     * @return the maximum SCU level
     */
    public int getMaxCharge();

    /**
     * Get the max rate at which this device can charge or discharge, in
     * SCU/tick.  Note that this does not necessarily limit the amount of
     * charge used when the device is working, only when it is
     * charging/discharging via connected battery block or installed energy
     * cell.
     *
     * @return the max charge rate
     */
    public int getChargeRate();
}
