package me.desht.sensibletoolbox.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

public interface STBItem {
	/**
	 * Get the material and data used to represent this item.
	 *
	 * @return the material data
	 */
	MaterialData getMaterialData();

	Material getMaterial();

	/**
	 * Get the item's displayed name.
	 *
	 * @return the item name
	 */
	String getItemName();

	String getDisplaySuffix();

	/**
	 * Get the base lore to display for the item.
	 *
	 * @return the item lore
	 */
	String[] getLore();

	String[] getExtraLore();

	/**
	 * Get the recipe used to create the item.
	 *
	 * @return the recipe, or null if the item does not have a vanilla crafting recipe
	 */
	Recipe getRecipe();

	Recipe[] getExtraRecipes();

	Class<? extends STBItem> getCraftingRestriction(Material mat);

	boolean isIngredientFor(ItemStack result);

	boolean hasGlow();

	ItemStack getSmeltingResult();

	boolean isEnchantable();

	ItemStack toItemStack();

	ItemStack toItemStack(int amount);

	String getItemTypeID();
}
