package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class IronCombineHoe extends CombineHoe {

    public IronCombineHoe() {
        super();
    }

    public IronCombineHoe(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_HOE;
    }

    @Override
    public String getItemName() {
        return "Iron Combine Hoe";
    }

    @Override
    public Recipe getMainRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("SSS", "HCW", "SSS");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('H', Material.IRON_HOE);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('W', Material.IRON_SWORD);
        return recipe;
    }
}
