package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;

public class IronCombineHoe extends CombineHoe {

	public IronCombineHoe(ConfigurationSection conf) {
		super(conf);
	}

	public IronCombineHoe() {
	}

	@Override
	public Material getBaseMaterial() {
		return Material.IRON_HOE;
	}

	@Override
	public String getItemName() {
		return "Iron Combine Hoe";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("SSS", "HCW", "SSS");
		recipe.setIngredient('S', Material.STRING);
		recipe.setIngredient('H', Material.IRON_HOE);
		recipe.setIngredient('C', Material.CHEST);
		recipe.setIngredient('W', Material.IRON_SWORD);
		return recipe;
	}
}
