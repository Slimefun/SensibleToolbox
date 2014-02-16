package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.recipes.FuelItems;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.CoalType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Coal;
import org.bukkit.material.MaterialData;

public class StirlingGenerator extends Generator {
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.ORANGE);
	private static final FuelItems fuelItems = new FuelItems();
	static {
		fuelItems.addFuel(new Coal(CoalType.CHARCOAL).toItemStack(), 15, 60);
		fuelItems.addFuel(new ItemStack(Material.COAL), 15, 120);
		fuelItems.addFuel(new ItemStack(Material.COAL_BLOCK), 15, 1440);
		fuelItems.addFuel(new ItemStack(Material.BLAZE_ROD), 15, 180);
		fuelItems.addFuel(new ItemStack(Material.LOG), 10, 40);
		fuelItems.addFuel(new ItemStack(Material.LOG_2), 10, 40);
		fuelItems.addFuel(new ItemStack(Material.WOOD), 5, 20);
		fuelItems.addFuel(new ItemStack(Material.STICK), 2.5, 20);
	}
	private FuelItems.FuelValues currentFuel;

	public StirlingGenerator() {
		super();
		currentFuel = null;
	}

	public StirlingGenerator(ConfigurationSection conf) {
		super(conf);
		if (getProgress() > 0) {
			currentFuel = fuelItems.get(getInventory().getItem(getProgressItemSlot()));
		}
	}

	@Override
	public int[] getInputSlots() {
		return new int[] { 10 };
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];  // no output slot
	}

	@Override
	public int[] getUpgradeSlots() {
		return new int[0]; // maybe in future
	}

	@Override
	public int getUpgradeLabelSlot() {
		return -1;
	}

	@Override
	protected void playActiveParticleEffect() {
		if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled() && getTicksLived() % 20 == 0) {
			ParticleEffect.FLAME.play(getLocation().add(0.5, 1.0, 0.5), 0.3f, 0.6f, 0.3f, 0.001f, 15);
		}
	}

	@Override
	public int getEnergyCellSlot() {
		return 36;
	}

	@Override
	public int getChargeDirectionSlot() {
		return 37;
	}

	@Override
	public int getInventoryGUISize() {
		return 45;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Stirling Generator";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Converts burnable items to power",
				"Generates ⌁ 15 SCU / tick"
		};
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("III", "PCP", "RGR");
		recipe.setIngredient('I', Material.IRON_INGOT);
		recipe.setIngredient('P', Material.PISTON_BASE);
		recipe.setIngredient('C', Material.CAULDRON_ITEM);
		recipe.setIngredient('G', Material.GOLD_INGOT);
		recipe.setIngredient('R', Material.REDSTONE);
		return recipe;
	}

	@Override
	public int getMaxCharge() {
		return 5000;
	}

	@Override
	public int getChargeRate() {
		return 50;
	}

	@Override
	public int getProgressItemSlot() {
		return 12;
	}

	@Override
	public int getProgressCounterSlot() {
		return 3;
	}

	@Override
	public Material getProgressIcon() {
		return Material.FLINT_AND_STEEL;
	}

	@Override
	public boolean acceptsItemType(ItemStack item) {
		return fuelItems.has(item);
	}

	@Override
	public void onServerTick() {
		if (isRedstoneActive()) {
			if (getProcessing() == null && getCharge() < getMaxCharge()) {
				for (int slot : getInputSlots()) {
					if (getInventory().getItem(slot) != null) {
						pullItemIntoProcessing(slot);
						break;
					}
				}
			} else if (getProgress() > 0) {
				// currently processing....
				setProgress(getProgress() - 1);
				setCharge(Math.min(getMaxCharge(), getCharge() + currentFuel.getCharge()));
				playActiveParticleEffect();
				if (getProgress() <= 0) {
					// fuel burnt
					setProcessing(null);
					updateBlock(false);
				}
			}
		}
		super.onServerTick();
	}

	private void pullItemIntoProcessing(int inputSlot) {
		ItemStack stack = getInventory().getItem(inputSlot);
		ItemStack toProcess = stack.clone();
		toProcess.setAmount(1);
		setProcessing(toProcess);
		currentFuel = fuelItems.get(toProcess);
		getProgressCounter().initialize(currentFuel.getBurnTime());
		setProgress(currentFuel.getBurnTime());
		stack.setAmount(stack.getAmount() - 1);
		getInventory().setItem(inputSlot, stack.getAmount() > 0 ? stack : null);
		updateBlock(false);
	}
}
