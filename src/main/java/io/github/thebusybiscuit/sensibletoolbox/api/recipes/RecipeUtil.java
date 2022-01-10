package io.github.thebusybiscuit.sensibletoolbox.api.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;

/**
 * Collection of miscellaneous recipe-related utility methods.
 * 
 * @author desht
 * @author TheBusyBiscuit
 */
public final class RecipeUtil {

    private RecipeUtil() {}

    private static final Set<Material> vanillaSmelts = new HashSet<>();
    private static final Map<ItemStack, List<ItemStack>> reverseCustomSmelts = new HashMap<>();

    public static void setupRecipes() {
        for (String itemId : SensibleToolbox.getItemRegistry().getItemIds()) {
            try {
                BaseSTBItem item = SensibleToolbox.getItemRegistry().getItemById(itemId);

                addWorkbenchRecipes(item);
                addFurnaceRecipes(item);
                addCustomMachineRecipes(item);

            } catch (Exception x) {
                SensibleToolboxPlugin.getInstance().getLogger().log(Level.SEVERE, x, () -> "Could not register recipe for item: " + itemId);
            }
        }
    }

    private static void addWorkbenchRecipes(@Nonnull BaseSTBItem item) {
        // add custom workbench recipes
        Recipe mainRecipe = item.getMainRecipe();

        if (mainRecipe != null) {
            Bukkit.addRecipe(mainRecipe);
        }

        for (Recipe extraRecipe : item.getExtraRecipes()) {
            Bukkit.addRecipe(extraRecipe);
        }
    }

    private static void addFurnaceRecipes(@Nonnull BaseSTBItem item) {
        // add custom furnace recipes
        ItemStack stack = item.getSmeltingResult();

        if (stack != null) {
            NamespacedKey key = new NamespacedKey(SensibleToolboxPlugin.getInstance(), item.getItemTypeID() + "_furnacerecipe");
            Bukkit.addRecipe(new FurnaceRecipe(key, stack, item.getMaterial(), 0, 200));
            recordReverseSmelt(stack, item.toItemStack());
        }
    }

    private static void addCustomMachineRecipes(@Nonnull BaseSTBItem item) {
        // add custom processing recipes for any machine items
        if (item instanceof BaseSTBMachine) {
            ((BaseSTBMachine) item).addCustomRecipes(CustomRecipeManager.getManager());
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

    private static void recordReverseSmelt(@Nonnull ItemStack result, @Nonnull ItemStack ingredient) {
        if (!reverseCustomSmelts.containsKey(result)) {
            reverseCustomSmelts.put(result, new ArrayList<>());
        }

        reverseCustomSmelts.get(result).add(ingredient);
    }

    /**
     * Given an item, get all the items which could be smelted into that item
     * via a vanilla furnace recipe, possibly one which was added by an STB
     * item.
     *
     * @param stack
     *            the item stack to check
     * @return a list of the ingredients which could be smelted into the item
     */
    @Nonnull
    public static List<ItemStack> getSmeltingIngredientsFor(@Nonnull ItemStack stack) {
        List<ItemStack> res = reverseCustomSmelts.get(stack);
        return res == null ? Collections.emptyList() : res;
    }

    /**
     * Check if vanilla items of the given material may be smelted in a
     * furnace (or smelter). "Vanilla" means an item stack with no custom
     * metadata defined, in particular STB item metadata.
     *
     * @param mat
     *            the material to check
     * @return true if vanilla items of the material may be smelted
     */
    public static boolean isVanillaSmelt(@Nonnull Material mat) {
        return vanillaSmelts.contains(mat);
    }

    @Nonnull
    public static String makeRecipeKey(@Nonnull ItemStack item) {
        String res = item.getType().toString();

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            res += ":" + item.getItemMeta().getDisplayName();
        }

        return res;
    }

    @Nonnull
    public static String makeRecipeKey(boolean ignoreData, @Nullable ItemStack item) {
        if (item == null) {
            return "";
        }

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
