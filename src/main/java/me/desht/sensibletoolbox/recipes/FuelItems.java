package me.desht.sensibletoolbox.recipes;

import me.desht.dhutils.Debugger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FuelItems {
	private final Map<ItemStack, FuelValues> fuels = new HashMap<ItemStack, FuelValues>();
	private final Map<Material, FuelValues> fuelMaterials = new HashMap<Material, FuelValues>();

	public void addFuel(ItemStack stack, boolean ignoreData, double chargePerTick, int burnTime) {
		if (ignoreData) {
			fuelMaterials.put(stack.getType(), new FuelValues(chargePerTick, burnTime));
		} else {
			fuels.put(getSingle(stack), new FuelValues(chargePerTick, burnTime));
		}
		Debugger.getInstance().debug("register burnable fuel: " + stack + " -> " + get(stack).toString());
	}

	public FuelValues get(ItemStack stack) {
		FuelValues res = fuels.get(getSingle(stack));
		return res == null ? fuelMaterials.get(stack.getType()) : res;
	}

	public boolean has(ItemStack stack) {
		return fuels.containsKey(getSingle(stack)) || fuelMaterials.containsKey(stack.getType());
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

		public double getTotalFuelValue() {
			return total;
		}

		@Override
		public String toString() {
			return charge + " SCU/t over " + burnTime + "t = " + total + " SCU";
		}
	}
}
