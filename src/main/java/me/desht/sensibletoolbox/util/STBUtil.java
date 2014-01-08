package me.desht.sensibletoolbox.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

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
	 * @param type
	 * @return
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
}
