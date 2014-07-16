package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.Filtering;
import me.desht.sensibletoolbox.api.util.Filter;
import org.apache.commons.lang.Validate;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class FilterTypeGadget extends ClickableGadget {
    private Filter.FilterType filterType;

    public FilterTypeGadget(InventoryGUI gui, int slot) {
        super(gui, slot);
        Validate.isTrue(gui.getOwningItem() instanceof Filtering, "Filter Type gadget can only be added to filtering items!");
        filterType = ((Filtering) getGUI().getOwningItem()).getFilter().getFilterType();
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        int n = (filterType.ordinal() + 1) % Filter.FilterType.values().length;
        filterType = Filter.FilterType.values()[n];
        event.setCurrentItem(filterType.getTexture());
        ((Filtering) getGUI().getOwningItem()).getFilter().setFilterType(filterType);
    }

    @Override
    public ItemStack getTexture() {
        return filterType.getTexture();
    }
}
