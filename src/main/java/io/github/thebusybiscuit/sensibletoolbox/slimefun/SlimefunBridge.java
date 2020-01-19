package io.github.thebusybiscuit.sensibletoolbox.slimefun;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.STBFurnaceRecipe;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.SimpleCustomRecipe;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.BioEngine;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.HeatEngine;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.MagmaticEngine;
import io.github.thebusybiscuit.sensibletoolbox.items.RecipeBook;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;

public final class SlimefunBridge {
	
	private SlimefunBridge() {}
	
	private static void patch(String id, RecipeType recipeType, ItemStack recipe) {
		SlimefunItem item = SlimefunItem.getByID(id);
		if (item != null) {
			item.setRecipe(new ItemStack[] {null, null, null, null, recipe, null, null, null, null});
			item.setRecipeType(recipeType);
		}
	}
	
	public static void initiate() {
		Category items = new Category(new CustomItem(Material.SHEARS, "&7STB - Items", "", "&a> Click to open"));
		Category blocks = new Category(new CustomItem(Material.PURPLE_STAINED_GLASS, "&7STB - Blocks and Machines", "", "&a> Click to open"));
		
		for (String id : SensibleToolboxPlugin.getInstance().getItemRegistry().getItemIds()) {
			BaseSTBItem item = SensibleToolboxPlugin.getInstance().getItemRegistry().getItemById(id);
			Category category = item.toItemStack().getType().isBlock() ? blocks: items;
			List<ItemStack> recipe = new ArrayList<>();
			RecipeType recipeType = null;
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
				}
				else if (r instanceof STBFurnaceRecipe) {
					recipe.add(null);
					recipe.add(null);
					recipe.add(null);
					recipe.add(null);
					recipe.add(((STBFurnaceRecipe) r).getIngredient());
					recipe.add(null);
					recipe.add(null);
					recipe.add(null);
					recipe.add(null);
				}
				else if (item.getRecipe() instanceof ShapelessRecipe) {
					recipeType = RecipeType.SHAPELESS_RECIPE;
					for (ItemStack input: ((ShapelessRecipe) item.getRecipe()).getIngredientList()) {
						if (input == null) recipe.add(null);
						else recipe.add(RecipeBook.getIngredient(item, input));
					}
					for (int i = recipe.size(); i < 9; i++) {
						recipe.add(null);
					}
				}
				else if (item.getRecipe() instanceof ShapedRecipe) {
					recipeType = RecipeType.SHAPED_RECIPE;
					for (String row : ((ShapedRecipe) item.getRecipe()).getShape()) {
				        for (int i = 0; i < 3; i++) {
				        	try {
				        		recipe.add(RecipeBook.getIngredient(item, ((ShapedRecipe) item.getRecipe()).getIngredientMap().get(Character.valueOf(row.charAt(i)))));
				        	} catch(StringIndexOutOfBoundsException x) {
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
			
			if (id.equalsIgnoreCase("bioengine")) {
				Set<ItemStack> fuels = ((BioEngine) item).getFuelInformation();
				if (fuels.size() % 2 != 0) fuels.add(null);
				sfItem = new ExcludedGadget(category, item.toItemStack(), id.toUpperCase(), null, null, fuels.toArray(new ItemStack[fuels.size()]));
			}
			else if (id.equalsIgnoreCase("magmaticengine")) {
				Set<ItemStack> fuels = ((MagmaticEngine) item).getFuelInformation();
				if (fuels.size() % 2 != 0) fuels.add(null);
				sfItem = new ExcludedGadget(category, item.toItemStack(), id.toUpperCase(), null, null, fuels.toArray(new ItemStack[fuels.size()]));
			}
			else if (id.equalsIgnoreCase("heatengine")) {
				Set<ItemStack> fuels = ((HeatEngine) item).getFuelInformation();
				if (fuels.size() % 2 != 0) fuels.add(null);
				sfItem = new ExcludedGadget(category, item.toItemStack(), id.toUpperCase(), null, null, fuels.toArray(new ItemStack[fuels.size()]));
			}
			else sfItem = new ExcludedBlock(category, item.toItemStack(), id.toUpperCase(), null, null);
			
			sfItem.setReplacing(true);
			sfItem.setRecipeType(recipeType);
			sfItem.setRecipe(recipe.toArray(new ItemStack[recipe.size()]));
			if (r != null) sfItem.setRecipeOutput(r.getResult());
			sfItem.register();
		}
		
		patch("INFERNALDUST", RecipeType.MOB_DROP, new CustomItem(Material.BLAZE_SPAWN_EGG, "&a&oBlaze"));
		patch("ENERGIZEDGOLDINGOT", RecipeType.FURNACE, SlimefunItem.getByID("ENERGIZEDGOLDDUST").getItem());
		patch("QUARTZDUST", new RecipeType(SlimefunItem.getByID("MASHER").getItem()), new ItemStack(Material.QUARTZ));
		patch("ENERGIZEDIRONINGOT", RecipeType.FURNACE, SlimefunItem.getByID("ENERGIZEDIRONDUST").getItem());
		patch("SILICONWAFER", RecipeType.FURNACE, SlimefunItem.getByID("QUARTZDUST").getItem());
		patch("IRONDUST", new RecipeType(SlimefunItem.getByID("MASHER").getItem()), new ItemStack(Material.IRON_INGOT));
		patch("GOLDDUST", new RecipeType(SlimefunItem.getByID("MASHER").getItem()), new ItemStack(Material.GOLD_INGOT));
		patch("FISHBAIT", new RecipeType(SlimefunItem.getByID("FERMENTER").getItem()), new ItemStack(Material.ROTTEN_FLESH));
	}

}
