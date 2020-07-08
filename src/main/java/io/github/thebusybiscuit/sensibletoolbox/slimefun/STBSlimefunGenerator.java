package io.github.thebusybiscuit.sensibletoolbox.slimefun;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;

public class STBSlimefunGenerator extends STBSlimefunItem implements RecipeDisplayItem {

    private final List<ItemStack> fuel;

    public STBSlimefunGenerator(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, List<ItemStack> fuel) {
        super(category, item, recipeType, recipe);

        this.fuel = fuel;
    }

    @Override
    public List<ItemStack> getDisplayRecipes() {
        return fuel;
    }

}
