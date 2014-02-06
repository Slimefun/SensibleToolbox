package me.desht.sensibletoolbox.util;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.*;

import java.util.HashMap;

public class VanillaInventoryUtils {
	public static Inventory getVanillaInventoryFor(Block target) {
		switch (target.getType()) {
			case CHEST:
				Chest c = (Chest) target.getState();
				if (c.getInventory().getHolder() instanceof DoubleChest) {
					DoubleChest dc = (DoubleChest) c.getInventory().getHolder();
					return dc.getInventory();
				} else {
					return c.getBlockInventory();
				}
			case DISPENSER: case HOPPER: case DROPPER: case FURNACE: case BREWING_STAND:
			case BURNING_FURNACE:
				return ((InventoryHolder) target.getState()).getInventory();
			// TODO other vanilla inventory types
			default:
				return null;
		}
	}

	/**
	 * Attempt to insert items from the given buffer into the given block, which should be a
	 * vanilla inventory holder.  Items successfully inserted will be removed from the buffer stack.
	 * Any items which could not be inserted will be returned.
	 *
	 *
	 * @param target the block to insert into
	 * @param source the item stack to take items from
	 * @param amount the number of items from the buffer to insert
	 * @param side the side on which insertion is occurring (some blocks care about this, e.g. furnace)
	 * @return the number of items actually inserted
	 */
	public static int vanillaInsertion(Block target, ItemStack source, int amount, BlockFace side, boolean sorting) {
		if (source == null || source.getAmount() == 0) {
			return 0;
		}
		Inventory targetInv = getVanillaInventoryFor(target);

		if (targetInv != null) {
			if (sorting && !sortingOK(source, targetInv)) {
				return 0;
			}
			ItemStack stack = source.clone();
			stack.setAmount(Math.min(amount, stack.getAmount()));
			System.out.println("insert " + STBUtil.describeItemStack(stack) + " into " + target);
			HashMap<Integer,ItemStack> excess;
			switch (targetInv.getType()) {
				case FURNACE:
					if (side == BlockFace.DOWN) {
						// no insertion from below
						return 0;
					}
					excess = addToFurnace((FurnaceInventory) targetInv, stack, side);
					break;
				case BREWING:
					if (side == BlockFace.DOWN) {
						// no insertion from below
						return 0;
					}
					excess = addToBrewingStand((BrewerInventory) targetInv, stack, side);
					break;
				default:
					excess = targetInv.addItem(stack);
					break;
			}
			if (!excess.isEmpty()) {
				for (ItemStack s : excess.values()) {
					if (s.isSimilar(source)) {
						source.setAmount((source.getAmount() - stack.getAmount()) + s.getAmount());
						return stack.getAmount() - s.getAmount();
					}
				}
				return stack.getAmount(); // shouldn't get here!
			} else {
				source.setAmount(source.getAmount() - stack.getAmount());
				return stack.getAmount();
			}
		}
		return 0;
	}


	/**
	 * Attempt to pull items from an inventory into a receiving buffer.
	 *
	 * @param target the block containing the target inventory
	 * @param amount the desired number of items
	 * @param buffer an array of item stacks into which to insert the transferred items
	 * @param filter a filter to whitelist/blacklist items
	 * @return the items pulled, or null if nothing was pulled
	 */
	public static ItemStack pullFromInventory(Block target, int amount, ItemStack buffer, Filter filter) {
		Inventory targetInv = getVanillaInventoryFor(target);
		if (targetInv == null) {
			return null;
		}
		IntRange range = getExtractionSlots(targetInv);
		for (int slot = range.getMinimumInteger(); slot <= range.getMaximumInteger(); slot++) {
			ItemStack stack = targetInv.getItem(slot);
			if (stack != null) {
				if ((filter == null || filter.shouldPass(stack)) && (buffer == null || stack.isSimilar(buffer))) {
					System.out.println("pulling " + STBUtil.describeItemStack(stack) + " from " + target);
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
						targetInv.setItem(slot, stack.getAmount() > 0 ? stack : null);
						return buffer;
					}
				}
			}
		}
		return null;
	}

	private static IntRange getExtractionSlots(Inventory inv) {
		switch (inv.getType()) {
			case FURNACE: return new IntRange(2);
			case BREWING: return new IntRange(0, 2);
			default: return new IntRange(0, inv.getSize() - 1);
		}
	}

	private static HashMap<Integer, ItemStack> addToBrewingStand(BrewerInventory targetInv, ItemStack stack, BlockFace side) {
		HashMap<Integer, ItemStack> res = new HashMap<Integer, ItemStack>();
		ItemStack excess = null;
		if (side == BlockFace.UP) {
			// ingredient slot
			if (!STBUtil.isPotionIngredient(stack.getData())) {
				excess = stack;
			} else {
				excess = putStack(targetInv, 3, stack);
			}
		} else {
			// water/potion slots
			if (stack.getType() != Material.GLASS_BOTTLE && stack.getType() != Material.POTION) {
				excess = stack;
			} else {
				for (int slot = 0; slot <= 2; slot++) {
					excess = putStack(targetInv, slot, stack);
					if (excess == null) {
						break; // all fitted
					} else {
						// some or none fitted, continue with other slots
						stack.setAmount(excess.getAmount());
					}
				}
			}
		}
		if (excess != null) {
			res.put(0, excess);
		}
		return res;
	}

	private static HashMap<Integer, ItemStack> addToFurnace(FurnaceInventory targetInv, ItemStack stack, BlockFace side) {
		HashMap<Integer, ItemStack> res = new HashMap<Integer, ItemStack>();
		int slot;
		switch (side) {
			case UP: slot = 0; break; // smelting slot
			default: slot = 1; break; // fuel slot
		}
		ItemStack excess = putStack(targetInv, slot, stack);
		if (excess != null) {
			res.put(slot, excess);
		}
		return res;
	}

	/**
	 * Attempt to put the given item stack in the given slot.  Some or all items may not fit if there's
	 * already something in the slot.
	 *
	 * @param inv the inventory
	 * @param slot the slot to insert into
	 * @param stack the items to insert
	 * @return the items left over, or null if nothing was left over (all inserted)
	 */
	private static ItemStack putStack(Inventory inv, int slot, ItemStack stack) {
		ItemStack current = inv.getItem(slot);
		if (current == null) {
			inv.setItem(slot, stack);
			return null;
		} else if (current.isSimilar(stack)) {
			int toAdd = Math.min(stack.getAmount(), current.getType().getMaxStackSize() - current.getAmount());
			current.setAmount(current.getAmount() + toAdd);
			inv.setItem(slot, current);
			if (toAdd < stack.getAmount()) {
				ItemStack leftover = stack.clone();
				leftover.setAmount(stack.getAmount() - toAdd);
				return leftover;
			} else {
				return null;
			}
		} else {
			return stack;
		}
	}

	private static boolean sortingOK(ItemStack candidate, Inventory inv) {
		boolean isEmpty = true;
		for (ItemStack stack : inv) {
			if (candidate.isSimilar(stack)) {
				return true;
			} else if (stack != null) {
				isEmpty = false;
			}
		}
		return isEmpty;
	}
}
