package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.storage.BlockPosition;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.RelativePosition;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

public abstract class BaseSTBBlock extends BaseSTBItem {
	public static final String STB_MULTI_BLOCK = "STB_MultiBlock_Origin";
	private PersistableLocation persistableLocation;
	private BlockFace facing;

	protected BaseSTBBlock() {
	}

//	protected static Configuration getConfigFromMap(Map<String, Object> map) {
//		Configuration conf = new MemoryConfiguration();
//		for (Map.Entry<String,Object> e : map.entrySet()) {
//			conf.set(e.getKey(), e.getValue());
//		}
//		return conf;
//	}

	public BaseSTBBlock(ConfigurationSection conf) {
		setFacing(BlockFace.valueOf(conf.getString("facing", "SELF")));
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("facing", facing == null ? "SELF" : facing.toString());
		return conf;
	}

	public BlockFace getFacing() {
		return facing;
	}

	public void setFacing(BlockFace facing) {
		this.facing = facing;
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
	public RelativePosition[] getBlockStructure() { return new RelativePosition[0]; }

	/**
	 * Called every tick for each STB block that is placed in the world.
	 */
	public void onServerTick() { }

	/**
	 * Get the location of the base block of this STB block.  This could be null if called
	 * on an STB Block object which has not yet been placed in the world (i.e. in item form).
	 *
	 * @return the base block location
	 */
	public Location getLocation() {
		return persistableLocation == null ? null : persistableLocation.getLocation();
	}

	/**
	 * Set the location of the base block of this STB block.  This should only be called when the
	 * block is first placed, or when deserialized.
	 *
	 * @param loc the base block location
	 * @throws IllegalStateException if the caller attempts to set a non-null location when the object already has a location set
	 */
	public void setLocation(Location loc) {
		if (loc != null) {
			if (persistableLocation != null && !loc.equals(persistableLocation.getLocation())) {
				throw new IllegalStateException("Attempt to change the location of existing STB block @ " + persistableLocation);
			}
			Block origin = loc.getBlock();
			persistableLocation = new PersistableLocation(loc);
			BlockPosition pos0 = new BlockPosition(origin.getLocation());
			for (RelativePosition pos : getBlockStructure()) {
				Block b1 = getMultiBlock(pos);
				System.out.println("multiblock for " + this + " -> " + b1);
				b1.setMetadata(STB_MULTI_BLOCK, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), pos0));
			}
		} else {
			if (persistableLocation != null) {
				Block origin = persistableLocation.getBlock();
				for (RelativePosition pos : getBlockStructure()) {
					Block b1 = getMultiBlock(pos);
					b1.removeMetadata(STB_MULTI_BLOCK, SensibleToolboxPlugin.getInstance());
				}
			}
			persistableLocation = null;
		}
	}

	protected Block getMultiBlock(RelativePosition pos) {
		if (getLocation() == null) {
			return null;
		}
		Block b = getLocation().getBlock();
		int dx = 0, dz = 0;
		switch (getFacing()) {
			case NORTH: dz = -pos.getFront(); dx = -pos.getLeft(); break;
			case SOUTH: dz = pos.getFront(); dx = pos.getLeft(); break;
			case EAST: dz = -pos.getLeft(); dx = pos.getFront(); break;
			case WEST: dz = pos.getLeft(); dx = -pos.getFront(); break;
		}
		return b.getRelative(dx, pos.getUp(), dz);
	}

	/**
	 * Called when an STB block is placed.  Subclasses may override this method, but should take care
	 * to call the superclass method.
	 *
	 * @param event the block place event
	 */
	public void handleBlockPlace(BlockPlaceEvent event) {
		Block origin = event.getBlock();
		setFacing(STBUtil.getFaceFromYaw(event.getPlayer().getLocation().getYaw()).getOppositeFace());
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
		Block origin = getLocation().getBlock();
		origin.setType(Material.AIR);
		for (RelativePosition pos : getBlockStructure()) {
			Block b = getMultiBlock(pos);
			b.setType(Material.AIR);
		}
		blockRemoved(getLocation());
		event.setCancelled(true);
	}

	protected void blockPlaced(Location loc) {
		LocationManager.getManager().registerLocation(loc, this);
	}

	protected void blockRemoved(Location loc) {
		LocationManager.getManager().unregisterLocation(loc);
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
		if (getLocation() != null) {
			Block b = getLocation().getBlock();
			b.setTypeIdAndData(getBaseMaterial().getId(), getBaseBlockData(), true);
			LocationManager.getManager().registerLocation(getLocation(), this);
		}
	}

	protected void attachLabelSign(PlayerInteractEvent event) {
		Block signBlock = event.getClickedBlock().getRelative(event.getBlockFace());
		signBlock.setTypeIdAndData(event.getBlockFace() == BlockFace.UP ? Material.SIGN_POST.getId() : Material.WALL_SIGN.getId(), (byte) 0, false);
		Sign sign = (Sign) signBlock.getState();
		org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
		if (event.getBlockFace() == BlockFace.UP) {
			s.setFacingDirection(STBUtil.getFaceFromYaw(event.getPlayer().getLocation().getYaw()).getOppositeFace());
		} else {
			s.setFacingDirection(event.getBlockFace());
		}
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
