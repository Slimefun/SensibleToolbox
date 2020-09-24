package me.desht.sensibletoolbox.items.components;

import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class SubspaceTransponder extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.BREWING_STAND_ITEM);

    public SubspaceTransponder() {
        super();
    }

    public SubspaceTransponder(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
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
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        IntegratedCircuit ic = new IntegratedCircuit();
        EnergizedGoldIngot eg = new EnergizedGoldIngot();
        registerCustomIngredients(ic, eg);
        recipe.shape("DGE", " G ", " C ");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('E', Material.EYE_OF_ENDER);
        recipe.setIngredient('G', eg.getMaterialData());
        recipe.setIngredient('C', ic.getMaterialData());
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
