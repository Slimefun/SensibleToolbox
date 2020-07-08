package io.github.thebusybiscuit.sensibletoolbox.api;

import org.bukkit.inventory.ItemStack;

public interface LightMeterHolder {

    /**
     * Get the slot in the block's GUI where a light meter can be displayed.
     *
     * @return a slot number
     */
    public int getLightMeterSlot();

    /**
     * Return the item stack that should be used to represent this light
     * meter.
     *
     * @return an item stack
     */
    public ItemStack getLightMeterIndicator();
}
