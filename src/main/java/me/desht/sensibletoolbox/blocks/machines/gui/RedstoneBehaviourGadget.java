package me.desht.sensibletoolbox.blocks.machines.gui;

import me.desht.sensibletoolbox.api.RedstoneBehaviour;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RedstoneBehaviourGadget extends ClickableGadget {
	private RedstoneBehaviour behaviour;

	public RedstoneBehaviourGadget(InventoryGUI owner) {
		super(owner);
		behaviour = getGUI().getOwner().getRedstoneBehaviour();
	}

	@Override
	public void onClicked(InventoryClickEvent event) {
		int n = (behaviour.ordinal() + 1) % RedstoneBehaviour.values().length;
		behaviour = RedstoneBehaviour.values()[n];
		event.setCurrentItem(behaviour.getTexture());
		getGUI().getOwner().setRedstoneBehaviour(behaviour);
	}

	@Override
	public ItemStack getTexture() {
		return behaviour.getTexture();
	}
}
