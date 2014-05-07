package me.desht.sensibletoolbox.items.components;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class SimpleCircuit extends BaseSTBItem {
    private static MaterialData md = new MaterialData(Material.REDSTONE_COMPARATOR);

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
        recipe.shape("CDC", "RTR", "CGC");
        recipe.setIngredient('C', cb.getMaterialData());
        recipe.setIngredient('D', Material.DIODE);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('T', Material.REDSTONE_TORCH_ON);
        recipe.setIngredient('G', Material.GOLD_NUGGET);
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
