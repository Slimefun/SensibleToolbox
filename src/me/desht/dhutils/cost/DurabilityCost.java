package me.desht.dhutils.cost;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DurabilityCost extends Cost {

	private final Material material;

	protected DurabilityCost(Material material, double quantity) {
		super(quantity);
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	@Override
	public String getDescription() {
		return (int)getQuantity() + " durability from " + material;
	}

	@Override
	public boolean isAffordable(Player player) {
		short maxDurability = material.getMaxDurability();
		HashMap<Integer, ? extends ItemStack> matchingInvSlots = player.getInventory().all(material);
		int total = 0;
		for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
			total += maxDurability - entry.getValue().getDurability();
		}
		return total >= getQuantity();
	}

	@Override
	public void apply(Player player) {
		short maxDurability = material.getMaxDurability();

		HashMap<Integer, ? extends ItemStack> matchingInvSlots = player.getInventory().all(material);

		short total = (short) getQuantity();
		boolean damaging = total > 0;

		for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
			short currentDurability = entry.getValue().getDurability();
			short newDurability = (short) Math.max(0, currentDurability + total);

			if (newDurability >= maxDurability) {
				// break the item - reduce inventory count by 1
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
				int newAmount = entry.getValue().getAmount() - 1;
				if (newAmount == 0) {
					player.getInventory().setItem(entry.getKey(), new ItemStack(Material.AIR));
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
		player.updateInventory();
	}

    @Override
    public boolean isApplicable(Player player) {
        return getMaterial().getMaxDurability() > 0;
    }
}
