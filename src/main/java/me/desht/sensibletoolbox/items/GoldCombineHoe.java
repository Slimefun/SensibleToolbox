package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.Map;

public class GoldCombineHoe extends CombineHoe {
	private static final MaterialData md = new MaterialData(Material.GOLD_HOE);

	public GoldCombineHoe() {
		super();
	}

	public GoldCombineHoe(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Gold Combine Hoe";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("SSS", "HCW", "SSS");
		recipe.setIngredient('S', Material.STRING);
		recipe.setIngredient('H', Material.GOLD_HOE);
		recipe.setIngredient('C', Material.CHEST);
		recipe.setIngredient('W', Material.GOLD_SWORD);
		return recipe;
	}
}
