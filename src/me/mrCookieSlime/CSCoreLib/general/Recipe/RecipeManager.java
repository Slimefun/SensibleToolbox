package me.mrCookieSlime.CSCoreLib.general.Recipe;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class RecipeManager {
	
	public static void removeRecipe(Material type) {
		Iterator<Recipe> recipes = Bukkit.recipeIterator();
		Recipe recipe;
		while (recipes.hasNext()) {
			recipe = recipes.next();
			if (recipe != null && recipe.getResult().getType() == type) {
				recipes.remove();
			}
		}
	}
	
	public static void removeVanillaRecipe(Material type) {
		Iterator<Recipe> recipes = Bukkit.recipeIterator();
		Recipe recipe;
		while (recipes.hasNext()) {
			recipe = recipes.next();
			if (recipe != null && recipe.getResult().isSimilar(new ItemStack(type))) {
				recipes.remove();
			}
		}
	}
	
	public static void removeRecipe(Material type, short durability) {
		Iterator<Recipe> recipes = Bukkit.recipeIterator();
		Recipe recipe;
		while (recipes.hasNext()) {
			recipe = recipes.next();
			if (recipe != null && recipe.getResult().getType() == type && recipe.getResult().getDurability() == durability) {
				recipes.remove();
			}
		}
	}

}
