package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class WoodCombineHoe extends CombineHoe {

    public WoodCombineHoe() {
        super();
    }

    public WoodCombineHoe(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.WOODEN_HOE;
    }

    @Override
    public String getItemName() {
        return "Wooden Combine Hoe";
    }

    @Override
    public Recipe getMainRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("SSS", "HCW", "SSS");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('H', Material.WOODEN_HOE);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('W', Material.WOODEN_SWORD);
        return recipe;
    }
}
