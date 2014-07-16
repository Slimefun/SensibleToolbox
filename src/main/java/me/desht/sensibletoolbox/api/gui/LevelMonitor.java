package me.desht.sensibletoolbox.api.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LevelMonitor extends MonitorGadget {
    private final LevelReporter reporter;

    public LevelMonitor(InventoryGUI gui, LevelReporter reporter) {
        super(gui);
        this.reporter = reporter;
    }

    @Override
    public void repaint() {
        ItemStack stack;
        int level = reporter.getLevel();
        if (reporter.getMaxLevel() > 0) {
            stack = new ItemStack(reporter.getIcon());
            short max = stack.getType().getMaxDurability();
            int dur = (max * level) / reporter.getMaxLevel();
            stack.setDurability((short) (max - dur));
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(reporter.getMessage());
            stack.setItemMeta(meta);
            getGUI().getInventory().setItem(reporter.getSlot(), stack);
        }
    }

    @Override
    public int[] getSlots() {
        return new int[]{reporter.getSlot()};
    }

    public interface LevelReporter {
        public int getLevel();

        public int getMaxLevel();

        public Material getIcon();

        public int getSlot();

        public String getMessage();
    }
}
