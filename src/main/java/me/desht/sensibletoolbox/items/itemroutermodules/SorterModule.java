package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.VanillaInventoryUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

public class SorterModule extends DirectionalItemRouterModule {
	private static final Dye md = makeDye(DyeColor.PURPLE);

	public SorterModule() {}

	public SorterModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "I.R. Mod: Sorter";
	}

	@Override
	public String[] getLore() {
		return makeDirectionalLore(
				"Insert into an Item Router",
				"Places items into inventory IF",
				"- inventory is empty OR",
				"- inventory already contains that item"
		);
	}

	@Override
	public Recipe getRecipe() {
		registerCustomIngredients(new BlankModule());
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
		recipe.addIngredient(Material.PAPER);
		recipe.addIngredient(Material.SPIDER_EYE);
		recipe.addIngredient(Material.ARROW);
		return recipe;
	}

	@Override
	public boolean execute() {
		if (getOwner() != null && getOwner().getBufferItem() != null) {
			if (getFilter() != null && !getFilter().shouldPass(getOwner().getBufferItem())) {
				return false;
			}
			Debugger.getInstance().debug(2, "sorter in " + getOwner() + " has: " + getOwner().getBufferItem());
			Block b = getOwner().getLocation().getBlock();
			Block target = b.getRelative(getDirection());
			int nToInsert = getOwner().getStackSize();
			STBBlock stb = LocationManager.getManager().get(target.getLocation());
			int nInserted;
			if (stb instanceof STBInventoryHolder) {
				ItemStack toInsert = getOwner().getBufferItem().clone();
				toInsert.setAmount(Math.min(nToInsert, toInsert.getAmount()));
				nInserted = ((STBInventoryHolder) stb).insertItems(toInsert, getDirection().getOppositeFace(), true);
			} else {
				// vanilla inventory holder?
				nInserted = vanillaSortInsertion(target, nToInsert, getDirection().getOppositeFace());
			}
			getOwner().reduceBuffer(nInserted);
			return nInserted > 0;
		}
		return false;
	}

	private int vanillaSortInsertion(Block target, int amount, BlockFace side) {
		ItemStack buffer = getOwner().getBufferItem();
		int nInserted = VanillaInventoryUtils.vanillaInsertion(target, buffer, amount, side, true);
		if (nInserted > 0) {
			getOwner().setBufferItem(buffer.getAmount() == 0 ? null : buffer);
		}
		return nInserted;
	}
}
