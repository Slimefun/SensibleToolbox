package me.mrCookieSlime.CSCoreLib.general.Recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeCalculator {
	
	public static List<ItemStack> getIngredients(Recipe recipe) {
	    List<ItemStack> ingredients = new ArrayList<ItemStack>();
	    if ((recipe instanceof ShapedRecipe)) {
	      ShapedRecipe sr = (ShapedRecipe)recipe;
	      String[] shape = sr.getShape();

	      for (String row : shape) {
	        for (int i = 0; i < row.length(); i++) {
	          ItemStack stack = (ItemStack)sr.getIngredientMap().get(Character.valueOf(row.charAt(i)));
	          for (ItemStack ing : ingredients) {
	            int mss = ing.getType().getMaxStackSize();
	            if ((ing.isSimilar(stack)) && (ing.getAmount() < mss)) {
	              int canAdd = mss - ing.getAmount();
	              int add = Math.min(canAdd, stack.getAmount());
	              ing.setAmount(ing.getAmount() + add);
	              int remaining = stack.getAmount() - add;
	              if (remaining >= 1) {
	                stack.setAmount(remaining);
	              } else {
	                stack = null;
	                break;
	              }
	            }
	          }
	          if ((stack != null) && (stack.getAmount() > 0))
	            ingredients.add(stack);
	        }
	      }
	    }
	    else if ((recipe instanceof ShapelessRecipe)) {
	      for (ItemStack i : ((ShapelessRecipe)recipe).getIngredientList()) {
	        for (ItemStack ing : ingredients) {
	          int mss = ing.getType().getMaxStackSize();
	          if ((ing.isSimilar(i)) && (ing.getAmount() < mss)) {
	            int canAdd = mss - ing.getAmount();
	            ing.setAmount(ing.getAmount() + Math.min(canAdd, i.getAmount()));
	            int remaining = i.getAmount() - Math.min(canAdd, i.getAmount());
	            if (remaining < 1) break;
	            i.setAmount(remaining);
	          }

	        }

	        if (i.getAmount() > 0) {
	          ingredients.add(i);
	        }
	      }
	    }
	    return ingredients;
	  }
	
	public static ItemStack getSmeltedOutput(Material type) {
		ItemStack result = null;
		Iterator<Recipe> iter = Bukkit.recipeIterator();
		while (iter.hasNext()) {
		   Recipe recipe = iter.next();
		   if (!(recipe instanceof FurnaceRecipe)) continue;
		   if (((FurnaceRecipe) recipe).getInput().getType() != type) continue;
		   result = recipe.getResult();
		   break;
		}
		
		return result;
	}
}
