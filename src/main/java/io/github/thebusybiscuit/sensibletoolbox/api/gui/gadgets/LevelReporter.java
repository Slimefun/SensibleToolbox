package io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a block that can report a level for some attribute.
 * 
 * @author desht
 */
public interface LevelReporter {

    /**
     * Get the level of the quantity being monitored.
     *
     * @return an integer level
     */
    int getLevel();

    /**
     * Get the maximum possible level for the quantity being monitored.
     *
     * @return an integer level
     */
    int getMaxLevel();

    /**
     * Get the item used to represent the level. This item should support
     * a durability bar (e.g. a tool or armour item).
     *
     * @return the item used to show the level as a durability bar
     */
    ItemStack getLevelIcon();

    /**
     * Get the GUI slot in which the monitor icon should be shown.
     *
     * @return a gui slot number
     */
    int getLevelMonitorSlot();

    /**
     * Get the string to display as the monitor icon's tooltip.
     *
     * @return a message string
     */
    String getLevelMessage();
}