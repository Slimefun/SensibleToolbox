package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class AdvancedMoistureChecker extends MoistureChecker {

    public AdvancedMoistureChecker() {}

    public AdvancedMoistureChecker(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "Advanced Moisture Checker";
    }

    @Override
    public Recipe getRecipe() {
        MoistureChecker mc = new MoistureChecker();
        registerCustomIngredients(mc);
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(mc.getMaterial());
        recipe.addIngredient(Material.DIAMOND);
        return recipe;
    }

    protected int getRadius() {
        return 2;
    }
}
