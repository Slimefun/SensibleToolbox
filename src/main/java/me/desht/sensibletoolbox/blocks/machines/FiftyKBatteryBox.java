package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.energycells.FiftyKEnergyCell;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class FiftyKBatteryBox extends BatteryBox {
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.PURPLE);

	public FiftyKBatteryBox() {
	}

	public FiftyKBatteryBox(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "50K Battery Box";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("GGG", "GCG", "RIR");
		ItemStack stack = new ItemStack(Material.LEATHER_HELMET, 1, Material.LEATHER_HELMET.getMaxDurability());
		recipe.setIngredient('G', Material.GLASS);
		recipe.setIngredient('C', stack.getData()); // actually a 50k energy cell
		recipe.setIngredient('R', Material.REDSTONE);
		recipe.setIngredient('I', Material.GOLD_INGOT);
		return recipe;
	}

	@Override
	public Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
		return mat == Material.LEATHER_HELMET ? FiftyKEnergyCell.class : null;
	}

	@Override
	public int getMaxCharge() {
		return 50000;
	}

	@Override
	public int getChargeRate() {
		return 500;
	}
}
