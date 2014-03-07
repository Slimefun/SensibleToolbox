package me.desht.sensibletoolbox.items.itemroutermodules;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

public class PullerModule extends DirectionalItemRouterModule {
	private static final Dye md = makeDye(DyeColor.LIME);

	public PullerModule() {
	}

	public PullerModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public String getItemName() {
		return "I.R. Mod: Puller";
	}

	@Override
	public String[] getLore() {
		return makeDirectionalLore("Insert into an Item Router", "Pulls items from an adjacent inventory");
	}

	@Override
	public Recipe getRecipe() {
		BlankModule bm = new BlankModule();
		registerCustomIngredients(bm);
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
		recipe.addIngredient(bm.getMaterialData());
		recipe.addIngredient(Material.PISTON_STICKY_BASE);
		return recipe;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public boolean execute(Location loc) {
		return getItemRouter() != null && doPull(getDirection(), loc);
	}
}
