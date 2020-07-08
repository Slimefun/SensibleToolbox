package io.github.thebusybiscuit.sensibletoolbox.api.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.google.common.base.Joiner;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;

/**
 * Represents the custom recipes known by a specific type of machine.
 */
public class CustomRecipeCollection {

    private final Map<String, ProcessingResult> recipes = new HashMap<>();

    /**
     * Register a new custom recipe with the recipe manager.
     *
     * @param recipe
     *            the custom recipe to add
     * @param allowWild
     *            if true, add another recipe with the ingredients
     *            wildcarded (data value ignored)
     */
    public void addCustomRecipe(CustomRecipe recipe, boolean allowWild) {
        String key = recipe.makeKey(false);
        ProcessingResult pr = new ProcessingResult(recipe.getResult(), recipe.getProcessingTime());
        recipes.put(key, pr);

        if (allowWild) {
            recipes.put(recipe.makeKey(true), pr);
        }

        Debugger.getInstance().debug("added custom recipe: [" + key + "] => " + recipe.getResult() + " via " + recipe.getProcessorID());
    }

    /**
     * Register a new custom recipe with the recipe manager.
     *
     * @param recipe
     *            the custom recipe to add
     */
    public void addCustomRecipe(CustomRecipe recipe) {
        addCustomRecipe(recipe, false);
    }

    /**
     * Try to find a processing result, given a list of ingredients.
     *
     * @param shaped
     *            if true, use a shaped recipe
     * @param input
     *            list of input ingredients
     * @return a processing result, or null if nothing found
     */
    public ProcessingResult get(boolean shaped, ItemStack... input) {
        String key = makeKey(shaped, false, input);
        ProcessingResult res = recipes.get(key);

        if (res == null) {
            // check for a recipe with wildcarded data
            key = makeKey(shaped, true, input);
            res = recipes.get(key);
        }

        return res;
    }

    /**
     * Check if this collection has a recipe for the given ingredients.
     *
     * @param shaped
     *            if true, use a shaped recipe
     * @param input
     *            list of input ingredients
     * @return true if there is a known recipe, false otherwise
     */
    public boolean hasRecipe(boolean shaped, ItemStack... input) {
        String key = makeKey(shaped, false, input);

        if (recipes.containsKey(key)) {
            return true;
        }
        else {
            key = makeKey(shaped, true, input);
            return recipes.containsKey(key);
        }
    }

    private String makeKey(boolean shaped, boolean ignoreData, ItemStack... input) {
        if (input.length == 1) {
            // common case
            return "1x" + RecipeUtil.makeRecipeKey(ignoreData, input[0]);
        }
        List<String> l = new ArrayList<>(input.length);

        for (ItemStack stack : input) {
            if (stack == null) {
                if (shaped) {
                    l.add("");
                }
                else {
                    throw new IllegalArgumentException("null items not allowed for shapeless recipes");
                }
            }
            else {
                l.add(stack.getAmount() + "x" + RecipeUtil.makeRecipeKey(ignoreData, stack));
            }
        }

        return Joiner.on(";").join(MiscUtil.asSortedList(l));
    }
}
