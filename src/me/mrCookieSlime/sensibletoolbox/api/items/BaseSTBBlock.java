package me.mrCookieSlime.sensibletoolbox.api.items;

import java.util.BitSet;
import java.util.UUID;

import me.desht.sensibletoolbox.dhutils.PersistableLocation;
import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.api.AccessControl;
import me.mrCookieSlime.sensibletoolbox.api.RedstoneBehaviour;
import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;
import me.mrCookieSlime.sensibletoolbox.api.energy.ChargeableBlock;
import me.mrCookieSlime.sensibletoolbox.api.gui.InventoryGUI;
import me.mrCookieSlime.sensibletoolbox.api.gui.STBGUIHolder;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.core.storage.LocationManager;
import me.mrCookieSlime.sensibletoolbox.util.UnicodeSymbol;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.ChatPaginator;

/**
 * Represents an STB block; an STB item which can be placed as a block in the
 * world.
 */
public abstract class BaseSTBBlock extends BaseSTBItem {
	
    public static final String STB_BLOCK = "STB_Block";
    public static final String STB_MULTI_BLOCK = "STB_MultiBlock_Origin";
    private boolean needToScanSigns;
    private PersistableLocation persistableLocation;
    private BlockFace facing;
    private long ticksLived;
    private InventoryGUI inventoryGUI = null;
    private final STBGUIHolder guiHolder = new STBGUIHolder(this);
    private RedstoneBehaviour redstoneBehaviour;
    private AccessControl accessControl;
    private final BitSet labelSigns = new BitSet(4);
    private UUID owner;
    private int lastPower;
    private boolean pulsing;
    private boolean pendingRemoval;

    protected BaseSTBBlock() {
        super();
        setFacing(BlockFace.SELF);
        redstoneBehaviour = SensibleToolbox.getPluginInstance().getConfigCache().getDefaultRedstone();
        accessControl = SensibleToolbox.getPluginInstance().getConfigCache().getDefaultAccess();
        ticksLived = 0;
        needToScanSigns = false;
    }

    protected BaseSTBBlock(ConfigurationSection conf) {
        super(conf);
        setFacing(BlockFace.valueOf(conf.getString("facing", "SELF")));
        if (conf.contains("owner")) {
            setOwner(UUID.fromString(conf.getString("owner")));
        }
        redstoneBehaviour = RedstoneBehaviour.valueOf(conf.getString("redstoneBehaviour", "IGNORE"));
        accessControl = AccessControl.valueOf(conf.getString("accessControl", "PUBLIC"));
        ticksLived = 0;
        needToScanSigns = !conf.contains("labels");  // coming from pre-v0.0.4
        byte faces = (byte) conf.getInt("labels", 0);
        labelSigns.or(BitSet.valueOf(new byte[] { faces }));
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
        conf.set("labels", labelSigns.isEmpty() ? 0 : labelSigns.toByteArray()[0]);
        return conf;
    }

    /**
     * Get this block's current redstone behaviour; whether it ignores the presence of a redstone signal,
     * or requires the signal to be low or high to operate.
     *
     * @return the redstone behaviour
     */
    public final RedstoneBehaviour getRedstoneBehaviour() {
        return redstoneBehaviour;
    }

    /**
     * Set this block's redstone behaviour; whether it ignores the presence of a redstone signal,
     * or requires the signal to be low or high to operate.
     *
     * @param redstoneBehaviour the new desired redstone behaviour
     */
    public final void setRedstoneBehaviour(RedstoneBehaviour redstoneBehaviour) {
        this.redstoneBehaviour = redstoneBehaviour;
        update(false);
    }

    /**
     * Get this block's current access control; whether players other than the owner may access the
     * block's GUI or insert/extract items from it.
     *
     * @return the access control setting
     */
    public final AccessControl getAccessControl() {
        return accessControl;
    }

    /**
     * Set this block's access control; whether players other than the owner may access the
     * block's GUI or insert/extract items from it.
     *
     * @param accessControl the new desired access control
     */
    public final void setAccessControl(AccessControl accessControl) {
        this.accessControl = accessControl;
        update(false);
    }

