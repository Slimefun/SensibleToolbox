package me.desht.dhutils.cost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * This class can be used to apply an {@link ItemStack} cost onto a {@link Player} or {@link Inventory}.
 * 
 * @author desht
 * @author TheBusyBiscuit
 *
 */
public class ItemCost {

    private final List<ItemStack> taken = new ArrayList<>();
    private final ItemStack toMatch;
    private final int amount;
    private final boolean matchMeta;

    public ItemCost(@Nonnull Material mat, int amount) {
        this.amount = amount;
        this.matchMeta = false;
        this.toMatch = new ItemStack(mat, amount);
    }

    public ItemCost(@Nonnull ItemStack stack) {
        this.amount = stack.getAmount();
        this.matchMeta = true;
        this.toMatch = stack.clone();
    }

    public int getAmount() {
        return amount;
    }

    @Nonnull
    public Material getMaterial() {
        return toMatch.getType();
    }

    @Nonnull
    public List<ItemStack> getActualItemsTaken() {
        return taken;
    }

    public String getDescription() {
        return getAmount() + " " + getMaterial().toString().toLowerCase().replace("_", " ");
    }

    public boolean isAffordable(@Nonnull Player player) {
        int remainingCheck = getAmount();
        return getRemaining(remainingCheck, player.getInventory()) <= 0;
    }

    /**
     * Check if this item cost can be met from the player's inventory and zero
     * or more supplementary inventories.
     *
     * @param player
     *            the player to check
     * @param playerFirst
     *            true if the player's inventory should be checked before the extra inventories
     * @param extraInventories
     *            zero or more Inventory objects
     * @return true if the cost can be met; false otherwise
     */
    public boolean isAffordable(@Nonnull Player player, boolean playerFirst, Inventory... extraInventories) {
        List<Inventory> invs = new ArrayList<>(extraInventories.length + 1);

        if (playerFirst) {
            invs.add(player.getInventory());
            invs.addAll(Arrays.asList(extraInventories));
        }
        else {
            invs.addAll(Arrays.asList(extraInventories));
            invs.add(player.getInventory());
        }

        int remainingCheck = getAmount();

        for (Inventory inv : invs) {
            remainingCheck = getRemaining(remainingCheck, inv);

            if (remainingCheck <= 0) {
                return true;
            }
        }

        return false;
    }

    protected int getRemaining(int remainingCheck, @Nonnull Inventory inv) {
        Map<Integer, ? extends ItemStack> matchingInvSlots = inv.all(getMaterial());

        for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
            if (matches(entry.getValue())) {
                remainingCheck -= entry.getValue().getAmount();
                if (remainingCheck <= 0) break;
            }
        }

        return remainingCheck;
    }

    public void apply(@Nonnull Player player) {
        if (getAmount() > 0) {
            chargeItems(player.getInventory());
        }
        else {
            int dropped = addItems(player.getInventory());
            dropExcess(player, dropped);
        }
    }

    /**
     * Apply this cost to the given player plus zero or more supplementary inventories.
     *
     * @param player
     *            the player to give or take items from
     * @param playerFirst
     *            if true, then the player's inventory will be modified first
     * @param extraInventories
     *            zero or more supplementary inventories to give or take items from
     */
    public void apply(@Nonnull Player player, boolean playerFirst, Inventory... extraInventories) {
        Inventory[] invs = new Inventory[extraInventories.length + 1];

        if (playerFirst) {
            invs[0] = player.getInventory();
            System.arraycopy(extraInventories, 0, invs, 1, extraInventories.length);
        }
        else {
            System.arraycopy(extraInventories, 0, invs, 0, extraInventories.length);
            invs[extraInventories.length] = player.getInventory();
        }

        if (getAmount() > 0) {
            chargeItems(invs);
        }
        else {
            int dropped = addItems(invs);
            dropExcess(player, dropped);
        }
    }

    private int addItems(Inventory... inventories) {
        int remaining = -getAmount();

        for (Inventory inv : inventories) {
            remaining = addToOneInventory(inv, remaining);

            if (remaining == 0) {
                break;
            }
        }

        return remaining;
    }

    private int chargeItems(Inventory... inventories) {
        taken.clear();

        int remaining = getAmount();

        for (Inventory inv : inventories) {
            remaining = takeFromOneInventory(inv, remaining);

            if (remaining == 0) {
                break;
            }
        }

        return remaining;
    }

    private int addToOneInventory(@Nonnull Inventory inventory, int quantity) {
        int maxStackSize = inventory.getMaxStackSize();

        while (quantity > maxStackSize) {
            Map<Integer, ItemStack> toDrop = inventory.addItem(new ItemStack(getMaterial(), maxStackSize));

            if (!toDrop.isEmpty()) {
                // this inventory is full; return the number of items that could not be added
                return toDrop.get(0).getAmount() + (quantity - maxStackSize);
            }

            quantity -= maxStackSize;
        }

        Map<Integer, ItemStack> toDrop = inventory.addItem(new ItemStack(getMaterial(), quantity));
        return toDrop.isEmpty() ? 0 : toDrop.get(0).getAmount();
    }

    private int takeFromOneInventory(@Nonnull Inventory inventory, int quantity) {
        Map<Integer, ? extends ItemStack> matchingInvSlots = inventory.all(getMaterial());

        for (Entry<Integer, ? extends ItemStack> entry : matchingInvSlots.entrySet()) {
            if (matches(entry.getValue())) {
                quantity -= entry.getValue().getAmount();
                if (quantity < 0) {
                    entry.getValue().setAmount(-quantity);
                    taken.add(entry.getValue().clone());
                    break;
                }
                else {
                    inventory.removeItem(entry.getValue());
                    taken.add(entry.getValue().clone());
                }

                if (quantity == 0) {
                    break;
                }
            }
        }
        return quantity;
    }

    protected boolean matches(@Nonnull ItemStack stack) {
        if (toMatch.getType() != stack.getType()) {
            return false;
        }
        else if (matchMeta) {
            String d1 = stack.hasItemMeta() ? stack.getItemMeta().getDisplayName() : null;
            String d2 = toMatch.hasItemMeta() ? toMatch.getItemMeta().getDisplayName() : null;

            if ((d1 != null && !d1.equals(d2)) || (d2 != null && !d2.equals(d1))) {
                return false;
            }

            return true;
        }
        else {
            return true;
        }
    }

    private void dropExcess(@Nonnull Player player, int amount) {
        while (amount > 0) {
            ItemStack stack = new ItemStack(getMaterial(), Math.min(amount, getMaterial().getMaxStackSize()));
            player.getWorld().dropItemNaturally(player.getLocation(), stack);
            amount -= getMaterial().getMaxStackSize();
        }
    }
}
