package me.desht.sensibletoolbox.recipes;

import me.desht.sensibletoolbox.api.STBItem;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple one-to-one mapping of an ingredient to a result.  See
 * {@link me.desht.sensibletoolbox.recipes.ShapelessCustomRecipe} for a class
 * which supports multiple ingredients and results.
 */
public class SimpleCustomRecipe implements CustomRecipe {
    private final String processorID;  // id of STB item which makes this
    private final ItemStack ingredient;
    private final ItemStack result;
    private final int processingTime; // in ticks

    public SimpleCustomRecipe(STBItem processor, ItemStack ingredient, ItemStack result, int processingTime) {
        this.processorID = processor.getItemTypeID();
        this.ingredient = ingredient;
        this.result = result;
        this.processingTime = processingTime;
    }

//    public SimpleCustomRecipe(String itemId, ItemStack ingredient, ItemStack result, int processingTime) {
//        this.processorID = itemId;
//        this.ingredient = ingredient;
//        this.result = result;
//        this.processingTime = processingTime;
//    }

    /**
     * Get the time in ticks needed to make this recipe.
     *
     * @return the processing time, in ticks
     */
    public int getProcessingTime() {
        return processingTime;
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    /**
     * Get the item ID of the STB machine which is used to make this recipe.
     *
     * @return the STB item ID
     */
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
     * Get the input ingredient for this recipe.
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
