package me.desht.sensibletoolbox.items.energycells;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class FiftyKEnergyCell extends EnergyCell {
	public FiftyKEnergyCell() { }

	public FiftyKEnergyCell(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public int getMaxCharge() {
		return 50000;
	}

	@Override
	public int getChargeRate() {
		return 500;
	}

	@Override
	public Color getColor() {
		return Color.PURPLE;
	}

	@Override
	public String getItemName() {
		return "50K Energy Cell";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		ItemStack stack = new ItemStack(Material.LEATHER_HELMET, 1, Material.LEATHER_HELMET.getMaxDurability());
		recipe.shape("III", "CCC", "GIG");
		recipe.setIngredient('I', Material.IRON_INGOT);
		recipe.setIngredient('C', stack.getData()); // in fact, a 10k energy cell
		recipe.setIngredient('G', Material.GOLD_INGOT);
		return recipe;
	}

	@Override
	public Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
		return mat == Material.LEATHER_HELMET ? TenKEnergyCell.class : null;
	}
}
