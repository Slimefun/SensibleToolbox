package me.desht.sensibletoolbox.api.util;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.util.UUIDRegistry;
import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
    public static boolean isBlockAccessible(Player player, Block block) {
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
    public static boolean isBlockAccessible(UUID uuid, Block block) {
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

}
