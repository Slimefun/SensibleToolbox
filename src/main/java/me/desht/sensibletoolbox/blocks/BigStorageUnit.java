package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.ItemNames;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.blocks.machines.AbstractProcessingMachine;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.io.IOException;
import java.util.UUID;

public class BigStorageUnit extends AbstractProcessingMachine {
	private static final MaterialData md = STBUtil.makeLog(TreeSpecies.DARK_OAK);
	private static final int TICK_RATE = 5;
	private ItemStack stored;
	private int storageAmount;
	private int outputAmount;
	private int maxCapacity;
	private final String signLabel[] = new String[4];
	private int oldTotalAmount = -1;

	public BigStorageUnit() {
		setStored(null);
		signLabel[0] = makeItemLabel();
		oldTotalAmount = storageAmount = outputAmount = 0;
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
		setStorageAmount(conf.getInt("amount"));
		oldTotalAmount = storageAmount;
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		Inventory inv = Bukkit.createInventory(null, 9);
		inv.setItem(0, stored);
		conf.set("stored", BukkitSerialization.toBase64(inv, 1));
		conf.set("amount", storageAmount);
		return conf;
	}

	public void setStorageAmount(int storageAmount) {
		this.storageAmount = Math.max(0, storageAmount);
	}

	public int getStorageAmount() {
		return storageAmount;
	}

	public int getTotalAmount() {
		return getStorageAmount() + outputAmount;
	}

	public ItemStack getStored() {
		return stored;
	}

	public int getOutputAmount() {
		return outputAmount;
	}

	public void setOutputAmount(int outputAmount) {
		this.outputAmount = outputAmount;
	}

	public void setStored(ItemStack stored) {
		Debugger.getInstance().debug(this + " set stored item = " + stored);
		if (stored != null) {
			this.stored = stored.clone();
			this.stored.setAmount(1);
		} else {
			this.stored = null;
		}
		maxCapacity = getStackCapacity() * (stored == null ? 64 : stored.getMaxStackSize());
		if (stored != null) {
			String[] lines = WordUtils.wrap(ItemNames.lookup(stored), 15).split("\\n");
			signLabel[2] = lines[0];
			if (lines.length > 1) {
				signLabel[3] = lines[1];
			}
		} else {
			signLabel[2] = ChatColor.ITALIC + "Empty";
			signLabel[3] = "";
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
		return new int[] { 41, 42, 43, 44 };
	}

	@Override
	public int getUpgradeLabelSlot() {
		return 40;
	}

	@Override
	public int getInventoryGUISize() {
		return 45;
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
			ItemStack stackIn = getInventoryItem(inputSlot);
			if (stackIn != null && (stored == null || stackIn.isSimilar(stored) && !isFull()))  {
				double chargeNeeded = getChargePerOperation(stackIn.getAmount());
				if (getCharge() >= chargeNeeded) {
					if (stored == null) {
						setStored(stackIn);
					}
					int toPull = Math.min(stackIn.getAmount(), maxCapacity - getStorageAmount());
					System.out.println(this + "pull " + stackIn + " into storage, toPull=" + toPull);
					setStorageAmount(getStorageAmount() + toPull);
					stackIn.setAmount(stackIn.getAmount() - toPull);
					setInventoryItem(inputSlot, stackIn.getAmount() > 0 ? stackIn : null);
					setCharge(getCharge() - chargeNeeded);
					if (stackIn.getAmount() == 0) {
						// workaround to avoid leaving ghost items in the input slot
						STBUtil.forceInventoryRefresh(getInventory());
					}
				}
			}

			ItemStack stackOut = getOutputItem();
			int newAmount = stackOut == null ? 0 : stackOut.getAmount();
			if (getOutputAmount() != newAmount) {
				System.out.println("output buffer size change! " + getOutputAmount() + " => " + newAmount);
				setOutputAmount(newAmount);
			}

			// 2. top up the output stack from storage
			if (stored != null) {
				int toPush = Math.min(getStorageAmount(), stored.getMaxStackSize() - getOutputAmount());
				if (toPush > 0) {
					System.out.println("push " + toPush + " from storage to output");
					if (stackOut == null) {
						stackOut = stored.clone();
						stackOut.setAmount(toPush);
					} else {
						stackOut.setAmount(stackOut.getAmount() + toPush);
					}
					setOutputItem(stackOut);
					setStorageAmount(getStorageAmount() - toPush);
				}
			}

			// 3. perform any necessary updates if storage has changed
			if (getTotalAmount() != oldTotalAmount) {
				signLabel[1] = getTotalAmount() > 0 ? Integer.toString(getTotalAmount()) : "";
				if (getTotalAmount() == 0) {
					setStored(null);
				}
				Debugger.getInstance().debug(2, this + " amount changed! " + oldTotalAmount + " -> " + getTotalAmount());
				getProgressMeter().setMaxProgress(maxCapacity);
				setProcessing(stored);
				setProgress(maxCapacity - getStorageAmount());
				updateBlock(false);
				updateAttachedLabelSigns();
				oldTotalAmount = getTotalAmount();
			}
		}

		super.onServerTick();
	}

	protected void setOutputItem(ItemStack stackOut) {
		setInventoryItem(getOutputSlots()[0], stackOut);
	}

