package me.desht.dhutils.cost;

import org.bukkit.entity.Player;

public class HealthCost extends Cost {

	protected HealthCost(double quantity) {
		super(quantity);
	}

	@Override
	public String getDescription() {
		return getQuantity() + " health";
	}

	@Override
	public boolean isAffordable(Player player) {
		return player.getHealth() > getQuantity();
	}

	@Override
	public void apply(Player player) {
		double min = getQuantity() > player.getMaxHealth() ? 0.0 : 1.0;
		player.setHealth(getAdjustedQuantity((int) player.getHealth(), getQuantity(), min, player.getMaxHealth()));
	}

}
