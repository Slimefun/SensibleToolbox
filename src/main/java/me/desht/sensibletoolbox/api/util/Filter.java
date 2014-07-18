package me.desht.sensibletoolbox.api.util;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Filter {
//    private static final String LIST_ITEM = ChatColor.LIGHT_PURPLE + "\u2022 " + ChatColor.AQUA;
    private final List<ItemStack> filteredItems = new ArrayList<ItemStack>();
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
     * @param whiteList true if items in the list should be whitelisted; false otherwise
     * @param itemStacks a list of item stacks
     * @param filterType whether to filter by material, material & data, or by material/data/item-meta
     * @return a new Filter object
     */
    public static Filter fromItemList(boolean whiteList, List<ItemStack> itemStacks, FilterType filterType) {
        Filter f = new Filter();
        f.setWhiteList(whiteList);
        f.setFilterType(filterType);
        for (ItemStack s : itemStacks) {
            f.addItem(s);
        }
        return f;
    }

//    public String[] formatFilterLore() {
//        String[] lore = new String[(size() + 1) / 2 + 2];
//        String what = isWhiteList() ? "white-listed" : "black-listed";
//        String s = size() == 1 ? "" : "s";
//        lore[0] = ChatColor.GOLD + getFilterType().getLabel();
//        lore[1] = ChatColor.GOLD.toString() + size() + " item" + s + " " + what;
//        int i = 2;
//        for (ItemStack stack : listFiltered()) {
//            int n = i / 2 + 1;
//            String name = ItemNames.lookup(stack);
//            lore[n] = lore[n] == null ? LIST_ITEM + name : lore[n] + " " + LIST_ITEM + name;
//            i++;
//        }
//        return lore;
//    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Filter clone() throws CloneNotSupportedException {
        return Filter.fromItemList(whiteList, new ArrayList<ItemStack>(filteredItems), filterType);
    }

    /**
     * Add an item to this filter.
     *
     * @param stack the item to add
     */
    public void addItem(ItemStack stack) {
        filteredItems.add(stack);
    }

    /**
     * Check if this filter, with its current items/whitelisting/filter-type,
     * should allow the given item to pass.
     *
     * @param stack the item to check
     * @return true if the filter should pass the item; false otherwise
     */
    public boolean shouldPass(ItemStack stack) {
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
            case BLOCK_DATA:
                for (ItemStack f : filteredItems) {
                    if (f.getType() == stack.getType() && f.getDurability() == stack.getDurability()) {
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
        }
        return !whiteList;
    }

    /**
     * Get a list of the items filtered by this filter.
     *
     * @return a list of item stacks
     */
    public List<ItemStack> listFiltered() {
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
     * Check if the filter whitelists or blacklists items.  A whitelist filter
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
     * @param whiteList true if this filter should whitelist; false if it
     *                  should blacklist
     */
    public void setWhiteList(boolean whiteList) {
        this.whiteList = whiteList;
    }

    /**
     * Get the filtering type of this filter.
     *
     * @return the filtering type
     */
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * Change the filtering type of this filter.
     *
     * @param filterType the filtering type
     */
    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    /**
     * Represents different levels of filtering precision.
     */
    public enum FilterType {
        /**
         * Match only the item's material.  E.g. don't care about wool colour,
         * stone texture, coal vs. charcoal etc.
         */
        MATERIAL("Filter by Material"),
        /**
         * Match the item's material and its data byte (sometimes referred to
         * as metadata or damage values).  E.g. green wool is distinguished
         * from white wool, and a fully repaired diamond sword is
         * distinguished from a damaged diamond sword.
         */
        BLOCK_DATA("Filter by Material/Block Meta"),
        /**
         * Match the item's material, data byte and item meta (sometimes
         * referred to as NBT data).  E.g. a diamond sword with Looting I is
         * distinguished from a diamond sword with Looting II, even if their
         * damage values are identical.
         */
        ITEM_META("Filter by Material/Block Meta/Item Meta");

        private final String label;

        FilterType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
