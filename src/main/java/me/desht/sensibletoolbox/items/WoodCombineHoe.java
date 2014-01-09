package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;

public class WoodCombineHoe extends CombineHoe {
	public WoodCombineHoe() {

	}

	public WoodCombineHoe(Map<String, Object> map) {
		super(map);
	}

	@Override
	public Material getBaseMaterial() {
		return Material.WOOD_HOE;
	}

	@Override
	public String getItemName() {
		return "Wooden Combine Hoe";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1, false));
		recipe.shape("SSS", "HCW", "SSS");
		recipe.setIngredient('S', Material.STRING);
		recipe.setIngredient('H', Material.WOOD_HOE);
		recipe.setIngredient('C', Material.CHEST);
		recipe.setIngredient('W', Material.WOOD_SWORD);
		return recipe;
	}
}
