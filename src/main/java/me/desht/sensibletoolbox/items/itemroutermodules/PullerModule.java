package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.VanillaInventoryUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
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
		registerCustomIngredients(new BlankModule());
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
		recipe.addIngredient(Material.PAPER); // in fact, a Blank Module
		recipe.addIngredient(Material.PISTON_STICKY_BASE);
		return recipe;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public boolean execute() {
		return getOwner() != null && doPull(getDirection());
	}
}
