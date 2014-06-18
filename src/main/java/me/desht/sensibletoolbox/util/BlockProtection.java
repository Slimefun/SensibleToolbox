package me.desht.sensibletoolbox.util;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockProtection {
    public static boolean isBlockAccessible(Player player, Block block) {
        LWC lwc = SensibleToolboxPlugin.getInstance().getLWC();
        if (lwc != null) {
            Protection prot = lwc.findProtection(block);
            System.out.println("prot = " + prot);
            System.out.println("lwc: can access " + block + " = " + lwc.canAccessProtection(player, prot));
            return lwc.canAccessProtection(player, prot);
        } else {
            return true;
        }
    }
}
