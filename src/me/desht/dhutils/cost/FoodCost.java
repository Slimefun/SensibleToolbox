package me.desht.dhutils.cost;

import org.bukkit.entity.Player;

public class FoodCost extends Cost {

	public FoodCost(double quantity) {
		super(quantity);
	}

	@Override
	public String getDescription() {
		return (int) getQuantity() + " hunger";
	}

	@Override
	public boolean isAffordable(Player player) {
		return player.getFoodLevel() > getQuantity();
	}

	@Override
	public void apply(Player player) {
		player.setFoodLevel((int) getAdjustedQuantity(player.getFoodLevel(), getQuantity(), 1, 20));
	}
}
