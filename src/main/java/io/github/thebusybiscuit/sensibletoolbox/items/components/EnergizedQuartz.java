package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class EnergizedQuartz extends BaseSTBItem {
	
    public static final MaterialData md = new MaterialData(Material.QUARTZ);

    public EnergizedQuartz() {
    }

    public EnergizedQuartz(ConfigurationSection conf) {
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Energized Quartz";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Strangely glowing..."};
    }

    @Override
    public Recipe getRecipe() {
    	ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
        InfernalDust dust = new InfernalDust();
        registerCustomIngredients(dust);
        recipe.addIngredient(dust.getMaterialData());
        recipe.addIngredient(new MaterialData(Material.QUARTZ));
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