    /**
     * You should not need to call this method directly; it is used to link the item's
     * inventory GUI with the item itself.
     *
     * @return the item's GUI holder object
     */
    public final STBGUIHolder getGuiHolder() {
        return guiHolder;
    }

    /**
     * Get the GUI object for this block, if any has been created.
     *
     * @return the block's GUI (may be null)
     */
    public final InventoryGUI getGUI() {
        return inventoryGUI;
    }

    private void setGUI(InventoryGUI inventoryGUI) {
        this.inventoryGUI = inventoryGUI;
    }

    /**
     * Get the direction that this block faces.  Note that the block may not
     * necessarily have any visual representation of a facing direction.
     * <p/>
     * An STB block's initial facing direction is determined by the
     * direction that the player who placed it was facing at the time.
     *
     * @return the facing direction of the block, or SELF if the block has not been placed
     */
    public final BlockFace getFacing() {
        return facing;
    }

    /**
     * Set the direction that this block faces.  Note that the block may not
     * necessarily have any visual representation of a facing direction, but
     * label signs can be useful indicator.
     *
     * @param facing the desired new facing direction
     */
    public final void setFacing(BlockFace facing) {
        this.facing = facing;
    }

    /**
     * Get the UUID of the player who owns this block.  The initial owner of
     * an STB block is the player who placed it.
     *
     * @return the owning player's UUID, or null if the block has not been placed yet
     */
    public final UUID getOwner() {
        return owner;
    }

    /**
     * Set the UUID of the owning player
     *
     * @param owner the new owning player's UUID
     */
    public final void setOwner(UUID owner) {
        this.owner = owner;
    }

    /**
     * Get the number of ticks since this block was placed in the world.  This
     * will return 0 if called on a block which has not yet been placed, or
     * on a block whose {@link #getTickRate()} method returns 0.
     *
     * @return the number of ticks lived
     */
    public final long getTicksLived() {
        return ticksLived;
    }

    /**
     * Check if this block may be interacted with by the given player.  If the
     * player has the permission node "stb.access.any" then this method will
     * always return true.
     *
     * @param player the player to check
     * @return true if the block may be accessed
     */
    public final boolean hasAccessRights(Player player) {
        switch (getAccessControl()) {
            case PUBLIC:
                return true;
            case PRIVATE:
                return getOwner().equals(player.getUniqueId())
                        || player.hasPermission("stb.access.any");
            case RESTRICTED:
                return getOwner().equals(player.getUniqueId())
                        || player.hasPermission("stb.access.any")
                        || SensibleToolbox.getFriendManager().isFriend(getOwner(), player.getUniqueId());
            default:
                return false;
        }
    }

    /**
     * Check if this block may be interacted with by the player of the given
     * UUID, based on its current security settings.  Note that no player
     * permission check is done here; see
     * {@link BaseSTBItem#checkPlayerPermission(org.bukkit.entity.Player, me.desht.sensibletoolbox.api.items.BaseSTBItem.ItemAction)}.
     *
     * @param uuid the UUID to check
     * @return true if the block may be accessed
     */
    public final boolean hasAccessRights(UUID uuid) {
        switch (getAccessControl()) {
            case PUBLIC:
                return true;
            case PRIVATE:
                return uuid == null || getOwner().equals(uuid);
            case RESTRICTED:
                return uuid == null || getOwner().equals(uuid)
                        || SensibleToolbox.getFriendManager().isFriend(getOwner(), uuid);
            default:
                return false;
        }
    }

    /**
     * Check if this block is active based on its redstone behaviour settings and the presence
     * or absence of a redstone signal.
     *
     * @return true if the block is active, false otherwise
     */
    public final boolean isRedstoneActive() {
        switch (getRedstoneBehaviour()) {
            case IGNORE:
                return true;
            case HIGH:
                return getLocation().getBlock().isBlockIndirectlyPowered();
            case LOW:
                return !getLocation().getBlock().isBlockIndirectlyPowered();
            case PULSED:
                return pulsing;
            default:
                return false;
        }
    }

