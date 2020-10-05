package me.desht.dhutils.cuboid;

import javax.annotation.Nonnull;

public enum CuboidDirection {

    NORTH,
    EAST,
    SOUTH,
    WEST,
    UP,
    DOWN,
    HORIZONTAL,
    VERTICAL,
    BOTH,
    UNKNOWN;

    @Nonnull
    public CuboidDirection getOpposite() {
        switch (this) {
        case NORTH:
            return SOUTH;
        case EAST:
            return WEST;
        case SOUTH:
            return NORTH;
        case WEST:
            return EAST;
        case HORIZONTAL:
            return VERTICAL;
        case VERTICAL:
            return HORIZONTAL;
        case UP:
            return DOWN;
        case DOWN:
            return UP;
        case BOTH:
            return BOTH;
        default:
            return UNKNOWN;
        }
    }
}