package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class DiamondCombineHoe extends CombineHoe {
	
    public DiamondCombineHoe() {
        super();
    }

    public DiamondCombineHoe(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_HOE;
    }

    @Override
    public String getItemName() {
        return "Diamond Combine Hoe";
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("SSS", "HCW", "SSS");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('H', Material.DIAMOND_HOE);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('W', Material.DIAMOND_SWORD);
        return recipe;
    }

    @Override
    public int getWorkRadius() {
        return 2;
    }
}
