package me.desht.sensibletoolbox.api;

/**
 * Represents an STB block which cares about the light level.
 *
 * @deprecated use {@link me.desht.sensibletoolbox.api.LightMeterHolder}
 */
@Deprecated
public interface LightSensitive {
    /**
     * Get the current ambient light level.
     *
     * @return the light level
     */
    public byte getLightLevel();

    /**
     * Get the slot in the block's GUI where a light meter can be displayed.
     *
     * @return a slot number
     */
    public int getLightMeterSlot();
}
