package me.desht.sensibletoolbox.items.filter;

import me.desht.sensibletoolbox.util.Filter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class ReverseItemFilter extends AbstractItemFilter {
	public ReverseItemFilter() {
	}

	public ReverseItemFilter(ConfigurationSection conf) {
		super(conf, false);
	}

	public ReverseItemFilter(Filter filter) {
		super(filter);
	}

	@Override
	public String getItemName() {
		return "Item Blocker";
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("WSW", "SRS", "WSW");
		recipe.setIngredient('W', Material.STICK);
		recipe.setIngredient('S', Material.STRING);
		recipe.setIngredient('R', Material.REDSTONE_TORCH_ON);
		return recipe;
	}
}
