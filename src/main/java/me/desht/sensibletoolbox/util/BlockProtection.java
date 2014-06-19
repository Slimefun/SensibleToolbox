package me.desht.sensibletoolbox.util;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.util.UUIDRegistry;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

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
                System.out.println("prot = " + prot);
                System.out.println("prot owner = " + prot.getOwner());
                System.out.println("lwc: can access " + block + " = " + lwc.canAccessProtection(player, prot));
                return lwc.canAccessProtection(player, prot);
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
                System.out.println("prot = " + prot);
                System.out.println("prot uuid = " + UUIDRegistry.getUUID(prot.getOwner()));
                return uuid.equals(UUIDRegistry.getUUID(prot.getOwner()));
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

}
