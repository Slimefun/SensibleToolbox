package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;

public class GoldCombineHoe extends CombineHoe {
	public GoldCombineHoe(ConfigurationSection conf) {
		super(conf);
	}

	public GoldCombineHoe() {
	}

	@Override
	public Material getBaseMaterial() {
		return Material.GOLD_HOE;
	}

	@Override
	public String getItemName() {
		return "Gold Combine Hoe";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("SSS", "HCW", "SSS");
		recipe.setIngredient('S', Material.STRING);
		recipe.setIngredient('H', Material.GOLD_HOE);
		recipe.setIngredient('C', Material.CHEST);
		recipe.setIngredient('W', Material.GOLD_SWORD);
		return recipe;
	}
}
