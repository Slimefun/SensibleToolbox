package io.github.thebusybiscuit.sensibletoolbox.slimefun;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;

public class STBSlimefunGenerator extends STBSlimefunItem implements RecipeDisplayItem {

    private final List<ItemStack> fuel;

    @ParametersAreNonnullByDefault
    public STBSlimefunGenerator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, List<ItemStack> fuel) {
        super(itemGroup, item, recipeType, recipe);

        this.fuel = fuel;
    }

    @Override
    public List<ItemStack> getDisplayRecipes() {
        return fuel;
    }

}
