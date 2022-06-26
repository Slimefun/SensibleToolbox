package me.desht.dhutils.blocks;

import com.google.common.base.Preconditions;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;

/**
 * Represents the relative position of an auxiliary block from a
 * multi-block structure. The position is relative to the block's base
 * location as returned by {@link BaseSTBBlock#getLocation()} and the
 * block's orientation as returned by {@link BaseSTBBlock#getFacing()}
 * 
 * @author desht
 */
public class RelativePosition {

    private final int front;
    private final int up;
    private final int left;

    public RelativePosition(int front, int up, int left) {
        Preconditions.checkArgument(front != 0 || up != 0 || left != 0, "At least one of front, up, left must be non-zero");
        this.front = front;
        this.up = up;
        this.left = left;
    }

    /**
     * Get the distance in front of the base block.
     *
     * @return the distance in front, may be negative
     */
    public int getFront() {
        return front;
    }

    /**
     * Get the distance above of the base block.
     *
     * @return the distance above, may be negative
     */
    public int getUp() {
        return up;
    }

    /**
     * Get the distance to the left of the base block.
     *
     * @return the distance to the left, may be negative
     */
    public int getLeft() {
        return left;
    }
}