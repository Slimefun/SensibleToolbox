package me.desht.sensibletoolbox.api.util;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.util.UUIDRegistry;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

/**
 * Check protections in force for various vanilla blocks and inventories.
 *
 */
public class BlockProtection {
    /**
     * Check if the given player has access rights for the given block.
     *
     * @param player player to check
     * @param block block to check
     * @return true if the player may access the block's contents
     */
    public static boolean isInventoryAccessible(Player player, Block block) {
        LWC lwc = SensibleToolboxPlugin.getInstance().getLWC();
        if (lwc != null) {
            Protection prot = lwc.findProtection(block);
            if (prot != null) {
                boolean ok = lwc.canAccessProtection(player, prot);
                Debugger.getInstance().debug(2, "LWC check: can " + player.getName() + " access " + block + "? " + ok);
                return ok;
            } else {
                return true;
            }
        } else if (SensibleToolboxPlugin.getInstance().isWorldGuardAvailable()) {
            ApplicableRegionSet set = WGBukkit.getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation());
            return set.allows(DefaultFlag.CHEST_ACCESS, WGBukkit.getPlugin().wrapPlayer(player));
        } else {
            return true;
        }
    }

    /**
     * Check if the player with the given UUID has access rights for the given block.
     *
     * @param uuid UUID to check (may be null; if so, always return true)
     * @param block block to check
     * @return true if the player may access the block's contents
     */
    public static boolean isInventoryAccessible(UUID uuid, Block block) {
        if (uuid == null) {
            return true;
        }
        LWC lwc = SensibleToolboxPlugin.getInstance().getLWC();
        if (lwc != null) {
            Protection prot = lwc.findProtection(block);
            if (prot != null) {
                boolean ok = uuid.equals(UUIDRegistry.getUUID(prot.getOwner()));
                Debugger.getInstance().debug(2, "LWC check: can UUID " + uuid + " access " + block + "? " + ok);
                return ok;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * Check if the given player UUID has construction rights for the given
     * block.
     * </p>
     * If the UUID is for an offline player, false will be returned; this may
     * change in future as protection plugins offer improved UUID support.
     *
     * @param uuid the player's uuid
     * @param block the block to check for
     * @return true if the player has construction rights; false otherwise
     */
    public static boolean playerCanBuild(UUID uuid, Block block, Operation op) {
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
    public static boolean playerCanBuild(Player player, Block block, Operation op) {
        if (SensibleToolboxPlugin.getInstance().isWorldGuardAvailable()) {
            return WGBukkit.getPlugin().canBuild(player, block);
        } else if (SensibleToolboxPlugin.getInstance().isPreciousStonesAvailable()) {
            switch (op) {
                case PLACE:
                    return PreciousStones.API().canPlace(player, block.getLocation());
                case BREAK:
                    return PreciousStones.API().canBreak(player, block.getLocation());
                default:
                    return false;
            }
        } else {
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
