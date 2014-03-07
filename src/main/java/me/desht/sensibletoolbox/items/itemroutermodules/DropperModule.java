package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.dhutils.Debugger;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class DropperModule extends DirectionalItemRouterModule {
	private static final MaterialData md = makeDye(DyeColor.GRAY);

	public DropperModule() {}

	public DropperModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public String getItemName() {
		return "I.R. Mod: Dropper";
	}

	@Override
	public String[] getLore() {
		return makeDirectionalLore("Insert into an Item Router", "Drops items onto the ground", "in the configured direction");
	}

	@Override
	public Recipe getRecipe() {
		BlankModule bm = new BlankModule();
		registerCustomIngredients(bm);
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
		recipe.addIngredient(bm.getMaterialData());
		recipe.addIngredient(Material.COBBLESTONE);
		return recipe;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public boolean execute(Location loc) {
		if (getItemRouter() != null && getItemRouter().getBufferItem() != null) {
			if (getFilter() != null && !getFilter().shouldPass(getItemRouter().getBufferItem())) {
				return false;
			}
			int toDrop = getItemRouter().getStackSize();
			ItemStack stack = getItemRouter().extractItems(BlockFace.SELF, null, toDrop, null);
			if (stack != null) {
				Location targetLoc = getTargetLocation(loc).add(0.5, 0.5, 0.5);
				Item item = targetLoc.getWorld().dropItem(targetLoc, stack);
				item.setVelocity(new Vector(0, 0, 0));
				Debugger.getInstance().debug(2, "dropper dropped " + stack + " from " + getItemRouter());
			}
			return true;
		}
		return false;
	}
}
