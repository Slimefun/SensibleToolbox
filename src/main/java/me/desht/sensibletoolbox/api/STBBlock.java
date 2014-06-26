package me.desht.sensibletoolbox.api;

import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.util.RelativePosition;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface STBBlock extends STBItem {
    /**
     * Get this block's current redstone behaviour; whether it ignores the presence of a redstone signal,
     * or requires the signal to be low or high to operate.
     *
     * @return the redstone behaviour
     */
    public RedstoneBehaviour getRedstoneBehaviour();

    /**
     * Set this block's redstone behaviour; whether it ignores the presence of a redstone signal,
     * or requires the signal to be low or high to operate.
     *
     * @param redstoneBehaviour the new desired redstone behaviour
     */
    void setRedstoneBehaviour(RedstoneBehaviour redstoneBehaviour);

    /**
     * Get this block's current access control; whether players other than the owner may access the
     * block's GUI or insert/extract items from it.
     *
     * @return the access control setting
     */
    public AccessControl getAccessControl();

    /**
     * Set this block's access control; whether players other than the owner may access the
     * block's GUI or insert/extract items from it.
     *
     * @param accessControl the new desired access control
     */
    void setAccessControl(AccessControl accessControl);

    /**
     * Get the GUI object for this block, if any.
     *
     * @return the block's GUI (may be null)
     */
    public InventoryGUI getGUI();

    /**
     * Get the direction that this block faces.  Note that the block may not necessarily have any
     * visual representation of a facing direction.  A block's facing direction is determined by the
     * direction that the player who placed was facing at the time.
     *
     * @return the facing direction of the block
     */
    public BlockFace getFacing();

    /**
     * Set the direction that this block faces.  Note that the block may not necessarily have any
     * visual representation of a facing direction.
     *
     * @param facing the desired new facing direction
     */
    void setFacing(BlockFace facing);

    /**
     * Get the UUID of the owning player
     *
     * @return the owning player's UUID
     */
    public UUID getOwner();

    /**
     * Set the UUID of the owning player
     *
     * @param owner the new owning player's UUID
     */
    void setOwner(UUID owner);

    /**
     * Get the structure for a multi-block STB block.
     *
     * @return an array of relative block positions, the origin being the block that is placed by the player
     */
    public RelativePosition[] getBlockStructure();

    /**
     * Get the location of the base block of this STB block.  This could be null if called
     * on an STB Block object which has not yet been placed in the world (i.e. in item form).
     *
     * @return the base block location
     */
    public Location getLocation();

    /**
     * Get a persistable location for the base block of this STB block.  This could be null if called
     * on an STB Block object which has not yet been placed in the world (i.e. in item form).
     *
     * @return the base block location
     */
    public PersistableLocation getPersistableLocation();

    void moveTo(Location oldLoc, Location newLoc);

    void updateBlock(boolean redraw);

    /**
     * Check if this STB block can be pushed or pulled by a piston, and if doing so would break it.
     * The default behaviour is to allow movement; override this in subclasses to modify the behaviour.
     *
     * @return the move reaction: one of MOVE, BLOCK, or BREAK
     */
    public PistonMoveReaction getPistonMoveReaction();

    /**
     * Check if this block may be interacted with by the given player, based on its
     * current security settings, and the player's permissions.  If the player has
     * the permission node "stb.access.any" then this method will always return true.
     *
     * @param player the player to check
     * @return true if the block may be accessed
     */
    public boolean hasAccessRights(Player player);

    /**
     * Check if this block may be interacted with by the player of the given UUID, based
     * on its current security settings.  Note that no player permission check is done here.
     *
     * @param uuid the UUID to check
     * @return true if the block may be accessed
     */
    public boolean hasAccessRights(UUID uuid);

    /**
     * Get the location relative to this block in the given direction.
     *
     * @param face the direction
     * @return the relative location
     */
    public Location getRelativeLocation(BlockFace face);
}
