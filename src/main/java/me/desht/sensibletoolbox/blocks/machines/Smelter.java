package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.GoldDust;
import me.desht.sensibletoolbox.items.IronDust;
import me.desht.sensibletoolbox.recipes.CustomRecipe;
import me.desht.sensibletoolbox.recipes.CustomRecipeCollection;
import me.desht.sensibletoolbox.recipes.CustomRecipeManager;
import me.desht.sensibletoolbox.recipes.ProcessingResult;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.Iterator;

public class Smelter extends AbstractIOMachine {
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.LIGHT_BLUE);
//	private static final CustomRecipeCollection recipes = new CustomRecipeCollection();
//
//	static {
//		Iterator<Recipe> iter = Bukkit.recipeIterator();
//		while (iter.hasNext()) {
//			Recipe r = iter.next();
//			if (r instanceof FurnaceRecipe) {
//				FurnaceRecipe fr = (FurnaceRecipe) r;
//				ItemStack input = fr.getInput();
//				// why does the input item for a furnace recipe always have 32767 durability ?
//				if (input.getDurability() == 32767) input.setDurability((short) 0);
//				recipes.addCustomRecipe(input, fr.getResult(), getProcessingTime(input));
//			}
//		}
//		recipes.addCustomRecipe(new GoldDust().toItemStack(1), new ItemStack(Material.GOLD_INGOT), 120);
//		recipes.addCustomRecipe(new IronDust().toItemStack(1), new ItemStack(Material.IRON_INGOT), 120);
//	}

	private static int getProcessingTime(ItemStack stack) {
		if (stack.getType().isEdible()) {
			return 40;  // food cooks a lot quicker than ores etc.
		}
		return 120;
	}

	public Smelter() {
	}

	public Smelter(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public void addCustomRecipes(CustomRecipeManager crm) {
		Iterator<Recipe> iter = Bukkit.recipeIterator();
		while (iter.hasNext()) {
			Recipe r = iter.next();
			if (r instanceof FurnaceRecipe) {
				FurnaceRecipe fr = (FurnaceRecipe) r;
				ItemStack input = fr.getInput();
				// why does the input item for a furnace recipe always have 32767 durability ?
				if (input.getDurability() == 32767) input.setDurability((short) 0);
				crm.addCustomRecipe(new CustomRecipe(this, input, fr.getResult(), getProcessingTime(input)));
			}
		}
		crm.addCustomRecipe(new CustomRecipe(this, new GoldDust().toItemStack(1), new ItemStack(Material.GOLD_INGOT), 120));
		crm.addCustomRecipe(new CustomRecipe(this, new IronDust().toItemStack(1), new ItemStack(Material.IRON_INGOT), 120));
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
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Smelter";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Smelts items", "Like a vanilla furnace but", "faster and more efficient" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("SSS", "CIC", "RGR");
		recipe.setIngredient('C', Material.BRICK);
		recipe.setIngredient('S', Material.STONE);
		recipe.setIngredient('I', Material.IRON_BLOCK);
		recipe.setIngredient('R' ,Material.REDSTONE);
		recipe.setIngredient('G', Material.GOLD_INGOT);
		return recipe;
	}
//
//	@Override
//	public boolean acceptsItemType(ItemStack item) {
//		return CustomRecipeManager.getManager().hasRecipe(this, item);
//	}
//
//	@Override
//	protected ProcessingResult getCustomRecipeFor(ItemStack stack) {
//		return CustomRecipeManager.getManager().getRecipe(this, stack);
//	}

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
	public int getMaxCharge() {
		return 1000;
	}

	@Override
	public int getChargeRate() {
		return 20;
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
	protected void playStartupSound() {
		getLocation().getWorld().playSound(getLocation(), Sound.FIRE, 1.0f, 1.0f);
	}

	@Override
	protected void playActiveParticleEffect() {
		if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled() && getTicksLived() % 20 == 0) {
				ParticleEffect.FLAME.play(getLocation().add(0.5, 0.5, 0.5), 0.5f, 0.5f, 0.5f, 0.001f, 7);
		}
	}
}
