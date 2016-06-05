package me.mrCookieSlime.sensibletoolbox.listeners;

import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.blocks.TrashCan;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class TrashCanListener extends STBBaseListener {
    public TrashCanListener(SensibleToolboxPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onTrashCanClosed(InventoryCloseEvent event) {
        TrashCan can = TrashCan.getTrashCan(event.getInventory());
        if (can != null) {
            can.emptyTrash(true);
        }
    }

    @EventHandler
    public void onMoveItemToTrashCan(InventoryMoveItemEvent event) {
        final TrashCan can = TrashCan.getTrashCan(event.getDestination());
        if (can != null) {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    can.emptyTrash(false);
                }
            });
        }
    }

}
