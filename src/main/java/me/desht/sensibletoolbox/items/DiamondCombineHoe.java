package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class DiamondCombineHoe extends CombineHoe {
    private static final MaterialData md = new MaterialData(Material.DIAMOND_HOE);

    public DiamondCombineHoe() {
        super();
    }

    public DiamondCombineHoe(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Diamond Combine Hoe";
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("SSS", "HCW", "SSS");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('H', Material.DIAMOND_HOE);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('W', Material.DIAMOND_SWORD);
        return recipe;
    }
}
