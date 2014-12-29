package me.mrCookieSlime.sensibletoolbox.items.machineupgrades;

import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;

import org.bukkit.configuration.ConfigurationSection;

public abstract class MachineUpgrade extends BaseSTBItem {
    private int amount;

    protected MachineUpgrade() {
    }

    public MachineUpgrade(ConfigurationSection conf) {
        super(conf);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
