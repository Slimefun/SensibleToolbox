package me.desht.dhutils.cuboid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * A {@link Cuboid} represents a cubic region of {@link Block Blocks}.
 * 
 * @author desht
 * @author TheBusyBiscuit
 *
 */
public class Cuboid implements Iterable<Block> {

    protected final UUID worldUUID;

    protected final int lowerX;
    protected final int lowerY;
    protected final int lowerZ;

    protected final int upperX;
    protected final int upperY;
    protected final int upperZ;

    /**
     * Construct a Cuboid given two Location objects which represent any two corners
     * of the Cuboid.
     *
     * @param l1
     *            one of the corners
     * @param l2
     *            the other corner
     */
    @ParametersAreNonnullByDefault
    public Cuboid(Location l1, Location l2) {
        if (!l1.getWorld().equals(l2.getWorld())) {
            throw new IllegalArgumentException("locations must be on the same world");
        }

        worldUUID = l1.getWorld().getUID();
        lowerX = Math.min(l1.getBlockX(), l2.getBlockX());
        lowerY = Math.min(l1.getBlockY(), l2.getBlockY());
        lowerZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
        upperX = Math.max(l1.getBlockX(), l2.getBlockX());
        upperY = Math.max(l1.getBlockY(), l2.getBlockY());
        upperZ = Math.max(l1.getBlockZ(), l2.getBlockZ());
    }

    /**
     * Construct a one-block Cuboid at the given Location of the Cuboid.
     *
     * @param l1
     *            location of the Cuboid
     */
    @ParametersAreNonnullByDefault
    public Cuboid(Location l1) {
        this(l1, l1);
    }

    /**
     * Copy constructor.
     *
     * @param other
     *            the Cuboid to copy
     */
    @ParametersAreNonnullByDefault
    public Cuboid(Cuboid other) {
        this(other.getWorld(), other.lowerX, other.lowerY, other.lowerZ, other.upperX, other.upperY, other.upperZ);
    }

