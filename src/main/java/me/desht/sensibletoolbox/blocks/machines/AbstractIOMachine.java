package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.CustomRecipeCollection;
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

	protected abstract CustomRecipeCollection.CustomRecipe getCustomRecipeFor(ItemStack stack);

	@Override
	protected void playOutOfChargeSound() {
		getLocation().getWorld().playSound(getLocation(), Sound.BLAZE_DEATH, 1.0f, 0.25f);
	}

	@Override
	public void onServerTick() {
		if (isRedstoneActive()) {
			if (getProcessing() == null) {
				// not doing any processing - anything in input to take?
				for (int slot : getInputSlots()) {
					if (getInventory().getItem(slot) != null) {
						pullItemIntoProcessing(slot);
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
					CustomRecipeCollection.CustomRecipe recipe = getCustomRecipeFor(getProcessing());
					pushItemIntoOutput(recipe.getResult());
				}
			}
		}
		super.onServerTick();
	}

	private void pushItemIntoOutput(ItemStack result) {
		if (getAutoEjectDirection() != null && getAutoEjectDirection() != BlockFace.SELF) {
			result = autoEject(result);
		}

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
	 * Attempt to auto-eject a resulting item.
	 *
	 * @param result the item to eject
	 * @return anything that could not be auto-ejected
	 */
	private ItemStack autoEject(ItemStack result) {
		Block target = getLocation().getBlock().getRelative(getAutoEjectDirection());
		if (!target.getType().isSolid() || target.getType() == Material.WALL_SIGN) {
			// no block there - just drop the item
			target.getWorld().dropItem(target.getLocation().add(0.5, 0.5, 0.5), result);
			result = null;
		} else {
			BaseSTBBlock stb = LocationManager.getManager().get(target.getLocation());
			ItemStack toInsert = result.clone();
			if (stb instanceof STBInventoryHolder) {
				// try to insert into STB block
				int nInserted = ((STBInventoryHolder) stb).insertItems(toInsert, getAutoEjectDirection().getOppositeFace(), false);
				result = toInsert;
				result.setAmount(result.getAmount() - nInserted);
			} else {
				// vanilla insertion?
				int nInserted = VanillaInventoryUtils.vanillaInsertion(target, toInsert, 1, getAutoEjectDirection().getOppositeFace(), false);
				result = toInsert;
			}
		}
		return result;
	}

	private void pullItemIntoProcessing(int inputSlot) {
		ItemStack stack = getInventory().getItem(inputSlot);
		ItemStack toProcess = stack.clone();
		toProcess.setAmount(1);
		setProcessing(toProcess);
		CustomRecipeCollection.CustomRecipe recipe = getCustomRecipeFor(toProcess);
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
}
