package me.desht.sensibletoolbox.api;

import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

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
		MACHINE(Material.MAGMA_CREAM, "Charge Machine"),
		CELL(Material.SLIME_BALL, "Charge Energy Cell");
		private final Material material;
		private final String label;

		private ChargeDirection(Material material, String label) {
			this.label = label;
			this.material = material;
		}

		public ItemStack getTexture() {
			ItemStack res = new ItemStack(material);
			ItemMeta meta = res.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.UNDERLINE + label);
			res.setItemMeta(meta);
			return res;
		}
	}
}
