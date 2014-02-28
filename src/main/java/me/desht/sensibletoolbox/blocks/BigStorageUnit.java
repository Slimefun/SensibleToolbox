package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.ItemNames;
import me.desht.sensibletoolbox.blocks.machines.AbstractProcessingMachine;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.io.IOException;

public class BigStorageUnit extends AbstractProcessingMachine {
	private static final MaterialData md = new MaterialData(Material.LOG_2, (byte) 1);
	private static final int TICK_RATE = 5;
	private ItemStack stored;
	private int amount;
	private int maxCapacity;
	private final String signLabel[] = new String[4];
	private int oldAmount = -1;

	public BigStorageUnit() {
		setStored(null);
		signLabel[0] = makeItemLabel();
		amount = 0;
	}

	public BigStorageUnit(ConfigurationSection conf) {
		super(conf);
		signLabel[0] = makeItemLabel();
		try {
			Inventory inv = BukkitSerialization.fromBase64(conf.getString("stored"));
			setStored(inv.getItem(0));
		} catch (IOException e) {
			e.printStackTrace();
		}
		setAmount(conf.getInt("amount"));
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		Inventory inv = Bukkit.createInventory(null, 9);
		inv.setItem(0, stored);
		conf.set("stored", BukkitSerialization.toBase64(inv, 1));
		conf.set("amount", amount);
		return conf;
	}

	public void setAmount(int amount) {
		this.amount = amount;
		signLabel[1] = amount > 0 ? Integer.toString(amount) : "";
	}

	public int getAmount() {
		return amount;
	}

	public ItemStack getStored() {
		return stored;
	}

	public void setStored(ItemStack stored) {
		Debugger.getInstance().debug(this + " set stored item = " + stored);
		this.stored = stored;
		maxCapacity = getStackCapacity() * (stored == null ? 64 : stored.getMaxStackSize());
		if (stored != null) {
			String[] lines = WordUtils.wrap(ItemNames.lookup(stored), 15).split("\\n");
			signLabel[2] = lines[0];
			if (lines.length > 1) {
				signLabel[3] = lines[1];
			}
		} else {
			signLabel[2] = signLabel[3] = "";
		}
	}

	@Override
	public int[] getInputSlots() {
		return new int[] { 10 };
	}

	@Override
	public int[] getOutputSlots() {
		return new int[] { 14 };
	}

	@Override
	public int[] getUpgradeSlots() {
		return new int[] { 50, 51, 52, 53 };
	}

	@Override
	public int getUpgradeLabelSlot() {
		return 49;
	}

	@Override
	protected void playActiveParticleEffect() {
		// nothing
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "BSU";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Big Storage Unit", "Stores up to " + getStackCapacity() + " stacks", "of a single item type" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("LSL", "L L", "LLL");
		recipe.setIngredient('L', Material.LOG);
		recipe.setIngredient('S', Material.WOOD_STEP);
		return recipe;
	}

