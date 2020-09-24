package io.github.thebusybiscuit.sensibletoolbox.api.util;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import me.desht.dhutils.Debugger;

/**
 * Utility methods to interact with vanilla inventories.
 */
public class VanillaInventoryUtils {

    public static boolean isVanillaInventory(Block b) {
        return getVanillaInventory(b).isPresent();
    }

    /**
     * Get the vanilla inventory for the given block.
     *
     * @param target
     *            the block containing the target inventory
     * @return the block's inventory, or null if the block does not have one
     */
    public static Optional<Inventory> getVanillaInventory(Block target) {
        BlockState state = target.getState();

        if (state instanceof InventoryHolder) return Optional.of(((InventoryHolder) state).getInventory());
        else return Optional.empty();
    }

    /**
     * Attempt to insert items from the given buffer into the given block,
     * which should be a vanilla inventory holder. Items successfully
     * inserted will be removed from the buffer stack.
     *
     * @param target
     *            the block to insert into
     * @param source
     *            the item stack to take items from
     * @param amount
     *            the number of items from the buffer to insert
     * @param side
     *            the side on which insertion is occurring
     *            (some blocks care about this, e.g. furnace)
     * @param inserterId
     *            UUID of the player doing the insertion
     *            (may be null or the UUID of an offline player)
     * @return the number of items actually inserted
     */
    public static int vanillaInsertion(Block target, ItemStack source, int amount, BlockFace side, boolean sorting, UUID inserterId) {
        if (source == null || source.getAmount() == 0) {
            return 0;
        }

        if (!SensibleToolbox.getProtectionManager().hasPermission(Bukkit.getOfflinePlayer(inserterId), target, ProtectableAction.ACCESS_INVENTORIES)) {
            return 0;
        }

        Optional<Inventory> targetInv = getVanillaInventory(target);

        if (!targetInv.isPresent()) return 0;
        else return vanillaInsertion(targetInv.get(), source, amount, side, sorting);
    }

    /**
     * Attempt to insert items from the given buffer into the given inventory.
     * Items successfully inserted will be removed from the buffer stack.
     *
     * @param targetInv
     *            the inventory to insert into
     * @param source
     *            the item stack to take items from
     * @param amount
     *            the number of items from the buffer to insert
     * @param side
     *            the side on which insertion is occurring (some blocks care about this, e.g. furnace)
     * @return the number of items actually inserted
     */
    public static int vanillaInsertion(Inventory targetInv, ItemStack source, int amount, BlockFace side, boolean sorting) {
        if (targetInv != null) {
            if (sorting && !sortingOK(source, targetInv)) {
                return 0;
            }

            ItemStack stack = source.clone();
            stack.setAmount(Math.min(amount, stack.getAmount()));
            Debugger.getInstance().debug(2, "inserting " + stack + " into " + targetInv.getHolder());
            HashMap<Integer, ItemStack> excess;

            switch (targetInv.getType()) {
            case FURNACE:
                if (side == BlockFace.DOWN) {
                    // no insertion from below
                    return 0;
                }
                excess = addToFurnace((FurnaceInventory) targetInv, stack, side);
                break;
            case BREWING:
                if (side == BlockFace.DOWN) {
                    // no insertion from below
                    return 0;
                }
                excess = addToBrewingStand((BrewerInventory) targetInv, stack, side);
                break;
            default:
                excess = targetInv.addItem(stack);
                break;
            }

            if (!excess.isEmpty()) {
                for (ItemStack s : excess.values()) {
                    if (s.isSimilar(source)) {
                        source.setAmount((source.getAmount() - stack.getAmount()) + s.getAmount());
                        return stack.getAmount() - s.getAmount();
                    }
                }
                return stack.getAmount(); // shouldn't get here!
            }
            else {
                source.setAmount(source.getAmount() - stack.getAmount());
                return stack.getAmount();
            }
        }
        return 0;
    }

    /**
     * Attempt to pull items from an inventory into a receiving buffer.
     *
     * @param target
     *            the block containing the target inventory
     * @param amount
     *            the desired number of items
     * @param buffer
     *            an item stack into which to insert
     *            the transferred items
     * @param filter
     *            a filter to whitelist/blacklist items
     * @param pullerId
     *            UUID of the player doing the pulling
     *            (may be null or the UUID of an offline player)
     * @return the items pulled, or null if nothing was pulled
     */
    public static ItemStack pullFromInventory(Block target, int amount, ItemStack buffer, Filter filter, UUID pullerId) {
        if (!SensibleToolbox.getProtectionManager().hasPermission(Bukkit.getOfflinePlayer(pullerId), target, ProtectableAction.ACCESS_INVENTORIES)) {
            return null;
        }

        Optional<Inventory> targetInv = getVanillaInventory(target);

        if (!targetInv.isPresent()) return null;
        else return pullFromInventory(targetInv.get(), amount, buffer, filter);
    }

