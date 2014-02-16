package me.desht.sensibletoolbox.recipes;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CustomRecipe implements Recipe {
	private final String itemId;  // id of STB item which makes this
	private final ItemStack ingredient;
	private final ItemStack result;
	private final int processingTime; // in ticks

	public CustomRecipe(BaseSTBItem processor, ItemStack ingredient, ItemStack result, int processingTime) {
		this.itemId = processor.getItemID();
		this.ingredient = ingredient;
		this.result = result;
		this.processingTime = processingTime;
	}

	public CustomRecipe(String itemId, ItemStack ingredient, ItemStack result, int processingTime) {
		this.itemId = itemId;
		this.ingredient = ingredient;
		this.result = result;
		this.processingTime = processingTime;
	}

	public int getProcessingTime() {
		return processingTime;
	}

	public ItemStack getResult() {
		return result;
	}

	public String getItemId() {
		return itemId;
	}

	public ItemStack getIngredient() {
		return ingredient;
	}

	@Override
	public String toString() {
		return "Custom recipe: " + itemId + " : " + ingredient + " -> " + result + " in " + processingTime + " ticks";
	}
}
