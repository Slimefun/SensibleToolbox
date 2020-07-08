package io.github.thebusybiscuit.sensibletoolbox.api.recipes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Collection;

/**
 * Represents a recipe which takes a defined length of time and a specific
 * STB machine to process it.
 */
public interface CustomRecipe extends Recipe {
    
    /**
     * Get the time in server ticks needed to make this recipe.
     *
     * @return the processing time, in ticks
     */
    public int getProcessingTime();

    /**
     * Get the item ID of the STB machine which is used to make this recipe.
     *
     * @return the STB item ID
     */
    public String getProcessorID();

    /**
     * Add a supplementary result to this recipe.
     *
     * @param result the supplementary result to add
     */
    public void addSupplementaryResult(SupplementaryResult result) throws UnsupportedOperationException;

    /**
     * List the possible supplementary results from this recipe.
     *
     * @return a collection of supplementary results
     */
    public Collection<SupplementaryResult> listSupplementaryResults();

    /**
     * Perform a one-off calculation of the actual supplementary results for
     * this recipe, based on the result chances.  The return value of this
     * method is likely to be different each time it is called, based on the
     * defined result chances.
     *
     * @return a collection of item stacks
     */
    public Collection<ItemStack> calculateSupplementaryResults();

    /**
     * Construct a key for this recipe based on its ingredients, which
     * uniquely identifies it, so that it can be efficiently looked up.
     *
     * @param ignoreData if true, create a key without the items' data values
     * @return a unique key string for this recipe
     */
    public String makeKey(boolean ignoreData);
}
