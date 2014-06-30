package me.desht.sensibletoolbox.api;

public interface EnderTunable {
    /**
     * Get the ender frequency of this tunable object.
     *
     * @return the object's current ender frequency
     */
    public int getEnderFrequency();

    /**
     * Change the ender frequency of this tunable object.
     *
     * @param frequency the new ender frequency
     */
    public void setEnderFrequency(int frequency);

    /**
     * Check if this object is on a global or personal frequency.  Global
     * frequency indicates that the inventory is common to all players;
     * personal frequency indicates that the inventory is separate for each
     * player accessing this object.
     *
     * @return true if this a global frequency; false otherwise
     */
    public boolean isGlobal();

    /**
     * Define whether this tunable object should be on a global or personal
     * frequency.
     *
     * @param global true if this should be a global frequency; false otherwise
     */
    public void setGlobal(boolean global);
}
