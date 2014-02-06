package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.items.BaseSTBItem;
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
		return new String[] { "Insert into an Item Router", "Drops items onto the ground", "in the configured direction" };
	}

	@Override
	public Recipe getRecipe() {
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
		recipe.addIngredient(Material.PAPER); // in fact, a Blank Module
		recipe.addIngredient(Material.COBBLESTONE);
		return recipe;
	}

	@Override
	public Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
		return mat == Material.PAPER ? BlankModule.class : null;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public boolean execute() {
		if (getOwner() != null && getOwner().getBufferItem() != null) {
			if (getFilter() != null && !getFilter().shouldPass(getOwner().getBufferItem())) {
				return false;
			}
			System.out.println("prepare to drop " + getOwner().getBufferItem());
			Block b = getOwner().getLocation().getBlock();
			Block target = b.getRelative(getDirection());
			int toDrop = getOwner().getStackSize();
			ItemStack stack = getOwner().extractItems(BlockFace.SELF, null, toDrop);
			if (stack != null) {
				Location loc = target.getLocation().add(0.5, 0.5, 0.5);
				Item item = loc.getWorld().dropItem(loc, stack);
				item.setVelocity(new Vector(0, 0, 0));
			}
			return true;
		}
		return false;
	}
}
