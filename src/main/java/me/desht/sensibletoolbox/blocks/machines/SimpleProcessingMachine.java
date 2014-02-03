package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.blocks.machines.gui.ProgressCounter;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Represents a straightforward machine which processes items from its input slots to
 * an internal processing store, and places a resulting item in its output slots.
 */
public abstract class SimpleProcessingMachine extends BaseSTBMachine {
	private static final long PROGRESS_INTERVAL = 10;
	private double progress; // ticks remaining till this work cycle is done
	private ItemStack processing = null; // item currently being processed
	private final ProgressCounter progressCounter;

	protected SimpleProcessingMachine() {
		super();
		progressCounter = new ProgressCounter(this, getProgressItemSlot(), getProgressCounterSlot(), getProgressIcon());
	}

	public SimpleProcessingMachine(ConfigurationSection conf) {
		super(conf);
		progressCounter = new ProgressCounter(this, getProgressItemSlot(), getProgressCounterSlot(), getProgressIcon());
		setProgress(conf.getInt("progress"));
		if (getProgress() > 0) {
			thawSlots(conf.getString("processing", ""), getProgressItemSlot());
		}
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		if (getProgress() > 0) {
			conf.set("processing", freezeSlots(getProgressItemSlot()));
		}
		conf.set("progress", getProgress());
		return conf;
	}

	protected abstract CustomRecipeCollection.CustomRecipe getCustomRecipeFor(ItemStack stack);
	protected abstract int getProgressItemSlot();
	protected abstract int getProgressCounterSlot();
	protected abstract Material getProgressIcon();
	protected abstract void playActiveParticleEffect();

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = Math.max(0, progress);
		progressCounter.repaintNeeded();
	}

	public ItemStack getProcessing() {
		return processing;
	}

	public void setProcessing(ItemStack processing) {
//		System.out.println("set processing = " + processing);
		this.processing = processing;
		if (processing == null) {
			progressCounter.repaint();
		}
	}

	@Override
	public void setLocation(Location loc) {
		if (getProcessing() != null) {
			loc.getWorld().dropItemNaturally(loc, getProcessing());
			setProcessing(null);
		}
		super.setLocation(loc);
	}

	@Override
	public void onServerTick() {
		if (isActive()) {
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
					ItemStack result = recipe.getResult();
					int slot = findOutputSlot(result);
					if (slot >= 0) {
						// good - move result to output and clear processing
						pushItemIntoOutput(result, slot);
					} else {
						// jammed
//						System.out.println("jammed! no space in output");
						setJammed(true);
					}
				}
			}
			if (getLocation().getWorld().getFullTime() % PROGRESS_INTERVAL == 0 && getInventory().getViewers().size() > 0) {
				progressCounter.doRepaint();
			}
		}
		super.onServerTick();
	}

	private void pushItemIntoOutput(ItemStack result, int slot) {
		if (getAutoEjectDirection() != null && getAutoEjectDirection() != BlockFace.SELF) {
			Block target = getLocation().getBlock().getRelative(getAutoEjectDirection());
			if (!target.getType().isSolid() || target.getType() == Material.WALL_SIGN) {
				// no block there - just drop the item
				Item item = target.getWorld().dropItem(target.getLocation().add(0.5, 0.5, 0.5), result);
//				item.setVelocity(new Vector(0, 0, 0));
				result = null;
			} else {
				BaseSTBBlock stb = LocationManager.getManager().get(target.getLocation());
				ItemStack toInsert = result.clone();
				if (stb instanceof STBInventoryHolder) {
					// try to insert into STB block
					int nInserted = ((STBInventoryHolder) stb).insertItems(toInsert, getAutoEjectDirection().getOppositeFace());
					result = toInsert;
					result.setAmount(result.getAmount() - nInserted);
				} else {
					// vanilla insertion?
					int nInserted = STBUtil.vanillaInsertion(target, toInsert, 1);
					result = toInsert;
				}
			}
		}

		if (result != null && result.getAmount() > 0) {
			ItemStack stack = getInventory().getItem(slot);
			if (stack == null) {
				stack = result;
			} else {
				stack.setAmount(stack.getAmount() + result.getAmount());
			}
			getInventory().setItem(slot, stack);
//			System.out.println("set output slot " + slot + " = " + STBUtil.describeItemStack(stack));
		}

		updateBlock(false);
		setProcessing(null);
	}

	private void pullItemIntoProcessing(int inputSlot) {
		ItemStack stack = getInventory().getItem(inputSlot);
		ItemStack toProcess = stack.clone();
		toProcess.setAmount(1);
		setProcessing(toProcess);
		CustomRecipeCollection.CustomRecipe recipe = getCustomRecipeFor(toProcess);
		progressCounter.setInitialProgress(recipe.getProcessingTime());
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
