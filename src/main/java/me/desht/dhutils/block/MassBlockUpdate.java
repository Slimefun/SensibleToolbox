package me.desht.dhutils.block;

import java.util.concurrent.TimeUnit;

public interface MassBlockUpdate {
	public enum RelightingStrategy {
		/**
		 * Do not do any relighting calculations at all.  If any block
		 * lighting properties (i.e. light emission or light blocking)
		 * change, this may result in incorrect lighting of the changed
		 * blocks.  This strategy should be used if you are certain
		 * that no lighting properties are being changed, or if your
		 * plugin will handle relighting itself.
		 */
		NEVER,

		/**
		 * Immediately relight any blocks whose lighting properties
		 * have changed.  For very large changes (on the order of tens
		 * of thousands or more), this may result in some server lag.
		 */
		IMMEDIATE,

		/**
		 * Carry out relighting over the next several ticks, to
		 * minimise the risk of server lag.  Note that this carries
		 * a non-trivial server-side memory cost, as updated block
		 * locations need to be temporarily stored pending lighting
		 * updates.
		 */
		DEFERRED,

        /**
         * Immediately notify the client which blocks have changed.
         * Recalculate relighting in the background like DEFERRED mode.
         */
        HYBRID,
	};

	/**
	 * Make a fast block change at the given coordinates.  Clients will
	 * not see this change until {@link #notifyClients()} is called.
	 *
	 * @param x X-coordinate of the block
	 * @param y Y-coordinate of the block
	 * @param z Z-coordinate of the block
	 * @param materialId the new material ID for the block
	 * @return whether the block was actually changed
	 */
	public boolean setBlock(int x, int y, int z, int materialId);

	/**
	 * Make a fast block change at the given coordinates.  Clients will
	 * not see this change until {@link #notifyClients()} is called.
	 *
	 * @param x X-coordinate of the block
	 * @param y Y-coordinate of the block
	 * @param z Z-coordinate of the block
	 * @param materialId the new material ID for the block
	 * @param data the new block data
	 * @return whether the block was actually changed
	 */
	public boolean setBlock(int x, int y, int z, int materialId, int data);

	/**
	 * Recalculate lighting on all chunks affected by this mass block
	 * update, and resend any altered chunks to all players within
	 * viewing distance of the change.
	 */
	public void notifyClients();

	/**
	 * Set the block relighting strategy for this mass block update.
	 * The default strategy is RelightingStrategy.IMMEDIATE.
	 * 
	 * @param strategy the desired re-lighting strategy
	 */
	public void setRelightingStrategy(RelightingStrategy strategy);

	/**
	 * For relighting with RelightingStrategy.DEFERRED, specify the 
	 * maximum time the plugin should spend carrying out re-lighting
	 * per server tick.  The default is 1ms (a server tick is 50ms).
	 * This value is ignored for other relighting strategies.
	 *
	 * @param value the value in units of the given time unit
	 * @param timeUnit the time unit
	 */
	public void setMaxRelightTimePerTick(long value, TimeUnit timeUnit);

	/**
	 * For relighting with RelightingStrategy.DEFERRED, get the number
	 * of blocks that still need relighting.
	 *
	 * @return the number of blocks the need re-lighting
	 */
	public int getBlocksToRelight();
}