    /**
     * Called when an STB block receives a damage event.  The default
     * behaviour is to ignore the event.
     *
     * @param event the block damage event
     */
    public void onBlockDamage(BlockDamageEvent event) {
    }

    /**
     * Called when an STB block receives a physics event, i.e. a neighbouring
     * block has changed state in some way.  The default behaviour is to
     * ignore the event.
     *
     * @param event the block physics event
     */
    public void onBlockPhysics(BlockPhysicsEvent event) {
    }

    /**
     * Called when the redstone power being supplied to an STB block changes.
     *
     * @param oldPower the block's current power level
     * @param newPower the block's new power level
     */
    public void onRedstonePowerChanged(int oldPower, int newPower) {
    }

    /**
     * Don't call this method directly; override
     * {@link #onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent)}
     * instead.
     *
     * @param event the the block physics event
     */
    public final void handlePhysicsEvent(BlockPhysicsEvent event) {
        int power = event.getBlock().getBlockPower();
        if (power != lastPower) {
            onRedstonePowerChanged(lastPower, power);
            if (lastPower == 0 && power > 0 && getRedstoneBehaviour() == RedstoneBehaviour.PULSED) {
                pulsing = true;
                onServerTick();
                pulsing = false;
            }
            lastPower = power;
        }
        onBlockPhysics(event);
    }

    /**
     * Called when an STB block is interacted with by a player.  The default
     * behaviour allows for the block to be labelled by left-clicking it with
     * a sign in hand.  If you override this method and want to keep this
     * behaviour, be sure to call super.onInteractBlock().
     * <p/>
     * Note that cancelling this event will also prevent the block from being
     * broken by normal means.
     *
     * @param event the interaction event
     */
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SIGN
                && event.getClickedBlock().getType() != Material.WALL_SIGN && event.getClickedBlock().getType() != Material.SIGN_POST) {
            // attach a label sign
            if (attachLabelSign(event)) {
                labelSigns.set(STBUtil.getFaceRotation(getFacing(), event.getBlockFace()));
            }
            event.setCancelled(true);
        }
    }

    /**
     * Called when a sign attached to an STB block is updated.  The default
     * behaviour is to ignore the event.
     *
     * @param event the sign change event
     * @return true if the sign should be popped off the block
     */
    public boolean onSignChange(SignChangeEvent event) {
        return false;
    }

    /**
     * Called when this STB block has been hit by an explosion.  The default
     * behaviour is to return true; STB blocks will break and drop their item
     * form if hit by an explosion.
     *
     * @param event the explosion event
     * @return true if the explosion should cause the block to break, false otherwise
     */
    public boolean onEntityExplode(EntityExplodeEvent event) {
        return true;
    }

    /**
     * Get a list of extra blocks this STB block has.  By default this returns
     * an empty list, but multi-block structures should override this.  Each
     * element of the list is a vector containing a relative offset from the
     * item's base location.
     *
     * @return an array of relative offsets for extra blocks in the item
     */
    public RelativePosition[] getBlockStructure() {
        return new RelativePosition[0];
    }

    /**
     * This method should not be called directly.  It is automatically called
     * every tick for every block placed in the world for which
     * {@link #getTickRate()} returns a non-zero value.
     */
    public final void tick() {
        ticksLived++;
    }

    /**
     * Called every tick for each STB block that is placed in the world, for
     * any STB block where {@link #getTickRate()} returns a non-zero value.
     * Override this method to define any periodic behaviour of the block.
     */
    public void onServerTick() {
    }

    /**
     * Defines the rate at which the block ticks. {@link #onServerTick()} will
     * be called this frequently.  Override this method to have the block
     * tick at the desired frequency.  The default rate of 0 means that the
     * block will not tick at all.
     */
    public int getTickRate() {
        return 0;
    }

