package io.github.thebusybiscuit.sensibletoolbox.items.multibuilder;

import javax.annotation.Nonnull;

import org.bukkit.block.BlockFace;

enum BuildFace {

    NORTH_SOUTH(BlockFace.EAST, BlockFace.DOWN, BlockFace.WEST, BlockFace.UP),
    EAST_WEST(BlockFace.NORTH, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.UP),
    UP_DOWN(BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH);

    private final BlockFace[] faces;

    BuildFace(@Nonnull BlockFace... faces) {
        this.faces = faces;
    }

    @Nonnull
    public BlockFace[] getFaces() {
        return faces;
    }

}
