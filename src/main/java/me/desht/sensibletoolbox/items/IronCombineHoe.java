package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.Map;

public class IronCombineHoe extends CombineHoe {
    private static final MaterialData md = new MaterialData(Material.IRON_HOE);

    public IronCombineHoe() {
        super();
    }

    public IronCombineHoe(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Iron Combine Hoe";
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("SSS", "HCW", "SSS");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('H', Material.IRON_HOE);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('W', Material.IRON_SWORD);
        return recipe;
    }
}
