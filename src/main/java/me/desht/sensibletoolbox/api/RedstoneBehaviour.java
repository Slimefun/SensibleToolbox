package me.desht.sensibletoolbox.api;

import com.google.common.collect.Lists;
import me.desht.sensibletoolbox.api.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.List;

/**
 * Represents how a STB block reacts to the presence or absence of a redstone signal.
 */
public enum RedstoneBehaviour {
    IGNORE(Material.SULPHUR, ChatColor.GRAY, "Operate regardless of", "redstone signal level"),
    HIGH(Material.REDSTONE, ChatColor.RED, "Require a redstone", "signal to operate"),
    LOW(Material.GLOWSTONE_DUST, ChatColor.YELLOW, "Require no redstone", "signal to operate"),
    PULSED(STBUtil.makeColouredMaterial(Material.INK_SACK, DyeColor.BLUE), ChatColor.DARK_AQUA, "Operate once per", "redstone pulse");

    private final MaterialData material;
    private final List<String> lore = Lists.newArrayList();
    private final ChatColor colour;

    RedstoneBehaviour(MaterialData mat, ChatColor col, String... lore) {
        this.material = mat;
        this.colour = col;
        for (String l : lore) {
            this.lore.add(ChatColor.GRAY + l);
        }
    }

    RedstoneBehaviour(Material mat, ChatColor col, String... lore) {
        this(new MaterialData(mat), col, lore);
    }

    public ItemStack getTexture() {
        ItemStack res = material.toItemStack();
        ItemMeta meta = res.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.UNDERLINE + "Redstone Mode:" + colour + " " + this.toString());
        meta.setLore(lore);
        res.setItemMeta(meta);
        return res;
    }
}
