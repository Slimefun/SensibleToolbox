package me.desht.sensibletoolbox.listeners;

import com.google.common.io.Files;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BagOfHolding;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class BagListener extends STBBaseListener {
	public BagListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player && event.getInventory().getTitle().equals(new BagOfHolding().getInventoryTitle())) {
			Player player = (Player) event.getPlayer();
			String encoded = BukkitSerialization.toBase64(event.getInventory());
//			System.out.println("save: encoded = " + encoded);
			File file = BagOfHolding.getSaveFile(player);
			System.out.println("saving bag of holding to " + file);
			try {
				Files.write(encoded, file, Charset.forName("UTF-8"));
			} catch (IOException e) {
				MiscUtil.errorMessage(player, "Can't save bag contents!");
			}
		}
	}
}