	@Override
	public void setLocation(Location loc) {
		if (loc == null && getProcessing() != null) {
			if (dropsItemsOnBreak()) {
				// dump contents on floor (could make a big mess)
				Location current = getLocation();
				storageAmount = Math.min(4096, storageAmount);  // max 64 stacks will be dropped
				while (storageAmount > 0) {
					ItemStack stack = stored.clone();
					stack.setAmount(Math.min(storageAmount, stored.getMaxStackSize()));
					current.getWorld().dropItemNaturally(current, stack);
					storageAmount -= stored.getMaxStackSize();
				}
				setStorageAmount(0);
			}
		}
		super.setLocation(loc);
		if (loc != null) {
			getProgressMeter().setMaxProgress(maxCapacity);
			setProcessing(stored);
			setProgress(maxCapacity - storageAmount);
			ItemStack output = getOutputItem();
			outputAmount = output == null ? 0 : output.getAmount();
		}
	}

	@Override
	public void onInteractBlock(PlayerInteractEvent event) {
		ItemStack inHand = event.getPlayer().getItemInHand();
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && getStored() != null &&
				hasAccessRights(event.getPlayer()) &&
				(inHand.getType() == Material.AIR || inHand.isSimilar(getStored()))) {
			System.out.println(this + " left click, hand = " + inHand + ", stored = " + getStored());
			// try to extract items from the output stack
			int wanted = event.getPlayer().isSneaking() ? 1 : getStored().getMaxStackSize();
			int nExtracted = Math.min(wanted, getOutputAmount());
			if (nExtracted > 0) {
				Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation().add(0.5, 0.5, 0.5);
				ItemStack stack = getStored().clone();
				stack.setAmount(nExtracted);
				loc.getWorld().dropItem(loc, stack);
				setOutputAmount(getOutputAmount() - nExtracted);
				stack.setAmount(getOutputAmount());
				setOutputItem(stack);
			}
			event.setCancelled(true);
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking() &&
				hasAccessRights(event.getPlayer()) &&
				inHand.getType() != Material.AIR &&
				(getStored() == null || inHand.isSimilar(getStored()))) {
			double chargeNeeded = getChargePerOperation(inHand.getAmount());
			if (getCharge() >= chargeNeeded) {
				setStorageAmount(getStorageAmount() + inHand.getAmount());
				if (getStored() == null) {
					setStored(inHand);
				}
				event.getPlayer().setItemInHand(null);
				setCharge(getCharge() - chargeNeeded);
			} else {
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
				MiscUtil.errorMessage(event.getPlayer(), getItemName() + " has insufficient charge to accept items.");
			}
			event.setCancelled(true);
		} else {
			super.onInteractBlock(event);
		}
	}

	protected boolean dropsItemsOnBreak() {
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
		return stored != null && storageAmount >= getStackCapacity() * stored.getMaxStackSize();
	}

	@Override
	public String getProgressMessage() {
		return ChatColor.YELLOW + "Stored: " + getStorageAmount() + "/" + maxCapacity;
	}

	@Override
	public boolean acceptsItemType(ItemStack stack) {
		return stored == null || stored.isSimilar(stack);
	}

	@Override
	protected String[] getSignLabel() {
		return signLabel;
	}

	public ItemStack getOutputItem() {
		return getInventoryItem(getOutputSlots()[0]);
	}

	@Override
	public int insertItems(ItemStack item, BlockFace face, boolean sorting, UUID uuid) {
		if (!hasAccessRights(uuid)) {
			return 0;
		}
		double chargeNeeded = getChargePerOperation(item.getAmount());
		if (!isRedstoneActive() || getCharge() < chargeNeeded) {
			return 0;
		} else if (stored == null) {
			setStored(item);
			setStorageAmount(item.getAmount());
			setCharge(getCharge() - chargeNeeded);
			return item.getAmount();
		} else if (item.isSimilar(stored)) {
			int toInsert = Math.min(item.getAmount(), maxCapacity - getStorageAmount());
			setStorageAmount(getStorageAmount() + toInsert);
			setCharge(getCharge() - chargeNeeded);
			return toInsert;
		} else {
			return 0;
		}
	}

	@Override
	public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid) {
		if (!hasAccessRights(uuid)) {
			return null;
		}
		double chargeNeeded = getChargePerOperation(amount);
		if (!isRedstoneActive() || getStorageAmount() == 0 && getOutputAmount() == 0 || getCharge() < chargeNeeded) {
			return null;
		}

		if (receiver != null) {
			amount = Math.min(amount, receiver.getMaxStackSize() - receiver.getAmount());
			if (getStorageAmount() > 0 && !receiver.isSimilar(getStored())) {
				return null;
			}
			if (amount > getStorageAmount() && getOutputAmount() > 0 && !receiver.isSimilar(getOutputItem())) {
				return null;
			}
		}

		int fromStorage = Math.min(getStorageAmount(), amount);
		if (fromStorage > 0) {
			amount -= fromStorage;
			setStorageAmount(getStorageAmount() - fromStorage);
		}
		int fromOutput = 0;
		if (amount > 0) {
			fromOutput = Math.min(getOutputAmount(), amount);
			if (fromOutput > 0) {
				setOutputAmount(getOutputAmount() - fromOutput);
				ItemStack output = getOutputItem();
				output.setAmount(getOutputAmount());
				setOutputItem(output.getAmount() > 0 ? output : null);
			}
		}

		setCharge(getCharge() - chargeNeeded);

		System.out.println(this + "pulling " + fromStorage + " from storage & " + fromOutput + " from output stack");

		if (receiver == null) {
			ItemStack returned = stored.clone();
			returned.setAmount(fromStorage + fromOutput);
			return returned;
		} else {
			receiver.setAmount(receiver.getAmount() + fromStorage + fromOutput);
			return receiver;
		}
	}

	public double getChargePerOperation(int nItems) {
		return 0.0;
	}
}
