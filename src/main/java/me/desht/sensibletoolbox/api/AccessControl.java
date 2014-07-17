package me.desht.sensibletoolbox.api;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
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
    /**
     * All players may access this block.
     */
    PUBLIC,
    /**
     * Only the owner may access this block.
     */
    PRIVATE,
}
