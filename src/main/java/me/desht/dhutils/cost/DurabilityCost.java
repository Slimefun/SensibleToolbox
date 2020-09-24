package me.desht.dhutils.cost;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

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
        return (int) getQuantity() + " durability from " + material;
    }

    @Override
    public boolean isAffordable(Player player) {
        short maxDurability = material.getMaxDurability();
        Map<Integer, ? extends ItemStack> matchingInvSlots = player.getInventory().all(material);
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

        int total = (int) getQuantity();
        boolean damaging = total > 0;

        for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
            ItemMeta meta = entry.getValue().getItemMeta();

            if (meta instanceof Damageable) {
                int currentDamage = ((Damageable) meta).getDamage();
                int newDamage = Math.max(0, currentDamage + total);

                if (newDamage >= maxDurability) {
                    // break the item - reduce inventory count by 1
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    int newAmount = entry.getValue().getAmount() - 1;

                    if (newAmount == 0) {
                        player.getInventory().setItem(entry.getKey(), null);
                    }
                    else {
                        entry.getValue().setAmount(newAmount);
                    }

                    newDamage = maxDurability;
                }
                else {
                    ((Damageable) meta).setDamage(newDamage);
                }

                int delta = currentDamage - newDamage;
                total += delta;

                if (damaging) {
                    if (total <= 0) break;
                }
                else {
                    if (total >= 0) break;
                }

                entry.getValue().setItemMeta(meta);
            }

        }
    }

    @Override
    public boolean isApplicable(Player player) {
        return getMaterial().getMaxDurability() > 0;
    }
}
