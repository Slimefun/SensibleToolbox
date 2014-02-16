package me.desht.sensibletoolbox.recipes;

import me.desht.dhutils.Debugger;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomRecipeCollection {
	private final Map<ItemStack, ProcessingResult> recipes = new HashMap<ItemStack, ProcessingResult>();

	public void addCustomRecipe(ItemStack input, ItemStack result, int processingTime) {
		recipes.put(CustomRecipeManager.makeSingle(input), new ProcessingResult(result, processingTime));
		Debugger.getInstance().debug("added custom recipe: " + input + " -> " + get(input).toString());
	}

	public void addCustomRecipe(CustomRecipe recipe) {
		recipes.put(CustomRecipeManager.makeSingle(recipe.getIngredient()), new ProcessingResult(recipe.getResult(), recipe.getProcessingTime()));
	}

	public ProcessingResult get(ItemStack input) {
		return recipes.get(CustomRecipeManager.makeSingle(input));
	}

	public boolean hasRecipe(ItemStack input) {
		return recipes.containsKey(CustomRecipeManager.makeSingle(input));
	}

}
