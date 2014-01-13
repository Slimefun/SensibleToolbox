package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Map;

public abstract class BaseSTBBlock extends BaseSTBItem {
	public static final String STB_MULTI_BLOCK = "STB_MultiBlock_Origin";
	private PersistableLocation persistableLocation;

	protected static Configuration getConfigFromMap(Map<String, Object> map) {
		Configuration conf = new MemoryConfiguration();
		for (Map.Entry e : map.entrySet()) {
//			System.out.println("GCFM: " + e.getKey() + " = " + e.getValue());
			conf.set((String) e.getKey(), e.getValue());
		}
		return conf;
	}

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
	public BlockPosition[] getBlockStructure() { return new BlockPosition[0]; }

	/**
	 * Called every tick for each STB block that is placed in the world.
	 *
	 * @param pLoc the location of the STB block
	 */
	public void onServerTick(Location pLoc) { }

	/**
	 * Get the location of the base block of this STB block.
	 *
	 * @return the base block location
	 */
	public Location getBaseLocation() {
		return persistableLocation == null ? null : persistableLocation.getLocation();
	}

	/**
	 * Set the location of the base block of this STB block.  This should only be called when the
	 * block is first placed, or when deserialized.
	 *
	 * @param baseLocation the base block location
	 */
	public void setBaseLocation(Location baseLocation) {
		if (baseLocation != null) {
			Block origin = baseLocation.getBlock();
			persistableLocation = new PersistableLocation(baseLocation);
			BlockPosition pos0 = new BlockPosition(origin.getLocation());
			for (BlockPosition pos : getBlockStructure()) {
				Block b1 = origin.getRelative(pos.getX(), pos.getY(), pos.getZ());
				b1.setMetadata(STB_MULTI_BLOCK, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), pos0));
			}
		} else {
			if (persistableLocation != null) {
				Block origin = persistableLocation.getBlock();
				for (BlockPosition pos : getBlockStructure()) {
					Block b1 = origin.getRelative(pos.getX(), pos.getY(), pos.getZ());
					b1.removeMetadata(STB_MULTI_BLOCK, SensibleToolboxPlugin.getInstance());
				}
			}
			persistableLocation = null;
		}
	}

	/**
	 * Called when an STB block is placed.  Subclasses may override this method, but should take care
	 * to call the superclass method.
	 *
	 * @param event the block place event
	 */
	public void handleBlockPlace(BlockPlaceEvent event) {
		Block origin = event.getBlock();
		blockPlaced(origin.getLocation());
	}

	/**
	 * Called when an STB block is broken.  Subclasses may override this method, but should take care
	 * to call the superclass method.
	 *
	 * @param event the block break event
	 */
	public void handleBlockBreak(BlockBreakEvent event) {
		Block brokenBlock = event.getBlock();
		brokenBlock.getWorld().dropItemNaturally(brokenBlock.getLocation(), toItemStack(1));
		System.out.println("broken!");
		Block origin = getBaseLocation().getBlock();
		origin.setType(Material.AIR);
		blockRemoved(getBaseLocation());
		event.setCancelled(true);
		for (BlockPosition pos : getBlockStructure()) {
			Block b = origin.getRelative(pos.getX(), pos.getY(), pos.getZ());
			b.setType(Material.AIR);
		}
	}

	protected void blockPlaced(Location loc) {
		setBaseLocation(loc);
		SensibleToolboxPlugin.getInstance().getLocationManager().registerLocation(loc, this);
	}

	protected void blockRemoved(Location loc) {
		setBaseLocation(null);
		SensibleToolboxPlugin.getInstance().getLocationManager().unregisterLocation(loc, this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BaseSTBBlock that = (BaseSTBBlock) o;

		if (persistableLocation != null ? !persistableLocation.equals(that.persistableLocation) : that.persistableLocation != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return persistableLocation != null ? persistableLocation.hashCode() : 0;
	}

	public void updateBlock() {
		if (getBaseLocation() != null) {
			Block b = getBaseLocation().getBlock();
			b.setTypeIdAndData(getBaseMaterial().getId(), getBaseBlockData(), true);
			SensibleToolboxPlugin.getInstance().getLocationManager().updateLocation(getBaseLocation(), this);
		}
	}

	protected void attachLabelSign(PlayerInteractEvent event) {
		Block signBlock = event.getClickedBlock().getRelative(event.getBlockFace());
		signBlock.setTypeIdAndData(event.getBlockFace() == BlockFace.UP ? Material.SIGN_POST.getId() : Material.WALL_SIGN.getId(), (byte) 0, false);
		Sign sign = (Sign) signBlock.getState();
		org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
		s.setFacingDirection(event.getBlockFace());
		sign.setData(s);
		String[] text = getSignLabel();
		for (int i = 0; i < text.length; i++) {
			sign.setLine(i, text[i]);
		}
		sign.update();
		ItemStack stack = event.getPlayer().getItemInHand();
		if (stack.getAmount() > 1) {
			stack.setAmount(stack.getAmount() - 1);
			event.getPlayer().setItemInHand(stack);
		} else {
			event.getPlayer().setItemInHand(null);
		}
	}

	protected String[] getSignLabel() {
		return new String[] { getItemName(), "", "", "" };
	}
}
