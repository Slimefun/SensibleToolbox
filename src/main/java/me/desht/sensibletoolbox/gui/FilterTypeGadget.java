package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.FilterType;
import me.desht.sensibletoolbox.api.Filtering;
import org.apache.commons.lang.Validate;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class FilterTypeGadget extends ClickableGadget {
	private FilterType filterType;

	public FilterTypeGadget(InventoryGUI gui) {
		super(gui);
		Validate.isTrue(gui.getOwningItem() instanceof Filtering, "Filter Type gadget can only be added to filtering items!");
		filterType = ((Filtering)getGUI().getOwningItem()).getFilter().getFilterType();
	}

	@Override
	public void onClicked(InventoryClickEvent event) {
		int n = (filterType.ordinal() + 1) % FilterType.values().length;
		filterType = FilterType.values()[n];
		event.setCurrentItem(filterType.getTexture());
		((Filtering)getGUI().getOwningItem()).getFilter().setFilterType(filterType);
	}

	@Override
	public ItemStack getTexture() {
		return filterType.getTexture();
	}
}
