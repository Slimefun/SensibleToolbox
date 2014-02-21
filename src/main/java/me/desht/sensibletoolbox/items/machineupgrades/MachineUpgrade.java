package me.desht.sensibletoolbox.items.machineupgrades;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class MachineUpgrade extends BaseSTBItem {
	private int amount;

	protected MachineUpgrade() {
	}

	public MachineUpgrade(ConfigurationSection conf) {
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
