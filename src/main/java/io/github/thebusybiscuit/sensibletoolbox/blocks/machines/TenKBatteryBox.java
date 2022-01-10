package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.items.energycells.TenKEnergyCell;

public class TenKBatteryBox extends BatteryBox {

    public TenKBatteryBox() {}

    public TenKBatteryBox(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.RED_STAINED_GLASS;
    }

    @Override
    public String getItemName() {
        return "10K Battery Box";
    }

    @Override
    public Recipe getMainRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("GGG", "GCG", "RIR");
        TenKEnergyCell cell = new TenKEnergyCell();
        cell.setCharge(0.0);
        registerCustomIngredients(cell);
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('C', cell.getMaterial());
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('I', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public int getMaxCharge() {
        return 10000;
    }

    @Override
    public int getChargeRate() {
        return isRedstoneActive() ? 100 : 0;
    }
}
