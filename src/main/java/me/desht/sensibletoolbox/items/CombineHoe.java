package me.desht.sensibletoolbox.items;

import me.desht.dhutils.ItemNames;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class CombineHoe extends BaseSTBItem {
	private Material seedType;
	private int amount;

	public CombineHoe(Configuration conf) {
		if (conf.contains("seeds") && conf.contains("amount")) {
			setSeedBagContents(Material.getMaterial(conf.getString("seeds")), conf.getInt("amount"));
		}
	}

	protected CombineHoe() {
		seedType = null;
		amount = 0;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("seeds", getSeedType() == null ? "" : getSeedType().toString());
		res.put("amount", getSeedAmount());
		return res;
	}

	public Material getSeedType() {
		return seedType;
	}

	public int getSeedAmount() {
		return amount;
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Right-click dirt/grass: till 3x3 area",
				"Left-click plants: harvest 3x3 area",
				"Right-click soil: sow 3x3 area from seed bag",
				"Right-click air: open seed bag"
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
	public void handleItemInteraction(PlayerInteractEvent event) {
		Block b = event.getClickedBlock();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (b.getType() == Material.SOIL) {
				plantSeeds(event.getPlayer(), b);
				event.setCancelled(true);
			} else if (b.getType() == Material.DIRT || b.getType() == Material.GRASS) {
				tillSoil(event.getPlayer(), b);
				event.setCancelled(true);
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (STBUtil.isPlant(b.getType())) {
				harvestPlants(event.getPlayer(), b);
				event.setCancelled(true);
			}
		} else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			Configuration conf = BaseSTBItem.getItemAttributes(event.getPlayer().getItemInHand());
			setSeedBagContents(Material.getMaterial(conf.getString("seeds")), conf.getInt("amount"));
			Inventory inv = Bukkit.createInventory(event.getPlayer(), 9, getInventoryTitle());
			populateSeedBag(inv);
			event.getPlayer().openInventory(inv);
		}
	}

	private void populateSeedBag(Inventory inv) {
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

	public String getInventoryTitle() {
		return ChatColor.DARK_GREEN + getItemName() + ": Seed Bag";
	}

	private void plantSeeds(Player player, Block b) {
		Configuration conf = BaseSTBItem.getItemAttributes(player.getItemInHand());
		setSeedBagContents(Material.getMaterial(conf.getString("seeds")), conf.getInt("amount"));
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
		setSeedBagContents(getSeedType(), amountLeft);
		player.setItemInHand(toItemStack(1));
		player.updateInventory();
	}

	private void harvestPlants(Player player, Block b) {
		harvestLayer(player, b);
		damageHeldItem(player, (short) 1);
	}

	public void harvestLayer(Player player, Block b) {
		for (Block b1 : STBUtil.getSurroundingBlocks(b)) {
			if (STBUtil.isPlant(b1.getType())) {
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

	public void setSeedBagContents(Material seedType, int count) {
		this.seedType = count > 0 ? seedType : null;
		this.amount = count;
	}
}
