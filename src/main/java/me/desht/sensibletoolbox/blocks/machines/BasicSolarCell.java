package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

public class BasicSolarCell extends BaseSTBMachine {
	private static final MaterialData md = new MaterialData(Material.LAPIS_BLOCK);
	private static final int ENERGY_RATE = 20;
	private static final double SCU_PER_TICK = 1.0;
	private static final int LIGHT_SLOT = 13;
	private static final ItemStack BRIGHT = new Wool(DyeColor.YELLOW).toItemStack();
	private static final ItemStack DIM = new Wool(DyeColor.ORANGE).toItemStack();
	private static final ItemStack DARK = new Wool(DyeColor.BLACK).toItemStack();

	static {
		InventoryGUI.setDisplayName(BRIGHT, "Full Efficiency");
		InventoryGUI.setDisplayName(DIM, "Reduced Efficiency");
		InventoryGUI.setDisplayName(DARK, "No Power Output");
	}

	public BasicSolarCell() {

	}

	public BasicSolarCell(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public int[] getInputSlots() {
		return new int[0];  // no input
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];  // no output
	}

	@Override
	public int[] getUpgradeSlots() {
		return new int[0];  // maybe one day
	}

	@Override
	public int getUpgradeLabelSlot() {
		return -1;
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
	public boolean hasGlow() {
		return true;
	}

	@Override
	public String getItemName() {
		return "Basic Solar Cell";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Generates up to 1 SCU/t", "while outside in bright sunlight" };
	}

	@Override
	public Recipe getRecipe() {
		SimpleCircuit sc = new SimpleCircuit();
		registerCustomIngredients(sc);
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("DDD", "IQI", "RGR");
		recipe.setIngredient('D', Material.DAYLIGHT_DETECTOR);
		recipe.setIngredient('I', sc.getMaterialData());
		recipe.setIngredient('Q', Material.QUARTZ_BLOCK);
		recipe.setIngredient('R', Material.REDSTONE);
		recipe.setIngredient('G', Material.GOLD_INGOT);
		return recipe;
	}

	@Override
	public boolean acceptsEnergy(BlockFace face) {
		return false;
	}

	@Override
	public boolean suppliesEnergy(BlockFace face) {
		return true;
	}

	@Override
	public int getMaxCharge() {
		return 500;
	}

	@Override
	public int getChargeRate() {
		return 10;
	}

	@Override
	public int getEnergyCellSlot() {
		return 18;
	}

	@Override
	public int getChargeDirectionSlot() {
		return 19;
	}

	@Override
	public int getInventoryGUISize() {
		return 27;
	}

	@Override
	public boolean shouldTick() {
		return true;
	}

	@Override
	public void onServerTick() {
		if (getTicksLived() % ENERGY_RATE == 0) {
			Block b = getLocation().getBlock().getRelative(BlockFace.UP);
			byte lightFromSky = b.getLightFromSky();
			byte overallLight = lightFromSky < 14 ? 0 : b.getLightLevel();
			if (lightFromSky < 15) overallLight--;

			if (!getGUI().getViewers().isEmpty()) {
				getGUI().getInventory().setItem(LIGHT_SLOT, getIndicator(overallLight));
			}
			if (getCharge() < getMaxCharge()) {
				double toAdd = SCU_PER_TICK * ENERGY_RATE * getChargeMult(overallLight);
				setCharge(getCharge() + toAdd);
			}
		}
		super.onServerTick();
	}

	private double getChargeMult(byte light) {
		switch (light) {
			case 15: return 1.0;
			case 14: return 0.75;
			case 13: return 0.5;
			case 12: return 0.25;
			default: return 0.0;
		}
	}

	private ItemStack getIndicator(byte light) {
		if (light < 12) {
			return DARK;
		} else if (light < 15) {
			return DIM;
		} else {
			return BRIGHT;
		}
	}
}
