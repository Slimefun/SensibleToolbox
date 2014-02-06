package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.api.STBMachine;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.Filter;
import me.desht.sensibletoolbox.util.STBUtil;
import me.desht.sensibletoolbox.util.VanillaInventoryUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import java.util.Arrays;

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
		return new String[] { "Insert into an Item Router", "Pulls items from an adjacent inventory" };
	}

	@Override
	public Recipe getRecipe() {
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
		recipe.addIngredient(Material.PAPER); // in fact, a Blank Module - see below
		recipe.addIngredient(Material.PISTON_STICKY_BASE);
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
		if (getOwner() != null) {
			ItemStack inBuffer = getOwner().getBufferItem();
			if (inBuffer != null && inBuffer.getAmount() >= inBuffer.getType().getMaxStackSize()) {
				return false;
			}
			int nToPull = getOwner().getStackSize();
			Block b = getOwner().getLocation().getBlock();
			Block target = b.getRelative(getDirection());
			BaseSTBBlock stb = LocationManager.getManager().get(target.getLocation());
			ItemStack pulled;
			if (stb instanceof STBInventoryHolder) {
				pulled = ((STBInventoryHolder)stb).extractItems(getDirection().getOppositeFace(), inBuffer, nToPull);
			} else {
				// possible vanilla inventory holder
				pulled = VanillaInventoryUtils.pullFromInventory(target, nToPull, inBuffer, getFilter());
			}
			if (pulled != null) {
				if (stb != null) {
					stb.updateBlock(false);
				}
				getOwner().setBufferItem(inBuffer == null ? pulled : inBuffer);
				return true;
			}
		}
		return false;
	}
}
