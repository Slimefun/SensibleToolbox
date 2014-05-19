package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class WoodCombineHoe extends CombineHoe {
    private static final MaterialData md = new MaterialData(Material.WOOD_HOE);

    public WoodCombineHoe() {
        super();
    }

    public WoodCombineHoe(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Wooden Combine Hoe";
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("SSS", "HCW", "SSS");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('H', Material.WOOD_HOE);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('W', Material.WOOD_SWORD);
        return recipe;
    }
}
