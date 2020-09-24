package io.github.thebusybiscuit.sensibletoolbox.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.blocks.TrashCan;

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
