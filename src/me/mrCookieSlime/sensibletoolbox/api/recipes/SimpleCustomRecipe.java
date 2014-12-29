package me.mrCookieSlime.sensibletoolbox.api.recipes;

import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple one-to-one mapping of an ingredient to a result.  See
 * {@link ShapelessCustomRecipe} for a class
 * which supports multiple ingredients and results.
 */
public class SimpleCustomRecipe implements CustomRecipe {
    private final String processorID;  // id of STB item which makes this
    private final ItemStack ingredient;
    private final ItemStack result;
    private final int processingTime; // in ticks

    public SimpleCustomRecipe(BaseSTBItem processor, ItemStack ingredient, ItemStack result, int processingTime) {
        this.processorID = processor.getItemTypeID();
        this.ingredient = ingredient;
        this.result = result;
        this.processingTime = processingTime;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    public String getProcessorID() {
        return processorID;
    }

    @Override
    public void addSupplementaryResult(SupplementaryResult result) {
        throw new UnsupportedOperationException("SimpleCustomRecipe doesn't have supplementary results");
    }

    /**
     * {@inheritDoc}
     *
     * @return an empty list
     */
    @Override
    public Collection<SupplementaryResult> listSupplementaryResults() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * @return an empty list
     */
    @Override
    public Collection<ItemStack> calculateSupplementaryResults() {
        return Collections.emptyList();
    }

    @Override
    public String makeKey(boolean ignoreData) {
        return "1x" + RecipeUtil.makeRecipeKey(ignoreData, ingredient);
    }

    /**
     * Get the single input ingredient for this recipe.
     *
     * @return the ingredient
     */
    public ItemStack getIngredient() {
        return ingredient;
    }

    @Override
    public String toString() {
        return "Custom recipe: " + processorID + " : " + ingredient + " -> " + result + " in " + processingTime + " ticks";
    }
}
