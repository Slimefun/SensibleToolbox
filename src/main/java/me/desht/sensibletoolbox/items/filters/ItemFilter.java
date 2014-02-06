package me.desht.sensibletoolbox.items.filters;

import me.desht.sensibletoolbox.util.Filter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class ItemFilter extends AbstractItemFilter {
	public ItemFilter() {
	}

	public ItemFilter(ConfigurationSection conf) {
		super(conf, true);
	}

	public ItemFilter(Filter filter) {
		super(filter);
	}

	@Override
	public String getItemName() {
		return "Item Passer";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("WSW", "SRS", "WSW");
		recipe.setIngredient('W', Material.STICK);
		recipe.setIngredient('S', Material.STRING);
		recipe.setIngredient('R', Material.REDSTONE);
		return recipe;
	}
}
