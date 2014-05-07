package me.desht.sensibletoolbox.listeners;

import com.google.common.io.Files;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BagOfHolding;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class BagOfHoldingListener extends STBBaseListener {
    public BagOfHoldingListener(SensibleToolboxPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void bagOfHoldingClosed(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player && event.getInventory().getTitle().equals(new BagOfHolding().getInventoryTitle())) {
            Player player = (Player) event.getPlayer();
            String encoded = BukkitSerialization.toBase64(event.getInventory());
            File file = BagOfHolding.getSaveFile(player);
            try {
                Files.write(encoded, file, Charset.forName("UTF-8"));
                Debugger.getInstance().debug("saved bag of holding to " + file);
            } catch (IOException e) {
                MiscUtil.errorMessage(player, "Can't save bag contents! " + e.getMessage());
            }
        }
    }

}
