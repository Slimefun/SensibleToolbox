package io.github.thebusybiscuit.sensibletoolbox.core.storage;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.bukkit.Location;

/**
 * Stores the X,Y,Z position of a block - no world information
 * 
 * @author desht
 */
public class BlockPosition {

    private static final Pattern STRING_PATTERN = Pattern.compile(",");

    private final int x;
    private final int y;
    private final int z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPosition(Location loc) {
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlockPosition that = (BlockPosition) o;

        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Nonnull
    public static BlockPosition fromString(@Nonnull String s) {
        String[] f = STRING_PATTERN.split(s);
        return new BlockPosition(Integer.parseInt(f[0]), Integer.parseInt(f[1]), Integer.parseInt(f[2]));
    }

    @Override
    public String toString() {
        return getX() + "," + getY() + "," + getZ();
    }
}
