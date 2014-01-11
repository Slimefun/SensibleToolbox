package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.blocks.PaintCan;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class PaintCanListener extends STBBaseListener {
	public PaintCanListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPaintMixerClosed(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player && event.getInventory().getTitle().equals(PaintCan.getMixerTitle())) {
			Player player = (Player) event.getPlayer();
			Object o = STBUtil.getMetadataValue(player, PaintCan.STB_PAINT_CAN);
			if (o != null && o instanceof Location) {
				BaseSTBBlock stb = plugin.getLocationManager().get((Location) o);
				if (stb != null && stb instanceof PaintCan) {
					PaintCan can = (PaintCan) stb;
					if (can.tryMix(event.getInventory())) {
						player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 1.0f, 1.0f);
						player.removeMetadata(PaintCan.STB_PAINT_CAN, plugin);
						can.getBaseLocation().getBlock().setType(can.getBaseMaterial());
						can.getBaseLocation().getBlock().setData(can.getBaseBlockData());
					}
					// return any items left in the inventory to the player
					for (ItemStack item : event.getInventory()) {
						if (item != null) {
							player.getWorld().dropItemNaturally(player.getLocation(), item);
						}
					}
				}
			}
		}
	}
}
