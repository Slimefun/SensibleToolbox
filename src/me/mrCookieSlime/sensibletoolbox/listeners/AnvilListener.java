package me.mrCookieSlime.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.dhutils.MiscUtil;
import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class AnvilListener extends STBBaseListener {
    public AnvilListener(SensibleToolboxPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onAnvil(InventoryClickEvent event) {
        // The whole Anvil system is horribly broken right now, so we're just going to disallow
        // any kind of anvil activity for STB items.
        if (event.getWhoClicked() instanceof Player && event.getInventory().getType() == InventoryType.ANVIL) {
            if (event.getSlotType() == InventoryType.SlotType.CRAFTING) {
                if (SensibleToolbox.getItemRegistry().isSTBItem(event.getCursor())) {
                    event.setCancelled(true);
                    MiscUtil.errorMessage((Player) event.getWhoClicked(), "Sensible Toolbox items don't fit in a vanilla anvil.");
                }
            } else if (event.getRawSlot() > 2 && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (SensibleToolbox.getItemRegistry().isSTBItem(event.getCurrentItem())) {
                    event.setCancelled(true);
                    MiscUtil.errorMessage((Player) event.getWhoClicked(), "Sensible Toolbox items don't fit in a vanilla anvil.");
                }
            }
        }
    }
}
