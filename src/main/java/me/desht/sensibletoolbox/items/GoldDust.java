package me.desht.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class GoldDust extends BaseSTBItem {
	@Override
	public Material getBaseMaterial() {
		return Material.GLOWSTONE_DUST;
	}

	@Override
	public String getItemName() {
		return "Gold Dust";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Smelt in a Smelter or Furnace", " to get gold ingots"};
	}

	@Override
	public Recipe getRecipe() {
		return null;  // Only made by the Masher
	}

	@Override
	public boolean hasGlow() {
		return true;
	}

	@Override
	public ItemStack getSmeltingResult() {
		return new ItemStack(Material.GOLD_INGOT);
	}
}
