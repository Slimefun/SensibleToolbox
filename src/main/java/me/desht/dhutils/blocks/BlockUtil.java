package me.desht.dhutils.blocks;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
import org.bukkit.util.Vector;

public final class BlockUtil {

    private BlockUtil() {}

    private static final BlockFace[] allFaces = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP };

    private static Map<BlockFace, BlockFace> toLeft = new EnumMap<>(BlockFace.class);

    static {
        toLeft.put(BlockFace.NORTH, BlockFace.WEST);
        toLeft.put(BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_WEST);
        toLeft.put(BlockFace.NORTH_EAST, BlockFace.NORTH_WEST);
        toLeft.put(BlockFace.EAST_NORTH_EAST, BlockFace.NORTH_WEST);
        toLeft.put(BlockFace.EAST, BlockFace.NORTH);
        toLeft.put(BlockFace.EAST_SOUTH_EAST, BlockFace.NORTH_EAST);
        toLeft.put(BlockFace.SOUTH_EAST, BlockFace.NORTH_EAST);
        toLeft.put(BlockFace.SOUTH_SOUTH_EAST, BlockFace.NORTH_EAST);
        toLeft.put(BlockFace.SOUTH, BlockFace.EAST);
        toLeft.put(BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_EAST);
        toLeft.put(BlockFace.SOUTH_WEST, BlockFace.SOUTH_EAST);
        toLeft.put(BlockFace.WEST_SOUTH_WEST, BlockFace.SOUTH_EAST);
        toLeft.put(BlockFace.WEST, BlockFace.SOUTH);
        toLeft.put(BlockFace.WEST_NORTH_WEST, BlockFace.SOUTH_WEST);
        toLeft.put(BlockFace.NORTH_WEST, BlockFace.SOUTH_WEST);
        toLeft.put(BlockFace.NORTH_NORTH_WEST, BlockFace.SOUTH_WEST);
    }

    /**
     * Get the BlockFace 90 degrees to the left of the given BlockFace.
     *
     * @param facing
     * @return
     */
    public static BlockFace getLeft(BlockFace facing) {
        if (!toLeft.containsKey(facing)) {
            throw new IllegalArgumentException("can't pass " + facing + " to getLeft()");
        }
        return toLeft.get(facing);
    }

    /**
     * Get the nearest face on the given block relative to the given location.
     *
     * @param block
     * @param loc
     * @return
     */
    public static BlockFace getNearestFace(Block block, Location loc) {
        Location bLoc = block.getLocation().add(0.5, 0.5, 0.5);
        Vector v = loc.toVector().subtract(bLoc.toVector());
        float min = Float.MAX_VALUE;
        BlockFace wantedFace = null;
        for (BlockFace face : allFaces) {
            Vector v1 = new Vector(face.getModX(), face.getModY(), face.getModZ());
            float angle = v.angle(v1);
            if (angle < min) {
                min = angle;
                wantedFace = face;
            }
        }
        return wantedFace;
    }

    /**
     * Check if the given block is an attachable material, and if so, if the block it's
     * attached to can actually hold it.
     *
     * @param block
     *            the block to check
     * @return false if the block is still attached OK, true if it has become detached
     */
    public static boolean isAttachableDetached(Block block) {
        BlockState bs = block.getState();
        if (bs.getData() instanceof Attachable) {
            Attachable a = (Attachable) bs.getData();
            Block attachedBlock = block.getRelative(a.getAttachedFace());
            return !attachedBlock.getType().isSolid();
        } else {
            return false;
        }
    }

    public static BlockAndPosition getTargetPoint(Player player, Set<Material> transparent, int maxDistance) {
        List<Block> lastBlocks = player.getLastTwoTargetBlocks(transparent, maxDistance);
        Block block = lastBlocks.get(1);
        BlockFace face = block.getFace(lastBlocks.get(0));

        Vector plane = new Vector(block.getX(), block.getY(), block.getZ());
        // this is the lower northwest point of the block
        // we need a point (any point) on the correct side of the block
        if (Tag.WALL_SIGNS.isTagged(block.getType())) {
            // of course signs are thin, and we don't have hitbox data available via Bukkit
            // so we'll have to cheat...
            Sign s = (Sign) block.getState().getData();

            switch (s.getRotation()) {
                case EAST:
                    plane.add(new Vector(0.125, 0.0, 0.0));
                    break;
                case WEST:
                    plane.add(new Vector(0.875, 0.0, 0.0));
                    break;
                case NORTH:
                    plane.add(new Vector(0.0, 0.0, 0.875));
                    break;
                case SOUTH:
                    plane.add(new Vector(0.0, 0.0, 0.125));
                    break;
                default:
                    break;
            }
        } else {
            switch (face) {
                case EAST:
                    plane.add(new Vector(1.0, 0.0, 0.0));
                    break;
                case SOUTH:
                    plane.add(new Vector(0.0, 0.0, 1.0));
                    break;
                case UP:
                    plane.add(new Vector(0.0, 1.0, 0.0));
                    break;
                default:
                    break;
            }
        }

        // normal to the block face
        Vector normal = new Vector(face.getModX(), face.getModY(), face.getModZ());

        // get any two points along the line the player is looking
        // player's eye location is an obvious choice
        // second point can be anywhere along the line of sight
        Location loc = player.getEyeLocation();
        Vector eye = new Vector(loc.getX(), loc.getY(), loc.getZ());
        Vector p2 = eye.clone().add(player.getLocation().getDirection());

        Vector isect = isectLinePlane(eye, p2, plane, normal, 0.0000001);
        return new BlockAndPosition(block, face, isect);
    }

    @Nullable
    private static Vector isectLinePlane(Vector p0, Vector p1, Vector plane, Vector normal, double epsilon) {
        Vector u = p1.clone().subtract(p0);
        Vector w = p0.clone().subtract(plane);
        double dot = normal.dot(u);

        if (Math.abs(dot) > epsilon) {
            double fac = -normal.dot(w) / dot;
            u.multiply(fac);
            return p0.clone().add(u);
        } else {
            return null;
        }
    }
}
