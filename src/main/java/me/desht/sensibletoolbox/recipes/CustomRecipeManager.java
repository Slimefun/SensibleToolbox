package me.desht.sensibletoolbox.recipes;

import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
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

    public static synchronized CustomRecipeManager getManager() {
        if (instance == null) {
            instance = new CustomRecipeManager();
        }
        return instance;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

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

    public void addCustomRecipe(CustomRecipe recipe) {
        addCustomRecipe(recipe, false);
    }

    public List<CustomRecipe> getRecipesFor(ItemStack result) {
        List<CustomRecipe> res = reverseMap.get(makeSingle(result));
        return res == null ? new ArrayList<CustomRecipe>() : new ArrayList<CustomRecipe>(res);
    }

    public ProcessingResult getRecipe(BaseSTBMachine machine, ItemStack... ingredients) {
        CustomRecipeCollection collection = map.get(machine.getItemTypeID());
        return collection == null ? null : collection.get(machine.hasShapedRecipes(), ingredients);
    }

    public boolean hasRecipe(BaseSTBMachine machine, ItemStack... ingredients) {
        CustomRecipeCollection collection = map.get(machine.getItemTypeID());
        return collection != null && collection.hasRecipe(machine.hasShapedRecipes(), ingredients);
    }

    public Set<ItemStack> getAllResults() {
        return reverseMap.keySet();
    }

    public static ItemStack makeSingle(ItemStack stack) {
        ItemStack stack2 = stack.clone();
        stack2.setAmount(1);
        return stack2;
    }
}
