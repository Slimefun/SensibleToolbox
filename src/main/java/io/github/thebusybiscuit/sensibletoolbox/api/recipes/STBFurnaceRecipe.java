package io.github.thebusybiscuit.sensibletoolbox.api.recipes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 * A special case for furnace recipes; allows for multiple recipes per material
 * (which could be the case with STB item ingredients).  This isn't supported
 * by the plain Bukkit FurnaceRecipe which doesn't allow for setting an
 * ItemStack as an ingredient, only a Material or MaterialData.
 */
public class STBFurnaceRecipe implements Recipe {
    private final ItemStack ingredient;
    private final ItemStack result;

    public STBFurnaceRecipe(ItemStack result, ItemStack ingredient) {
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    public ItemStack getIngredient() {
        return ingredient;
    }
}
