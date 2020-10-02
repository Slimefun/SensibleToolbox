package io.github.thebusybiscuit.sensibletoolbox.api;

/**
 * Represents how a STB block reacts to the presence or absence of a redstone
 * signal. The precise nature of the block's reaction is dependent on the
 * block itself, and not all behaviours are supported by all blocks.
 * 
 * @author desht
 */
public enum RedstoneBehaviour {

    /**
     * Ignore the presence or absence of a redstone signal.
     */
    IGNORE,

    /**
     * Only operate when the block is powered by a signal.
     */
    HIGH,

    /**
     * Only operate when the block is not powered by a signal.
     */
    LOW,

    /**
     * Carry out one operation when a redstone pulse is detected.
     */
    PULSED,
}
