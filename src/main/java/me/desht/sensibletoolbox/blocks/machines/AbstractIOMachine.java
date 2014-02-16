package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.recipes.CustomRecipeManager;
import me.desht.sensibletoolbox.recipes.ProcessingResult;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.VanillaInventoryUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a machine which processes items from its input slots to
 * an internal processing store, and places resulting items in its output slots.
 */
public abstract class AbstractIOMachine extends AbstractProcessingMachine {
	protected AbstractIOMachine() {
		super();
	}

	public AbstractIOMachine(ConfigurationSection conf) {
		super(conf);
	}

//	protected abstract ProcessingResult getCustomRecipeFor(ItemStack stack);

	@Override
	protected void playStartupSound() {
		getLocation().getWorld().playSound(getLocation(), Sound.HORSE_SKELETON_IDLE, 1.0f, 0.5f);
	}

	@Override
	protected void playOutOfChargeSound() {
		getLocation().getWorld().playSound(getLocation(), Sound.ENDERDRAGON_HIT, 1.0f, 0.5f);
	}

	@Override
	public void onServerTick() {
		if (isRedstoneActive()) {
			if (getProcessing() == null) {
				// not doing any processing - anything in input to take?
				for (int slot : getInputSlots()) {
					if (getInventory().getItem(slot) != null) {
						pullItemIntoProcessing(slot);
						playStartupSound();
						break;
					}
				}
			} else {
				if (getProgress() > 0 && getCharge() > 0) {
					// currently processing....
					setProgress(getProgress() - getSpeedMultiplier());
					setCharge(getCharge() - getPowerMultiplier());
					playActiveParticleEffect();
				}
				if (getProgress() <= 0 && !isJammed()) {
					// done processing - try to move item into output
					ProcessingResult recipe = getCustomRecipeFor(getProcessing());
					if (recipe != null) {
						// shouldn't ever be null, but let's be paranoid here
						pushItemIntoOutput(recipe.getResult());
					}
				}
			}
			if (getAutoEjectDirection() != null && getAutoEjectDirection() != BlockFace.SELF && getTicksLived() % 10 == 0) {
				for (int slot : getOutputSlots()) {
					ItemStack stack = getInventory().getItem(slot);
					if (stack != null) {
						if (autoEject(stack)) {
							stack.setAmount(stack.getAmount() - 1);
							getInventory().setItem(slot, stack.getAmount() == 0 ? null : stack);
						}
						break;
					}
				}
			}
		}
		super.onServerTick();
	}

	private void pushItemIntoOutput(ItemStack result) {
//		if (getAutoEjectDirection() != null && getAutoEjectDirection() != BlockFace.SELF) {
//			result = autoEject(result);
//		}

		if (result != null && result.getAmount() > 0) {
			int slot = findOutputSlot(result);
			if (slot >= 0) {
				// good, there's space to move it out of processing
				ItemStack stack = getInventory().getItem(slot);
				if (stack == null) {
					stack = result;
				} else {
					stack.setAmount(stack.getAmount() + result.getAmount());
				}
				getInventory().setItem(slot, stack);
			} else {
				// no space!
				setJammed(true);
			}
		}

		if (!isJammed()) {
			setProcessing(null);
			updateBlock(false);
		}
	}

	/**
	 * Attempt to auto-eject one from an output slot.
	 *
	 * @param result the item to eject
	 * @return true if an item was ejected, false otherwise
	 */
	private boolean autoEject(ItemStack result) {
		Block target = getLocation().getBlock().getRelative(getAutoEjectDirection());
		ItemStack item = result.clone();
		item.setAmount(1);
		if (!target.getType().isSolid() || target.getType() == Material.WALL_SIGN) {
			// no block there - just drop the item
			target.getWorld().dropItem(target.getLocation().add(0.5, 0.5, 0.5), result);
			return true;
		} else {
			BaseSTBBlock stb = LocationManager.getManager().get(target.getLocation());
			int nInserted;
			if (stb instanceof STBInventoryHolder) {
				// try to insert into STB block
				nInserted = ((STBInventoryHolder) stb).insertItems(item, getAutoEjectDirection().getOppositeFace(), false);
			} else {
				// vanilla insertion?
				nInserted = VanillaInventoryUtils.vanillaInsertion(target, item, 1, getAutoEjectDirection().getOppositeFace(), false);
			}
			return nInserted > 0;
		}
	}

	private void pullItemIntoProcessing(int inputSlot) {
		ItemStack stack = getInventory().getItem(inputSlot);
		ItemStack toProcess = stack.clone();
		toProcess.setAmount(1);
		ProcessingResult recipe = getCustomRecipeFor(toProcess);
		if (recipe == null) {
			// shouldn't happen but...
			getLocation().getWorld().dropItemNaturally(getLocation(), stack);
			getInventory().setItem(inputSlot, null);
			return;
		}
		setProcessing(toProcess);
		getProgressCounter().initialize(recipe.getProcessingTime());
		setProgress(recipe.getProcessingTime());
		if (stack.getAmount() > 1) {
			stack.setAmount(stack.getAmount() - 1);
		} else {
			stack = null;
		}
		getInventory().setItem(inputSlot, stack);
		updateBlock(false);
	}


	@Override
	public boolean acceptsItemType(ItemStack item) {
		return CustomRecipeManager.getManager().hasRecipe(this, item);
	}

	protected ProcessingResult getCustomRecipeFor(ItemStack stack) {
		return CustomRecipeManager.getManager().getRecipe(this, stack);
	}

	@Override
	public boolean acceptsEnergy(BlockFace face) {
		return true;
	}

	@Override
	public boolean suppliesEnergy(BlockFace face) {
		return false;
	}
}
