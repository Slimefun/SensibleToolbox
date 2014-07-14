package me.desht.sensibletoolbox.api;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public interface STBMachine extends ChargeableBlock, STBInventoryHolder {
    /**
     * Check if the machine is jammed; if it has work to do, but no space in its output slot(s).
     *
     * @return true if the machine is jammed
     */
    public boolean isJammed();

    /**
     * Check if the given item would be accepted as input to this machine.  By default, every item type
     * is accepted, but this can be overridden in subclasses.
     *
     * @param item the item to check
     * @return true if the item is accepted, false otherwise
     */
    public boolean acceptsItemType(ItemStack item);

    /**
     * Check if the given slot accepts items as input.
     *
     * @param slot the slot to check
     * @return true if this is an input slot, false otherwise
     */
    public boolean isInputSlot(int slot);

    /**
     * Check if the given slot can be used to extract items.
     *
     * @param slot the slot to check
     * @return true if this is an output slot, false otherwise
     */
    public boolean isOutputSlot(int slot);

    /**
     * Check if the given slot can be used to install upgrades.
     *
     * @param slot the slot to check
     * @return true if this is an upgrade slot, false otherwise
     */
    public boolean isUpgradeSlot(int slot);

    /**
     * Get the charge direction for any energy cell that may be installed in this machine.
     *
     * @return the current charge direction
     */
    public ChargeDirection getChargeDirection();

    /**
     * Set the charge direction for any energy cell that may be installed in this machine.
     *
     * @param chargeDirection the new charge direction
     */
    public void setChargeDirection(ChargeDirection chargeDirection);

    /**
     * Get the slot number in the machine's GUI where a charge meter may be shown.
     *
     * @return a slot number
     */
    public int getChargeMeterSlot();

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
}
