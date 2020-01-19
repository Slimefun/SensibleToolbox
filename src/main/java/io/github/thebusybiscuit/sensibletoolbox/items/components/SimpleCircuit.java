package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class SimpleCircuit extends BaseSTBItem {
    private static MaterialData md = new MaterialData(Material.DIODE);

    public SimpleCircuit() {
    }

    public SimpleCircuit(ConfigurationSection conf) {
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Simple Electronic Circuit";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Used as a component in", "various machinery "};
    }

    @Override
    public Recipe getRecipe() {
        CircuitBoard cb = new CircuitBoard();
        registerCustomIngredients(cb);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack(2));
        recipe.shape("CDC", "GTG", "CGC");
        recipe.setIngredient('C', cb.getMaterialData());
        recipe.setIngredient('D', Material.DIODE);
        recipe.setIngredient('T', Material.REDSTONE_TORCH_ON);
        recipe.setIngredient('G', Material.GOLD_NUGGET);
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
