package me.desht.sensibletoolbox.items;

import me.desht.dhutils.ItemNames;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public abstract class CombineHoe extends BaseSTBItem {
	private Material seedType;
	private int seedAmount;
	private InventoryGUI gui;

	public static String getInventoryTitle() {
		return ChatColor.DARK_GREEN + "Seed Bag";
	}

	public CombineHoe() {
		super();
		seedType = null;
		seedAmount = 0;
	}

	public CombineHoe(ConfigurationSection conf) {
		super(conf);
		setSeedAmount(conf.getInt("amount"));
		setSeedType(Material.getMaterial(conf.getString("seeds")));
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("amount", getSeedAmount());
		conf.set("seeds", getSeedType() == null ? "" : getSeedType().toString());
		return conf;
	}

	public Material getSeedType() {
		return seedType;
	}

	public void setSeedType(Material seedType) {
		this.seedType = seedType;
	}

	public int getSeedAmount() {
		return seedAmount;
	}

	public void setSeedAmount(int seedAmount) {
		this.seedAmount = seedAmount;
	}

	@Override
	public boolean isEnchantable() {
		return false;
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Right-click dirt/grass:" + ChatColor.RESET + " till 3x3 area",
				"Right-click soil:" + ChatColor.RESET + " sow 3x3 area",
				"Right-click other:" + ChatColor.RESET + " open seed bag",
				"Left-click plants:" + ChatColor.RESET + " harvest 3x3 area",
				"Left-click leaves:" + ChatColor.RESET + " break 3x3x3 area",
		};
	}

	@Override
	public String[] getExtraLore() {
		if (getSeedType() != null && getSeedAmount() > 0) {
			String s = ItemNames.lookup(new ItemStack(getSeedType()));
			return new String[] { ChatColor.WHITE  + "Seeds: " + ChatColor.GOLD + getSeedAmount() + " x " + s };
		} else {
			return new String[0];
		}
	}

	@Override
	public boolean hasGlow() {
		return true;
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		Block b = event.getClickedBlock();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (b.getType() == Material.SOIL) {
				plantSeeds(event.getPlayer(), b);
				event.setCancelled(true);
				return;
			} else if (b.getType() == Material.DIRT || b.getType() == Material.GRASS) {
				tillSoil(event.getPlayer(), b);
				event.setCancelled(true);
				return;
			}
		}
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			gui = new InventoryGUI(event.getPlayer(), this, 9, getInventoryTitle());
			for (int i = 0; i < gui.getInventory().getSize(); i++) {
				gui.setSlotType(i, InventoryGUI.SlotType.ITEM);
			}
			populateSeedBag(gui);
			gui.show(event.getPlayer());
		}
	}

	@Override
	public void onBreakBlockWithItem(BlockBreakEvent event) {
		Block b = event.getBlock();
		Player player = event.getPlayer();
		if (b.getType() == Material.LEAVES || b.getType() == Material.LEAVES_2) {
			harvestLayer(player, b);
			if (!player.isSneaking()) {
				harvestLayer(player, b.getRelative(BlockFace.UP));
				harvestLayer(player, b.getRelative(BlockFace.DOWN));
			}
			damageHeldItem(player, (short) 1);
		} else if (STBUtil.isPlant(b.getType())) {
			harvestLayer(player, b);
			damageHeldItem(player, (short) 1);
		}
	}

	private boolean verifyUnique(Inventory inv, ItemStack stack, int exclude) {
		for (int i = 0; i < inv.getSize(); i++) {
			if (i != exclude && inv.getItem(i) != null && inv.getItem(i).getType() != stack.getType()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
		if (onCursor.getType() == Material.AIR) {
			return true;
		} else if (STBUtil.getCropType(onCursor.getType()) == null) {
			return false;
		} else if (!verifyUnique(gui.getInventory(), onCursor, slot)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean onPlayerInventoryClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
		return true;
	}

	@Override
	public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
		if (STBUtil.getCropType(toInsert.getType()) == null) {
			return 0;
		} else if (!verifyUnique(gui.getInventory(), toInsert, slot)) {
			return 0;
		} else {
			HashMap<Integer,ItemStack> excess = gui.getInventory().addItem(toInsert);
			int inserted = toInsert.getAmount();
			for (ItemStack stack : excess.values()) {
				inserted -= stack.getAmount();
			}
			return inserted;
		}
	}

	@Override
	public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
		return true;
	}

	@Override
	public boolean onClickOutside(HumanEntity player) {
		return false;
	}

	@Override
	public void onGUIClosed(HumanEntity player) {
		Material seedType = null;
		int count = 0;
		String err = null;
		for (int i = 0; i < gui.getInventory().getSize(); i++) {
			ItemStack stack = gui.getInventory().getItem(i);
			if (stack != null) {
				if (seedType != null && seedType != stack.getType()) {
					player.getWorld().dropItemNaturally(player.getLocation(), stack);
					err = "Mixed items in the seed bag??";
				} else if (STBUtil.getCropType(stack.getType()) == null) {
					player.getWorld().dropItemNaturally(player.getLocation(), stack);
					err = "Non-seed items in the seed bag??";
				} else {
					seedType = stack.getType();
					count += stack.getAmount();
				}
			}
		}
		if (err != null) {
			MiscUtil.errorMessage((Player) player, err);
		}
		setSeedAmount(count);
		setSeedType(seedType);
		player.setItemInHand(toItemStack());
	}

	private void populateSeedBag(InventoryGUI gui) {
		Inventory inv = gui.getInventory();
		if (getSeedType() != null && getSeedAmount() > 0) {
			int nFullStacks = getSeedAmount() / getSeedType().getMaxStackSize();
			int remainder = getSeedAmount() % getSeedType().getMaxStackSize();
			for (int i = 0; i < nFullStacks && i < inv.getSize(); i++) {
				inv.setItem(i, new ItemStack(getSeedType(), getSeedType().getMaxStackSize()));
			}
			if (remainder > 0 && nFullStacks < inv.getSize()) {
				inv.setItem(nFullStacks, new ItemStack(getSeedType(), remainder));
			}
		}
	}

	private void plantSeeds(Player player, Block b) {
		if (getSeedType() == null || getSeedAmount() == 0) {
			return;
		}

		int amountLeft = getSeedAmount();
		for (Block b1 : STBUtil.getSurroundingBlocks(b)) {
			Block above = b1.getRelative(BlockFace.UP);
			if (b1.getType() == Material.SOIL && above.isEmpty()) {
				// candidate for sowing
				above.setType(STBUtil.getCropType(getSeedType()));
				above.setData((byte)0);
				amountLeft--;
				if (amountLeft == 0) {
					break;
				}
			}
		}
		setSeedAmount(amountLeft);
		player.setItemInHand(toItemStack());
		player.updateInventory();
	}

	public void harvestLayer(Player player, Block b) {
		for (Block b1 : STBUtil.getSurroundingBlocks(b)) {
			System.out.println("harvest layer: check " + b1);
			if (STBUtil.isPlant(b1.getType()) || b1.getType() == Material.LEAVES || b1.getType() == Material.LEAVES_2) {
				System.out.println("break the block!");
				b1.getWorld().playEffect(b1.getLocation(), Effect.STEP_SOUND, b1.getType());
				b1.breakNaturally();
			}
			if (player.isSneaking()) {
				break;
			}
		}
	}

	private void tillSoil(Player player, Block b) {
		ItemStack stack = player.getItemInHand();
		short count = 0;
		for (Block b1 : STBUtil.getSurroundingBlocks(b)) {
			Block above = b1.getRelative(BlockFace.UP);
			if ((b1.getType() == Material.DIRT || b1.getType() == Material.GRASS) && !above.getType().isSolid() && !above.isLiquid()) {
				b1.setType(Material.SOIL);
				count++;
				if (!above.isEmpty()) {
					above.breakNaturally();
				}
				if (stack.getDurability() + count >= stack.getType().getMaxDurability()) {
					break;
				}
			}
			if (player.isSneaking()) {
				break;
			}
		}
		damageHeldItem(player, count);
	}

	public void damageHeldItem(Player player, short amount) {
		ItemStack stack = player.getItemInHand();
		stack.setDurability((short) (stack.getDurability() + amount));
		if (stack.getDurability() >= stack.getType().getMaxDurability()) {
			player.setItemInHand(null);
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
		} else {
			player.setItemInHand(stack);
		}
	}
}
