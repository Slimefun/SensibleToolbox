package me.desht.sensibletoolbox.recipes;

import me.desht.dhutils.Debugger;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the custom recipes known by a specific type of machine.
 */
public class CustomRecipeCollection {
    private final Map<String, ProcessingResult> recipes = new HashMap<String, ProcessingResult>();

    public void addCustomRecipe(ItemStack input, ItemStack result, int processingTime) {
        recipes.put(makeRecipeKey(input), new ProcessingResult(result, processingTime));
        Debugger.getInstance().debug("added custom recipe: " + input + " -> " + get(input).toString());
    }

    public void addCustomRecipe(CustomRecipe recipe) {
        recipes.put(makeRecipeKey(recipe.getIngredient()), new ProcessingResult(recipe.getResult(), recipe.getProcessingTime()));
        Debugger.getInstance().debug("added custom recipe: " + recipe.getIngredient().getType() + ":" + recipe.getIngredient().getDurability()
                + " -> " + recipe.getResult() + " via " + recipe.getProcessorID());
    }

    public ProcessingResult get(ItemStack input) {
        ProcessingResult res = recipes.get(makeRecipeKey(input));
        if (res == null && input.getDurability() != -1) {
            // perhaps there's a recipe with wildcarded data?
            ItemStack stack2 = input.clone();
            stack2.setDurability((short) 32767);
            res = recipes.get(makeRecipeKey(stack2));
        }
        return res;
    }

    public boolean hasRecipe(ItemStack input) {
        String key = makeRecipeKey(input);
        if (recipes.containsKey(key)) {
            return true;
        } else if (input.getDurability() != -1) {
            ItemStack stack2 = input.clone();
            stack2.setDurability((short) 32767);
            return recipes.containsKey(makeRecipeKey(stack2));
        } else {
            return false;
        }
    }

    private String makeRecipeKey(ItemStack item) {
        String res = item.getType() + ":" + item.getDurability();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            res += ":" + item.getItemMeta().getDisplayName();
        }
        return res;
    }
}
