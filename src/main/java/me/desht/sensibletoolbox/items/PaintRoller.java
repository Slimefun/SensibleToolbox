package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class PaintRoller extends PaintBrush {
	private static final MaterialData md = new MaterialData(Material.IRON_SPADE);

	public PaintRoller() {
		super();
	}

	public PaintRoller(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
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
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
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
