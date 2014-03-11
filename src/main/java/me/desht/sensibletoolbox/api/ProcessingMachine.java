package me.desht.sensibletoolbox.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a machine which processes an item in some way.,
 */
public interface ProcessingMachine extends STBMachine {
	/**
	 * Get the ticks remaining until this work cycle is complete, or 0 if the machine is not currently
	 * processing anything.
	 *
	 * @return the ticks remaining
	 */
	public double getProgress();

	/**
	 * Get the item that is currently being processed.
	 *
	 * @return the item being processed, or null if nothing is being processed
	 */
	public ItemStack getProcessing();

	/**
	 * Set the item to be processed.
	 *
	 * @param item the item to be processed, or null to process nothing
	 */
	public void setProcessing(ItemStack item);

	/**
	 * Get the inventory slot where the progress bar should be displayed.
	 *
	 * @return the slot number, or -1 if no bar should be displayed
	 */
	public int getProgressCounterSlot();

	/**
	 * Get the inventory slot where the item currently being processed can be displayed.
	 *
	 * @return the slot number, or -1 if no item should be displayed
	 */
	public int getProgressItemSlot();

	/**
	 * Get the material used to display the progress bar.  This material must have a durability,
	 * e.g. a tool or armour item.
	 *
	 * @return the material used to display the progress bar
	 */
	public Material getProgressIcon();

	/**
	 * Return a string to display as a tooltip on the progress counter icon.
	 *
	 * @return a progress message tooltip
	 */
	public String getProgressMessage();
}
