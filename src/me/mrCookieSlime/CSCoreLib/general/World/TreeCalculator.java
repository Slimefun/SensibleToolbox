package me.mrCookieSlime.CSCoreLib.general.World;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class TreeCalculator {
	
	public static void getTree(Block anchor, ArrayList<Block> logs, ArrayList<Block> leaves) {

		// Limits:
		if(logs.size() > 128) return;
		if(leaves.size() > 256) return;

		Block nextAnchor = null;

		// North:
		nextAnchor = anchor.getRelative(BlockFace.NORTH);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// North-east:
		nextAnchor = anchor.getRelative(BlockFace.NORTH_EAST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// East:
		nextAnchor = anchor.getRelative(BlockFace.EAST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// South-east:
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// South:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// South-west:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH_WEST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// West:
		nextAnchor = anchor.getRelative(BlockFace.WEST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// North-west:
		nextAnchor = anchor.getRelative(BlockFace.NORTH_WEST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Shift anchor one up:
		anchor = anchor.getRelative(BlockFace.UP);

		// Up-north:
		nextAnchor = anchor.getRelative(BlockFace.NORTH);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-north-east:
		nextAnchor = anchor.getRelative(BlockFace.NORTH_EAST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-east:
		nextAnchor = anchor.getRelative(BlockFace.EAST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-south-east:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH_EAST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-south:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-south-west:
		nextAnchor = anchor.getRelative(BlockFace.SOUTH_WEST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-west:
		nextAnchor = anchor.getRelative(BlockFace.WEST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up-north-west:
		nextAnchor = anchor.getRelative(BlockFace.NORTH_WEST);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}

		// Up:
		nextAnchor = anchor.getRelative(BlockFace.SELF);
		if((nextAnchor.getType().equals(Material.LOG) || nextAnchor.getType().equals(Material.LOG_2)) && !logs.contains(nextAnchor)){
			logs.add(nextAnchor);
			getTree(nextAnchor, logs, leaves);
		}
		else if((nextAnchor.getType().equals(Material.LEAVES) || nextAnchor.getType().equals(Material.LEAVES_2)) && !logs.contains(nextAnchor)){
			leaves.add(nextAnchor);
		}
	}
}
