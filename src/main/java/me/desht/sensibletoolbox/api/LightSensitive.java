package me.desht.sensibletoolbox.api;

import org.bukkit.inventory.ItemStack;

/**
 * Represents an STB block which cares about the light level.
 */
public interface LightSensitive {
    /**
     * Get the current ambient light level.
     *
     * @return the light level
     */
    public byte getLightLevel();

    /**
     * Get the slot in the block's GUI where a light meter can be displayed.
     *
     * @return a slot number
     */
    public int getLightMeterSlot();

    public ItemStack getIndicator();
}
