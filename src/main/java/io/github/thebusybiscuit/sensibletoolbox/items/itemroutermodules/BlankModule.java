package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;

public class BlankModule extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.PAPER);

    public BlankModule() {
    }

    public BlankModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
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
        ShapedRecipe recipe = new ShapedRecipe(toItemStack(8));
        recipe.shape("PPP", "PRP", "PBP");
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('B', STBUtil.makeColouredMaterial(Material.INK_SACK, DyeColor.BLUE));
        return recipe;
    }
}
