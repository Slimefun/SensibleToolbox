package me.desht.dhutils.cuboid;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * The {@link CuboidIterator} is used to iterate through a {@link Cuboid}.
 * This can be obtained through {@link Cuboid#iterator()}.
 * 
 * @author desht
 * @author TheBusyBiscuit
 *
 */
class CuboidIterator implements Iterator<Block> {

    private World world;

    private int x = 0;
    private int y = 0;
    private int z = 0;

    private int baseX;
    private int baseY;
    private int baseZ;

    private int sizeX;
    private int sizeY;
    private int sizeZ;

    @ParametersAreNonnullByDefault
    public CuboidIterator(World w, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = w;
        baseX = x1;
        baseY = y1;
        baseZ = z1;
        sizeX = Math.abs(x2 - x1) + 1;
        sizeY = Math.abs(y2 - y1) + 1;
        sizeZ = Math.abs(z2 - z1) + 1;
    }

    public boolean hasNext() {
        return x < sizeX && y < sizeY && z < sizeZ;
    }

    @Nonnull
    public Block next() {
        if (!hasNext()) {
            throw new NoSuchElementException("You have iterated through the whole Cuboid!");
        }

        Block b = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
        x++;

        if (x >= sizeX) {
            x = 0;
            y++;

            if (y >= sizeY) {
                y = 0;
                ++z;
            }
        }

        return b;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cuboid Iterators don't support the removal of blocks.");
    }
}