package me.desht.sensibletoolbox.items.energycells;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.BuildersMultiTool;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class TenKEnergyCell extends EnergyCell {
	public TenKEnergyCell() {
		super();
	}

	public TenKEnergyCell(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public int getMaxCharge() {
		return 10000;
	}

	@Override
	public int getChargeRate() {
		return 100;
	}

	@Override
	public Color getColor() {
		return Color.MAROON;
	}

	@Override
	public String getItemName() {
		return "10K Energy Cell";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("WWW", "WSW", "GRG");
		recipe.setIngredient('W', Material.WOOD);
		recipe.setIngredient('S', Material.SUGAR);
		recipe.setIngredient('R', Material.REDSTONE);
		recipe.setIngredient('G', Material.GOLD_INGOT);
		return recipe;
	}

}
