package me.desht.sensibletoolbox.items;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.block.Skull;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Map;

public class TrashCan extends BaseSTBBlock {
	public static TrashCan deserialize(Map<String, Object> map) {
		// no specific config for this block
		return new TrashCan();
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
	public void handleBlockPlace(BlockPlaceEvent event) {
		// ensure there's enough room
		if (!event.getBlockPlaced().getRelative(BlockFace.UP).isEmpty()) {
			MiscUtil.errorMessage(event.getPlayer(), "Not enough room here (need one empty block above).");
			event.setCancelled(true);
			return;
		}

		// the item's displayname becomes the inventory's title - let's make it stand out
		ItemMeta meta = event.getItemInHand().getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED + "!!! " + getItemName() + " !!!");
		event.getItemInHand().setItemMeta(meta);

		super.handleBlockPlace(event);

		// put a skull on top of the main block
		Block above = event.getBlock().getRelative(BlockFace.UP);
		above.setType(Material.SKULL);
		Skull skull = (Skull) above.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner("MHF_TNT2");
		org.bukkit.material.Skull sk = (org.bukkit.material.Skull) skull.getData();
		sk.setFacingDirection(BlockFace.SELF);
		skull.setData(sk);
		skull.setRotation(getRotation(event.getPlayer().getLocation()));
		skull.update();
	}

	@Override
	public Vector[] getBlockStructure() {
		return new Vector[] { new Vector(0, 1, 0) };
	}

	public void emptyTrash(boolean noisy) {
		Location l = getBaseLocation();
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
			System.out.println("clear trashcan " + d);
			d.getInventory().clear();
		}
	}

	private static BlockFace getRotation(Location loc) {
		double rot = loc.getYaw() % 360;
		if (rot < 0) {
			rot += 360;
		}
		if ((0 <= rot && rot < 45) || (315 <= rot && rot < 360.0)) {
			return BlockFace.NORTH;
		} else if (45 <= rot && rot < 135) {
			return BlockFace.EAST;
		} else if (135 <= rot && rot < 225) {
			return BlockFace.SOUTH;
		} else if (225 <= rot && rot < 315) {
			return BlockFace.WEST;
		} else {
			throw new IllegalArgumentException("impossible rotation: " + rot);
		}
	}

	public static TrashCan getTrashCan(Inventory inv) {
		InventoryHolder h = inv.getHolder();
		if (h instanceof Dropper) {
			Dropper d = (Dropper) h;
			BaseSTBBlock stb = SensibleToolboxPlugin.getInstance().getLocationManager().get(d.getLocation());
			if (stb != null && stb instanceof TrashCan) {
				return (TrashCan) stb;
			}
		}
		return null;
	}
}
