package me.desht.sensibletoolbox.api.recipes;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.api.items.BaseSTBMachine;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a shapeless recipe with potentially multiple ingredients and
 * multiple results, with a definable chance for each result to be produced.
 */
public class ShapelessCustomRecipe implements CustomRecipe {
    private final List<SupplementaryResult> extra = Lists.newArrayList();
    private final Map<String,Integer> ingredients = Maps.newHashMap();
    private final String processorID;
    private final int processingTime;
    private final ItemStack result;

    public ShapelessCustomRecipe(ItemStack result, BaseSTBMachine processor, int processingTime) {
        this.result = result;
        this.processingTime = processingTime;
        this.processorID = processor.getItemTypeID();
    }

    @Override
    public int getProcessingTime() {
        return processingTime;
    }

    @Override
    public String getProcessorID() {
        return processorID;
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    /**
     * Add an ingredient to this recipe.
     *
     * @param ingredient the ingredient to add
     */
    public void addIngredient(ItemStack ingredient) {
        String key = RecipeUtil.makeRecipeKey(ingredient);
        int val = ingredients.containsKey(key) ? ingredients.get(key) : 0;
        ingredients.put(key, val + ingredient.getAmount());
    }

//    public void removeIngredient(ItemStack ingredient) {
//        String key = RecipeUtil.makeRecipeKey(ingredient);
//        if (ingredients.containsKey(key)) {
//            int val = ingredients.get(key) - ingredient.getAmount();
//            if (val <= 0) {
//                ingredients.remove(key);
//            } else {
//                ingredients.put(key, val);
//            }
//        }
//    }
//
//    public boolean matches(boolean exact, ItemStack... stacks) {
//        Map<String,Integer> copy = Maps.newHashMap(ingredients);
//        for (ItemStack stack : stacks) {
//            String key = RecipeUtil.makeRecipeKey(stack);
//            if (copy.containsKey(key)) {
//                copy.put(key, copy.get(key) - stack.getAmount());
//            }
//        }
//        for (int i : copy.values()) {
//            if (exact && i != 0 || !exact && i > 0) {
//                return false;
//            }
//        }
//        return true;
//    }

    @Override
    public void addSupplementaryResult(SupplementaryResult result) {
        extra.add(result);
    }

    @Override
    public Collection<SupplementaryResult> listSupplementaryResults() {
        return extra;
    }

    @Override
    public Collection<ItemStack> calculateSupplementaryResults() {
        List<ItemStack> res = Lists.newArrayList();
        Random rnd = new Random();
        for (SupplementaryResult sr : listSupplementaryResults()) {
            if (rnd.nextInt(1000) < sr.getChance()) {
                res.add(sr.getResult());
            }
        }
        return res;
    }

    @Override
    public String makeKey(boolean ignoreData) {
        List<String> l = Lists.newArrayList();
        for (Map.Entry<String,Integer> m : ingredients.entrySet()) {
            String s = ignoreData ? m.getKey().replaceAll(":\\d+", "") : m.getKey();
            l.add(m.getValue() + "x" + s);
        }
        return Joiner.on(";").join(MiscUtil.asSortedList(l));
    }
}
