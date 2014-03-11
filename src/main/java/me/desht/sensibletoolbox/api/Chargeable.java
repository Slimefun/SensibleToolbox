package me.desht.sensibletoolbox.api;

/**
 * Represents an STB item or block which can hold an electric charge (SCU: Sensible Charge Units).
 */
public interface Chargeable {
	/**
	 * Get the current SCU level.
	 *
	 * @return the current SCU level
	 */
	public double getCharge();

	/**
	 * Adjust the current SCU level
	 *
	 * @param charge the new SCU level
	 */
	public void setCharge(double charge);

	/**
	 * Get the maximum SCU level for this device
	 *
	 * @return the maximum SCU level
	 */
	public int getMaxCharge();

	/**
	 * Get the max rate at which this device can charge or discharge in SCU/tick.  Note that this does
	 * not necessarily limit the amount of charge used when the device is working, only when it is
	 * charging/discharging via connected battery block or installed energy cell.
	 *
	 * @return the max charge rate
 	 */
	public int getChargeRate();
}
