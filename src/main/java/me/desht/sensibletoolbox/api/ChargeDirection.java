package me.desht.sensibletoolbox.api;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Represents the direction of charging selected for this machine.
 */
public enum ChargeDirection {
    MACHINE(Material.MAGMA_CREAM, "Charge Machine", "Energy will transfer from", "an installed energy cell", "to this machine"),
    CELL(Material.SLIME_BALL, "Charge Energy Cell", "Energy will transfer from", "this machine to", "an installed energy cell");
    private final Material material;
    private final String label;
    private final List<String> lore = Lists.newArrayList();

    private ChargeDirection(Material material, String label, String... lore) {
        this.label = label;
        this.material = material;
        for (String l : lore) {
            this.lore.add(ChatColor.GRAY + l);
        }
    }

    public ItemStack getTexture() {
        ItemStack res = new ItemStack(material);
        ItemMeta meta = res.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.UNDERLINE + label);
        meta.setLore(lore);
        res.setItemMeta(meta);
        return res;
    }
}