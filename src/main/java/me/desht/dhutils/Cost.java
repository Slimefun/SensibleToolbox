package me.desht.dhutils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

@Deprecated
/**
 * @deprecated Use {@link me.desht.dhutils.cost.Cost}
 */
public class Cost {

	private static Economy economy = null;

	private final CostType type;
	private final int id;
	private final Short data;
	private final double quantity;

	/**
	 * Construct a new Cost object, charging 1 of the given item ID
	 *
	 * @param id	ID of the item to charge, 0 for economy credits
	 */
	public Cost(int id) {
		this(id, null, 1);
	}

	/**
	 * Construct a new Cost object.
	 *
	 * @param id	ID of the item to charge, 0 for economy credits
	 * @param data	Data value of the item, may be null
	 * @param quantity	Quantity to charge, may be negative
	 */
	public Cost(int id, Short data, double quantity) {
		this(id == 0 ? CostType.MONEY : CostType.ITEM, id, data, quantity);
	}

	/**
	 * Construct a new Cost object of the given type.
	 *
	 * @param type	Type of cost to apply
	 * @param id	ID of the item to charge, 0 for economy credits
	 * @param data	Data value of the item, may be null
	 * @param quantity	Quantity to charge, may be negative
	 */
	public Cost(CostType type, int id, Short data, double quantity) {
		this.type = type;
		this.id = id;
		this.data = data;
		this.quantity = quantity;
	}

	/**
	 * Construct a new Cost object from the given string specification.
	 *
	 * @param costSpec	The specification, in the format <i>id[:data],quantity</i>
	 * @throws IllegalArgumentException if the specification contains an error
	 */
	public Cost(String costSpec) {
		//System.out.println("cost = " + costSpec);
		String[] itemAndQuantity = costSpec.split(",");
		if (itemAndQuantity.length < 2) {
			quantity = 1.0;
		} else {
			quantity = Double.parseDouble(itemAndQuantity[1]);
		}
		boolean durability = itemAndQuantity.length > 2 && (itemAndQuantity[2].startsWith("d") || itemAndQuantity[2].startsWith("D"));

		String[] idAndData = itemAndQuantity[0].split(":");
		if (idAndData.length < 1 || idAndData.length > 2)
			throw new IllegalArgumentException("cost: item format must be <id[:data]>");

		String itemType = idAndData[0].toUpperCase();
		if (itemType.equals("E")) {
			id = 0;
			type = CostType.MONEY;
		} else if (itemType.equals("X")) {
			id = 0;
			type = CostType.EXPERIENCE;
		} else if (itemType.equals("F")) {
			id = 0;
			type = CostType.FOOD;
		} else if (itemType.equals("H")) {
			id = 0;
			type = CostType.HEALTH;
		} else if (itemType.matches("[0-9]+")) {
			id = Integer.parseInt(itemType);
			if (id == 0) {
				type = CostType.MONEY;
			} else if (durability) {
				type = CostType.DURABILITY;
			} else {
				type = CostType.ITEM;
			}
		} else if (itemType.length() > 1) {
			Material mat = Material.matchMaterial(itemType);
			if (mat != null) {
				// a material name
				type = durability ? CostType.DURABILITY : CostType.ITEM;
				id = mat.getId();
			} else {
				// maybe it's a potion name?
				PotionEffectType pt = PotionEffectType.getByName(itemType);
				if (pt == null) {
					throw new IllegalArgumentException("cost: unknown material or potion type '" + itemType + "'");
				}
				type = CostType.POTION_EFFECT;
				id = pt.getId();
			}
		} else {
			throw new IllegalArgumentException("cost: unknown item type '" + itemType + "'");
		}

		if (idAndData.length == 2) {
			data = Short.parseShort(idAndData[1]);
		} else {
			data = null;
		}
	}

	public int getId() {
		return id;
	}

	public Short getData() {
		return data;
	}

	public double getQuantity() {
		return quantity;
	}

	public CostType getType() {
		return type;
	}

	@Override
	public String toString() {
		String dataStr = data == null ? "" : ":" + data;
		return type.toString() + "," + id + dataStr + "," + quantity;
	}

