package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.items.components.IntegratedCircuit;

public class DenseSolar extends BasicSolarCell {

    public DenseSolar() {}

    public DenseSolar(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.GRAY_STAINED_GLASS;
    }

    @Override
    public String getItemName() {
        return "Dense Solar";
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        BasicSolarCell bs = new BasicSolarCell();
        IntegratedCircuit ic = new IntegratedCircuit();
        registerCustomIngredients(bs, ic);
        recipe.shape("SSS", "SIS", "SSS");
        recipe.setIngredient('S', bs.getMaterial());
        recipe.setIngredient('I', ic.getMaterial());
        return recipe;
    }

    @Override
    public int getMaxCharge() {
        return 800;
    }

    @Override
    public int getChargeRate() {
        return 12;
    }

    @Override
    protected DyeColor getCapColor() {
        return DyeColor.CYAN;
    }

    @Override
    protected double getPowerOutput() {
        return 4.0;
    }
}
