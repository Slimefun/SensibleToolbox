package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class PaintRoller extends PaintBrush {
	@Override
	public Material getBaseMaterial() {
		return Material.IRON_SPADE;
	}

	@Override
	public String getItemName() {
		return "Paint Roller";
	}

	@Override
	public int getMaxPaintLevel() {
		return 100;
	}

	@Override
	public boolean hasGlow() {
		return true;
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("WWW", "III", " S ");
		recipe.setIngredient('W', Material.WOOL);
		recipe.setIngredient('I', Material.IRON_INGOT);
		recipe.setIngredient('S', Material.STICK);
		return recipe;
	}

	@Override
	protected int getMaxBlocksAffected() {
		return 25;
	}
}
