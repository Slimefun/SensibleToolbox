package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class SpeedModule extends ItemRouterModule {
	public SpeedModule() {}

	public SpeedModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public String getItemName() {
		return "Item Router Speed Module";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Insert into an Item Router",
				"Each Speed Module will increase",
				"operating speed by 1 per ticks from",
				"the base rate of 1 per 20 ticks.",
				"Max: 3 Speed Modules"
		};
	}

	@Override
	public Recipe getRecipe() {
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
		recipe.addIngredient(Material.PAPER);
		recipe.addIngredient(Material.BLAZE_POWDER);
		recipe.addIngredient(Material.EMERALD);
		return recipe;
	}

	@Override
	public Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
		return mat == Material.PAPER ? BlankModule.class : null;
	}

	@Override
	public boolean execute() {
		return false;  // passive module
	}
}
