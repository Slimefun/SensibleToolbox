package io.github.thebusybiscuit.sensibletoolbox.api.recipes;

import org.bukkit.inventory.ItemStack;

/**
 * Represents the result of a custom recipe; an item and the time it takes
 * to make that item.
 */
public class ProcessingResult {
    private final ItemStack result;
    private final int processingTime; // in ticks

    public ProcessingResult(ItemStack result, int processingTime) {
        this.result = result;
        this.processingTime = processingTime;
    }

    /**
     * Get the base processing time, in ticks, that the machine takes to make the
     * item.  This time may be modified by any upgrades in the machine.
     *
     * @return the base processing time, in ticks
     */
    public int getProcessingTime() {
        return processingTime;
    }

    /**
     * Get the resulting item.
     *
     * @return a item
     */
    public ItemStack getResult() {
        return result.clone();
    }

    @Override
    public String toString() {
        return "Custom recipe: " + result + " in " + processingTime + " ticks";
    }
}
