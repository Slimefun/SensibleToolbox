package io.github.thebusybiscuit.sensibletoolbox.api.recipes;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a supplementary result for a recipe; a possible "bonus" item,
 * with a defined chance of being produced.
 */
public class SupplementaryResult {

    private final ItemStack result;

    // x out of 1000
    private final int chance;

    /**
     * Create a supplementary result.
     *
     * @param result
     *            the item that may be produced
     * @param chance
     *            the chance, out of 1000, that the item will be produced
     */
    public SupplementaryResult(ItemStack result, int chance) {
        Preconditions.checkArgument(chance > 0 && chance <= 1000, "chance out of range: must be 0 < chance <= 1000");
        this.result = result;
        this.chance = chance;
    }

    /**
     * Get the item that may be produced.
     *
     * @return the item
     */
    @Nonnull
    public ItemStack getResult() {
        return result;
    }

    /**
     * Get the chance that this item will be produced.
     *
     * @return a chance, out of 1000, that the item will be produced
     */
    public int getChance() {
        return chance;
    }
}
