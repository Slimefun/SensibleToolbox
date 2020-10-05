package me.desht.dhutils.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * A simple data class for storing a {@link Block}, a {@link BlockFace} and a {@link Vector}.
 * 
 * @author desht
 *
 */
public class BlockAndPosition {

    public final Block block;
    public final BlockFace face;
    public final Vector point;

    public BlockAndPosition(@Nonnull Block block, @Nonnull BlockFace face, @Nullable Vector point) {
        this.block = block;
        this.face = face;
        this.point = point;
    }
}