package io.github.thebusybiscuit.sensibletoolbox.api.gui;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import io.github.thebusybiscuit.sensibletoolbox.api.Filtering;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.util.FilterType;

/**
 * A GUI gadget which can display and change a filter's filter type.
 */
public class FilterTypeGadget extends CyclerGadget<FilterType> {

    /**
     * Construct a filter type gadget.
     *
     * @param gui
     *            the GUI that the gadget belongs to
     * @param slot
     *            the GUI slot that the gadget occupies
     */
    public FilterTypeGadget(InventoryGUI gui, int slot) {
        super(gui, slot, "Filter Type");
        Validate.isTrue(gui.getOwningItem() instanceof Filtering, "Filter Type gadget can only be added to filtering items!");
        add(FilterType.MATERIAL, ChatColor.GRAY, Material.STONE, "Match material only");
        add(FilterType.ITEM_META, ChatColor.LIGHT_PURPLE, Material.ENCHANTED_BOOK, "Match material, block metadata", "and item metadata (NBT)");
        setInitialValue(((Filtering) getGUI().getOwningItem()).getFilter().getFilterType());
    }

    @Override
    protected boolean ownerOnly() {
        return false;
    }

    @Override
    protected void apply(BaseSTBItem stbItem, FilterType newValue) {
        ((Filtering) getGUI().getOwningItem()).getFilter().setFilterType(newValue);
    }
}
