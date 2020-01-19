package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.FiftyKBatteryBox;

public class UnlinkedSCURelay extends BaseSTBItem {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.CYAN);

    public UnlinkedSCURelay() {
        super();
    }

    public UnlinkedSCURelay(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Unlinked SCU Relay";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Craft a pair of these", "together to link them" };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        FiftyKBatteryBox bb = new FiftyKBatteryBox();
        IntegratedCircuit ic = new IntegratedCircuit();
        EnergizedGoldIngot eg = new EnergizedGoldIngot();
        registerCustomIngredients(bb, ic, eg);
        recipe.shape("GCG", " E ", " B ");
        recipe.setIngredient('B', bb.getMaterialData());
        recipe.setIngredient('C', ic.getMaterialData());
        recipe.setIngredient('E', Material.ENDER_CHEST);
        recipe.setIngredient('G', eg.getMaterialData());
        return recipe;
    }
}
