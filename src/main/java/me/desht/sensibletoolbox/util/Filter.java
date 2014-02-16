package me.desht.sensibletoolbox.util;

import me.desht.dhutils.ItemNames;
import me.desht.sensibletoolbox.api.FilterType;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Filter {
	private static final String LIST_ITEM = ChatColor.LIGHT_PURPLE + "\u2022 " + ChatColor.AQUA;
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

	public static Filter fromItemList(boolean whiteList, List<ItemStack> itemStacks, FilterType filterType) {
		Filter f = new Filter();
		f.setWhiteList(whiteList);
		f.setFilterType(filterType);
		for (ItemStack s : itemStacks) {
			f.addItem(s);
		}
		return f;
	}

	public String[] formatFilterLore() {
		String[] lore = new String[(size() + 1) / 2 + 2];
		String what = isWhiteList() ? "white-listed" : "black-listed";
		String s = size() == 1 ? "" : "s";
		lore[0] = ChatColor.GOLD + getFilterType().getLabel();
		lore[1] = ChatColor.GOLD.toString() + size() + " item" + s + " " + what;
		int i = 2;
		for (ItemStack stack : listFiltered()) {
			int n = i / 2 + 1;
			String name = ItemNames.lookup(stack);
			lore[n] = lore[n] == null ? LIST_ITEM + name : lore[n] + " " + LIST_ITEM + name;
			i++;
		}
		return lore;
	}

	@SuppressWarnings("CloneDoesntCallSuperClone")
	@Override
	public Filter clone() {
		return Filter.fromItemList(whiteList, new ArrayList<ItemStack>(filteredItems), filterType);
	}

	public void addItem(ItemStack stack) {
		filteredItems.add(stack);
	}

	public boolean shouldPass(ItemStack stack) {
		if (!whiteList && filteredItems.isEmpty()) {
			return true;
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

	public List<ItemStack> listFiltered() {
		return filteredItems;
	}

	public int size() {
		return filteredItems.size();
	}

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

	public boolean isWhiteList() {
		return whiteList;
	}

	public void setWhiteList(boolean whiteList) {
		this.whiteList = whiteList;
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}
}
