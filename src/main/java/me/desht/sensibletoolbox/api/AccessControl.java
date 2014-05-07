package me.desht.sensibletoolbox.api;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.util.Arrays;
import java.util.UUID;

/**
 * Represents the user-based access control in force for this STB block.
 */
public enum AccessControl {
    PUBLIC(DyeColor.GREEN, "Public Access"),
    PRIVATE(DyeColor.RED, "Owner Only Access");
    private final String label;
    private final DyeColor color;

    AccessControl(DyeColor color, String label) {
        this.color = color;
        this.label = label;
    }

    public ItemStack getTexture(UUID owner) {
        ItemStack res = new Wool(color).toItemStack();
        ItemMeta meta = res.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.UNDERLINE + label);
        String name = SensibleToolboxPlugin.getInstance().getUuidTracker().getPlayer(owner);
        if (name == null) {
            name = owner.toString();
        }
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Owner: " + name));
        res.setItemMeta(meta);
        return res;
    }
}
