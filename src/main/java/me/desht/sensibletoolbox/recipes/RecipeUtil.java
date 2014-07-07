package me.desht.sensibletoolbox.recipes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.desht.sensibletoolbox.api.STBItem;
import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;

public class RecipeUtil {
    private static final Set<Material> vanillaSmelts = Sets.newHashSet();
    private static final Map<ItemStack, List<ItemStack>> reverseCustomSmelts = Maps.newHashMap();

    public static void setupRecipes() {
        for (String key : BaseSTBItem.getItemIds()) {
            // add custom workbench recipes
            STBItem item = BaseSTBItem.getItemById(key);
            Recipe r = item.getRecipe();
            if (r != null) {
                Bukkit.addRecipe(r);
            }
            for (Recipe r2 : item.getExtraRecipes()) {
                Bukkit.addRecipe(r2);
            }

            // add custom furnace recipes
            ItemStack stack = item.getSmeltingResult();
            if (stack != null) {
                Bukkit.addRecipe(new FurnaceRecipe(stack, item.getMaterialData()));
                recordReverseSmelt(stack, item.toItemStack());
            }

            // add custom processing recipes for any machine items
            if (item instanceof BaseSTBMachine) {
                ((BaseSTBMachine) item).addCustomRecipes(CustomRecipeManager.getManager());
            }
        }
    }

    private static void recordReverseSmelt(ItemStack result, ItemStack ingredient) {
        if (!reverseCustomSmelts.containsKey(result)) {
            reverseCustomSmelts.put(result, new ArrayList<ItemStack>());
        }
        reverseCustomSmelts.get(result).add(ingredient);
    }

    /**
     * Given an item, get all the items which could be smelted into that item
     * via a vanilla furnace recipe, possibly one which was added by an STB
     * item.
     *
     * @param stack the item stack to check
     * @return a list of the ingredients which could be smelted into the item
     */
    public static List<ItemStack> getSmeltingIngredientsFor(ItemStack stack) {
        List<ItemStack> res = reverseCustomSmelts.get(stack);
        return res == null ? Collections.<ItemStack>emptyList() : res;
    }

    public static void findVanillaFurnaceMaterials() {
        Iterator<Recipe> iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            Recipe r = iter.next();
            if (r instanceof FurnaceRecipe) {
                Material mat = ((FurnaceRecipe) r).getInput().getType();
                vanillaSmelts.add(mat);
                recordReverseSmelt(r.getResult(), ((FurnaceRecipe) r).getInput());
            }
        }
    }

    /**
     * Check if vanilla items of the given material may be smelted in a
     * furnace (or smelter).  "Vanilla" means an item stack with no custom
     * metadata defined, in particular STB item metadata.
     *
     * @param mat the material to check
     * @return true if vanilla items of the material may be smelted
     */
    public static boolean isVanillaSmelt(Material mat) {
        return vanillaSmelts.contains(mat);
    }
}
