package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.api.RedstoneBehaviour;
import me.desht.sensibletoolbox.api.energy.ChargeDirection;
import me.desht.sensibletoolbox.api.items.AbstractProcessingMachine;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

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
