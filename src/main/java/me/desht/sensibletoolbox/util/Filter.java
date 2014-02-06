package me.desht.sensibletoolbox.util;

import me.desht.sensibletoolbox.items.filters.AbstractItemFilter;
import me.desht.sensibletoolbox.items.filters.ItemFilter;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Filter {
	private final Set<String> filtered = new HashSet<String>();
	private final boolean whiteList;

	/**
	 * Default filters which blocks nothing.
	 */
	public Filter() {
		whiteList = false;
	}

	public Filter(AbstractItemFilter itemFilter) {
		this.whiteList = itemFilter instanceof ItemFilter;
		for (String s : itemFilter.listFiltered()) {
			filtered.add(s);
		}
	}

	public Filter(boolean whiteList, List<String> filter) {
		this.whiteList = whiteList;
		for (String s : filter) {
			filtered.add(s);
		}
	}

	@Override
	public Filter clone() {
		return new Filter(whiteList, new ArrayList<String>(filtered));
	}

	public void addItem(ItemStack stack) {
		filtered.add(makeKey(stack));
	}

	private String makeKey(ItemStack stack) {
		return stack.getDurability() > 0 ? stack.getType() + ":" + stack.getDurability() : stack.getType().toString();
	}

	public boolean shouldPass(ItemStack stack) {
		if (!whiteList && filtered.isEmpty()) {
			return true;
		}
		String k = makeKey(stack);
		return whiteList ? filtered.contains(k) : !filtered.contains(k);
	}

	public List<String> listFiltered() {
		return new ArrayList<String>(filtered);
	}

	public int size() {
		return filtered.size();
	}

	public void clear() {
		filtered.clear();
	}

	@Override
	public String toString() {
		String res = whiteList ? "whitelist" : "blacklist";
		res += Arrays.toString(filtered.toArray(new String[filtered.size()]));
		return res;
	}

	public boolean isWhiteList() {
		return whiteList;
	}
}
