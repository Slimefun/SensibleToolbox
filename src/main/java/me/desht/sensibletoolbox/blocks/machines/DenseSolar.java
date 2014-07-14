package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.items.components.IntegratedCircuit;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class DenseSolar extends BasicSolarCell {
    public DenseSolar() {
    }

    public DenseSolar(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "Dense Solar";
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        BasicSolarCell bs = new BasicSolarCell();
        IntegratedCircuit ic = new IntegratedCircuit();
        registerCustomIngredients(bs, ic);
        recipe.shape("SSS", "SIS", "SSS");
        recipe.setIngredient('S', bs.getMaterialData());
        recipe.setIngredient('I', ic.getMaterialData());
        return recipe;
    }

    @Override
    public int getMaxCharge() {
        return 240;
    }

    @Override
    public int getChargeRate() {
        return 12;
    }

    @Override
    protected DyeColor getCapColour() {
        return DyeColor.CYAN;
    }

    @Override
    protected double getPowerOutput() {
        return 4.0;
    }
}
