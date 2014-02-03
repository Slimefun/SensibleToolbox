package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class AdvancedMoistureChecker extends MoistureChecker {
	@Override
	public String getItemName() {
		return "Advanced Moisture Checker";
	}

	@Override
	public Recipe getRecipe() {
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
		recipe.addIngredient(Material.GHAST_TEAR); // in fact, a Moisture Checker - see below
		recipe.addIngredient(Material.DIAMOND);
		return recipe;
	}

	@Override
	public Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
		return mat == Material.GHAST_TEAR ? MoistureChecker.class : null;
	}

	protected int getRadius() {
		return 2;
	}
}