	@Override
	public Recipe[] getExtraRecipes() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("LSL", "L L", "LLL");
		recipe.setIngredient('L', Material.LOG_2);
		recipe.setIngredient('S', Material.WOOD_STEP);
		return new Recipe[] { recipe };
	}

	@Override
	public boolean acceptsEnergy(BlockFace face) {
		return false;
	}

	@Override
	public boolean suppliesEnergy(BlockFace face) {
		return false;
	}

	@Override
	public int getMaxCharge() {
		return 0;
	}

	@Override
	public int getChargeRate() {
		return 0;
	}

	public int getStackCapacity() {
		return 128;
	}

	@Override
	public boolean shouldTick() {
		return true;
	}

	@Override
	public void onServerTick() {
		if (getTicksLived() % TICK_RATE == 0) {
			// 1. move items from input to storage
			int inputSlot = getInputSlots()[0];
			ItemStack stackIn = getGUI().getInventory().getItem(inputSlot);
			if (stackIn != null && (stored == null || stackIn.isSimilar(stored) && !isFull()))  {
				if (stored == null) {
					ItemStack copy = stackIn.clone();
					copy.setAmount(1);
					setStored(copy);
				}
				int toPull = Math.min(stackIn.getAmount(), maxCapacity - amount);
				setAmount(amount + toPull);
				stackIn.setAmount(stackIn.getAmount() - toPull);
				System.out.println("set input slot = " + stackIn);
				getGUI().getInventory().setItem(inputSlot, stackIn.getAmount() > 0 ? stackIn : null);
				if (stackIn.getAmount() == 0) {
					// workaround to avoid leaving ghost items in the input slot
					STBUtil.forceInventoryRefresh(getInventory());
				}
			}

			// 2. move items from storage to output
			if (stored != null) {
				int outputSlot = getOutputSlots()[0];
				ItemStack stackOut = getGUI().getInventory().getItem(outputSlot);
				int toPush = Math.min(amount, stored.getMaxStackSize() - (stackOut == null ? 0 : stackOut.getAmount()));
				if (toPush > 0) {
					setAmount(amount - toPush);
					if (stackOut == null) {
						stackOut = stored.clone();
						stackOut.setAmount(toPush);
					} else {
						stackOut.setAmount(stackOut.getAmount() + toPush);
					}
					if (amount == 0) {
						setStored(null);
					}
					getGUI().getInventory().setItem(outputSlot, stackOut);
				}
			}

			// 3. perform any necessary updates if storage has changed
			if (amount != oldAmount) {
				getProgressMeter().setMaxProgress(maxCapacity);
				setProcessing(stored);
				setProgress(maxCapacity - amount);
				updateBlock(false);
				updateAttachedLabelSigns();
				oldAmount = amount;
			}
		}

		super.onServerTick();
	}

	@Override
	public void setLocation(Location loc) {
		if (loc == null && getProcessing() != null) {
			if (dropsItemsOnBreak()) {
				// dump contents on floor (could make a big mess)
				Location current = getLocation();
				amount = Math.min(4096, amount);  // max 64 stacks will be dropped
				while (amount > 0) {
					ItemStack stack = stored.clone();
					stack.setAmount(Math.min(amount, stored.getMaxStackSize()));
					current.getWorld().dropItemNaturally(current, stack);
					amount -= stored.getMaxStackSize();
				}
				amount = 0;
				stored = null;
			}
		}
		super.setLocation(loc);
		if (loc != null) {
			getProgressMeter().setMaxProgress(maxCapacity);
			setProcessing(stored);
			setProgress(maxCapacity - amount);
		}
	}

	private boolean dropsItemsOnBreak() {
		return true;
	}

	@Override
	public int getProgressItemSlot() {
		return 12;
	}

	@Override
	public int getProgressCounterSlot() {
		return 3;
	}

	@Override
	public Material getProgressIcon() {
		return Material.DIAMOND_CHESTPLATE;
	}

	public boolean isFull() {
		return stored != null && amount >= getStackCapacity() * stored.getMaxStackSize();
	}

	@Override
	public String getProgressMessage() {
		return "Stored: " + getAmount() + "/" + maxCapacity;
	}

	@Override
	public boolean acceptsItemType(ItemStack stack) {
		return stored == null || stored.isSimilar(stack);
	}

	@Override
	protected String[] getSignLabel() {
		return signLabel;
	}

	@Override
	public int insertItems(ItemStack item, BlockFace face, boolean sorting) {
		if (stored == null) {
			ItemStack in = item.clone();
			in.setAmount(1);
			setStored(in);
			setAmount(item.getAmount());
			return item.getAmount();
		} else if (item.isSimilar(stored)) {
			int toInsert = Math.min(item.getAmount(), maxCapacity - getAmount());
			setAmount(getAmount() + toInsert);
			return toInsert;
		} else {
			return 0;
		}
	}

	@Override
	public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount) {
		if (stored == null) {
			return null;
		} else if (receiver == null) {
			ItemStack returned = stored.clone();
			int nExtracted = Math.min(amount, getAmount());
			returned.setAmount(nExtracted);
			setAmount(getAmount() - nExtracted);
			return returned;
		} else if (receiver.isSimilar(stored)) {
			int nExtracted = Math.min(amount, getAmount());
			nExtracted = Math.min(nExtracted, receiver.getMaxStackSize() - receiver.getAmount());
			receiver.setAmount(receiver.getAmount() + nExtracted);
			setAmount(getAmount() - nExtracted);
			if (getAmount() <= 0) {
				setStored(null);
			}
			return receiver;
		} else {
			return null;
		}
	}
}
