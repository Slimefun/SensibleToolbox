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

public interface STBMachine extends Chargeable, STBInventoryHolder {
	/**
	 * Check if the machine is jammed; if it has work to do, but no space in its output slot(s).
	 *
	 * @return true if the machine is jammed
	 */
	public boolean isJammed();

	/**
	 * Get this machine's current redstone behaviour; how it acts in the presences or absence of
	 * a redstone signal.
	 *
	 * @return the machine's redstone behaviour
	 */
	public RedstoneBehaviour getRedstoneBehaviour();

	public AccessControl getAccessControl();

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

//	/**
//	 * Check what's in the machine's output slot(s), without actually taking anything.
//	 *
//	 * @return an array of ItemStack objects, one stack per output slot
//	 */
//	ItemStack[] peekOutput();

//	/**
//	 * Attempt to pull one or more items from the given output slot of the machine.  The actual ItemStack
//	 * returned may be of a smaller amount than requested, depending on what's in the slot.
//	 *
//	 * @param slot the output slot number
//	 * @param nItems the number of items to try and take
//	 * @return the ItemStack that was pulled
//	 */
//	ItemStack pullFromOutput(int slot, int nItems);

	/**
	 * Represents how the machine reacts to the presence or absence of a redstone signal.
	 */
	public enum RedstoneBehaviour {
		IGNORE(Material.SULPHUR, "Ignore Redstone"),
		HIGH(Material.REDSTONE, "Require Signal"),
		LOW(Material.GLOWSTONE_DUST, "Require No Signal");
		private final Material material;
		private final String label;

		RedstoneBehaviour(Material mat, String label) {
			this.material = mat;
			this.label = label;
		}

		public ItemStack getTexture() {
			ItemStack res = new ItemStack(material);
			ItemMeta meta = res.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + label);
			res.setItemMeta(meta);
			return res;
		}
	}

	/**
	 * Represents the user-based access control in force for this machine.
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

		public ItemStack getTexture() {
			ItemStack res = new Wool(color).toItemStack(1);
			ItemMeta meta = res.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + label);
			res.setItemMeta(meta);
			return res;
		}
	}

	/**
	 * Represents the direction of charging selected for this machine.
	 */
	public enum ChargeDirection {
		MACHINE(Material.MAGMA_CREAM, "Charge Machine"),
		CELL(Material.SLIME_BALL, "Charge Energy Cell");
		private final Material material;
		private final String label;

		ChargeDirection(Material material, String label) {
			this.label = label;
			this.material = material;
		}

		public ItemStack getTexture() {
			ItemStack res = new ItemStack(material);
			ItemMeta meta = res.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + label);
			res.setItemMeta(meta);
			return res;
		}
	}
}
