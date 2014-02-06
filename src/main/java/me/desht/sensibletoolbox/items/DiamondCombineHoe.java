package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.Map;

public class DiamondCombineHoe extends CombineHoe {
	private static final MaterialData md = new MaterialData(Material.DIAMOND_HOE);

	public DiamondCombineHoe(ConfigurationSection conf) {
		super(conf);
	}

	public DiamondCombineHoe() {
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
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
