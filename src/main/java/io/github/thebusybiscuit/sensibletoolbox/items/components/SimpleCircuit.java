package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class SimpleCircuit extends BaseSTBItem {

    public SimpleCircuit() {}

    public SimpleCircuit(ConfigurationSection conf) {}

    @Override
    public Material getMaterial() {
        return Material.REPEATER;
    }

    @Override
    public String getItemName() {
        return "Simple Electronic Circuit";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Used as a component in", "various machinery " };
    }

    @Override
    public Recipe getRecipe() {
        CircuitBoard cb = new CircuitBoard();
        registerCustomIngredients(cb);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack(2));
        recipe.shape("CDC", "GTG", "CGC");
        recipe.setIngredient('C', cb.getMaterial());
        recipe.setIngredient('D', Material.REPEATER);
        recipe.setIngredient('T', Material.REDSTONE_TORCH);
        recipe.setIngredient('G', Material.GOLD_NUGGET);
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
