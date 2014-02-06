package me.desht.sensibletoolbox.util;

import me.desht.dhutils.Debugger;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FuelItems {
	private final Map<ItemStack, FuelValues> fuels = new HashMap<ItemStack, FuelValues>();

	public void addFuel(ItemStack stack, double chargePerTick, int burnTime) {
		fuels.put(getSingle(stack), new FuelValues(chargePerTick, burnTime));
		Debugger.getInstance().debug("register burnable fuel: " + stack + " -> " + get(stack).toString());
	}

	public FuelValues get(ItemStack stack) {
		return fuels.get(getSingle(stack));
	}

	public boolean has(ItemStack stack) {
		return fuels.containsKey(getSingle(stack));
	}

	private ItemStack getSingle(ItemStack stack) {
		if (stack.getAmount() == 1) {
			return stack;
		} else {
			ItemStack stack2 = stack.clone();
			stack2.setAmount(1);
			return stack2;
		}
	}

	public class FuelValues {
		private final double charge; // per tick
		private final int burnTime; // total ticks
		private final double total;

		public FuelValues(double charge, int burnTime) {
			this.charge = charge;
			this.burnTime = burnTime;
			total = charge * burnTime;
		}

		public double getCharge() {
			return charge;
		}

		public int getBurnTime() {
			return burnTime;
		}

		@Override
		public String toString() {
			return "[Fuel: " + charge + " SCU/tick for " + burnTime + " ticks = " + total + " SCU]";
		}
	}
}
