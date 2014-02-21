package me.desht.sensibletoolbox.recipes;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CustomRecipe implements Recipe {
	private final String processorID;  // id of STB item which makes this
	private final ItemStack ingredient;
	private final ItemStack result;
	private final int processingTime; // in ticks

	public CustomRecipe(BaseSTBItem processor, ItemStack ingredient, ItemStack result, int processingTime) {
		this.processorID = processor.getItemTypeID();
		this.ingredient = ingredient;
		this.result = result;
		this.processingTime = processingTime;
	}

	public CustomRecipe(String itemId, ItemStack ingredient, ItemStack result, int processingTime) {
		this.processorID = itemId;
		this.ingredient = ingredient;
		this.result = result;
		this.processingTime = processingTime;
	}

	/**
	 * Get the time in ticks needed to make this recipe.
	 *
	 * @return the processing time, in ticks
	 */
	public int getProcessingTime() {
		return processingTime;
	}

	@Override
	public ItemStack getResult() {
		return result;
	}

	/**
	 * Get the item ID of the STB machine which is used to make this recipe.
	 *
	 * @return the STB item ID
	 */
	public String getProcessorID() {
		return processorID;
	}

	/**
	 * Get the input ingredient for this recipe.
	 *
	 * @return the ingredient
	 */
	public ItemStack getIngredient() {
		return ingredient;
	}

	@Override
	public String toString() {
		return "Custom recipe: " + processorID + " : " + ingredient + " -> " + result + " in " + processingTime + " ticks";
	}
}
