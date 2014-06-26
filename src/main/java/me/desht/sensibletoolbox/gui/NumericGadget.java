package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NumericGadget extends ClickableGadget {
    private final String title;
    private final IntRange range;
    private final int incr;
    private final int altIncr;
    private final NumericListener callback;
    private final ItemStack icon = new ItemStack(Material.PAPER);
    private int value;

    public NumericGadget(InventoryGUI gui, int slot, String title, IntRange range, int value, int incr, int altIncr, NumericListener callback) {
        super(gui, slot);
        this.title = title;
        this.range = range;
        this.value = value;
        this.incr = incr;
        this.altIncr = altIncr;
        this.callback = callback;
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        int newValue = value;
        if (event.isLeftClick()) {
            newValue -= event.isShiftClick() ? altIncr : incr;
        } else if (event.isRightClick()) {
            newValue += event.isShiftClick() ? altIncr : incr;
        }
        newValue = Math.max(Math.min(newValue, range.getMaximumInteger()), range.getMinimumInteger());
        if (newValue != value && callback.run(newValue)) {
            value = newValue;
            event.setCurrentItem(getTexture());
        } else {
            // vetoed by the block!
            if (event.getWhoClicked() instanceof Player) {
               STBUtil.complain((Player) event.getWhoClicked());
            }
        }
    }

    public void setValue(int value) {
        Validate.isTrue(range.containsInteger(value), "Value " + value + " is out of range");
        this.value = value;
        updateGUI();
    }

    @Override
    public ItemStack getTexture() {
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + title + ": " + ChatColor.AQUA + value);
        String max = range.getMaximumInteger() == Integer.MAX_VALUE ? "\u221e" : Integer.toString(range.getMaximumInteger());
        String min = range.getMaximumInteger() == Integer.MIN_VALUE ? "-\u221e" : Integer.toString(range.getMinimumInteger());
        String[] lore = {
                "Valid value range: " + min + "-" + max,
                "L-Click: -" + incr,
                "R-Click: +" + incr,
                "With Shift held, +/-" + altIncr
        };
        meta.setLore(InventoryGUI.makeLore(lore));
        icon.setItemMeta(meta);
        return icon;
    }

    public interface NumericListener {
        public boolean run(int newValue);
    }
}