    /**
     * Attempt to pull items from an inventory into a receiving buffer.
     *
     * @param targetInv
     *            the target inventory
     * @param amount
     *            the desired number of items
     * @param buffer
     *            an item stack into which to insert
     *            the transferred items
     * @param filter
     *            a filter to whitelist/blacklist items
     * @return the items pulled, or null if nothing was pulled
     */
    public static ItemStack pullFromInventory(Inventory targetInv, int amount, ItemStack buffer, Filter filter) {
        if (targetInv == null) {
            return null;
        }
        IntRange range = getExtractionSlots(targetInv);
        for (int slot = range.getMinimumInteger(); slot <= range.getMaximumInteger(); slot++) {
            ItemStack stack = targetInv.getItem(slot);

            if (stack != null) {
                if ((filter == null || filter.shouldPass(stack)) && (buffer == null || stack.isSimilar(buffer))) {
                    Debugger.getInstance().debug(2, "pulling " + stack + " from " + targetInv.getHolder());
                    int toTake = Math.min(amount, stack.getAmount());

                    if (buffer != null) {
                        toTake = Math.min(toTake, buffer.getType().getMaxStackSize() - buffer.getAmount());
                    }

                    if (toTake > 0) {
                        if (buffer == null) {
                            buffer = stack.clone();
                            buffer.setAmount(toTake);
                        }
                        else {
                            buffer.setAmount(buffer.getAmount() + toTake);
                        }

                        stack.setAmount(stack.getAmount() - toTake);
                        targetInv.setItem(slot, stack.getAmount() > 0 ? stack : null);
                        return buffer;
                    }
                }
            }
        }

        return null;
    }

    private static IntRange getExtractionSlots(Inventory inv) {
        switch (inv.getType()) {
        case FURNACE:
            return new IntRange(2);
        case BREWING:
            return new IntRange(0, 2);
        default:
            return new IntRange(0, inv.getSize() - 1);
        }
    }

    private static HashMap<Integer, ItemStack> addToBrewingStand(BrewerInventory targetInv, ItemStack stack, BlockFace side) {
        HashMap<Integer, ItemStack> res = new HashMap<>();
        ItemStack excess = null;

        if (side == BlockFace.UP) {
            // ingredient slot
            if (!STBUtil.isPotionIngredient(stack.getType())) {
                excess = stack;
            }
            else {
                excess = putStack(targetInv, 3, stack);
            }
        }
        else {
            // water/potion slots
            if (stack.getType() != Material.GLASS_BOTTLE && stack.getType() != Material.POTION) {
                excess = stack;
            }
            else {
                for (int slot = 0; slot <= 2; slot++) {
                    excess = putStack(targetInv, slot, stack);

                    if (excess == null) {
                        break; // all fitted
                    }
                    else {
                        // some or none fitted, continue with other slots
                        stack.setAmount(excess.getAmount());
                    }
                }
            }
        }

        if (excess != null) {
            res.put(0, excess);
        }
        return res;
    }

    private static HashMap<Integer, ItemStack> addToFurnace(FurnaceInventory targetInv, ItemStack stack, BlockFace side) {
        HashMap<Integer, ItemStack> res = new HashMap<>();
        int slot;

        switch (side) {
        case UP:
            slot = 0;
            break; // smelting slot
        default:
            slot = 1;
            break; // fuel slot
        }

        ItemStack excess = putStack(targetInv, slot, stack);

        if (excess != null) {
            res.put(slot, excess);
        }

        return res;
    }

    /**
     * Attempt to put the given item stack in the given slot. Some or all items
     * may not fit if there's already something in the slot.
     *
     * @param inv
     *            the inventory
     * @param slot
     *            the slot to insert into
     * @param stack
     *            the items to insert
     * @return the items left over, or null if nothing was left over (all inserted)
     */
    private static ItemStack putStack(Inventory inv, int slot, ItemStack stack) {
        ItemStack current = inv.getItem(slot);

        if (current == null) {
            inv.setItem(slot, stack);
            return null;
        }
        else if (current.isSimilar(stack)) {
            int toAdd = Math.min(stack.getAmount(), current.getType().getMaxStackSize() - current.getAmount());
            current.setAmount(current.getAmount() + toAdd);
            inv.setItem(slot, current);
            if (toAdd < stack.getAmount()) {
                ItemStack leftover = stack.clone();
                leftover.setAmount(stack.getAmount() - toAdd);
                return leftover;
            }
            else {
                return null;
            }
        }
        else {
            return stack;
        }
    }

    private static boolean sortingOK(ItemStack candidate, Inventory inv) {
        boolean isEmpty = true;

        for (ItemStack stack : inv) {
            if (candidate.isSimilar(stack)) {
                return true;
            }
            else if (stack != null) {
                isEmpty = false;
            }
        }

        return isEmpty;
    }
}
