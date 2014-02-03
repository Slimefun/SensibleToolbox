package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.api.STBMachine;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.Filter;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;

import java.util.Arrays;

public class PullerModule extends DirectionalItemRouterModule {

	public PullerModule() {
	}

	public PullerModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public String getItemName() {
		return "Item Router Puller Module";
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
			if (stb instanceof STBInventoryHolder) {
				ItemStack pulled = ((STBInventoryHolder)stb).extractItems(getDirection().getOppositeFace(), inBuffer, nToPull);
				if (pulled != null) {
					stb.updateBlock(false);
					getOwner().setBufferItem(inBuffer == null ? pulled : inBuffer);
					return true;
				}
			} else {
				// vanilla inventory holder?
//				ItemStack[] buffer = new ItemStack[] { inBuffer };
				int p = doVanillaExtraction(target, nToPull, inBuffer);
				return p > 0;
			}
		}

		return false;
	}

	private int doVanillaExtraction(Block target, int toTake, ItemStack buffer) {
		Inventory targetInv = null;
		switch (target.getType()) {
			case CHEST:
				Chest c = (Chest) target.getState();
				if (c.getInventory().getHolder() instanceof DoubleChest) {
					DoubleChest dc = (DoubleChest) c.getInventory().getHolder();
					targetInv = dc.getInventory();
				} else {
					targetInv = c.getBlockInventory();
				}
				break;
			case HOPPER: case DROPPER: case DISPENSER:
				BlockState bs = target.getState();
				targetInv = ((InventoryHolder) bs).getInventory();
				break;
			default:
				break;
		}
		if (targetInv != null) {
			ItemStack res = pullFromInventory(targetInv, toTake, buffer, getFilter());
			if (res != null) {
				getOwner().setBufferItem(res);
				return res.getAmount();
			}
		}
		return 0;
	}

	/**
	 * Attempt to pull items from an inventory into a receiving buffer.
	 *
	 * @param inv the inventory to pull from
	 * @param amount the desired number of items
	 * @param buffer an array of item stacks into which to insert the transferred items
	 * @return the items pulled, or null if nothing was pulled
	 */
	public static ItemStack pullFromInventory(Inventory inv, int amount, ItemStack buffer, Filter filter) {
		for (int slot = 0; slot < inv.getSize(); slot++) {
			ItemStack stack = inv.getItem(slot);
			if (stack != null) {
				if ((filter == null || filter.matches(stack)) && (buffer == null || stack.isSimilar(buffer))) {
					System.out.println("pulling " + STBUtil.describeItemStack(stack));
					int toTake = Math.min(amount, stack.getAmount());
					if (buffer != null) {
						toTake = Math.min(toTake, buffer.getType().getMaxStackSize() - buffer.getAmount());
					}
					if (toTake > 0) {
						if (buffer == null) {
							buffer = stack.clone();
							buffer.setAmount(toTake);
						} else {
							buffer.setAmount(buffer.getAmount() + toTake);
						}
						stack.setAmount(stack.getAmount() - toTake);
						inv.setItem(slot, stack.getAmount() > 0 ? stack : null);
						return buffer;
					}
				}
			}
		}
		return null;
	}
}
