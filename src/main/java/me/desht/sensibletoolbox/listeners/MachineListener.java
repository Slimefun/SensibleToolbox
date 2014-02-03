package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.STBMachine;
import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class MachineListener extends STBBaseListener {
	public MachineListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getHolder() instanceof STBMachine) {
			BaseSTBMachine stb = (BaseSTBMachine) event.getInventory().getHolder();
			System.out.println("machine inv click! slot=" + event.getSlot() + " raw=" + event.getRawSlot());
			stb.handleInventoryClick(event);
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getInventory().getHolder() instanceof STBMachine) {
			BaseSTBMachine stb = (BaseSTBMachine) event.getInventory().getHolder();
			System.out.println("machine inv drag!");
			stb.handleInventoryDrag(event);
		}
	}

	@EventHandler
	public void onInventoryMove(InventoryMoveItemEvent event) {
		if (event.getDestination().getHolder() instanceof STBMachine) {
			BaseSTBMachine stb = (BaseSTBMachine) event.getDestination().getHolder();
			System.out.println("move item into machine!");
			stb.handleInventoryInsertion(event);
		} else if (event.getSource().getHolder() instanceof STBMachine) {
			BaseSTBMachine stb = (BaseSTBMachine) event.getSource().getHolder();
			System.out.println("move item into machine!");
			stb.handleInventoryExtraction(event);
		}
	}
}
