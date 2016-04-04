package me.mrCookieSlime.sensibletoolbox.slimefun.machines;

import java.util.Arrays;
import java.util.List;

import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;
import me.mrCookieSlime.sensibletoolbox.api.recipes.CustomRecipeManager;
import me.mrCookieSlime.sensibletoolbox.api.recipes.SimpleCustomRecipe;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.slimefun.SlimefunIOMachine;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class CompactSmeltery extends SlimefunIOMachine {
	
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.BROWN);

    public CompactSmeltery() {
    }

    public CompactSmeltery(ConfigurationSection conf) {
        super(conf);
    }

	@Override
	protected String getSlimefunMachine() {
		return "GRIND_STONE";
	}
	
	@Override
    public void addCustomRecipes(final CustomRecipeManager crm) {
		crm.addCustomRecipe(new SimpleCustomRecipe(this, SlimefunItems.IRON_DUST, new ItemStack(Material.IRON_INGOT), 140));
		crm.addCustomRecipe(new SimpleCustomRecipe(this, SlimefunItems.GOLD_DUST, SlimefunItems.GOLD_4K, 140));
		crm.addCustomRecipe(new SimpleCustomRecipe(this, SlimefunItems.SILVER_DUST, SlimefunItems.SILVER_INGOT, 140));
		crm.addCustomRecipe(new SimpleCustomRecipe(this, SlimefunItems.COPPER_DUST, SlimefunItems.COPPER_INGOT, 140));
		crm.addCustomRecipe(new SimpleCustomRecipe(this, SlimefunItems.TIN_DUST, SlimefunItems.TIN_INGOT, 140));
		crm.addCustomRecipe(new SimpleCustomRecipe(this, SlimefunItems.ZINC_DUST, SlimefunItems.ZINC_INGOT, 140));
		crm.addCustomRecipe(new SimpleCustomRecipe(this, SlimefunItems.LEAD_DUST, SlimefunItems.LEAD_INGOT, 140));
		crm.addCustomRecipe(new SimpleCustomRecipe(this, SlimefunItems.ALUMINUM_DUST, SlimefunItems.ALUMINUM_INGOT, 140));
		crm.addCustomRecipe(new SimpleCustomRecipe(this, SlimefunItems.MAGNESIUM_DUST, SlimefunItems.MAGNESIUM_INGOT, 140));
	}

	@Override
	public List<ItemStack> getSlimefunRecipe() {
		return Arrays.asList(
				null,
				SlimefunItems.LAVA_CRYSTAL,
				null,
				SlimefunItems.GOLD_10K,
				SlimefunItems.BASIC_CIRCUIT_BOARD,
				SlimefunItems.GOLD_10K,
				SlimefunItems.STEEL_INGOT,
				SlimefunItems.DAMASCUS_STEEL_INGOT,
				SlimefunItems.STEEL_INGOT
		);
	}

	@Override
	protected void onMachineStartup() {
		if (SensibleToolbox.getPluginInstance().getConfigCache().isNoisyMachines()) {
            getLocation().getWorld().playSound(getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
        }
	}

	@Override
	public ItemStack getProgressIcon() {
		return new ItemStack(Material.FLINT_AND_STEEL);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Compact Smeltery";
	}

	@Override
	public String[] getLore() {
		return new String[] {"A powered Smeltery which is", "only capable of smelting Dust to Ingots"};
	}
    
}
