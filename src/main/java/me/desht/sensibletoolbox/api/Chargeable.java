package me.desht.sensibletoolbox.api;

/**
 * Represents an STB item which can hold an electric charge.
 */
public interface Chargeable {
	public double getCharge();
	public void setCharge(double charge);
	public int getMaxCharge();
}
