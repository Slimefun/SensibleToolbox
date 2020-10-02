package io.github.thebusybiscuit.sensibletoolbox.api.enderstorage;

/**
 * Represents an STB item or block which has an ender frequency
 * including global/personal visibility.
 * 
 * @author desht
 */
public interface EnderTunable {

    /**
     * Get the ender frequency of this tunable object.
     *
     * @return the object's current ender frequency
     */
    int getEnderFrequency();

    /**
     * Change the ender frequency of this tunable object.
     *
     * @param frequency
     *            the new ender frequency
     */
    void setEnderFrequency(int frequency);

    /**
     * Check if this object is on a global or personal frequency. Global
     * frequency indicates that the inventory is common to all players;
     * personal frequency indicates that the inventory is separate for each
     * player accessing this object.
     *
     * @return true if this a global frequency; false otherwise
     */
    boolean isGlobal();

    /**
     * Define whether this tunable object should be on a global or personal
     * frequency.
     *
     * @param global
     *            true if this should be a global frequency; false otherwise
     */
    void setGlobal(boolean global);
}
