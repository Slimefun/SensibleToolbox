package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.items.energycells.FiftyKEnergyCell;

public class FiftyKBatteryBox extends BatteryBox {

    public FiftyKBatteryBox() {}

    public FiftyKBatteryBox(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.PURPLE_STAINED_GLASS;
    }

    @Override
    public String getItemName() {
        return "50K Battery Box";
    }

    @Override
    public Recipe getMainRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("GGG", "GCG", "RIR");
        FiftyKEnergyCell cell = new FiftyKEnergyCell();
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
        return 50000;
    }

    @Override
    public int getChargeRate() {
        return isRedstoneActive() ? 500 : 0;
    }
}
