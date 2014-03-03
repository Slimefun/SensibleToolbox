package me.desht.sensibletoolbox.items.components;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

public class CircuitBoard extends BaseSTBItem {
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.CARPET, DyeColor.GREEN);

	public CircuitBoard() {
	}

	public CircuitBoard(ConfigurationSection conf) {
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Circuit Board";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Used in the construction", "of electronic circuits" };
	}

	@Override
	public Recipe getRecipe() {
		Dye greenDye = new Dye();
		greenDye.setColor(DyeColor.GREEN);
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(2));
		recipe.addIngredient(Material.STONE_PLATE);
		recipe.addIngredient(greenDye);
		return recipe;
	}
}
