package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class PaintBrush extends BaseSTBItem {
	@Override
	public Material getBaseMaterial() {
		return Material.ARROW;
	}

	@Override
	public String getItemName() {
		return "Paintbrush";
	}

	@Override
	public String[] getLore() {
		return new String[] { "..." };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("R", "S", "S");
		recipe.setIngredient('R', Material.STRING);
		recipe.setIngredient('S', Material.STICK);
		return recipe;
	}
}
