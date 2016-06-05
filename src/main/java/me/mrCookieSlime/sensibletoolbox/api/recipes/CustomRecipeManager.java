package me.mrCookieSlime.sensibletoolbox.api.recipes;

import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBMachine;

import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * This class manages custom recipes known to STB.  A custom recipe requires a
 * machine (object which subclasses BaseSTBMachine) to produce the result.
 * Custom recipes may be shaped (ShapedCustomRecipe) or more commonly
 * shapeless (ShapelessCustomRecipe).
 */
public class CustomRecipeManager {
    // maps a STB item ID to the custom recipes that STB item knows about
    private static final Map<String, CustomRecipeCollection> map = new HashMap<String, CustomRecipeCollection>();
    // maps an item to all the custom recipes for it
    private static final Map<ItemStack, List<CustomRecipe>> reverseMap = new HashMap<ItemStack, List<CustomRecipe>>();
    private static CustomRecipeManager instance;

    /**
     * Get the custom recipe manager instance.
     *
     * @return the custom recipe manager instance
     */
    public static synchronized CustomRecipeManager getManager() {
        if (instance == null) {
            instance = new CustomRecipeManager();
        }
        return instance;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Add a custom recipe for some STB machine.
     *
     * @param recipe the recipe to add
     * @param allowWild allow wildcarded data values
     */
    public void addCustomRecipe(CustomRecipe recipe, boolean allowWild) {
        CustomRecipeCollection collection = map.get(recipe.getProcessorID());
        if (collection == null) {
            collection = new CustomRecipeCollection();
            map.put(recipe.getProcessorID(), collection);
        }
        collection.addCustomRecipe(recipe, allowWild);

        ItemStack result = makeSingle(recipe.getResult());
        if (!reverseMap.containsKey(result)) {
            reverseMap.put(result, new ArrayList<CustomRecipe>());
        }
        reverseMap.get(result).add(recipe);
    }

    /**
     * Add a custom recipe for some STB machine.
     *
     * @param recipe the recipe to add
     */
    public void addCustomRecipe(CustomRecipe recipe) {
        addCustomRecipe(recipe, false);
    }

    /**
     * Get all known custom recipes which will make the given item.
     *
     * @param result the item for which to find recipes for
     * @return a list of custom recipes which can make the given item
     */
    public List<CustomRecipe> getRecipesFor(ItemStack result) {
        List<CustomRecipe> res = reverseMap.get(makeSingle(result));
        return res == null ? new ArrayList<CustomRecipe>() : new ArrayList<CustomRecipe>(res);
    }

    /**
     * Get the result (including processing time) for the given machine and
     * ingredients.
     *
     * @param machine an STB machine
     * @param ingredients some items to process
     * @return the resulting item, if any (may be null if no match)
     */
    public ProcessingResult getRecipe(BaseSTBMachine machine, ItemStack... ingredients) {
        CustomRecipeCollection collection = map.get(machine.getItemTypeID());
        return collection == null ? null : collection.get(machine.hasShapedRecipes(), ingredients);
    }

    /**
     * Check if the given machine can make anything with the given items.
     *
     * @param machine an STB machine
     * @param ingredients some items to process
     * @return true if the machine has a recipe to process the items; false otherwise
     */
    public boolean hasRecipe(BaseSTBMachine machine, ItemStack... ingredients) {
        CustomRecipeCollection collection = map.get(machine.getItemTypeID());
        return collection != null && collection.hasRecipe(machine.hasShapedRecipes(), ingredients);
    }

    /**
     * Get a set of all possible items that can be made via custom recipes.
     *
     * @return a set of items
     */
    public Set<ItemStack> getAllResults() {
        return reverseMap.keySet();
    }

    private static ItemStack makeSingle(ItemStack stack) {
        ItemStack stack2 = stack.clone();
        stack2.setAmount(1);
        return stack2;
    }
}
