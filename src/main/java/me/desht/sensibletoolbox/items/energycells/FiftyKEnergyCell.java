package me.desht.sensibletoolbox.items.energycells;

import me.desht.sensibletoolbox.blocks.machines.FiftyKBatteryBox;
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
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		TenKEnergyCell cell = new TenKEnergyCell();
		cell.setCharge(0.0);
		registerCustomIngredients(cell);
		recipe.shape("III", "CCC", "GIG");
		recipe.setIngredient('I', Material.IRON_INGOT);
		recipe.setIngredient('C', cell.toItemStack().getData());
		recipe.setIngredient('G', Material.GOLD_INGOT);
		return recipe;
	}
}
