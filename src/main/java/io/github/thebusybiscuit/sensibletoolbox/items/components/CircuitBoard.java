package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;

public class CircuitBoard extends BaseSTBItem {
	
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.CARPET, DyeColor.GREEN);

    public CircuitBoard() {
    }

    public CircuitBoard(ConfigurationSection conf) {
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Circuit Board";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Used in the construction", "of electronic circuits"};
    }

    @Override
    public Recipe getRecipe() {
        Dye greenDye = new Dye();
        greenDye.setColor(DyeColor.GREEN);
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(2));
        recipe.addIngredient(Material.STONE_PLATE);
        recipe.addIngredient(greenDye);
        return recipe;
    }
}
