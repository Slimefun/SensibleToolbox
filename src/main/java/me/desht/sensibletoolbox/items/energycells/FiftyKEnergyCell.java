package me.desht.sensibletoolbox.items.energycells;

import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class FiftyKEnergyCell extends EnergyCell {
    public FiftyKEnergyCell() {
        super();
    }

    public FiftyKEnergyCell(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public int getMaxCharge() {
        return 50000;
    }

    @Override
    public int getChargeRate() {
        return 500;
    }

    @Override
    public Color getCellColor() {
        return Color.PURPLE;
    }

    @Override
    public String getItemName() {
        return "50K Energy Cell";
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        TenKEnergyCell cell = new TenKEnergyCell();
        cell.setCharge(0.0);
        registerCustomIngredients(cell);
        recipe.shape("III", "CCC", "GRG");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('C', STBUtil.getDontCareMaterialData(cell));
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }
}
