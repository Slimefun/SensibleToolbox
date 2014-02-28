package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.components.MachineFrame;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import me.desht.sensibletoolbox.recipes.CustomRecipe;
import me.desht.sensibletoolbox.recipes.CustomRecipeManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class Sawmill extends AbstractIOMachine {
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.BROWN);

	public Sawmill() {
	}

	public Sawmill(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public void addCustomRecipes(CustomRecipeManager crm) {
		for (short i = 0; i < 6; i++) {
			ItemStack in = i < 4 ? new ItemStack(Material.LOG, 1, i) : new ItemStack(Material.LOG_2, 1, (short) (i - 4));
			crm.addCustomRecipe(new CustomRecipe(this, in, new ItemStack(Material.WOOD, 6, i), 60));
		}
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.WOOD_DOOR), new ItemStack(Material.WOOD, 6), 40));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.TRAP_DOOR), new ItemStack(Material.WOOD, 3), 40));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.WOOD_PLATE), new ItemStack(Material.WOOD, 2), 40));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.WOOD_BUTTON), new ItemStack(Material.WOOD, 1), 40));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.WORKBENCH), new ItemStack(Material.WOOD, 4), 40));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.SIGN), new ItemStack(Material.WOOD, 2), 40));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.CHEST), new ItemStack(Material.WOOD, 8), 40));
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
		return Material.GOLD_AXE;
	}

	@Override
	public int[] getInputSlots() {
		return new int[] { 10 };
	}

	@Override
	public int[] getOutputSlots() {
		return new int[] { 14 };
	}

	@Override
	public int[] getUpgradeSlots() {
		return new int[] { 41, 42, 43, 44 };
	}

	@Override
	public int getUpgradeLabelSlot() {
		return 40;
	}

	@Override
	protected void playActiveParticleEffect() {
		if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled() && getTicksLived() % 20 == 0) {
			ParticleEffect.CLOUD.play(getLocation().add(0.5, 0.5, 0.5), 0.5f, 0.5f, 0.5f, 0.001f, 7);
		}
	}

	@Override
	protected void playStartupSound() {
		getLocation().getWorld().playSound(getLocation(), Sound.HORSE_WOOD, 1.0f, 0.5f);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Sawmill";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Processes wooden items" };
	}

	@Override
	public Recipe getRecipe() {
		SimpleCircuit sc = new SimpleCircuit();
		MachineFrame mf = new MachineFrame();
		registerCustomIngredients(sc, mf);
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("WAW", "IFI", "RGR");
		recipe.setIngredient('W', Material.WOOD);
		recipe.setIngredient('A', Material.IRON_AXE);
		recipe.setIngredient('I', sc.getMaterialData());
		recipe.setIngredient('F', mf.getMaterialData());
		recipe.setIngredient('R' ,Material.REDSTONE);
		recipe.setIngredient('G', Material.GOLD_INGOT);
		return recipe;
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
	public int getMaxCharge() {
		return 1000;
	}

	@Override
	public int getChargeRate() {
		return 20;
	}
}
