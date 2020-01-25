package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class BlankModule extends BaseSTBItem {

    public BlankModule() {
    }

    public BlankModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.PAPER;
    }

    @Override
    public String getItemName() {
        return "Blank Item Router Module";
    }

    @Override
    public String[] getLore() {
        return new String[]{
        		"Used for crafting active", 
        		" Item Router Modules "
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack(8));
        recipe.shape("PPP", "PRP", "PBP");
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('B', Material.LAPIS_LAZULI);
        return recipe;
    }
}
