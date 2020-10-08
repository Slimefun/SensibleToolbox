package io.github.thebusybiscuit.sensibletoolbox.api.recipes;

import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;

/**
 * Represents the result of a custom recipe; an item and the time it takes
 * to make that item.
 * 
 * @author desht
 */
public class ProcessingResult {

    private final ItemStack result;
    private final int processingTime;

    public ProcessingResult(@Nonnull ItemStack result, int processingTime) {
        this.result = result;
        this.processingTime = processingTime;
    }

    /**
     * Get the base processing time, in ticks, that the machine takes to make the
     * item. This time may be modified by any upgrades in the machine.
     *
     * @return the base processing time, in ticks
     */
    public int getProcessingTime() {
        return processingTime;
    }

    /**
     * Get the resulting {@link ItemStack}.
     *
     * @return a item
     */
    @Nonnull
    public ItemStack getResult() {
        return result.clone();
    }

    @Override
    public String toString() {
        return "Custom recipe: " + result + " in " + processingTime + " ticks";
    }
}
