package io.github.thebusybiscuit.sensibletoolbox.api.filters;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A class which can filter items based on several criteria: whitelist/blacklist,
 * filter by material, by block data or by item metadata.
 * 
 * @author desht
 * @author TheBusyBiscuit
 */
public class Filter implements Cloneable {

    private final List<ItemStack> filteredItems = new ArrayList<>();
    private FilterType filterType;
    private boolean whiteList;

    /**
     * Default filter which blocks nothing.
     */
    public Filter() {
        whiteList = false;
        filterType = FilterType.MATERIAL;
    }

    /**
     * Create a filter from the given list of items.
     *
     * @param whiteList
     *            true if items in the list should be whitelisted; false otherwise
     * @param itemStacks
     *            a list of item stacks
     * @param filterType
     *            whether to filter by material, material & data, or by material/data/item-meta
     * @return a new Filter object
     */
    @Nonnull
    public static Filter fromItemList(boolean whiteList, @Nonnull List<ItemStack> itemStacks, FilterType filterType) {
        Filter f = new Filter();
        f.setWhiteList(whiteList);
        f.setFilterType(filterType);

        for (ItemStack s : itemStacks) {
            f.addItem(s);
        }

        return f;
    }

    // public String[] formatFilterLore() {
    // String[] lore = new String[(size() + 1) / 2 + 2];
    // String what = isWhiteList() ? "white-listed" : "black-listed";
    // String s = size() == 1 ? "" : "s";
    // lore[0] = ChatColor.GOLD + getFilterType().getLabel();
    // lore[1] = ChatColor.GOLD.toString() + size() + " item" + s + " " + what;
    // int i = 2;
    // for (ItemStack stack : listFiltered()) {
    // int n = i / 2 + 1;
    // String name = ItemNames.lookup(stack);
    // lore[n] = lore[n] == null ? LIST_ITEM + name : lore[n] + " " + LIST_ITEM + name;
    // i++;
    // }
    // return lore;
    // }

    @Override
    public Filter clone() {
        return Filter.fromItemList(whiteList, new ArrayList<>(filteredItems), filterType);
    }

    /**
     * Add an item to this filter.
     *
     * @param stack
     *            the item to add
     */
    public void addItem(@Nonnull ItemStack stack) {
        filteredItems.add(stack);
    }

    /**
     * Check if this filter, with its current items/whitelisting/filter-type,
     * should allow the given item to pass.
     *
     * @param stack
     *            the item to check
     * 
     * @return true if the filter should pass the item; false otherwise
     */
    public boolean shouldPass(@Nonnull ItemStack stack) {
        Preconditions.checkArgument(stack != null, "Cannot filter null ItemStacks");

        if (filteredItems.isEmpty()) {
            return !whiteList;
        }

        switch (filterType) {
            case MATERIAL:
                for (ItemStack f : filteredItems) {
                    if (f.getType() == stack.getType()) {
                        return whiteList;
                    }
                }

                return !whiteList;
            case ITEM_META:
                for (ItemStack f : filteredItems) {
                    if (f.isSimilar(stack)) {
                        return whiteList;
                    }
                }

                return !whiteList;
            default:
                return !whiteList;
        }
    }

    /**
     * Get a list of the items filtered by this filter.
     *
     * @return a list of item stacks
     */
    @Nonnull
    public List<ItemStack> getFilterList() {
        return filteredItems;
    }

    /**
     * Get the number of items filtered by this filter.
     *
     * @return the number of items filtered
     */
    public int size() {
        return filteredItems.size();
    }

    /**
     * Remove all items from this filter.
     */
    public void clear() {
        filteredItems.clear();
    }

    @Override
    public String toString() {
        String res = whiteList ? "whitelist " : "blacklist ";
        res += filterType.toString() + " ";
        res += Arrays.toString(filteredItems.toArray(new ItemStack[filteredItems.size()]));
        return res;
    }

    /**
     * Check if the filter whitelists or blacklists items. A whitelist filter
     * allows only the items added to it to pass; a blacklist filter allows
     * any items <em>except</em> the items added to it to pass.
     *
     * @return true if this filter whitelists; false if it blacklists
     */
    public boolean isWhiteList() {
        return whiteList;
    }

    /**
     * Set the whitelist mode of this filter.
     *
     * @param whiteList
     *            true if this filter should whitelist; false if it
     *            should blacklist
     */
    public void setWhiteList(boolean whiteList) {
        this.whiteList = whiteList;
    }

    /**
     * Get the filtering type of this filter.
     *
     * @return the filtering type
     */

    @Nonnull
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * Change the filtering type of this filter.
     *
     * @param filterType
     *            the filtering type
     */
    public void setFilterType(@Nonnull FilterType filterType) {
        Preconditions.checkArgument(filterType != null, "FilterType cannot be null!");
        this.filterType = filterType;
    }
}
