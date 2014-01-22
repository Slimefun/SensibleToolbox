package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.machines.gui.ProgressCounter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a straightforward machine which processes items from its input slots to
 * an internal processing store, and places a resulting item in its output slots.
 */
public abstract class SimpleProcessingMachine extends BaseSTBMachine {
	private int progress; // ticks remaining till this work cycle is done
	private ItemStack processing = null; // item currently being processed
	private final ProgressCounter progressCounter;

	protected SimpleProcessingMachine() {
		super();
		progressCounter = new ProgressCounter(this, getProgressItemSlot(), getProgressCounterSlot());
	}

	public SimpleProcessingMachine(ConfigurationSection conf) {
		super(conf);
		progressCounter = new ProgressCounter(this, getProgressItemSlot(), getProgressCounterSlot());
		setProgress(conf.getInt("progress"));
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		// TODO: freeze processing slot
		conf.set("progress", getProgress());
		return conf;
	}

	protected abstract CustomRecipeCollection.CustomRecipe getCustomRecipeFor(ItemStack stack);
	protected abstract int getProgressItemSlot();
	protected abstract int getProgressCounterSlot();

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = Math.max(0, progress);
		if (progress > 0) {
			progressCounter.scheduleRepaint();
		} else {
			progressCounter.repaint();
		}
	}

	public ItemStack getProcessing() {
		return processing;
	}

	public void setProcessing(ItemStack processing) {
		System.out.println("set processing = " + processing);
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
		if (!isActive()) {
			return;
		}
		if (getProcessing() == null) {
			for (int slot : getInputSlots()) {
				if (getInventory().getItem(slot) != null) {
					// move an item into processing
					pullItemIntoProcessing(slot);
				}
			}
		} else {
			if (getProgress() > 0 && getCharge() > 0) {
				setProgress(getProgress() - 1);
				setCharge(getCharge() - 1);
				if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled() && getLocation().getWorld().getFullTime() % 20 == 0) {
					ParticleEffect.LARGE_SMOKE.play(getLocation().add(0.5, 1.0, 0.5), 0.2f, 1.0f, 0.2f, 0.001f, 5);
				}
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
					System.out.println("jammed! no space in output");
					setJammed(true);
				}
			}
		}
	}

	private void pushItemIntoOutput(ItemStack result, int slot) {
		ItemStack stack = getInventory().getItem(slot);
		if (stack == null) {
			stack = result;
		} else {
			stack.setAmount(stack.getAmount() + result.getAmount());
		}
		getInventory().setItem(slot, stack);
		updateBlock(false);
		setProcessing(null);
		System.out.println("set output slot " + slot + " = " + stack);
	}

	private void pullItemIntoProcessing(int slot) {
		ItemStack stack = getInventory().getItem(slot);
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
		System.out.println("set input slot " + slot + " = " + stack);
		getInventory().setItem(slot, stack);
		updateBlock(false);
	}
}
