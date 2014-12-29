package me.mrCookieSlime.CSCoreLib.general.Block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockAdjacents {
	
	public static Block[] getAdjacentBlocks(Block b) {
		return new Block[] {b.getRelative(BlockFace.UP), b.getRelative(BlockFace.DOWN), b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST), b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST)};
	}
	
	public static boolean hasAdjacentMaterial(Block b, Material m) {
		boolean bo = false;
		Block[] adjacents = getAdjacentBlocks(b);
		for (int i = 0; i < adjacents.length; i++) {
			if (adjacents[i].getType() == m) {
				bo = true;
			}
		}
		return bo;
	}
	
	public static boolean hasMaterialOnBothSides(Block b, Material m) {
		boolean bo = false;
		
		if (b.getRelative(BlockFace.NORTH).getType() == m && b.getRelative(BlockFace.SOUTH).getType() == m) {
			bo = true;
		}
		else if (b.getRelative(BlockFace.EAST).getType() == m && b.getRelative(BlockFace.WEST).getType() == m) {
			bo = true;
		}
		return bo;
	}
	
	public static boolean hasMaterialOnAllSides(Block b, Material m) {
		boolean bo = false;
		Block[] adjacents = getAdjacentBlocks(b);
		
		if (adjacents[2].getType() == m && adjacents[3].getType() == m && adjacents[4].getType() == m && adjacents[5].getType() == m) {
			bo = true;
		}
		return bo;
	}
	
	public static boolean isMaterial(Block b, Material m) {
		if (m == null) {
			return true;
		}
		else {
			return b.getType() == m;
		}
	}
	
	public static boolean hasMaterialOnSide(Block b, Material m) {
		if (m == null) {
			return true;
		}
		else {
			boolean bo = false;
			Block[] adjacents = getAdjacentBlocks(b);
			
			if (adjacents[2].getType() == m || adjacents[3].getType() == m || adjacents[4].getType() == m || adjacents[5].getType() == m) {
				bo = true;
			}
			return bo;
		}
	}
	
	public static boolean hasMaterialOnTop(Block b, Material m) {
		boolean bo = false;
		
		if (b.getRelative(BlockFace.UP).getType() == m) {
			bo = true;
		}
		return bo;
	}

}
