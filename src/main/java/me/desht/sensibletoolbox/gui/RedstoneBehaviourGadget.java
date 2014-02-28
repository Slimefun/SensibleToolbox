package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.RedstoneBehaviour;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RedstoneBehaviourGadget extends ClickableGadget {
	private RedstoneBehaviour behaviour;

	public RedstoneBehaviourGadget(InventoryGUI gui) {
		super(gui);
		behaviour = getGUI().getOwningBlock().getRedstoneBehaviour();
	}

	@Override
	public void onClicked(InventoryClickEvent event) {
		int n = (behaviour.ordinal() + 1) % RedstoneBehaviour.values().length;
		behaviour = RedstoneBehaviour.values()[n];
		event.setCurrentItem(behaviour.getTexture());
		getGUI().getOwningBlock().setRedstoneBehaviour(behaviour);
	}

	@Override
	public ItemStack getTexture() {
		return behaviour.getTexture();
	}
}
