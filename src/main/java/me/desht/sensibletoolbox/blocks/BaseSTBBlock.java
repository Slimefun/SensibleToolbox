package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.PermissionUtils;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.AccessControl;
import me.desht.sensibletoolbox.api.ChargeableBlock;
import me.desht.sensibletoolbox.api.RedstoneBehaviour;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.energynet.EnergyNetManager;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.STBGUIHolder;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.RelativePosition;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.ChatPaginator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class BaseSTBBlock extends BaseSTBItem implements STBBlock {
	public static final String STB_BLOCK = "STB_Block";
	public static final String STB_MULTI_BLOCK = "STB_MultiBlock_Origin";
	private PersistableLocation persistableLocation;
	private BlockFace facing;
	private long ticksLived;
	private InventoryGUI inventoryGUI = null;
	private final STBGUIHolder guiHolder = new STBGUIHolder(this);
	private RedstoneBehaviour redstoneBehaviour;
	private AccessControl accessControl;
	private List<BlockFace> labelSigns;
	private UUID owner;

	protected BaseSTBBlock() {
		super();
		setFacing(BlockFace.SELF);
		redstoneBehaviour = RedstoneBehaviour.IGNORE;
		accessControl = AccessControl.PUBLIC;
		ticksLived = 0;
	}

	public BaseSTBBlock(ConfigurationSection conf) {
		super(conf);
		setFacing(BlockFace.valueOf(conf.getString("facing", "SELF")));
		if (conf.contains("owner")) {
			setOwner(UUID.fromString(conf.getString("owner")));
		}
		redstoneBehaviour = RedstoneBehaviour.valueOf(conf.getString("redstoneBehaviour", "IGNORE"));
		accessControl = AccessControl.valueOf(conf.getString("accessControl", "PUBLIC"));
		ticksLived = 0;
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		if (getOwner() != null) {
			conf.set("owner", getOwner().toString());
		}
		conf.set("facing", getFacing().toString());
		conf.set("redstoneBehaviour", getRedstoneBehaviour().toString());
		conf.set("accessControl", getAccessControl().toString());
		return conf;
	}

	@Override
	public RedstoneBehaviour getRedstoneBehaviour() {
		return redstoneBehaviour;
	}

	@Override
	public void setRedstoneBehaviour(RedstoneBehaviour redstoneBehaviour) {
		this.redstoneBehaviour = redstoneBehaviour;
		updateBlock(false);
	}

	@Override
	public AccessControl getAccessControl() {
		return accessControl;
	}

	@Override
	public void setAccessControl(AccessControl accessControl) {
		this.accessControl = accessControl;
		updateBlock(false);
	}

	public STBGUIHolder getGuiHolder() {
		return guiHolder;
	}

	@Override
	public InventoryGUI getGUI() {
		return inventoryGUI;
	}

	protected void setGUI(InventoryGUI inventoryGUI) {
		this.inventoryGUI = inventoryGUI;
	}

	@Override
	public BlockFace getFacing() {
		return facing;
	}

	@Override
	public void setFacing(BlockFace facing) {
		this.facing = facing;
	}

	@Override
	public UUID getOwner() {
		return owner;
	}

	@Override
	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	protected long getTicksLived() {
		return ticksLived;
	}

	@Override
	public boolean hasAccessRights(Player player) {
		switch (getAccessControl()) {
			case PUBLIC: return true;
			case PRIVATE: return getOwner().equals(player.getUniqueId()) || PermissionUtils.isAllowedTo(player, "stb.access.any");
			default: return false;
		}
	}

	@Override
	public boolean hasAccessRights(UUID uuid) {
		switch (getAccessControl()) {
			case PUBLIC: return true;
			case PRIVATE: return uuid == null || getOwner().equals(uuid);
			default: return false;
		}
	}

	/**
	 * Check if this block is active based on its redstone behaviour settings and the presence
	 * or absence of a redstone signal.
	 *
	 * @return true if the block is active, false otherwise
	 */
	protected boolean isRedstoneActive() {
		switch (getRedstoneBehaviour()) {
			case IGNORE: return true;
			case HIGH: return getLocation().getBlock().isBlockIndirectlyPowered();
			case LOW: return !getLocation().getBlock().isBlockIndirectlyPowered();
			default: return false;
		}
	}

	/**
	 * Called when an STB block receives a damage event.  The default behaviour is to ignore
	 * the event.
	 *
	 * @param event the block damage event
	 */
	public void onBlockDamage(BlockDamageEvent event) { }

	/**
	 * Called when an STB block receives a physics event.  The default behaviour is to ignore
	 * the event.
	 *
	 * @param event the block physics event
	 */
	public void onBlockPhysics(BlockPhysicsEvent event) { }

	/**
	 * Called when an STB block is interacted with by a player.  The default behaviour allows
	 * for the block to be labelled by left-clicking it with a sign in hand.  If you override
	 * this method and want to keep this behaviour, be sure to call super.onInteractBlock()
	 *
	 * @param event the interaction event
	 */
	public void onInteractBlock(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == Material.SIGN
				&& event.getClickedBlock().getType() != Material.WALL_SIGN && event.getClickedBlock().getType() != Material.SIGN_POST ) {
			// attach a label sign
			attachLabelSign(event);
			event.setCancelled(true);
		}
	}

	/**
	 * Called when a sign attached to an STB block is updated.  The default behaviour is to ignore
	 * the event.
	 *
	 * @param event the sign change event
	 * @return true if the sign should be popped off the block
	 */
	public boolean onSignChange(SignChangeEvent event) {
		return false;
	}

	/**
	 * Called when this STB block has been hit by an explosion.  The default behaviour is to return
	 * true; STB blocks will break and drop their item form if hit by an explosion.
	 *
	 * @param event the explosion event
	 * @return true if the explosion should cause the block to break, false otherwise
	 */
	public boolean onEntityExplode(EntityExplodeEvent event) {
		return true;
	}

	/**
	 * Get a list of extra blocks this STB block has.  By default this returns an empty list,
	 * but multi-block structures should override this.  Each element of the list is a vector
	 * containing a relative offset from the item's base location.
	 *
	 * @return an array of relative offsets for extra blocks in the item
	 */
	@Override
	public RelativePosition[] getBlockStructure() { return new RelativePosition[0]; }

	/**
	 * Called every tick for each STB block that is placed in the world, for any STB block where
	 * shouldTick() returns true.  If you override this, you MUST call the superclass method in your
	 * overridden method!
	 */
	public void onServerTick() {
		ticksLived++;
	}

	/**
	 * Called when the chunk that an STB block is in gets loaded.
	 */
	public void onChunkLoad() { }

	/**
	 * Called when the chunk that an STB block is in gets unloaded.
	 */
	public void onChunkUnload() { }

	/**
	 * Check if this block needs to tick, i.e. have onServerTick() called every tick.  Only override
	 * this to return true for block which need to tick, for performance reasons.
	 *
	 * @return true if the block should tick
	 */
	public boolean shouldTick() {
		return false;
	}

	@Override
	public Location getLocation() {
		return persistableLocation == null ? null : persistableLocation.getLocation();
	}

	@Override
	public Location getRelativeLocation(BlockFace face) {
		return getLocation().add(face.getModX(), face.getModY(), face.getModZ());
	}

	@Override
	public PersistableLocation getPersistableLocation() {
		return persistableLocation;
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
			persistableLocation = new PersistableLocation(loc);
			for (RelativePosition pos : getBlockStructure()) {
				Block b1 = getMultiBlock(loc, pos);
				Debugger.getInstance().debug(2, "multiblock for " + this + " -> " + b1);
				b1.setMetadata(STB_MULTI_BLOCK, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), this));
			}
			labelSigns = findAttachedLabelSigns();
			setGUI(createGUI());
		} else {
			if (persistableLocation != null) {
				Location l = getLocation();
				for (RelativePosition pos : getBlockStructure()) {
					Block b1 = getMultiBlock(l, pos);
					b1.removeMetadata(STB_MULTI_BLOCK, SensibleToolboxPlugin.getInstance());
				}
			}
			persistableLocation = null;
			setGUI(null);
		}
	}

	private Block getMultiBlock(Location loc, RelativePosition pos) {
		Block b = loc.getBlock();
		int dx = 0, dz = 0;
		switch (getFacing()) {
			case NORTH: dz = -pos.getFront(); dx = -pos.getLeft(); break;
			case SOUTH: dz = pos.getFront(); dx = pos.getLeft(); break;
			case EAST: dz = -pos.getLeft(); dx = pos.getFront(); break;
			case WEST: dz = pos.getLeft(); dx = -pos.getFront(); break;
		}
		return b.getRelative(dx, pos.getUp(), dz);
	}

	@Override
	public void moveTo(Location oldLoc, Location newLoc) {
		for (RelativePosition pos : getBlockStructure()) {
			Block b1 = getMultiBlock(oldLoc, pos);
			b1.removeMetadata(STB_MULTI_BLOCK, SensibleToolboxPlugin.getInstance());
		}
		if (this instanceof ChargeableBlock) {
			EnergyNetManager.onMachineRemoved((ChargeableBlock) this);
		}
		persistableLocation = new PersistableLocation(newLoc);
		for (RelativePosition pos : getBlockStructure()) {
			Block b1 = getMultiBlock(newLoc, pos);
			Debugger.getInstance().debug(2, "multiblock for " + this + " -> " + b1);
			b1.setMetadata(STB_MULTI_BLOCK, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), this));
		}
		if (this instanceof ChargeableBlock) {
			EnergyNetManager.onMachinePlaced((ChargeableBlock) this);
		}
	}

	/**
	 * Called when an STB block is placed.  Subclasses may override this method, but should take care
	 * to call the superclass method.
	 *
	 * @param event the block place event
	 */
	public void onBlockPlace(BlockPlaceEvent event) {
		Location baseLoc = event.getBlock().getLocation();
		for (RelativePosition rPos : getBlockStructure()) {
			Block b = getMultiBlock(baseLoc, rPos);
			if (b.getType() != Material.AIR && b.getType() != Material.WATER && b.getType() != Material.STATIONARY_WATER) {
				MiscUtil.errorMessage(event.getPlayer(), "Not enough room to place this multi-block structure");
				event.setCancelled(true);
				return;
			}
		}
		placeBlock(event.getBlock(), event.getPlayer(), STBUtil.getFaceFromYaw(event.getPlayer().getLocation().getYaw()).getOppositeFace());
	}

	/**
	 * Called when an STB block is actually broken (the event handler runs with MONITOR
	 * priority).
	 *
	 * Subclasses may override this method, but should take care to call the superclass method.
	 *
	 * @param event the block break event
	 */
	public void onBlockBreak(BlockBreakEvent event) {
		breakBlock(event.getBlock());
	}

	protected void placeBlock(Block b, Player p, BlockFace facing) {
		setFacing(facing);
		setOwner(p.getUniqueId());
		LocationManager.getManager().registerLocation(b.getLocation(), this, true);
	}

	public void breakBlock(Block b) {
		Location baseLoc = this.getLocation();
		Block origin = baseLoc.getBlock();
		origin.setType(Material.AIR);
		for (RelativePosition pos : getBlockStructure()) {
			Block b1 = getMultiBlock(baseLoc, pos);
			b1.setType(Material.AIR);
		}
		LocationManager.getManager().unregisterLocation(baseLoc, this);
		b.getWorld().dropItemNaturally(b.getLocation(), toItemStack());
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

	@Override
	public void updateBlock(boolean redraw) {
		Location loc = getLocation();
		if (loc != null) {
			if (redraw) {
				Block b = loc.getBlock();
				// maybe one day Bukkit will have a block set method which takes a MaterialData
				b.setTypeIdAndData(getMaterial().getId(), getMaterialData().getData(), true);
			}
			LocationManager.getManager().updateLocation(loc, this);
		}
	}

	protected void attachLabelSign(PlayerInteractEvent event) {
		Block signBlock = event.getClickedBlock().getRelative(event.getBlockFace());

		if (!signBlock.isEmpty()) {
			// looks like some non-solid block is in front of the STB block we're attaching to
			BlockBreakEvent breakEvent = new BlockBreakEvent(signBlock, event.getPlayer());
			Bukkit.getPluginManager().callEvent(breakEvent);
			if (breakEvent.isCancelled()) {
				return;
			}
			signBlock.breakNaturally();
		}

		BlockPlaceEvent placeEvent = new BlockPlaceEvent(signBlock, signBlock.getState(), event.getClickedBlock(), event.getItem(), event.getPlayer(), true);
		Bukkit.getPluginManager().callEvent(placeEvent);
		if (placeEvent.isCancelled()) {
			return;
		}

		// ok, player is allowed to put a sign here
		signBlock.setType(event.getBlockFace() == BlockFace.UP ? Material.SIGN_POST : Material.WALL_SIGN);
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
		stack.setAmount(stack.getAmount() - 1);
		event.getPlayer().setItemInHand(stack.getAmount() <= 0 ? null : stack);
		labelSigns.add(event.getBlockFace());
	}

	protected String[] getSignLabel() {
		String[] lines = ChatPaginator.wordWrap(makeItemLabel(), 13);
		String[] res = new String[4];
		for (int i = 0; i < 4; i++) {
			res[i] = i < lines.length ? lines[i] : "";
		}
		return res;
	}

	@Override
	public String toString() {
		return "STB " + getItemName() + " @ " +
				(getLocation() == null ? "(null)" : MiscUtil.formatLocation(getLocation()));
	}

	private List<BlockFace> findAttachedLabelSigns() {
		List<BlockFace> res = new LinkedList<BlockFace>();
		if (getLocation() == null) {
			return res;
		}
		Block b = getLocation().getBlock();
		for (BlockFace face : new BlockFace[] { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP } ) {
			Block b1 = b.getRelative(face);
			if ((b1.getType() == Material.WALL_SIGN && face.getModY() == 0) || (b1.getType() == Material.SIGN_POST && face.getModY() == 1)) {
				Sign sign = (Sign) b.getRelative(face).getState();
				org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
				if (s.getAttachedFace() == face.getOppositeFace()) {
					res.add(face);
				}
			}
		}
		return res;
	}

	protected void updateAttachedLabelSigns() {
		if (labelSigns == null || labelSigns.isEmpty()) {
			return;
		}
		String[] text = getSignLabel();
		Block b = getLocation().getBlock();
		Iterator<BlockFace> iter = labelSigns.iterator();
		while (iter.hasNext()) {
			BlockFace face = iter.next();
			Block b1 = b.getRelative(face);
			if ((b1.getType() == Material.WALL_SIGN && face.getModY() == 0) || (b1.getType() == Material.SIGN_POST && face.getModY() == 1)) {
				Sign sign = (Sign) b1.getState();
				for (int i = 0; i < text.length; i++) {
					sign.setLine(i, text[i]);
				}
				sign.update();
			} else {
				// not a label sign (the sign must have been replaced or broken at some point)
				iter.remove();
			}
		}
	}

	protected String makeItemLabel() {
		return ChatColor.DARK_BLUE + "\u25a0" + getItemName();
	}

	/**
	 * Temporarily override the item display name, just before the item is placed.  The item
	 * display name is used as the inventory title for blocks such as the dropper.
	 *
	 * @param event the block place event
	 */
	protected void setInventoryTitle(BlockPlaceEvent event, final String tempTitle) {
		ItemStack inHand = event.getItemInHand();
		final Player player = event.getPlayer();
		ItemMeta meta = inHand.getItemMeta();
		meta.setDisplayName(tempTitle);
		inHand.setItemMeta(meta);
		if (inHand.getAmount() > 1) {
			// any remaining items need to have their proper title restored
			Bukkit.getScheduler().runTask(SensibleToolboxPlugin.getInstance(), new Runnable() {
				@Override
				public void run() {
					ItemStack inHand = player.getItemInHand();
					if (inHand.getType() == getMaterial()) {
						ItemMeta meta = inHand.getItemMeta();
						if (meta.getDisplayName().equals(tempTitle)) {
							player.setItemInHand(toItemStack(inHand.getAmount()));
						}
					}
				}
			});
		}
	}

	/**
	 * Builds the inventory-based GUI for this block.  Override in subclasses.
	 *
	 * @return the GUI object (may be null if this block doesn't have a GUI)
	 */
	protected InventoryGUI createGUI() {
		return null;
	}

	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		return PistonMoveReaction.MOVE;
	}

}
