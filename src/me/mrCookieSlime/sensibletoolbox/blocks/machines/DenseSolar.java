package me.mrCookieSlime.sensibletoolbox.blocks.machines;

import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.items.components.IntegratedCircuit;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class DenseSolar extends BasicSolarCell {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.GRAY);

    public DenseSolar() {
    }

    public DenseSolar(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
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
        return 800;
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
