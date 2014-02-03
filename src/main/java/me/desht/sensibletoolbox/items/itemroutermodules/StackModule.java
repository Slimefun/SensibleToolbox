package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class StackModule extends ItemRouterModule {
	public StackModule() {}

	public StackModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public String getItemName() {
		return "Item Router Stack Module";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Insert into an Item Router", "Item Router will move stacks", "instead of single items" };
	}

	@Override
	public Recipe getRecipe() {
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
		recipe.addIngredient(Material.PAPER);
		recipe.addIngredient(Material.DIAMOND);
		return recipe;
	}

	@Override
	public Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
		return mat == Material.PAPER ? BlankModule.class : null;
	}

	@Override
	public boolean execute() {
		return false; // passive module
	}
}
