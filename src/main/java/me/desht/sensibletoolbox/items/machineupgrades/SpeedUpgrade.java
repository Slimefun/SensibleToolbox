package me.desht.sensibletoolbox.items.machineupgrades;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class SpeedUpgrade extends MachineUpgrade {
	private static final MaterialData md = new MaterialData(Material.SUGAR);

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public boolean hasGlow() {
		return true;
	}

	@Override
	public String getItemName() {
		return "Speed Upgrade";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Place in a machine block" , "Increases speed by 40%", "Increases power usage by 60%" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("IRI", "IBI", "IGI");
		recipe.setIngredient('I', Material.IRON_FENCE);
		recipe.setIngredient('R', Material.REDSTONE);
		recipe.setIngredient('B', Material.BLAZE_ROD);
		recipe.setIngredient('G', Material.GOLD_INGOT);
		return recipe;
	}
}
