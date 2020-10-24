package io.github.thebusybiscuit.sensibletoolbox.items.recipebook;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import io.github.thebusybiscuit.sensibletoolbox.api.gui.CyclerGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

class RecipeTypeFilter extends CyclerGadget<RecipeType> {

    protected RecipeTypeFilter(InventoryGUI gui, int slot, String label) {
        super(gui, slot, label);
        add(RecipeType.ALL, ChatColor.GRAY, Material.BLACK_STAINED_GLASS, "All Recipes");
        add(RecipeType.VANILLA, ChatColor.WHITE, Material.WHITE_STAINED_GLASS, "Vanilla Recipes");
        add(RecipeType.STB, ChatColor.YELLOW, Material.YELLOW_STAINED_GLASS, "STB Recipes");
        setInitialValue(((RecipeBook) getGUI().getOwningItem()).getRecipeTypeFilter());
    }

    @Override
    protected boolean ownerOnly() {
        return false;
    }

    @Override
    protected void apply(BaseSTBItem stbItem, RecipeType newValue) {
        RecipeBook book = (RecipeBook) stbItem;
        book.setRecipeTypeFilter(newValue);
        book.buildFilteredList();
        book.drawItemsPage();
    }
}