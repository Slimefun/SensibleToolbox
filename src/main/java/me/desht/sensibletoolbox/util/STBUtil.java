package me.desht.sensibletoolbox.util;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public class STBUtil {

	private static BlockFace[] faces = new BlockFace[] {
			BlockFace.SELF,
			BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH,
			BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH
	};

	/**
	 * Check if the given material is a crop which can grow and/or be harvested.
	 *
	 * @param m the material to check
	 * @return true if the material is a crop
	 */
	public static boolean isCrop(Material m) {
		return m == Material.CROPS || m == Material.CARROT || m == Material.POTATO || m == Material.PUMPKIN_STEM || m == Material.MELON_STEM;
	}

	/**
	 * Check if the given material is any growing plant (not including leaves or trees)
	 *
	 * @param type the material to check
	 * @return true if the material is a plant
	 */
	public static boolean isPlant(Material type) {
		if (isCrop(type)) {
			return true;
		}
		switch (type) {
			case LONG_GRASS:
			case DOUBLE_PLANT:
			case YELLOW_FLOWER:
			case RED_ROSE:
			case SUGAR_CANE_BLOCK:
			case BROWN_MUSHROOM:
			case RED_MUSHROOM:
			case DEAD_BUSH:
			case SAPLING:
				return true;
		}
		return false;
	}

	/**
	 * Get the blocks horizontally surrounding the given block.
	 *
	 * @param b the centre block
	 * @return array of all blocks around the given block, including the given block as the first element
	 */
	public static Block[] getSurroundingBlocks(Block b) {
		Block[] result = new Block[faces.length];
		for (int i = 0; i < faces.length; i++) {
			result[i] = b.getRelative(faces[i]);
		}
		return result;
	}


	public static Material getCropType(Material seedType) {
		switch (seedType) {
			case SEEDS: return Material.CROPS;
			case POTATO_ITEM: return Material.POTATO;
			case CARROT_ITEM: return Material.CARROT;
			case PUMPKIN_SEEDS: return Material.PUMPKIN_STEM;
			case MELON_SEEDS: return Material.MELON_STEM;
			default: return null;
		}
	}

	public static Object getMetadataValue(Metadatable m, String key) {
		for (MetadataValue mv : m.getMetadata(key)) {
			if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
				return mv.value();
			}
		}
		return null;
	}

	public static ChatColor toChatColor(DyeColor c) {
		switch (c) {
			case BLACK: return ChatColor.DARK_GRAY;
			case BLUE: return ChatColor.DARK_BLUE;
			case BROWN: return ChatColor.GOLD;
			case CYAN: return ChatColor.AQUA;
			case GRAY: return ChatColor.GRAY;
			case GREEN: return ChatColor.DARK_GREEN;
			case LIGHT_BLUE: return ChatColor.BLUE;
			case LIME: return ChatColor.GREEN;
			case MAGENTA: return ChatColor.LIGHT_PURPLE;
			case ORANGE: return ChatColor.GOLD;
			case PINK: return ChatColor.LIGHT_PURPLE;
			case PURPLE: return ChatColor.DARK_PURPLE;
			case RED: return ChatColor.DARK_RED;
			case SILVER: return ChatColor.GRAY;
			case WHITE: return ChatColor.WHITE;
			case YELLOW: return ChatColor.YELLOW;
			default: throw new IllegalArgumentException("unknown dye color"  + c);
		}
	}

	public static boolean isColorable(Material mat) {
		switch (mat) {
			case STAINED_GLASS: case STAINED_GLASS_PANE: case STAINED_CLAY: case CARPET: case WOOL:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Create a skull with the given player skin
	 *
	 * @param b block in which to create the skull
	 * @param name name of the skin
	 * @param player if not null, skull will face the opposite direction the player face
	 * @return the skull (the caller should call skull.update() when ready)
	 */
	public static Skull setSkullHead(Block b, String name, Player player) {
		b.setType(Material.SKULL);
		Skull skull = (Skull) b.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner(name);
		org.bukkit.material.Skull sk = (org.bukkit.material.Skull) skull.getData();
		sk.setFacingDirection(BlockFace.SELF);
		skull.setData(sk);
		if (player != null) {
			skull.setRotation(getRotation(player.getLocation()));
		}
		return skull;
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
}
