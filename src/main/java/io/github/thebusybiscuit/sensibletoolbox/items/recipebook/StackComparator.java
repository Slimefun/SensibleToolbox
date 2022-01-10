package io.github.thebusybiscuit.sensibletoolbox.items.recipebook;

import java.util.Comparator;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import io.github.bakedlibs.dough.items.ItemUtils;

class StackComparator implements Comparator<ItemStack> {

    @Override
    public int compare(ItemStack item1, ItemStack item2) {
        String name1 = ChatColor.stripColor(ItemUtils.getItemName(item1) == null ? "" : ItemUtils.getItemName(item1));
        String name2 = ChatColor.stripColor(ItemUtils.getItemName(item2) == null ? "" : ItemUtils.getItemName(item2));
        return name1.compareTo(name2);
    }

}
