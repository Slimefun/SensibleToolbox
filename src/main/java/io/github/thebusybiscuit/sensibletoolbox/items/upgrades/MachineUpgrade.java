package io.github.thebusybiscuit.sensibletoolbox.items.upgrades;

import org.bukkit.configuration.ConfigurationSection;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public abstract class MachineUpgrade extends BaseSTBItem {

    private int amount;

    protected MachineUpgrade() {}

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
