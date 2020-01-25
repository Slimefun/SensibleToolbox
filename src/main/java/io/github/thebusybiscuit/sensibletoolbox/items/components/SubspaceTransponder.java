package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class SubspaceTransponder extends BaseSTBItem {

    public SubspaceTransponder() {
        super();
    }

    public SubspaceTransponder(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.BREWING_STAND;
    }

    @Override
    public String getItemName() {
        return "Subspace Transponder";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Used by some advanced", "items for cross-world", "communication" };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        IntegratedCircuit ic = new IntegratedCircuit();
        EnergizedGoldIngot eg = new EnergizedGoldIngot();
        registerCustomIngredients(ic, eg);
        recipe.shape("DGE", " G ", " C ");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('E', Material.ENDER_EYE);
        recipe.setIngredient('G', eg.getMaterial());
        recipe.setIngredient('C', ic.getMaterial());
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
