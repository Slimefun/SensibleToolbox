package io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;

/**
 * Monitors an integer quantity.
 * 
 * @author desht
 */
public class LevelMonitor extends MonitorGadget {

    private final LevelReporter reporter;

    /**
     * Constructs a new level monitor gadget.
     *
     * @param gui
     *            the GUI to add the gadget to
     * @param reporter
     *            the level reporter object
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
        return new int[] { reporter.getLevelMonitorSlot() };
    }
}