//    /**
//     * Called when the chunk that an STB block is in gets loaded.
//     */
//    public void onChunkLoad() {
//    }
//
//    /**
//     * Called when the chunk that an STB block is in gets unloaded.
//     */
//    public void onChunkUnload() {
//    }

    /**
     * Called when an STB block has completely burned away.  This is called
     * with EventPriority.MONITOR; do not attempt to cancel this event.
     *
     * @param event the block burn event
     */
    public void onBlockBurnt(BlockBurnEvent event) {
    }

    /**
     * Get the location of the base block of this STB block.  This could be
     * null if called on an STB Block object which has not yet been placed in
     * the world (i.e. in item form).
     *
     * @return the base block location
     */
    public final Location getLocation() {
        return persistableLocation == null ? null : persistableLocation.getLocation();
    }

    /**
     * Get the location relative to this block in the given direction.
     *
     * @param face the direction
     * @return the relative location
     */
    public final Location getRelativeLocation(BlockFace face) {
        return getLocation().add(face.getModX(), face.getModY(), face.getModZ());
    }

    /**
     * Get a persistable location for the base block of this STB block.  This
     * could be null if called on an STB Block object which has not yet been
     * placed in the world (i.e. in item form).
     *
     * @return the base block location
     */
    public final PersistableLocation getPersistableLocation() {
        return persistableLocation;
    }

    /**
     * This method should not be called directly; it is implicitly called when
     * a block is placed, either by player or by the
     * {@link #placeBlock(org.bukkit.block.Block, org.bukkit.entity.Player,
     * org.bukkit.block.BlockFace)} method.
     *
     * @param loc the base block location
     * @throws IllegalArgumentException if the new location is null or the block's current location is non-null
     */
    public final void setLocation(LocationManager.BlockAccess blockAccess, Location loc) {
        Validate.notNull(blockAccess, "Don't call this method directly");
        Validate.notNull(loc, "Location must not be null");
        Validate.isTrue(persistableLocation == null, "Attempt to change the location of existing STB block @ " + persistableLocation);
        persistableLocation = new PersistableLocation(loc);
    }

    public Block getAuxiliaryBlock(RelativePosition pos) {
        return getAuxiliaryBlock(getLocation(), pos);
    }

    public Block getAuxiliaryBlock(Location loc, RelativePosition pos) {
        Block b = loc.getBlock();
        int dx = 0, dz = 0;
        switch (getFacing()) {
            case NORTH:
                dz = -pos.getFront(); dx = -pos.getLeft();
                break;
            case SOUTH:
                dz = pos.getFront(); dx = pos.getLeft();
                break;
            case EAST:
                dz = -pos.getLeft(); dx = pos.getFront();
                break;
            case WEST:
                dz = pos.getLeft(); dx = -pos.getFront();
                break;
		default:
			break;
        }
        return b.getRelative(dx, pos.getUp(), dz);
    }

    /**
     * Don't call this method directly.
     *
     * @param oldLoc the block's previous location
     * @param newLoc the block's new location
     */
    public final void moveTo(LocationManager.BlockAccess blockAccess, final Location oldLoc, final Location newLoc) {
        Validate.notNull(blockAccess, "Don't call this method directly");

        oldLoc.getBlock().removeMetadata(BaseSTBBlock.STB_BLOCK, SensibleToolbox.getPluginInstance());
        for (RelativePosition pos : getBlockStructure()) {
            Block auxBlock = getAuxiliaryBlock(oldLoc, pos);
            auxBlock.removeMetadata(STB_MULTI_BLOCK, SensibleToolboxPlugin.getInstance());
        }
        if (this instanceof ChargeableBlock) {
            SensibleToolboxPlugin.getInstance().getEnergyNetManager().onMachineRemoved((ChargeableBlock) this);
        }

        persistableLocation = new PersistableLocation(newLoc);

        newLoc.getBlock().setMetadata(BaseSTBBlock.STB_BLOCK, new FixedMetadataValue(SensibleToolbox.getPluginInstance(), this));
        for (RelativePosition pos : getBlockStructure()) {
            Block auxBlock = getAuxiliaryBlock(newLoc, pos);
            auxBlock.setMetadata(STB_MULTI_BLOCK, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), this));
        }
        if (this instanceof ChargeableBlock) {
            SensibleToolboxPlugin.getInstance().getEnergyNetManager().onMachinePlaced((ChargeableBlock) this);
        }

        Bukkit.getScheduler().runTask(SensibleToolboxPlugin.getInstance(), new Runnable() {
            public void run() {
                Block b = oldLoc.getBlock();
                for (int rotation = 0; rotation < 4; rotation++) {
                    if (labelSigns.get(rotation)) {
                        Block signBlock = b.getRelative(STBUtil.getRotatedFace(getFacing(), rotation));
                        if (signBlock.getType() == Material.WALL_SIGN) {
                            signBlock.setType(Material.AIR);
                        }
                    }
                }
            }
        });

        Bukkit.getScheduler().runTaskLater(SensibleToolboxPlugin.getInstance(), new Runnable() {
            public void run() {
                Block b = newLoc.getBlock();
                for (int rotation = 0; rotation < 4; rotation++) {
                    if (labelSigns.get(rotation)) {
                        BlockFace face = STBUtil.getRotatedFace(getFacing(), rotation);
                        Block signBlock = b.getRelative(face);
                        if (!placeLabelSign(signBlock, face)) {
                            labelSigns.set(rotation, false);
                        }
                    }
                }
            }
        }, 2L);
    }

    /**
     * Don't call this method directly; it is implicitly called when a block
     * is registered with STB.  Override {@link #onBlockRegistered(org.bukkit.Location, boolean)}
     * if you need to run a specific task on registration.
     *
     * @param location the location where the block has been registered
     * @param isPlacing if true, this is being called because the block is
     *                  being placed by a player; if false, because the block
     *                  is being restored from persisted data
     */
    public final void preRegister(LocationManager.BlockAccess blockAccess, Location location, boolean isPlacing) {
        Validate.notNull(blockAccess, "Don't call this method directly");
        location.getBlock().setMetadata(BaseSTBBlock.STB_BLOCK, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), this));
        for (RelativePosition pos : getBlockStructure()) {
            Block auxBlock = getAuxiliaryBlock(location, pos);
            auxBlock.setMetadata(STB_MULTI_BLOCK, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), this));
        }

        setGUI(createGUI());

        // defer this so it's done after the block is actually placed in the world
        Bukkit.getScheduler().runTask(SensibleToolboxPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                lastPower = getLocation().getBlock().getBlockPower();
            }
        });

        if (needToScanSigns) {
            scanForAttachedLabelSigns();
            needToScanSigns = false;
        }

        onBlockRegistered(location, isPlacing);
    }

    /**
     * Called when an STB block has been broken and is being unregistered. The
     * block's location and GUI (if any) are still valid at this point, but
     * all physical blocks have been set to AIR.  Use this method to perform
     * any block-specific shutdown tasks, e.g. drop items from inventory.
     *
     * @param location location of the base (primary) block of this STB block
     */
    public void onBlockUnregistered(Location location) {
    }

    /**
     * Called when an STB block has been registered with the location manager
     * after being placed.  The physical block has not necessarily been drawn
     * in the world at this point, but {@link #getLocation()} will return the
     * block's location.
     *
     * @param location the location where the block has been registered
     * @param isPlacing if true, this is being called because the block is
     *                  being placed by a player; if false, because the block
     *                  is being restored from persisted data
     */
    public void onBlockRegistered(Location location, boolean isPlacing) {
    }

    /**
     * Do the basic initialisation for a newly-placed STB block.  This method
     * does not actually place the physical block in the world.  This is
     * automatically called when a block is placed by a player, but it can be
     * directly if a block should be placed by some other means.
     *
     * @param block the world block where this STB block is being placed
     * @param player the player placing the block
     * @param facing the direction that the block should face
     */
    public final void placeBlock(final Block block, Player player, BlockFace facing) {
        setFacing(facing);
        setOwner(player.getUniqueId());

        Location loc = block.getLocation();

        // register the block (preRegister()/onBlockRegister() will be called from here)
        LocationManager.getManager().registerLocation(loc, this, true);

        reattachLabelSigns(loc);
    }

    /**
     * Cause this STB block to break, removing it, any auxiliary blocks and
     * any attached label signs from the world.  This is automatically called
     * when a block is broken by a player, but can be called directly if the
     * block should be broken by some other means.
     *
     * @param dropItem if true, drop the block's item form as a collectable
     *                 item
     */
    public final void breakBlock(boolean dropItem) {
        Location baseLoc = this.getLocation();
        Block origin = baseLoc.getBlock();

        pendingRemoval = true;

        // remove any attached label signs
        scanForAttachedLabelSigns();
        for (int rotation = 0; rotation < 4; rotation++) {
            if (labelSigns.get(rotation)) {
                origin.getRelative(STBUtil.getRotatedFace(getFacing(), rotation)).setType(Material.AIR);
            }
        }

        // clear block metadata and set block(s) to AIR
        for (RelativePosition pos : getBlockStructure()) {
            Block auxBlock = getAuxiliaryBlock(baseLoc, pos);
            auxBlock.removeMetadata(STB_MULTI_BLOCK, SensibleToolboxPlugin.getInstance());
            auxBlock.setType(Material.AIR);
        }
        origin.removeMetadata(BaseSTBBlock.STB_BLOCK, SensibleToolboxPlugin.getInstance());
        origin.setType(Material.AIR);

        // unregister the block (onBlockUnregister() will be called from here)
        LocationManager.getManager().unregisterLocation(baseLoc, this);

        setGUI(null);
        setFacing(BlockFace.SELF);
        setOwner(null);

        if (dropItem) {
            origin.getWorld().dropItemNaturally(baseLoc, toItemStack());
        }
    }

    /**
     * Validate that this STB block (which may be a multi-block structure) is
     * placeable at the given location.
     *
     * @param baseLoc the location of the STB block's base block
     * @return true if the STB block is placeable; false otherwise
     */
    public final boolean validatePlaceable(Location baseLoc) {
        for (RelativePosition rPos : getBlockStructure()) {
            Block b = getAuxiliaryBlock(baseLoc, rPos);
            if (b.getType() != Material.AIR && b.getType() != Material.WATER && b.getType() != Material.STATIONARY_WATER) {
                return false;
            }
        }
        return true;
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

    /**
     * Mark this block as having been modified and thus in need of saving.
     * This should be called whenever a serializable field of a block is
     * modified to ensure that the change is saved to persistent storage.
     *<p>
     * This method is a no-op if the block is not actually placed in the
     * world, i.e. it's a item in an inventory
     *
     * @param redraw if true, also redraw the block in the world
     */
    public final void update(boolean redraw) {
        Location loc = getLocation();
        if (loc != null) {
            if (redraw) {
                repaint(loc.getBlock());
            }
            LocationManager.getManager().updateLocation(loc);
        }
    }

    /**
     * Called when a block needs to be repainted due to some state change. If
     * you override this method (to repaint auxiliary blocks), be sure to call
     * the superclass method.
     *
     * @param block the base (primary) block of this STB block
     */
    @SuppressWarnings("deprecation")
	public void repaint(Block block) {
        // maybe one day Bukkit will have a block set method which takes a MaterialData
        block.setTypeIdAndData(getMaterialData().getItemTypeId(), getMaterialData().getData(), true);
    }

    /**
     * Define whether this STB block can be pushed or pulled by a piston, and if
     * doing so would break it.  The default behaviour is to allow movement;
     * override this in subclasses to modify the behaviour.
     *
     * @return the move reaction: one of MOVE, BLOCK, or BREAK
     */
    public PistonMoveReaction getPistonMoveReaction() {
        return PistonMoveReaction.MOVE;
    }

    /**
     * Define whether this block supports the given redstone behaviour mode.
     *
     * @param behaviour the mode to check
     * @return true if the block supports this behaviour; false otherwise
     */
    public boolean supportsRedstoneBehaviour(RedstoneBehaviour behaviour) {
        return true;
    }

    /**
     * Builds the inventory-based GUI for this block.  Override in subclasses.
     *
     * @return the GUI object (may be null if this block doesn't have a GUI)
     */
    protected InventoryGUI createGUI() {
        return null;
    }

    /**
     * Define whether this block is flammable.  This can be used to override the
     * flammability of blocks, i.e. to make an STB block non-flammable even
     * if its base material is flammable.  The default behaviour is to return
     * false; i.e. STB blocks are <em>not</em> flammable by default.
     *
     * @return true if the block should be flammable; false otherwise
     */
    public boolean isFlammable() {
        return false;
    }

    /**
     * Check if this block is due to be removed.  This will be set to true by
     * the {@link #breakBlock(boolean)} method to indicate that the block will
     * be removed by the end of this server tick.
     *
     * @return true if the block is due to be removed; false otherwise
     */
    public boolean isPendingRemoval() {
        return pendingRemoval;
    }

    @Override
    public String toString() {
        return "STB " + getItemName() + " @ " +
                (getLocation() == null ? "(null)" : formatLocation(getLocation()));
    }

    private String formatLocation(Location loc)
    {
      return String.format("%d,%d,%d,%s", new Object[] { Integer.valueOf(loc.getBlockX()), Integer.valueOf(loc.getBlockY()), Integer.valueOf(loc.getBlockZ()), loc.getWorld().getName() });
    }

	/**
     * Get the label text for the block's label sign.  The default behaviour
     * is simply to put the block's item name on the first line of the sign;
     * override this in subclasses if you want a customised sign label.
     *
     * @param face the face to which the sign is attached
     * @return a 4-line string array of sign label text
     */
    protected String[] getSignLabel(BlockFace face) {
        String[] lines = ChatPaginator.wordWrap(makeItemLabel(face), 13);
        String[] res = new String[4];
        for (int i = 0; i < 4; i++) {
            res[i] = i < lines.length ? lines[i] : "";
        }
        return res;
    }

    /**
     * Updates any attached label signs.  Call this method when some data that
     * is represented on a label sign is changed.
     */
    protected void updateAttachedLabelSigns() {
        Location loc = getLocation();
        if (loc == null || labelSigns == null || labelSigns.isEmpty()) {
            return;
        }
        Block b = loc.getBlock();
        for (int rotation = 0; rotation < 4; rotation++) {
            if (!labelSigns.get(rotation)) {
                continue;
            }
            BlockFace face = STBUtil.getRotatedFace(getFacing(), rotation);
            String[] text = getSignLabel(face);
            Block b1 = b.getRelative(face);
            if (b1.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) b1.getState();
                for (int i = 0; i < text.length; i++) {
                    sign.setLine(i, text[i]);
                }
                sign.update();
            } else {
                // no sign here (the sign must have been replaced or broken at some point)
                labelSigns.set(rotation, false);
            }
        }
    }

    /**
     * Check if this block is actually placed, or currently in item form.
     *
     * @return true if this object is a placed STB block; false if it's in item form
     */
    public final boolean isPlaced() {
        return persistableLocation != null;
    }

    private void reattachLabelSigns(Location loc) {
        Block b = loc.getBlock();
        boolean rescanNeeded = false;
        for (int rotation = 0; rotation < 4; rotation++) {
            if (labelSigns.get(rotation)) {
                BlockFace face = STBUtil.getRotatedFace(getFacing(), rotation);
                if (!placeLabelSign(b.getRelative(face), face)) {
                    rescanNeeded = true;
                }
            }
        }
        if (rescanNeeded) {
            // not all of the signs could be placed (something in the way?)
            scanForAttachedLabelSigns();
            update(false);
        }
    }

    @SuppressWarnings("deprecation")
	private boolean attachLabelSign(PlayerInteractEvent event) {
        if (event.getBlockFace().getModY() != 0) {
            // only support placing a label sign on the side of a machine, not the top
            event.setCancelled(true);
            return false;
        }
        Block signBlock = event.getClickedBlock().getRelative(event.getBlockFace());
        Player player = event.getPlayer();

        if (!signBlock.isEmpty()) {
            // looks like some non-solid block is in the way
            STBUtil.complain(player, "There is no room to place a label sign there!");
            event.setCancelled(true);
            return false;
        }

        BlockPlaceEvent placeEvent = new BlockPlaceEvent(signBlock, signBlock.getState(), event.getClickedBlock(), event.getItem(), player, true);
        Bukkit.getPluginManager().callEvent(placeEvent);
        if (placeEvent.isCancelled()) {
            STBUtil.complain(player);
            return false;
        }

        // ok, player is allowed to put a sign here
        placeLabelSign(signBlock, event.getBlockFace());

        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack stack = player.getInventory().getItemInMainHand();
            stack.setAmount(stack.getAmount() - 1);
            player.setItemInHand(stack.getAmount() <= 0 ? null : stack);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);

        return true;
    }

    @SuppressWarnings("deprecation")
	private boolean placeLabelSign(Block signBlock, BlockFace face) {
        if (!signBlock.isEmpty() && signBlock.getType() != Material.WALL_SIGN) {
            // something in the way!
            signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(Material.SIGN));
            return false;
        } else {
            // using setTypeIdAndData() here because we don't want to cause a physics update
            signBlock.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte) 0, false);
            Sign sign = (Sign) signBlock.getState();
            org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
            s.setFacingDirection(face);
            sign.setData(s);
            String[] text = getSignLabel(face);
            for (int i = 0; i < text.length; i++) {
                sign.setLine(i, text[i]);
            }
            sign.update(false, false);
            return true;
        }
    }

    private void scanForAttachedLabelSigns() {
        labelSigns.clear();
        if (getLocation() == null) {
            return;
        }
        Block b = getLocation().getBlock();
        for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH}) {
            Block b1 = b.getRelative(face);
            if (b1.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) b.getRelative(face).getState();
                org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
                if (s.getAttachedFace() == face.getOppositeFace()) {
                    labelSigns.set(STBUtil.getFaceRotation(getFacing(), face));
                }
            }
        }
    }

    /**
     * Don't call this method directly; it is automatically called when a
     * label sign attached the block is broken.
     *
     * @param face the face to which the sign was attached
     */
    public void detachLabelSign(BlockFace face) {
        labelSigns.set(STBUtil.getFaceRotation(getFacing(), face), false);
        update(false);
    }

    private static final UnicodeSymbol[] faceSymbols = {UnicodeSymbol.SQUARE, UnicodeSymbol.ARROW_RIGHT, UnicodeSymbol.ARROW_DOWN, UnicodeSymbol.ARROW_LEFT};

    private String makeItemLabel(BlockFace face) {
        int rotation = STBUtil.getFaceRotation(getFacing(), face);
        return rotation == 1 ?
                ChatColor.DARK_BLUE + getItemName() + faceSymbols[rotation].toUnicode() :
                ChatColor.DARK_BLUE + faceSymbols[rotation].toUnicode() + getItemName();
    }

    /**
     * Represents the relative position of an auxiliary block from a
     * multi-block structure.  The position is relative to the block's base
     * location as returned by {@link BaseSTBBlock#getLocation()} and the
     * block's orientation as returned by {@link BaseSTBBlock#getFacing()}
     */
    public class RelativePosition {
        private final int front, up, left;

        public RelativePosition(int front, int up, int left) {
            Validate.isTrue(front != 0 || up != 0 || left != 0, "At least one of front, up, left must be non-zero");
            this.front = front;
            this.up = up;
            this.left = left;
        }

        /**
         * Get the distance in front of the base block.
         *
         * @return the distance in front, may be negative
         */
        public int getFront() {
            return front;
        }

        /**
         * Get the distance above of the base block.
         *
         * @return the distance above, may be negative
         */
        public int getUp() {
            return up;
        }

        /**
         * Get the distance to the left of the base block.
         *
         * @return the distance to the left, may be negative
         */
        public int getLeft() {
            return left;
        }
    }
}
