package me.mrCookieSlime.sensibletoolbox.slimefun.machines;

import java.util.Arrays;
import java.util.List;

import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.slimefun.SlimefunIOMachine;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ElectricalGrinder extends SlimefunIOMachine {
	
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.BROWN);

    public ElectricalGrinder() {
    }

    public ElectricalGrinder(ConfigurationSection conf) {
        super(conf);
    }

	@Override
	protected String getSlimefunMachine() {
		return "GRIND_STONE";
	}

	@Override
	public List<ItemStack> getSlimefunRecipe() {
		return Arrays.asList(
				null,
				new ItemStack(Material.IRON_PICKAXE),
				null,
				SlimefunItems.GOLD_6K,
				SlimefunItems.BASIC_CIRCUIT_BOARD,
				SlimefunItems.GOLD_6K,
				SlimefunItems.STEEL_INGOT,
				SlimefunItems.STEEL_INGOT,
				SlimefunItems.STEEL_INGOT
		);
	}

	@Override
	protected void onMachineStartup() {
		if (SensibleToolbox.getPluginInstance().getConfigCache().isNoisyMachines()) {
            getLocation().getWorld().playSound(getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
        }
	}

	@Override
	public ItemStack getProgressIcon() {
		return new ItemStack(Material.IRON_PICKAXE);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Electrical Grinder";
	}

	@Override
	public String[] getLore() {
		return new String[] {"Just like a normal Grind Stone", "but faster and powered"};
	}
    
}
