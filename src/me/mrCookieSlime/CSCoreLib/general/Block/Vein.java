package me.mrCookieSlime.CSCoreLib.general.Block;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Vein {
	
	public static void calculate(Location origin, Location anchor, List<Location> list, int max) {
		if (list.size() > max) return;
		
		for (BlockFace face: BlockFace.values()) {
			Block next = anchor.getBlock().getRelative(face);
			if (next.getType() == anchor.getBlock().getType() && !list.contains(next.getLocation())) {
				list.add(next.getLocation());
				calculate(origin, next.getLocation(), list, max);
			}
		}
	}

}
