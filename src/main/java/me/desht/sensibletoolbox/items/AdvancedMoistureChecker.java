package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class AdvancedMoistureChecker extends MoistureChecker {
	@Override
	public String getItemName() {
		return "Advanced Moisture Checker";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("S", "D", "I");
		recipe.setIngredient('S', Material.SIGN);
		recipe.setIngredient('D', Material.DIODE);
		recipe.setIngredient('I', Material.DIAMOND_SWORD);
		return recipe;
	}

	protected int getRadius() {
		return 2;
	}
}
