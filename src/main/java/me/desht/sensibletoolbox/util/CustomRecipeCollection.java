package me.desht.sensibletoolbox.util;

import me.desht.dhutils.Debugger;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomRecipeCollection {
	private final Map<ItemStack, CustomRecipe> recipes = new HashMap<ItemStack, CustomRecipe>();

	public void addCustomRecipe(ItemStack input, ItemStack result, int processingTime) {
		recipes.put(getSingle(input), new CustomRecipe(result, processingTime));
		Debugger.getInstance().debug("added custom recipe: " + input + " -> " + get(input).toString());
	}

	public CustomRecipe get(ItemStack input) {
		return recipes.get(getSingle(input));
	}

	public boolean hasRecipe(ItemStack input) {
		return recipes.containsKey(getSingle(input));
	}

	private ItemStack getSingle(ItemStack stack) {
		if (stack.getAmount() == 1) {
			return stack;
		} else {
			ItemStack stack2 = stack.clone();
			stack2.setAmount(1);
			return stack2;
		}
	}

	public class CustomRecipe {
		private final ItemStack result;
		private final int processingTime; // in ticks

		private CustomRecipe(ItemStack result, int processingTime) {
			this.result = result;
			this.processingTime = processingTime;
		}

		public int getProcessingTime() {
			return processingTime;
		}

		public ItemStack getResult() {
			return result;
		}

		@Override
		public String toString() {
			return "Custom recipe: " + result + " in " + processingTime + " ticks";
		}
	}
}
