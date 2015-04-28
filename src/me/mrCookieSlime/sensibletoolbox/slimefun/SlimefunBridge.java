package me.mrCookieSlime.sensibletoolbox.slimefun;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.MenuItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Reflection.ReflectionUtils;
import me.mrCookieSlime.Slimefun.Lists.Categories;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.ExcludedBlock;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.ExcludedGadget;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;
import me.mrCookieSlime.sensibletoolbox.api.recipes.STBFurnaceRecipe;
import me.mrCookieSlime.sensibletoolbox.api.recipes.SimpleCustomRecipe;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.BioEngine;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.HeatEngine;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.MagmaticEngine;
import me.mrCookieSlime.sensibletoolbox.items.RecipeBook;
import me.mrCookieSlime.sensibletoolbox.slimefun.machines.ElectricalOreWasher;
import me.mrCookieSlime.sensibletoolbox.slimefun.machines.ElectricalPanningMachine;
import me.mrCookieSlime.sensibletoolbox.slimefun.machines.FruitPicker;
import me.mrCookieSlime.sensibletoolbox.slimefun.machines.NuclearReactor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

public class SlimefunBridge {
	
	private static void patch(String id, RecipeType recipeType, ItemStack recipe) {
		SlimefunItem item = SlimefunItem.getByName(id);;
		if (item != null) {
			item.setRecipe(new ItemStack[] {null, null, null, null, recipe, null, null, null, null});
			item.setRecipeType(recipeType);
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public static void initiate() {
		Category items = new Category(new MenuItem(Material.SHEARS, "&7STB - Items", 0, "open"));
		Category blocks = new Category(new CustomItem(new MaterialData(Material.STAINED_GLASS, (byte) 10), "&7STB - Blocks and Machines", "", "&a> Click to open"));
		for (String id: SensibleToolboxPlugin.getInstance().getItemRegistry().getItemIds()) {
			BaseSTBItem item = SensibleToolboxPlugin.getInstance().getItemRegistry().getItemById(id);
			Category category = item.toItemStack().getType().isBlock() ? blocks: items;
			List<ItemStack> recipe = new ArrayList<ItemStack>();
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
			else if (item instanceof SlimefunIOMachine || item instanceof FruitPicker || item instanceof ElectricalPanningMachine || item instanceof ElectricalOreWasher || item instanceof NuclearReactor) {
				category = Categories.MACHINES_1;
				recipeType = RecipeType.ENHANCED_CRAFTING_TABLE;
				try {
					recipe = (List<ItemStack>) ReflectionUtils.getMethod(item.getClass(), "getSlimefunRecipe").invoke(item);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
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
			else if (id.equalsIgnoreCase("nuclearreactor")) {
				Set<ItemStack> fuels = ((NuclearReactor) item).getFuelInformation();
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
		
		patch("INFERNALDUST", RecipeType.MOB_DROP, new CustomItem(Material.MONSTER_EGG, "&a&oBlaze", 61));
		patch("ENERGIZEDGOLDINGOT", RecipeType.FURNACE, SlimefunItem.getByName("ENERGIZEDGOLDDUST").getItem());
		patch("QUARTZDUST", new RecipeType(SlimefunItem.getByName("MASHER").getItem()), new ItemStack(Material.QUARTZ));
		patch("ENERGIZEDIRONINGOT", RecipeType.FURNACE, SlimefunItem.getByName("ENERGIZEDIRONDUST").getItem());
		patch("SILICONWAFER", RecipeType.FURNACE, SlimefunItem.getByName("QUARTZDUST").getItem());
		patch("IRONDUST", new RecipeType(SlimefunItem.getByName("MASHER").getItem()), new ItemStack(Material.IRON_INGOT));
		patch("GOLDDUST", new RecipeType(SlimefunItem.getByName("MASHER").getItem()), new ItemStack(Material.GOLD_INGOT));
		patch("FISHBAIT", new RecipeType(SlimefunItem.getByName("FERMENTER").getItem()), new ItemStack(Material.ROTTEN_FLESH));
		
		STBSlimefunItems.load();
		
		Slimefun.addDescription("REACTOR_COOLANT_PORT", "&e1: Place this on the Bottom Side of a Reactor", "&e2: Fill it with Coolant Cells", "§e3: Make sure to supply more Coolant Cells", "&e since they get consumed over time");
	}

}
