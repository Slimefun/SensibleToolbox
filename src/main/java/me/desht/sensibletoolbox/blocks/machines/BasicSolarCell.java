package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.api.LightSensitive;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.LightMeter;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class BasicSolarCell extends BaseSTBMachine implements LightSensitive {
	private static final MaterialData md = new MaterialData(Material.LAPIS_BLOCK);
	private static final int ENERGY_RATE = 20;  // how often to recalculate light & energy levels
	private static final double SCU_PER_TICK = 1.0;
	private static final int LIGHT_SLOT = 13;

	private byte effectiveLightLevel;
	private int lightMeterId;

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
			calculateLightLevel();

			if (getCharge() < getMaxCharge()) {
				double toAdd = SCU_PER_TICK * ENERGY_RATE * getChargeMultiplier(getLightLevel());
				setCharge(getCharge() + toAdd);
			}

			getLightMeter().doRepaint();
		}
		super.onServerTick();
	}

	private LightMeter getLightMeter() {
		return (LightMeter) getGUI().getMonitor(lightMeterId);
	}

	private void calculateLightLevel() {
		Block b = getLocation().getBlock().getRelative(BlockFace.UP);
		byte lightFromSky = b.getLightFromSky();
		byte newLight = lightFromSky < 14 ? 0 : b.getLightLevel();
		if (lightFromSky < 15) newLight--;
		if (b.getWorld().hasStorm()) newLight--;
		if (newLight != effectiveLightLevel) {
			getLightMeter().repaintNeeded();
			effectiveLightLevel = newLight;
		}
	}

	@Override
	public byte getLightLevel() {
		return effectiveLightLevel;
	}

	@Override
	protected InventoryGUI createGUI() {
		InventoryGUI gui = super.createGUI();

		lightMeterId = gui.addMonitor(new LightMeter(gui));

		return gui;
	}

	@Override
	public int getLightMeterSlot() {
		return LIGHT_SLOT;
	}

	private double getChargeMultiplier(byte light) {
		switch (light) {
			case 15: return 1.0;
			case 14: return 0.75;
			case 13: return 0.5;
			case 12: return 0.25;
			default: return 0.0;
		}
	}

}
