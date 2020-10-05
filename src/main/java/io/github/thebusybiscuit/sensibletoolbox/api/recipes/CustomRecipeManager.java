package io.github.thebusybiscuit.sensibletoolbox.api.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;

/**
 * This class manages custom recipes known to STB. A custom recipe requires a
 * machine (object which subclasses BaseSTBMachine) to produce the result.
 * Custom recipes may be shaped (ShapedCustomRecipe) or more commonly
 * shapeless (ShapelessCustomRecipe).
 * 
 * @author desht
 */
public class CustomRecipeManager {

    // maps a STB item ID to the custom recipes that STB item knows about
    private static final Map<String, CustomRecipeCollection> map = new HashMap<>();

    // maps an item to all the custom recipes for it
    private static final Map<ItemStack, List<CustomRecipe>> reverseMap = new HashMap<>();
    private static CustomRecipeManager instance;

    /**
     * Get the custom recipe manager instance.
     *
     * @return the custom recipe manager instance
     */
    @Nonnull
    public static synchronized CustomRecipeManager getManager() {
        if (instance == null) {
            instance = new CustomRecipeManager();
        }

        return instance;
    }

    /**
     * Add a custom recipe for some STB machine.
     *
     * @param recipe
     *            the recipe to add
     * @param allowWild
     *            allow wildcarded data values
     */
    public void addCustomRecipe(@Nonnull CustomRecipe recipe, boolean allowWild) {
        Validate.notNull(recipe, "A custom recipe cannot be null");
        CustomRecipeCollection collection = map.get(recipe.getProcessorID());

        if (collection == null) {
            collection = new CustomRecipeCollection();
            map.put(recipe.getProcessorID(), collection);
        }

        collection.addCustomRecipe(recipe, allowWild);

        ItemStack result = makeSingle(recipe.getResult());

        if (!reverseMap.containsKey(result)) {
            reverseMap.put(result, new ArrayList<>());
        }

        reverseMap.get(result).add(recipe);
    }

    /**
     * Add a custom recipe for some STB machine.
     *
     * @param recipe
     *            the recipe to add
     */
    public void addCustomRecipe(@Nonnull CustomRecipe recipe) {
        addCustomRecipe(recipe, false);
    }

    /**
     * Get all known custom recipes which will make the given item.
     *
     * @param result
     *            the item for which to find recipes for
     * @return a list of custom recipes which can make the given item
     */
    public List<CustomRecipe> getRecipesFor(@Nonnull ItemStack result) {
        List<CustomRecipe> res = reverseMap.get(makeSingle(result));
        return res == null ? new ArrayList<>() : new ArrayList<>(res);
    }

    /**
     * Get the result (including processing time) for the given machine and
     * ingredients.
     *
     * @param machine
     *            an STB machine
     * @param ingredients
     *            some items to process
     * @return the resulting item, if any (may be null if no match)
     */
    public ProcessingResult getRecipe(BaseSTBMachine machine, ItemStack... ingredients) {
        CustomRecipeCollection collection = map.get(machine.getItemTypeID());
        return collection == null ? null : collection.get(machine.hasShapedRecipes(), ingredients);
    }

    /**
     * Check if the given machine can make anything with the given items.
     *
     * @param machine
     *            an STB machine
     * @param ingredients
     *            some items to process
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
    @Nonnull
    public Set<ItemStack> getAllResults() {
        return reverseMap.keySet();
    }

    @Nonnull
    private static ItemStack makeSingle(@Nonnull ItemStack stack) {
        ItemStack stack2 = stack.clone();
        stack2.setAmount(1);
        return stack2;
    }
}
