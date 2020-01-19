package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import io.github.thebusybiscuit.sensibletoolbox.api.RedstoneBehaviour;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.ChargeDirection;
import io.github.thebusybiscuit.sensibletoolbox.api.items.AbstractProcessingMachine;

public abstract class Generator extends AbstractProcessingMachine {
	
    protected Generator() {
        super();
        setChargeDirection(ChargeDirection.CELL);
    }

    protected Generator(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public boolean acceptsEnergy(BlockFace face) {
        return false;
    }

    @Override
    public boolean suppliesEnergy(BlockFace face) {
        return true;
    }

    @Override
    public boolean supportsRedstoneBehaviour(RedstoneBehaviour behaviour) {
        return behaviour != RedstoneBehaviour.PULSED;
    }
}
