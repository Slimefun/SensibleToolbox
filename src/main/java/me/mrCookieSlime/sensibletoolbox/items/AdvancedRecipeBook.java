package me.mrCookieSlime.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class AdvancedRecipeBook extends RecipeBook {
    public AdvancedRecipeBook() {
        super();
    }

    public AdvancedRecipeBook(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "Advanced Recipe Book";
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public String[] getExtraLore() {
        return new String[] { 
        		"Can pull items from adjacent",
        		"inventories during fabrication"
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
        RecipeBook book = new RecipeBook();
        registerCustomIngredients(book);
        recipe.addIngredient(Material.DIAMOND);
        recipe.addIngredient(book.getMaterialData());
        return recipe;
    }
}
