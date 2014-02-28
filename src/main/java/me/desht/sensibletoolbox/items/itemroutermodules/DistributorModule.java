package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.storage.LocationManager;
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

import java.util.List;

public class DistributorModule extends DirectionalItemRouterModule {
	private static final Dye md = makeDye(DyeColor.RED);

	private int nextNeighbour = 0;

	public DistributorModule() {}

	public DistributorModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "I.R. Mod: Distributor";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Pulls items from the configured",
				"direction, then sends them",
				"round-robin to all other",
				"adjacent inventories"
		};
	}

	@Override
	public Recipe getRecipe() {
		BlankModule bm = new BlankModule();
		registerCustomIngredients(bm);
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
		recipe.addIngredient(bm.getMaterialData());
		recipe.addIngredient(Material.PISTON_STICKY_BASE);
		recipe.addIngredient(Material.ARROW);
		return recipe;
	}

	@Override
	public boolean execute() {
		if (getOwner() == null) {
			return false; // shouldn't happen...
		}

		doPull(getDirection());

		if (getOwner().getNeighbours().size() > 1 && getOwner().getBufferItem() != null) {
			int nToInsert = getOwner().getStackSize();
			BlockFace face = getNextNeighbour();
			if (face == getDirection()) {
				return false;  // shouldn't happen...
			}
			Block b = getOwner().getLocation().getBlock();
			Block target = b.getRelative(face);
			BaseSTBBlock stb = LocationManager.getManager().get(target.getLocation());
			if (stb instanceof STBInventoryHolder) {
				ItemStack toInsert = getOwner().getBufferItem().clone();
				toInsert.setAmount(Math.min(nToInsert, toInsert.getAmount()));
				int nInserted = ((STBInventoryHolder) stb).insertItems(toInsert, face.getOppositeFace(), false);
				getOwner().reduceBuffer(nInserted);
				return nInserted > 0;
			} else {
				// vanilla inventory holder?
				return vanillaInsertion(target, nToInsert, getDirection().getOppositeFace());
			}
		}

		return false;
	}

	private BlockFace getNextNeighbour() {
		List<BlockFace> neighbours = getOwner().getNeighbours();
		nextNeighbour = (nextNeighbour + 1) % neighbours.size();
		if (neighbours.get(nextNeighbour) == getDirection()) {
			nextNeighbour = (nextNeighbour + 1) % neighbours.size();
		}
		return neighbours.get(nextNeighbour);
	}
}
