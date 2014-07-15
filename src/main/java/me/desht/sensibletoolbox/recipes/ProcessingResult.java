package me.desht.sensibletoolbox.recipes;

import org.bukkit.inventory.ItemStack;

public class ProcessingResult {
    private final ItemStack result;
    private final int processingTime; // in ticks

    public ProcessingResult(ItemStack result, int processingTime) {
        this.result = result;
        this.processingTime = processingTime;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public ItemStack getResult() {
        return result.clone();
    }

    @Override
    public String toString() {
        return "Custom recipe: " + result + " in " + processingTime + " ticks";
    }
}
