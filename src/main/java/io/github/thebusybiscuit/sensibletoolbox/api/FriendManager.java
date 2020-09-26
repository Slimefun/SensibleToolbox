package io.github.thebusybiscuit.sensibletoolbox.api;

import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * An interface to the STB friend system; managing the trust between
 * any two given players. This is primarily useful for STB blocks which
 * use the <em>Restricted</em> access control level - see
 * {@link io.github.thebusybiscuit.sensibletoolbox.api.AccessControl}
 * 
 * @author desht
 */
public interface FriendManager {

    /**
     * Add the player with ID id2 as a friend of the player with ID id1; this
     * means id1 will trust id2 as a friend. This friendship is not
     * commutative; id1 trusting id2 does not automatically mean that id2
     * trusts id1.
     *
     * @param id1
     *            ID of the player who will do the trusting
     * @param id2
     *            ID of the player being trusted as a friend
     */
    void addFriend(@Nonnull UUID id1, @Nonnull UUID id2);

    /**
     * Remove the player with ID id2 as a friend of the player with ID id1;
     * this means id1 will no longer trust id2 as a friend.
     *
     * @param id1
     *            ID of the player who will no longer do the trusting
     * @param id2
     *            ID of the player no longer being trusted as a friend
     */
    void removeFriend(@Nonnull UUID id1, @Nonnull UUID id2);

    /**
     * Check if id2 is a friend of id1, i.e. if id1 trusts id2 as a friend.
     *
     * @param id1
     *            ID of the player to check
     * @param id2
     *            ID of the possible friend
     * 
     * @return true if id2 is trusted by id1; false otherwise
     */
    boolean isFriend(@Nonnull UUID id1, @Nonnull UUID id2);

    /**
     * Get all the friends for the given player; the ID's of those players
     * who are trusted by the player.
     *
     * @param id
     *            ID of the player to check
     * 
     * @return a set of UUID's of all the players trusted by the given player
     */
    @Nonnull
    Set<UUID> getFriends(@Nonnull UUID id);
}
