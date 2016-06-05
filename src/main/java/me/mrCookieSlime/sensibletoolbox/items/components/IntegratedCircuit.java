package me.mrCookieSlime.sensibletoolbox.items.components;

import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class IntegratedCircuit extends BaseSTBItem {
    private static MaterialData md = new MaterialData(Material.REDSTONE_COMPARATOR);

    public IntegratedCircuit() {
    }

    public IntegratedCircuit(ConfigurationSection conf) {
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Integrated Circuit";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Used as a component", "in some more", "advanced machinery"};
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        EnergizedGoldIngot eg = new EnergizedGoldIngot();
        SiliconWafer si = new SiliconWafer();
        registerCustomIngredients(sc, eg, si);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("SCG");
        recipe.setIngredient('C', sc.getMaterialData());
        recipe.setIngredient('G', eg.getMaterialData());
        recipe.setIngredient('S', si.getMaterialData());
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
