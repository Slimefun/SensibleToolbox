package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.core.gui.STBInventoryGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

public class GUIUtil {
    public static InventoryGUI getOpenGUI(Player player) {
        return STBInventoryGUI.getOpenGUI(player);
    }

    public static ItemStack makeTexture(MaterialData material, String title, String... lore) {
        ItemStack res = material.toItemStack();
        ItemMeta meta = res.getItemMeta();
        meta.setDisplayName(title);
        if (lore.length > 0) {
            meta.setLore(makeLore(lore));
        }
        res.setItemMeta(meta);
        return res;
    }

    public static void setDisplayName(ItemStack stack, String disp) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(disp);
        stack.setItemMeta(meta);
    }

    public static List<String> makeLore(String... lore) {
        List<String> res = new ArrayList<String>();
        for (String s : lore) {
            res.add(ChatColor.GRAY + s);
        }
        return res;
    }

    public static InventoryGUI createGUI(InventoryGUIListener listener, int size, String title) {
        return new STBInventoryGUI(listener, size, title);
    }

    public static InventoryGUI createGUI(Player player, InventoryGUIListener listener, int size, String title) {
        return new STBInventoryGUI(player, listener, size, title);
    }
}
