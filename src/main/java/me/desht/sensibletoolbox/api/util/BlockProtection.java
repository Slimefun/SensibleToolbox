package me.desht.sensibletoolbox.api.util;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.util.UUIDRegistry;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.SensibleToolbox;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

/**
 * Check protections in force for various vanilla blocks and inventories.
 */
public class BlockProtection {
    private BlockProtectionType blockProtectionType = BlockProtectionType.BEST;
    private InvProtectionType invProtectionType = InvProtectionType.BEST;

    public BlockProtection(SensibleToolboxPlugin plugin) {
        Configuration config = plugin.getConfig();

        try {
            setInvProtectionType(InvProtectionType.valueOf(config.getString("inventory_protection").toUpperCase()));
        } catch (IllegalArgumentException e) {
            LogUtils.warning("invalid config value for 'inventory_protection' - using BEST");
            setInvProtectionType(InvProtectionType.BEST);
        }

        try {
            setBlockProtectionType(BlockProtectionType.valueOf(config.getString("block_protection").toUpperCase()));
        } catch (IllegalArgumentException e) {
            LogUtils.warning("invalid config value for 'block_protection' - using BEST");
            setBlockProtectionType(BlockProtectionType.BEST);
        }
    }

    /**
     * Set the block protection method to be used by STB.  If a type of BEST
     * is supplied, STB will choose the best available protection method.
     *
     * @param blockProtectionType the desired protection type
     */
    public void setBlockProtectionType(BlockProtectionType blockProtectionType) {
        switch (blockProtectionType) {
            case BEST:
                if (SensibleToolbox.getPluginInstance().isWorldGuardAvailable()) {
                    this.blockProtectionType = BlockProtectionType.WORLDGUARD;
                } else if (SensibleToolbox.getPluginInstance().isPreciousStonesAvailable()) {
                    this.blockProtectionType = BlockProtectionType.PRECIOUS_STONES;
                } else {
                    this.blockProtectionType = BlockProtectionType.BUKKIT;
                }
                break;
            default:
                if (blockProtectionType.isAvailable()) {
                    this.blockProtectionType = blockProtectionType;
                } else {
                    LogUtils.warning("Block protection type " + blockProtectionType + " is not available");
                    this.blockProtectionType = BlockProtectionType.BUKKIT;
                }
                break;
        }
        Debugger.getInstance().debug("Using block protection method: " + this.blockProtectionType);
    }


    /**
     * Set the inventory protection method to be used by STB.  If a type of
     * BEST is supplied, STB will choose the best available protection method.
     *
     * @param invProtectionType the desired protection type
     */
    public void setInvProtectionType(InvProtectionType invProtectionType) {
        switch (invProtectionType) {
            case BEST:
                if (SensibleToolbox.getPluginInstance().getLWC() != null) {
                    this.invProtectionType = InvProtectionType.LWC;
                } else if (SensibleToolbox.getPluginInstance().isWorldGuardAvailable()) {
                    this.invProtectionType = InvProtectionType.WORLDGUARD;
                } else {
                    this.invProtectionType = InvProtectionType.NONE;
                }
                break;
            default:
                if (invProtectionType.isAvailable()) {
                    this.invProtectionType = invProtectionType;
                } else {
                    LogUtils.warning("Inventory protection type " + invProtectionType + " is not available");
                    this.invProtectionType = InvProtectionType.NONE;
                }
                break;
        }
        Debugger.getInstance().debug("Using inventory protection method: " + this.invProtectionType);
    }

