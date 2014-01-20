package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;

public class DiamondCombineHoe extends CombineHoe {
	public DiamondCombineHoe(ConfigurationSection conf) {
		super(conf);
	}

	public DiamondCombineHoe() {
	}

	@Override
	public Material getBaseMaterial() {
		return Material.DIAMOND_HOE;
	}

	@Override
	public String getItemName() {
		return "Diamond Combine Hoe";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("SSS", "HCW", "SSS");
		recipe.setIngredient('S', Material.STRING);
		recipe.setIngredient('H', Material.DIAMOND_HOE);
		recipe.setIngredient('C', Material.CHEST);
		recipe.setIngredient('W', Material.DIAMOND_SWORD);
		return recipe;
	}
}
