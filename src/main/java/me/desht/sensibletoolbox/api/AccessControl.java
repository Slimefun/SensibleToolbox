package me.desht.sensibletoolbox.api;

/**
 * Represents the user-based access control in force for this STB block.
 */
public enum AccessControl {
    /**
     * All players may access this block.
     */
    PUBLIC,
    /**
     * Only the block's owner may access this block.
     */
    PRIVATE,
    /**
     * Only the owner and owner's friends may access this block.  See
     * {@link me.desht.sensibletoolbox.api.FriendManager}
     */
    RESTRICTED,
}