    /**
     * Check if the given player has access rights for the given block.
     *
     * @param player player to check
     * @param block block to check
     * @return true if the player may access the block's contents
     */
    public boolean isInventoryAccessible(Player player, Block block) {
        switch (invProtectionType) {
            case LWC:
                LWC lwc = SensibleToolboxPlugin.getInstance().getLWC();
                Protection prot = lwc.findProtection(block);
                if (prot != null) {
                    boolean ok = lwc.canAccessProtection(player, prot);
                    Debugger.getInstance().debug(2, "LWC check: can " + player.getName() + " access " + block + "? " + ok);
                    return ok;
                } else {
                    return true;
                }
            case WORLDGUARD:
                ApplicableRegionSet set = WGBukkit.getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation());
                return set.allows(DefaultFlag.CHEST_ACCESS, WGBukkit.getPlugin().wrapPlayer(player));
            case BEST:
                throw new IllegalArgumentException("should never get here!");
            default:
                return true;
        }
    }

    /**
     * Check if the player with the given UUID has access rights for the given
     * block.
     * <p/>
     * Note that not all protection plugins may support checking by
     * UUID yet; if a plugin which doesn't support UUID checking is in force,
     * then this method will return false if the player is offline, even if
     * they would normally have permission.
     *
     * @param uuid UUID to check (may be null; if so, always return true)
     * @param block block to check
     * @return true if the player may access the block's contents
     */
    public boolean isInventoryAccessible(UUID uuid, Block block) {
        if (uuid == null) {
            return true;
        }
        switch (invProtectionType) {
            case LWC:
                LWC lwc = SensibleToolboxPlugin.getInstance().getLWC();
                Protection prot = lwc.findProtection(block);
                if (prot != null) {
                    boolean ok = uuid.equals(UUIDRegistry.getUUID(prot.getOwner()));
                    Debugger.getInstance().debug(2, "LWC check: can UUID " + uuid + " access " + block + "? " + ok);
                    return ok;
                } else {
                    return true;
                }
            case WORLDGUARD:
                Player player = Bukkit.getPlayer(uuid);
                return player != null && isInventoryAccessible(player, block);
            case BEST:
                throw new IllegalArgumentException("should never get here!");
            default:
                return true;
        }
    }

    /**
     * Check if the given player UUID has construction rights for the given
     * block.
     * </p>
     * Note that not all protection plugins may support checking by UUID yet;
     * if a plugin which doesn't support UUID checking is in force, then this
     * method will return false if the player is offline, even if they would
     * normally have permission.
     *
     * @param uuid the player's uuid
     * @param block the block to check for
     * @return true if the player has construction rights; false otherwise
     */
    public boolean playerCanBuild(UUID uuid, Block block, Operation op) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && playerCanBuild(player, block, op);
    }

    /**
     * Check if the given player UUID has construction rights for the given
     * block.
     *
     * @param player the player
     * @param block the block to check for
     * @return true if the player has construction rights; false otherwise
     */
    public boolean playerCanBuild(Player player, Block block, Operation op) {
        switch (blockProtectionType) {
            case WORLDGUARD:
                return WGBukkit.getPlugin().canBuild(player, block);
            case PRECIOUS_STONES:
                switch (op) {
                    case PLACE:
                        return PreciousStones.API().canPlace(player, block.getLocation());
                    case BREAK:
                        return PreciousStones.API().canBreak(player, block.getLocation());
                    default:
                        return false;
                }
            case BUKKIT:
                switch (op) {
                    case PLACE:
                        BlockPlaceEvent placeEvent = new BlockPlaceEvent(block, block.getState(), block, player.getItemInHand(), player, true);
                        Bukkit.getPluginManager().callEvent(placeEvent);
                        return !placeEvent.isCancelled();
                    case BREAK:
                        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
                        Bukkit.getPluginManager().callEvent(breakEvent);
                        return !breakEvent.isCancelled();
                    default:
                        return false;
                }
            case BEST:
                throw new IllegalArgumentException("should never get here!");
            default:
                return true;
        }
    }

    /**
     * Defines the sort of protection STB will use to check if it can place or
     * break certain blocks.
     */
    public enum BlockProtectionType {
        /**
         * Use the best protection type available: WorldGuard, Precious Stones
         * or Bukkit, in that order.
         */
        BEST,
        /**
         * Use WorldGuard to check for protected regions.  If WorldGuard is
         * not installed, BUKKIT protection is used as a fallback.
         */
        WORLDGUARD,
        /**
         * Use the PreciousStones plugin to check for protected regions.
         * If PreciousStones is not installed, BUKKIT protection is used as a
         * fallback.
         */
        PRECIOUS_STONES,
        /**
         * Fire Bukkit BlockBreakEvent/BlockPlaceEvent to check for protected
         * regions.  No extra plugin is needed for this to work, but this may
         * cause significant extra data if you use you use a block logging
         * plugin.
         */
        BUKKIT,
        /**
         * No block protection at all (recommended only for single-player
         * or highly trusted player-base).
         */
        NONE;

        public boolean isAvailable() {
            switch (this) {
                case WORLDGUARD: return SensibleToolbox.getPluginInstance().isWorldGuardAvailable();
                case PRECIOUS_STONES: return SensibleToolbox.getPluginInstance().isPreciousStonesAvailable();
                default: return true;
            }
        }
    }

    /**
     * Defines the sort of protection STB will use to check if it can access
     * certain vanilla inventories, primarily chests (but also things like
     * hoppers/droppers/dispensers etc.)
     */
    public enum InvProtectionType {
        /**
         * Use the best protection type available: LWC, WorldGuard, none, in
         * that order.
         */
        BEST,
        /**
         * Use the LWC plugin for inventory protection.  LWC must be
         * installed; if not, there will be no inventory protection.
         */
        LWC,
        /**
         * Use the WorldGuard plugin for inventory protection.  WorldGuard
         * must be installed; if not, there will be no inventory protection.
         */
        WORLDGUARD,
        /**
         * No inventory protection at all (recommended only for single-player
         * or highly trusted player-base).
         */
        NONE;

        public boolean isAvailable() {
            switch (this) {
                case WORLDGUARD: return SensibleToolbox.getPluginInstance().isWorldGuardAvailable();
                case LWC: return SensibleToolbox.getPluginInstance().getLWC() != null;
                default: return true;
            }
        }
    }

    /**
     * The operation being checked.
     */
    public enum Operation {
        /**
         * Placing a block.
         */
        PLACE,
        /**
         * Breaking a block.
         */
        BREAK,
    }
}
