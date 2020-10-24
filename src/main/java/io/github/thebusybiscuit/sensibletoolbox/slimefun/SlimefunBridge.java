package io.github.thebusybiscuit.sensibletoolbox.slimefun;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.STBFurnaceRecipe;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.SimpleCustomRecipe;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.Generator;
import io.github.thebusybiscuit.sensibletoolbox.items.recipebook.RecipeBook;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.cscorelib2.recipes.MinecraftRecipe;

public final class SlimefunBridge implements SlimefunAddon {

    private final SensibleToolboxPlugin plugin;

    public SlimefunBridge(@Nonnull SensibleToolboxPlugin plugin) {
        this.plugin = plugin;

        Category items = new Category(new NamespacedKey(plugin, "items"), new CustomItem(Material.SHEARS, "&7STB - Items"));
        Category blocks = new Category(new NamespacedKey(plugin, "blocks"), new CustomItem(Material.PURPLE_STAINED_GLASS, "&7STB - Blocks and Machines"));

        for (String id : SensibleToolboxPlugin.getInstance().getItemRegistry().getItemIds()) {
            BaseSTBItem item = SensibleToolboxPlugin.getInstance().getItemRegistry().getItemById(id);
            Category category = item.toItemStack().getType().isBlock() ? blocks : items;
            List<ItemStack> recipe = new ArrayList<>();
            RecipeType recipeType = RecipeType.NULL;
            Recipe r = item.getRecipe();

            if (r != null) {
                if (r instanceof SimpleCustomRecipe) {
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(((SimpleCustomRecipe) r).getIngredient());
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(null);
                } else if (r instanceof STBFurnaceRecipe) {
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(((STBFurnaceRecipe) r).getIngredient());
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(null);
                    recipe.add(null);
                } else if (item.getRecipe() instanceof ShapelessRecipe) {
                    recipeType = new RecipeType(MinecraftRecipe.SHAPELESS_CRAFTING);

                    for (ItemStack input : ((ShapelessRecipe) item.getRecipe()).getIngredientList()) {
                        if (input == null) {
                            recipe.add(null);
                        } else {
                            recipe.add(RecipeBook.getIngredient(item, input));
                        }
                    }

                    for (int i = recipe.size(); i < 9; i++) {
                        recipe.add(null);
                    }
                } else if (item.getRecipe() instanceof ShapedRecipe) {
                    recipeType = new RecipeType(MinecraftRecipe.SHAPED_CRAFTING);

                    for (String row : ((ShapedRecipe) item.getRecipe()).getShape()) {
                        for (int i = 0; i < 3; i++) {
                            try {
                                recipe.add(RecipeBook.getIngredient(item, ((ShapedRecipe) item.getRecipe()).getIngredientMap().get(Character.valueOf(row.charAt(i)))));
                            } catch (StringIndexOutOfBoundsException x) {
                                recipe.add(null);
                            }
                        }
                    }

                    for (int i = recipe.size(); i < 9; i++) {
                        recipe.add(null);
                    }
                }
            }

            SlimefunItem sfItem = null;
            SlimefunItemStack itemStack = new SlimefunItemStack("STB_" + id.toUpperCase(Locale.ROOT), item.toItemStack());

            if (item instanceof Generator) {
                List<ItemStack> fuels = ((Generator) item).getFuelInformation();
                sfItem = new STBSlimefunGenerator(category, itemStack, recipeType, recipe.toArray(new ItemStack[0]), fuels);
            } else {
                sfItem = new STBSlimefunItem(category, itemStack, recipeType, recipe.toArray(new ItemStack[0]));
            }

            if (r != null) {
                sfItem.setRecipeOutput(r.getResult());
            }

            sfItem.register(this);
        }

        RecipeType masher = new RecipeType(new NamespacedKey(plugin, "masher"), SlimefunItem.getByID("STB_MASHER").getItem());
        RecipeType fermenter = new RecipeType(new NamespacedKey(plugin, "fermenter"), SlimefunItem.getByID("STB_FERMENTER").getItem());

        patch("STB_INFERNALDUST", RecipeType.MOB_DROP, new CustomItem(Material.BLAZE_SPAWN_EGG, "&a&oBlaze"));
        patch("STB_ENERGIZEDGOLDINGOT", new RecipeType(MinecraftRecipe.FURNACE), SlimefunItem.getByID("STB_ENERGIZEDGOLDDUST").getItem());
        patch("STB_QUARTZDUST", masher, new ItemStack(Material.QUARTZ));
        patch("STB_ENERGIZEDIRONINGOT", new RecipeType(MinecraftRecipe.FURNACE), SlimefunItem.getByID("STB_ENERGIZEDIRONDUST").getItem());
        patch("STB_SILICONWAFER", new RecipeType(MinecraftRecipe.FURNACE), SlimefunItem.getByID("STB_QUARTZDUST").getItem());
        patch("STB_IRONDUST", masher, new ItemStack(Material.IRON_INGOT));
        patch("STB_GOLDDUST", masher, new ItemStack(Material.GOLD_INGOT));
        patch("STB_FISHBAIT", fermenter, new ItemStack(Material.ROTTEN_FLESH));
    }

    @ParametersAreNonnullByDefault
    private void patch(String id, RecipeType recipeType, ItemStack recipe) {
        SlimefunItem item = SlimefunItem.getByID(id);

        if (item != null) {
            item.setRecipe(new ItemStack[] { null, null, null, null, recipe, null, null, null, null });
            item.setRecipeType(recipeType);
        }
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return plugin;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/Slimefun/SensibleToolbox/issues";
    }

}
