package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.RelativePosition;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;

public class TrashCan extends BaseSTBBlock {
	public TrashCan() {
	}

	public TrashCan(ConfigurationSection conf) {
		super(conf);
	}

	public static TrashCan getTrashCan(Inventory inv) {
		InventoryHolder h = inv.getHolder();
		if (h instanceof Dropper) {
			Dropper d = (Dropper) h;
			return LocationManager.getManager().get(d.getLocation(), TrashCan.class);
		}
		return null;
	}

	@Override
	public Material getBaseMaterial() {
		// note: item is multi-block structure when placed; dropper plus skeleton head
		return Material.DROPPER;
	}

	@Override
	public String getItemName() {
		return "Trash Can";
	}

	@Override
	public String[] getLore() {
		return new String[] { "DESTROYS any items which are", "placed or piped into it.", "Beware!" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("SSS", "OCO", "OOO");
		recipe.setIngredient('S', Material.STONE);
		recipe.setIngredient('C', Material.CHEST);
		recipe.setIngredient('O', Material.COBBLESTONE);
		return recipe;
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		// ensure there's enough room
		if (!event.getBlockPlaced().getRelative(BlockFace.UP).isEmpty()) {
			MiscUtil.errorMessage(event.getPlayer(), "Not enough room here (need one empty block above).");
			event.setCancelled(true);
			return;
		}

		setInventoryTitle(event, ChatColor.DARK_RED + "!!! " + getItemName() + " !!!");

		super.onBlockPlace(event);

		// put a skull on top of the main block
		Block above = event.getBlock().getRelative(BlockFace.UP);
		Skull skull = STBUtil.setSkullHead(above, "MHF_Exclamation", event.getPlayer());
		skull.update();
	}

	@Override
	public void onInteractBlock(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == Material.SIGN) {
			// attach a label sign
			attachLabelSign(event);
		}
	}

	@Override
	public RelativePosition[] getBlockStructure() {
		return new RelativePosition[] { new RelativePosition(0, 1, 0) };
	}

	/**
	 * Empty this trash can, permanently destroying its contents.
	 *
	 * @param noisy if true, play a sound effect if any items were destroyed
	 */
	public void emptyTrash(boolean noisy) {
		Location l = getLocation();
		if (l != null && l.getBlock().getType() == getBaseMaterial()) {
			Dropper d = (Dropper) l.getBlock().getState();
			// TODO: handle item filters
			if (noisy) {
				for (ItemStack stack : d.getInventory()) {
					if (stack != null) {
						d.getLocation().getWorld().playSound(d.getLocation(), Sound.EAT, 1.0f, 1.0f);
						break;
					}
				}
			}
			Debugger.getInstance().debug(this + ": trash emptied");
			d.getInventory().clear();
		}
	}
}
