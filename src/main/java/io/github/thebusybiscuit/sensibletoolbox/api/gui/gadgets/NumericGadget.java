package io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.thebusybiscuit.sensibletoolbox.api.gui.GUIUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;

/**
 * A GUI gadget which allows an integer value to be displayed
 * and changed.
 * 
 * @author desht
 */
public class NumericGadget extends ClickableGadget {

    private final String title;
    private final IntRange range;
    private final int incr;
    private final int altIncr;
    private final NumericListener callback;
    private final ItemStack icon = new ItemStack(Material.PAPER);
    private int value;

    /**
     * Construct a numeric gadget.
     *
     * @param gui
     *            the GUI that the gadget belongs to
     * @param slot
     *            the GUI slot that the gadget occupies
     * @param title
     *            the label for the numeric gadget
     * @param range
     *            the range for the gadget
     * @param value
     *            the initial value for the gadget
     * @param incr
     *            the primary increment
     * @param altIncr
     *            the alternate increment
     * @param callback
     *            the code to run when the gadget is clicked
     */
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

    /**
     * Change the value of this numeric gadget.
     *
     * @param value
     *            the new value
     */
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
        String[] lore = { "Valid value range: " + min + "-" + max, "L-Click: -" + incr, "R-Click: +" + incr, "With Shift held, +/-" + altIncr };
        meta.setLore(GUIUtil.makeLore(lore));
        icon.setItemMeta(meta);
        return icon;
    }
}