    /**
     * Construct a {@link Cuboid} in the given World and xyz co-ordinates
     *
     * @param world
     *            the Cuboid's {@link World}
     * @param x1
     *            X co-ordinate of corner 1
     * @param y1
     *            Y co-ordinate of corner 1
     * @param z1
     *            Z co-ordinate of corner 1
     * @param x2
     *            X co-ordinate of corner 2
     * @param y2
     *            Y co-ordinate of corner 2
     * @param z2
     *            Z co-ordinate of corner 2
     */
    @ParametersAreNonnullByDefault
    public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this(world.getUID(), x1, y1, z1, x2, y2, z2);
    }

    /**
     * Construct a {@link Cuboid} in the given world name and xyz co-ordinates.
     *
     * @param world
     *            the Cuboid's World {@link UUID}
     * @param x1
     *            X co-ordinate of corner 1
     * @param y1
     *            Y co-ordinate of corner 1
     * @param z1
     *            Z co-ordinate of corner 1
     * @param x2
     *            X co-ordinate of corner 2
     * @param y2
     *            Y co-ordinate of corner 2
     * @param z2
     *            Z co-ordinate of corner 2
     */
    private Cuboid(UUID world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldUUID = world;
        this.lowerX = Math.min(x1, x2);
        this.upperX = Math.max(x1, x2);
        this.lowerY = Math.min(y1, y2);
        this.upperY = Math.max(y1, y2);
        this.lowerZ = Math.min(z1, z2);
        this.upperZ = Math.max(z1, z2);
    }

    /**
     * Get the Location of the lower northeast corner of the Cuboid (minimum XYZ
     * co-ordinates).
     *
     * @return Location of the lower northeast corner
     */
    @Nonnull
    public Location getLowerNE() {
        return new Location(getWorld(), lowerX, lowerY, lowerZ);
    }

    /**
     * Get the Location of the upper southwest corner of the Cuboid (maximum XYZ
     * co-ordinates).
     *
     * @return Location of the upper southwest corner
     */
    @Nonnull
    public Location getUpperSW() {
        return new Location(getWorld(), upperX, upperY, upperZ);
    }

    /**
     * Get the the centre of the Cuboid
     *
     * @return Location at the centre of the Cuboid
     */
    @Nonnull
    public Location getCenter() {
        int x = getUpperX() + 1;
        int y = getUpperY() + 1;
        int z = getUpperZ() + 1;

        return new Location(getWorld(), getLowerX() + (x - getLowerX()) / 2.0, getLowerY() + (y - getLowerY()) / 2.0, getLowerZ() + (z - getLowerZ()) / 2.0);
    }

    /**
     * Get the Cuboid's world.
     *
     * @return the World object representing this Cuboid's world
     * @throws IllegalStateException
     *             if the world is not loaded
     */
    @Nonnull
    public World getWorld() {
        World world = Bukkit.getWorld(worldUUID);

        if (world == null) {
            throw new IllegalStateException("world '" + worldUUID + "' is not loaded");
        }

        return world;
    }

    /**
     * Get the size of this Cuboid along the X axis
     *
     * @return Size of Cuboid along the X axis
     */
    public int getSizeX() {
        return (upperX - lowerX) + 1;
    }

    /**
     * Get the size of this Cuboid along the Y axis
     *
     * @return Size of Cuboid along the Y axis
     */
    public int getSizeY() {
        return (upperY - lowerY) + 1;
    }

    /**
     * Get the size of this Cuboid along the Z axis
     *
     * @return Size of Cuboid along the Z axis
     */
    public int getSizeZ() {
        return (upperZ - lowerZ) + 1;
    }

    /**
     * Get the minimum X co-ordinate of this Cuboid
     *
     * @return the minimum X co-ordinate
     */
    public int getLowerX() {
        return lowerX;
    }

    /**
     * Get the minimum Y co-ordinate of this Cuboid
     *
     * @return the minimum Y co-ordinate
     */
    public int getLowerY() {
        return lowerY;
    }

    /**
     * Get the minimum Z co-ordinate of this Cuboid
     *
     * @return the minimum Z co-ordinate
     */
    public int getLowerZ() {
        return lowerZ;
    }

    /**
     * Get the maximum X co-ordinate of this Cuboid
     *
     * @return the maximum X co-ordinate
     */
    public int getUpperX() {
        return upperX;
    }

    /**
     * Get the maximum Y co-ordinate of this Cuboid
     *
     * @return the maximum Y co-ordinate
     */
    public int getUpperY() {
        return upperY;
    }

    /**
     * Get the maximum Z co-ordinate of this Cuboid
     *
     * @return the maximum Z co-ordinate
     */
    public int getUpperZ() {
        return upperZ;
    }

    /**
     * Get the Blocks at the eight corners of the Cuboid.
     *
     * @return array of Block objects representing the Cuboid corners
     */
    @Nonnull
    public Block[] getCorners() {
        Block[] res = new Block[8];
        World w = getWorld();

        res[0] = w.getBlockAt(lowerX, lowerY, lowerZ);
        res[1] = w.getBlockAt(lowerX, lowerY, upperZ);
        res[2] = w.getBlockAt(lowerX, upperY, lowerZ);
        res[3] = w.getBlockAt(lowerX, upperY, upperZ);
        res[4] = w.getBlockAt(upperX, lowerY, lowerZ);
        res[5] = w.getBlockAt(upperX, lowerY, upperZ);
        res[6] = w.getBlockAt(upperX, upperY, lowerZ);
        res[7] = w.getBlockAt(upperX, upperY, upperZ);

        return res;
    }

    /**
     * Expand the Cuboid in the given direction by the given amount. Negative amounts will
     * shrink the Cuboid in the given direction. Shrinking a cuboid's face past the opposite face
     * is not an error and will return a valid Cuboid.
     *
     * @param dir
     *            the direction in which to expand
     * @param amount
     *            the number of blocks by which to expand
     * @return a new Cuboid expanded by the given direction and amount
     */
    @Nonnull
    public Cuboid expand(@Nonnull CuboidDirection dir, int amount) {
        switch (dir) {
            case NORTH:
                return new Cuboid(worldUUID, lowerX - amount, lowerY, lowerZ, upperX, upperY, upperZ);
            case SOUTH:
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ, upperX + amount, upperY, upperZ);
            case EAST:
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ - amount, upperX, upperY, upperZ);
            case WEST:
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ, upperX, upperY, upperZ + amount);
            case DOWN:
                return new Cuboid(worldUUID, lowerX, lowerY - amount, lowerZ, upperX, upperY, upperZ);
            case UP:
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ, upperX, upperY + amount, upperZ);
            default:
                throw new IllegalArgumentException("invalid direction " + dir);
        }
    }

    /**
     * Shift the Cuboid in the given direction by the given amount.
     *
     * @param dir
     *            the direction in which to shift
     * @param amount
     *            the number of blocks by which to shift
     * @return a new Cuboid shifted by the given direction and amount
     */
    @Nonnull
    public Cuboid shift(@Nonnull CuboidDirection dir, int amount) {
        return expand(dir, amount).expand(dir.getOpposite(), -amount);
    }

    /**
     * Outset (grow) the Cuboid in the given direction by the given amount.
     *
     * @param dir
     *            the direction in which to outset (must be Horizontal, Vertical, or Both)
     * @param amount
     *            the number of blocks by which to outset
     * @return a new Cuboid outset by the given direction and amount
     */
    @Nonnull
    public Cuboid outset(@Nonnull CuboidDirection dir, int amount) {
        switch (dir) {
            case HORIZONTAL:
                return expand(CuboidDirection.NORTH, amount).expand(CuboidDirection.SOUTH, amount).expand(CuboidDirection.EAST, amount).expand(CuboidDirection.WEST, amount);
            case VERTICAL:
                return expand(CuboidDirection.DOWN, amount).expand(CuboidDirection.UP, amount);
            case BOTH:
                return outset(CuboidDirection.HORIZONTAL, amount).outset(CuboidDirection.VERTICAL, amount);
            default:
                throw new IllegalArgumentException("invalid direction " + dir);
        }
    }

    /**
     * Inset (shrink) the Cuboid in the given direction by the given amount. Equivalent
     * to calling outset() with a negative amount.
     *
     * @param dir
     *            the direction in which to inset (must be Horizontal, Vertical, or Both)
     * @param amount
     *            the number of blocks by which to inset
     * @return a new Cuboid inset by the given direction and amount
     */
    @Nonnull
    public Cuboid inset(@Nonnull CuboidDirection dir, int amount) {
        return outset(dir, -amount);
    }

    /**
     * Return true if the point at (x,y,z) is contained within this Cuboid.
     *
     * @param x
     *            the X co-ordinate
     * @param y
     *            the Y co-ordinate
     * @param z
     *            the Z co-ordinate
     * @return true if the given point is within this Cuboid, false otherwise
     */
    public boolean contains(int x, int y, int z) {
        return x >= lowerX && x <= upperX && y >= lowerY && y <= upperY && z >= lowerZ && z <= upperZ;
    }

    /**
     * Check if the given Block is contained within this Cuboid.
     *
     * @param b
     *            the Block to check for
     * @return true if the Block is within this Cuboid, false otherwise
     */
    public boolean contains(@Nonnull Block b) {
        return contains(b.getLocation());
    }

    /**
     * Check if the given Location is contained within this Cuboid.
     *
     * @param l
     *            the Location to check for
     * @return true if the Location is within this Cuboid, false otherwise
     */
    public boolean contains(@Nonnull Location l) {
        return worldUUID.equals(l.getWorld().getUID()) && contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    /**
     * Get the volume of this Cuboid.
     *
     * @return the Cuboid volume, in blocks
     */
    public int getVolume() {
        return getSizeX() * getSizeY() * getSizeZ();
    }

    /**
     * Contract the Cuboid, returning a Cuboid with any air around the edges removed, just
     * large enough to include all non-air blocks.
     *
     * @return a new Cuboid with no external air blocks
     */
    @Nonnull
    public Cuboid contract() {
        return this.contract(CuboidDirection.DOWN).contract(CuboidDirection.SOUTH).contract(CuboidDirection.EAST).contract(CuboidDirection.UP).contract(CuboidDirection.NORTH).contract(CuboidDirection.WEST);
    }

    /**
     * Contract the Cuboid in the given direction, returning a new Cuboid which has no exterior empty space.
     * E.g. a direction of Down will push the top face downwards as much as possible.
     *
     * @param dir
     *            the direction in which to contract
     * @return a new Cuboid contracted in the given direction
     */
    @Nonnull
    public Cuboid contract(@Nonnull CuboidDirection dir) {
        Cuboid face = getFace(dir.getOpposite());

        switch (dir) {
            case DOWN:
                while (face.containsOnly(Material.AIR) && face.getLowerY() > this.getLowerY()) {
                    face = face.shift(CuboidDirection.DOWN, 1);
                }
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ, upperX, face.getUpperY(), upperZ);
            case UP:
                while (face.containsOnly(Material.AIR) && face.getUpperY() < this.getUpperY()) {
                    face = face.shift(CuboidDirection.UP, 1);
                }
                return new Cuboid(worldUUID, lowerX, face.getLowerY(), lowerZ, upperX, upperY, upperZ);
            case NORTH:
                while (face.containsOnly(Material.AIR) && face.getLowerX() > this.getLowerX()) {
                    face = face.shift(CuboidDirection.NORTH, 1);
                }
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ, face.getUpperX(), upperY, upperZ);
            case SOUTH:
                while (face.containsOnly(Material.AIR) && face.getUpperX() < this.getUpperX()) {
                    face = face.shift(CuboidDirection.SOUTH, 1);
                }
                return new Cuboid(worldUUID, face.getLowerX(), lowerY, lowerZ, upperX, upperY, upperZ);
            case EAST:
                while (face.containsOnly(Material.AIR) && face.getLowerZ() > this.getLowerZ()) {
                    face = face.shift(CuboidDirection.EAST, 1);
                }
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ, upperX, upperY, face.getUpperZ());
            case WEST:
                while (face.containsOnly(Material.AIR) && face.getUpperZ() < this.getUpperZ()) {
                    face = face.shift(CuboidDirection.WEST, 1);
                }
                return new Cuboid(worldUUID, lowerX, lowerY, face.getLowerZ(), upperX, upperY, upperZ);
            default:
                throw new IllegalArgumentException("Invalid direction " + dir);
        }
    }

    /**
     * Get the Cuboid representing the face of this Cuboid. The resulting Cuboid will be
     * one block thick in the axis perpendicular to the requested face.
     *
     * @param dir
     *            which face of the Cuboid to get
     * @return the Cuboid representing this Cuboid's requested face
     */
    @Nonnull
    public Cuboid getFace(@Nonnull CuboidDirection dir) {
        switch (dir) {
            case DOWN:
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ, upperX, lowerY, upperZ);
            case UP:
                return new Cuboid(worldUUID, lowerX, upperY, lowerZ, upperX, upperY, upperZ);
            case NORTH:
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ, lowerX, upperY, upperZ);
            case SOUTH:
                return new Cuboid(worldUUID, upperX, lowerY, lowerZ, upperX, upperY, upperZ);
            case EAST:
                return new Cuboid(worldUUID, lowerX, lowerY, lowerZ, upperX, upperY, lowerZ);
            case WEST:
                return new Cuboid(worldUUID, lowerX, lowerY, upperZ, upperX, upperY, upperZ);
            default:
                throw new IllegalArgumentException("Invalid direction " + dir);
        }
    }

    /**
     * Check if the Cuboid contains only blocks of the given type
     *
     * @param material
     *            the material to check for
     * @return true if this Cuboid contains only blocks of the given type
     */
    public boolean containsOnly(@Nonnull Material material) {
        for (Block b : this) {
            if (b.getType() != material) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the Cuboid big enough to hold both this Cuboid and the given one.
     *
     * @param other
     *            the other Cuboid to include
     * @return a new Cuboid large enough to hold this Cuboid and the given Cuboid
     */
    @Nonnull
    public Cuboid getBoundingCuboid(@Nullable Cuboid other) {
        if (other == null) {
            return this;
        }

        int xMin = Math.min(getLowerX(), other.getLowerX());
        int yMin = Math.min(getLowerY(), other.getLowerY());
        int zMin = Math.min(getLowerZ(), other.getLowerZ());
        int xMax = Math.max(getUpperX(), other.getUpperX());
        int yMax = Math.max(getUpperY(), other.getUpperY());
        int zMax = Math.max(getUpperZ(), other.getUpperZ());

        return new Cuboid(worldUUID, xMin, yMin, zMin, xMax, yMax, zMax);
    }

    /**
     * Get a block relative to the lower NE point of the Cuboid.
     *
     * @param x
     *            the X co-ordinate
     * @param y
     *            the Y co-ordinate
     * @param z
     *            the Z co-ordinate
     * @return the block at the given position
     */
    @Nonnull
    public Block getRelativeBlock(int x, int y, int z) {
        return getWorld().getBlockAt(lowerX + x, lowerY + y, lowerZ + z);
    }

    /**
     * Get a block relative to the lower NE point of the Cuboid in the given World. This
     * version of getRelativeBlock() should be used if being called many times, to avoid
     * excessive calls to getWorld().
     *
     * @param w
     *            the World
     * @param x
     *            the X co-ordinate
     * @param y
     *            the Y co-ordinate
     * @param z
     *            the Z co-ordinate
     * @return the block at the given position
     */
    @Nonnull
    public Block getRelativeBlock(@Nonnull World w, int x, int y, int z) {
        return w.getBlockAt(lowerX + x, lowerY + y, lowerZ + z);
    }

    /**
     * Get a list of the chunks which are fully or partially contained in this cuboid.
     *
     * @return a list of Chunk objects
     */
    @Nonnull
    public List<Chunk> getChunks() {
        List<Chunk> res = new ArrayList<>();

        World w = getWorld();
        int x1 = getLowerX() & ~0xf;
        int x2 = getUpperX() & ~0xf;

        int z1 = getLowerZ() & ~0xf;
        int z2 = getUpperZ() & ~0xf;

        for (int x = x1; x <= x2; x += 16) {
            for (int z = z1; z <= z2; z += 16) {
                res.add(w.getChunkAt(x >> 4, z >> 4));
            }
        }

        return res;
    }

    @Nonnull
    public Iterator<Block> iterator() {
        return new CuboidIterator(getWorld(), lowerX, lowerY, lowerZ, upperX, upperY, upperZ);
    }

    @Override
    public String toString() {
        return "Cuboid: " + worldUUID + "," + lowerX + "," + lowerY + "," + lowerZ + "=>" + upperX + "," + upperY + "," + upperZ;
    }

}
