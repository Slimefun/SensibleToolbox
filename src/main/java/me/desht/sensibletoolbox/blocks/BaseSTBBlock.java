package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Map;

public abstract class BaseSTBBlock extends BaseSTBItem {
	private PersistableLocation persistableLocation;

	/**
	 * Called when an STB block receives a damage event.  Override this in implementing
	 * subclasses.
	 * @param event the block damage event
	 */
	public void handleBlockDamage(BlockDamageEvent event) { }

	/**
	 * Called when an STB block receives a physics event.  Override this in implementing
	 * subclasses.
	 * @param event the block physics event
	 */
	public void handleBlockPhysics(BlockPhysicsEvent event) { }

	/**
	 * Called when an STB block is interacted with by a player
	 *
	 * @param event the interaction event
	 */
	public void handleBlockInteraction(PlayerInteractEvent event) {	}

	/**
	 * Called when a sign attached to an STB block is updated.  Override this in implementing
	 * subclasses.
	 *
	 * @param event the sign change event
	 */
	public boolean handleSignConfigure(SignChangeEvent event) { return false; }

	/**
	 * Get a list of extra blocks this STB block has.  By default this returns an empty list,
	 * but multi-block structures should override this.  Each element of the list is a vector
	 * containing a relative offset from the item's base location.
	 *
	 * @return a list of offset vectors for extra blocks in the item
	 */
	public Vector[] getBlockStructure() { return new Vector[0]; }

	/**
	 * Called every tick for each STB block that is placed in the world.
	 *
	 * @param pLoc the location of the STB block
	 */
	public void onServerTick(PersistableLocation pLoc) { }

	/**
	 * Get the location of the base block of this STB block.
	 *
	 * @return the base block location
	 */
	public Location getBaseLocation() {
		return persistableLocation.getLocation();
	}

	/**
	 * Set the location of the base block of this STB block.  This should only be called when the
	 * block is first placed, or when deserialized.
	 *
	 * @param baseLocation the base block location
	 */
	public void setBaseLocation(Location baseLocation) {
		persistableLocation = new PersistableLocation(baseLocation);
	}

	/**
	 * Called when an STB block is placed.  Subclasses may override this method, but should take care
	 * to call the superclass method.
	 *
	 * @param event the block place event
	 */
	public void handleBlockPlace(BlockPlaceEvent event) {
		Block origin = event.getBlock();
		blockPlaced(origin);
		for (Vector v : getBlockStructure()) {
			Block b1 = origin.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ());
			b1.setMetadata(STB_MULTI_BLOCK,
					new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), origin.getLocation().toVector()));
		}
	}

	/**
	 * Called when an STB block is broken.  Subclasses may override this method, but should take care
	 * to call the superclass method.
	 *
	 * @param event the block break event
	 */
	public void handleBlockBreak(BlockBreakEvent event) {
		Block origin = getBaseLocation().getBlock();
		Block brokenBlock = event.getBlock();
		brokenBlock.getWorld().dropItemNaturally(brokenBlock.getLocation(), toItemStack(1));
		blockRemoved(origin);
		origin.setType(Material.AIR);

		for (Vector v : getBlockStructure()) {
			Block b = origin.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ());
			b.setType(Material.AIR);
			b.removeMetadata(STB_MULTI_BLOCK, SensibleToolboxPlugin.getInstance());
		}
		event.setCancelled(true);
	}

	protected void blockPlaced(Block b) {
		setBaseLocation(b.getLocation());
		SensibleToolboxPlugin.getInstance().getLocationManager().registerLocation(b.getLocation(), this);
	}

	protected void blockRemoved(Block b) {
		SensibleToolboxPlugin.getInstance().getLocationManager().unregisterLocation(b.getLocation(), this);
	}

	public void updateBlock() {
		if (getBaseLocation() != null) {
			Block b = getBaseLocation().getBlock();
			b.setTypeIdAndData(getBaseMaterial().getId(), getBaseBlockData(), true);
			SensibleToolboxPlugin.getInstance().getLocationManager().updateLocation(getBaseLocation(), this);
		}
	}

	protected static Configuration getConfigFromMap(Map<String, Object> map) {
		Configuration conf = new MemoryConfiguration();
		for (Map.Entry e : map.entrySet()) {
			System.out.println("GCFM: " + e.getKey() + " = " + e.getValue());
			conf.set((String) e.getKey(), e.getValue());
		}
		return conf;
	}
}
