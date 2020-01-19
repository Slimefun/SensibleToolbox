package io.github.thebusybiscuit.sensibletoolbox.api.recipes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;

/**
 * Collection of miscellaneous recipe-related utility methods.
 */
public class RecipeUtil {
    private static final Set<Material> vanillaSmelts = Sets.newHashSet();
    private static final Map<ItemStack, List<ItemStack>> reverseCustomSmelts = Maps.newHashMap();

    public static void setupRecipes() {
        for (String key : SensibleToolbox.getItemRegistry().getItemIds()) {
            // add custom workbench recipes
            BaseSTBItem item = SensibleToolbox.getItemRegistry().getItemById(key);
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

    public static String makeRecipeKey(ItemStack item) {
        String res = item.getType().toString();
        if (item.getDurability() != -1) {
            res += ":" + item.getDurability();
        }
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            res += ":" + item.getItemMeta().getDisplayName();
        }
        return res;
    }

    public static String makeRecipeKey(boolean ignoreData, ItemStack item) {
    	if (item == null) return "";
        String res = item.getType().toString();
        if (!ignoreData && item.getDurability() != 32767) {
            res += ":" + item.getDurability();
        }
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            res += ":" + item.getItemMeta().getDisplayName();
        }
        return res;
    }
}
