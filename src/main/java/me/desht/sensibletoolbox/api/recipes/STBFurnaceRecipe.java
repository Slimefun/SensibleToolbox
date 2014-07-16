package me.desht.sensibletoolbox.api.recipes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

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
