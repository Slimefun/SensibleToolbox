package me.desht.sensibletoolbox.recipes;

import me.desht.dhutils.Debugger;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the custom recipes known by a specific type of machine.
 */
public class CustomRecipeCollection {
    private final Map<ItemStack, ProcessingResult> recipes = new HashMap<ItemStack, ProcessingResult>();

    public void addCustomRecipe(ItemStack input, ItemStack result, int processingTime) {
        recipes.put(CustomRecipeManager.makeSingle(input), new ProcessingResult(result, processingTime));
        Debugger.getInstance().debug("added custom recipe: " + input + " -> " + get(input).toString());
    }

    public void addCustomRecipe(CustomRecipe recipe) {
        recipes.put(CustomRecipeManager.makeSingle(recipe.getIngredient()), new ProcessingResult(recipe.getResult(), recipe.getProcessingTime()));
        Debugger.getInstance().debug("added custom recipe: " + recipe.getIngredient().getType() + ":" + recipe.getIngredient().getDurability()
                + " -> " + recipe.getResult() + " via " + recipe.getProcessorID());
    }

    public ProcessingResult get(ItemStack input) {
        ItemStack single = CustomRecipeManager.makeSingle(input);
        ProcessingResult res = recipes.get(single);
        if (res == null && single.getDurability() != -1) {
            // perhaps there's a recipe with wildcarded data?
            single.setDurability((short) 32767);
            res = recipes.get(single);
        }
        return res;
    }

    public boolean hasRecipe(ItemStack input) {
        ItemStack single = CustomRecipeManager.makeSingle(input);
        if (recipes.containsKey(single)) {
            return true;
        } else if (single.getDurability() != -1) {
            single.setDurability((short) 32767);
            return recipes.containsKey(single);
        } else {
            return false;
        }
    }

}
