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

    /**
     * Get the material used to represent this item.
     *
     * @return the material
     */
    Material getMaterial();

    /**
     * Get the item's displayed name.
     *
     * @return the item name
     */
    String getItemName();

    /**
     * Get any suffix to be appended to the item's displayed name.  Override this in
     * implementing classes where you wish to represent some or all of the item's state
     * in the display name.
     *
     * @return the display suffix
     */
    String getDisplaySuffix();

    /**
     * Get the base lore to display for the item.
     *
     * @return the item lore
     */
    String[] getLore();

    /**
     * Get extra lore to be appended to the base lore.  Override this in
     * implementing classes where you wish to represent some or all of the item's state
     * in the item lore.
     *
     * @return the extra item lore
     */
    String[] getExtraLore();

    /**
     * Get the recipe used to create the item.
     *
     * @return the recipe, or null if the item does not have a vanilla crafting recipe
     */
    Recipe getRecipe();

    /**
     * Get any alternative recipes used to create the item.
     *
     * @return an array of recipes
     */
    Recipe[] getExtraRecipes();

    /**
     * Given a material name, return the type of STB item that crafting ingredients of this type
     * must be to count as a valid crafting ingredient for this item.
     *
     * @param mat the ingredient material
     * @return null for no restriction, or a BaseSTBItem subclass to specify a restriction
     */
    Class<? extends STBItem> getCraftingRestriction(Material mat);

    /**
     * Check if this item is used as an ingredient for the given resulting item.
     *
     * @param result the resulting item
     * @return true if this item may be used, false otherwise
     */
    boolean isIngredientFor(ItemStack result);

    /**
     * Check if the item should glow.  This will only work if ProtocolLib is installed.
     *
     * @return true if the item should glow
     */
    boolean hasGlow();

    /**
     * Get the item into which this item would be smelted.
     *
     * @return the resulting itemstack, or null if this object does not smelt
     */
    ItemStack getSmeltingResult();

    /**
     * Check if this item can be enchanted normally in an enchanting table.
     *
     * @return true if the item can be enchanted
     */
    boolean isEnchantable();

    /**
     * Get an ItemStack with one item from this STB item, serializing any item-specific data into the ItemStack.
     *
     * @return the new ItemStack
     */
    ItemStack toItemStack();

    /**
     * Get an ItemStack from this STB item, serializing any item-specific data into the ItemStack.
     *
     * @param amount number of items in the stack
     * @return the new ItemStack
     */
    ItemStack toItemStack(int amount);

    /**
     * Get the short type identifier code for this item.
     *
     * @return the item's type ID
     */
    String getItemTypeID();

    /**
     * Check if this item is wearable.  By default, any armour item will be wearable, but if you wish to use
     * an armour material for a non-wearable item, then override this method.
     *
     * @return true if the item is wearable
     */
    boolean isWearable();
}
