package io.github.thebusybiscuit.sensibletoolbox.api.gui;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;

/**
 * Monitors an integer quantity.
 */
public class LevelMonitor extends MonitorGadget {
    private final LevelReporter reporter;

    /**
     * Constructs a new level monitor gadget.
     *
     * @param gui the GUI to add the gadget to
     * @param reporter the level reporter object
     */
    public LevelMonitor(InventoryGUI gui, LevelReporter reporter) {
        super(gui);
        this.reporter = reporter;
    }

    @Override
    public void repaint() {
        ItemStack stack;
        int level = reporter.getLevel();
        if (reporter.getMaxLevel() > 0) {
            stack = reporter.getLevelIcon();
            STBUtil.levelToDurability(stack, level, reporter.getMaxLevel());
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(reporter.getLevelMessage());
            stack.setItemMeta(meta);
            getGUI().getInventory().setItem(reporter.getLevelMonitorSlot(), stack);
        }
    }

    @Override
    public int[] getSlots() {
        return new int[]{reporter.getLevelMonitorSlot()};
    }

    /**
     * Represents a block that can report a level for some attribute.
     */
    public interface LevelReporter {
        /**
         * Get the level of the quantity being monitored.
         *
         * @return an integer level
         */
        public int getLevel();

        /**
         * Get the maximum possible level for the quantity being monitored.
         *
         * @return an integer level
         */
        public int getMaxLevel();

        /**
         * Get the item used to represent the level.  This item should support
         * a durability bar (e.g. a tool or armour item).
         *
         * @return the item used to show the level as a durability bar
         */
        public ItemStack getLevelIcon();

        /**
         * Get the GUI slot in which the monitor icon should be shown.
         *
         * @return a gui slot number
         */
        public int getLevelMonitorSlot();

        /**
         * Get the string to display as the monitor icon's tooltip.
         *
         * @return a message string
         */
        public String getLevelMessage();
    }
}
