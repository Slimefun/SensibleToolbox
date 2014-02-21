package me.desht.sensibletoolbox.blocks;

import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class BigStorageUnit extends BaseSTBMachine {
	private static final MaterialData md = new MaterialData(Material.LOG_2, (byte) 1);
	private MaterialData type;
	private int amount;

	public BigStorageUnit() {
		type = null;
		amount = 0;
	}

	public BigStorageUnit(ConfigurationSection conf) {
		super(conf);
		Material mat = Material.getMaterial(conf.getString("material"));
		byte data = (byte) conf.getInt("data");
		type = new MaterialData(mat, data);
		amount = conf.getInt("amount");
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("material", type.getItemType().toString());
		conf.set("data", type.getData());
		conf.set("amount", amount);
		return conf;
	}

	@Override
	public int[] getInputSlots() {
		return new int[] { 10 };
	}

	@Override
	public int[] getOutputSlots() {
		return new int[] { 16 };
	}

	@Override
	public int[] getUpgradeSlots() {
		return new int[] { 50, 51, 52, 53 };
	}

	@Override
	public int getUpgradeLabelSlot() {
		return 49;
	}

	@Override
	protected void playActiveParticleEffect() {
		// nothing
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "BSU";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Big Storage Unit", "Stores up to 128 stacks", "of a single item type" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("LSL", "L L", "LLL");
		recipe.setIngredient('L', Material.LOG);
		recipe.setIngredient('S', Material.WOOD_STEP);
		return recipe;
	}

	@Override
	public Recipe[] getExtraRecipes() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("LSL", "L L", "LLL");
		recipe.setIngredient('L', Material.LOG_2);
		recipe.setIngredient('S', Material.WOOD_STEP);
		return new Recipe[] { recipe };
	}

	@Override
	public boolean acceptsEnergy(BlockFace face) {
		return false;
	}

	@Override
	public boolean suppliesEnergy(BlockFace face) {
		return false;
	}

	@Override
	public int getMaxCharge() {
		return 0;
	}

	@Override
	public int getChargeRate() {
		return 0;
	}

	public int getStackCapacity() {
		return 128;
	}

	@Override
	public boolean shouldTick() {
		return true;
	}

	@Override
	public void onServerTick() {
		// 1. move items from input to storage

		// 2. move items from storage to output

		super.onServerTick();
	}
}