	public String getDescription() {
		switch (type) {
		case MONEY:
			return economy == null ? "$" + quantity : economy.format(quantity);
		case DURABILITY:
			return (int)quantity + " durability from " + Material.getMaterial(id);
		case EXPERIENCE:
			return (int)quantity + " XP";
		case FOOD:
			return (int)quantity + " hunger";
		case HEALTH:
			return (int)quantity + " health";
		case ITEM:
			String desc = (int)quantity + " " + Material.getMaterial(id).toString().toLowerCase().replace("_", " ");
			if (data != null) desc += ":" + data;
			return desc;
		default:
			return "???";
		}
	}

	public boolean isApplicable(CommandSender sender) {
		if (getType() == CostType.DURABILITY) {
			Material mat = Material.getMaterial(getId());
			short maxDurability = mat.getMaxDurability();
			if (maxDurability == 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isAffordable(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;

		if (getQuantity() <= 0)
			return true;

		HashMap<Integer, ? extends ItemStack> matchingInvSlots;
		switch (getType()) {
		case MONEY:
			return economy == null || !economy.isEnabled() ? false : economy.has(player.getName(), getQuantity());
		case ITEM:
			matchingInvSlots = player.getInventory().all(Material.getMaterial(getId()));
			int remainingCheck = (int) getQuantity();
			for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
				if (getData() == null || (entry.getValue().getData() != null && entry.getValue().getData().getData() == getData())) {
					remainingCheck -= entry.getValue().getAmount();
					if (remainingCheck <= 0)
						break;
				}
			}
			return remainingCheck <= 0;
		case EXPERIENCE:
			ExperienceManager em = new ExperienceManager(player);
			return em.getCurrentExp() >= getQuantity();
		case FOOD:
			return player.getFoodLevel() > getQuantity();
		case HEALTH:
			return player.getHealth() > getQuantity();
		case DURABILITY:
			Material mat = Material.getMaterial(getId());
			short maxDurability = mat.getMaxDurability();
			matchingInvSlots = player.getInventory().all(mat);
			int total = 0;
			for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
				total += maxDurability - entry.getValue().getDurability();
			}
			return total >= getQuantity();
		case POTION_EFFECT:
			return true;
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	public void chargePlayer(CommandSender sender) {
		if (!(sender instanceof Player)) {
			return;
		}
		Player player = (Player) sender;

		if (getQuantity() == 0.0 && getType() != CostType.POTION_EFFECT)
			return;
		switch (getType()) {
		case MONEY:
			if (economy != null && economy.isEnabled()) {
				EconomyResponse resp;
				if (getQuantity() < 0.0) {
					resp = economy.depositPlayer(player.getName(), -getQuantity());
				} else {
					resp = economy.withdrawPlayer(player.getName(), getQuantity());
				}
				if (!resp.transactionSuccess()) {
					throw new DHUtilsException("Economy problem: " + resp.errorMessage);
				}
			} else {
				throw new DHUtilsException("Economy problem: attempt to make economy charge, but no economy plugin available");
			}
			break;
		case ITEM:
			if (getQuantity() > 0)
				chargeItems(player);
			else
				grantItems(player);
			player.updateInventory();
			break;
		case EXPERIENCE:
			ExperienceManager em = new ExperienceManager(player);
			em.changeExp((int) -getQuantity());
			break;
		case FOOD:
			player.setFoodLevel(getNewQuantity(player.getFoodLevel(), getQuantity(), 1, 20));
			break;
		case HEALTH:
			// note: player.getHealth() returns a double in CB 1.6+ but we still work with ints for now
			player.setHealth(getNewQuantity((int) player.getHealth(), getQuantity(), 1, 20));
			break;
		case DURABILITY:
			chargeDurability(player);
			player.updateInventory();
			break;
		case POTION_EFFECT:
			PotionEffectType pt = PotionEffectType.getById(id);
			if (player.hasPotionEffect(pt)) {
				player.removePotionEffect(pt);
			}
			if (getQuantity() > 0) {
				int amp = getData() == null ? 0 : getData() - 1;
				player.addPotionEffect(new PotionEffect(pt, (int)getQuantity() * 20, amp));
			}
			break;
		}
	}

	/**
	 * Give items to a player.
	 *
	 * @param player
	 */
	public void grantItems(Player player) {
		if (player == null) {
			return;
		}

		int maxStackSize = player.getInventory().getMaxStackSize();
		int quantity = (int) -getQuantity();
		int dropped = 0;

		while (quantity > maxStackSize) {
			dropped += addItems(player, maxStackSize);
			quantity -= maxStackSize;
		}
		dropped += addItems(player, quantity);

		if (dropped > 0) {
			MiscUtil.statusMessage(player, "&6Your inventory is full.  Some items dropped.");
		}
	}

	/**
	 * Take items from a player's inventory.  Doesn't check to see if there is enough -
	 * use playerCanAfford() for that.
	 *
	 * @param player
	 */
	public void chargeItems(Player player) {
		if (player == null) {
			return;
		}

		HashMap<Integer, ? extends ItemStack> matchingInvSlots = player.getInventory().all(Material.getMaterial(getId()));

		int remainingCheck = (int) getQuantity();
		for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
			if (getData() == null || (entry.getValue().getData() != null && entry.getValue().getData().getData() == getData())) {
				remainingCheck -= entry.getValue().getAmount();
				if (remainingCheck < 0) {
					entry.getValue().setAmount(-remainingCheck);
					break;
				} else if (remainingCheck == 0) {
					player.getInventory().removeItem(entry.getValue());
					break;
				} else {
					player.getInventory().removeItem(entry.getValue());
				}
			}
		}
	}

	private ItemStack makeStack(int quantity) {
		return data == null ? new ItemStack(getId(), quantity) : new ItemStack(getId(), quantity, getData());
	}

	private int addItems(Player player, int quantity) {
		Map<Integer, ItemStack> toDrop = player.getInventory().addItem(makeStack(quantity));
		if (toDrop.size() == 0) {
			return 0;
		}

		int dropped = 0;
		for (ItemStack is : toDrop.values()) {
			player.getWorld().dropItemNaturally(player.getLocation(), is);
			dropped += is.getAmount();
		}
		return dropped;
	}

	private void chargeDurability(Player player) {
		Material mat = Material.getMaterial(getId());
		short maxDurability = mat.getMaxDurability();

		HashMap<Integer, ? extends ItemStack> matchingInvSlots = player.getInventory().all(mat);

		short total = (short) getQuantity();
		boolean damaging = total > 0;

		for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {

			short currentDurability = entry.getValue().getDurability();
			short newDurability = (short) (currentDurability + total);

			if (newDurability < 0)
				newDurability = 0;

			if (newDurability >= maxDurability) {
				// break the item - reduce inventory count by 1
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
				int newAmount = entry.getValue().getAmount() - 1;
				if (newAmount == 0) {
					player.getInventory().setItem(entry.getKey(), new ItemStack(0));
				} else {
					entry.getValue().setAmount(newAmount);
				}
				newDurability = maxDurability;
			} else {
				entry.getValue().setDurability(newDurability);
			}

			int delta = currentDurability - newDurability;
			total += delta;

			if (damaging) {
				if (total <= 0) break;
			} else {
				if (total >= 0) break;
			}
		}
	}

	/**
	 * Charge a list of costs to the given player.
	 *
	 * @param sender	The player to charge
	 * @param costs		A List of Cost objects
	 */
	public static void chargePlayer(CommandSender sender, List<Cost> costs) {
		for (Cost c : costs) {
			c.chargePlayer(sender);
		}
	}

	/**
	 * Check if the player can afford to pay the costs.
	 *
	 * @param sender
	 * @param costs
	 * @return	True if the costs are affordable, false otherwise
	 */
	public static boolean playerCanAfford(CommandSender sender, List<Cost> costs) {
		for (Cost c : costs) {
			if (!c.isAffordable(sender))
				return false;
		}
		return true;
	}

	/**
	 * Check if the costs are applicable.
	 *
	 * @param sender
	 * @param costs
	 * @return
	 */
	public static boolean isApplicable(CommandSender sender, List<Cost> costs) {
		for (Cost c : costs) {
			if (!c.isApplicable(sender))
				return false;
		}
		return true;
	}

	private static int getNewQuantity(int original, double adjust, int min, int max) {
		int newQuantity = original - (int) adjust;
		if (newQuantity < min) {
			newQuantity = min;
		} else if (newQuantity > max) {
			newQuantity = max;
		}
		return newQuantity;
	}

	public static void setEconomy(Economy econ) {
		economy = econ;
	}

	public enum CostType {
		ITEM, MONEY, EXPERIENCE, FOOD, HEALTH, DURABILITY, POTION_EFFECT,
	}

}