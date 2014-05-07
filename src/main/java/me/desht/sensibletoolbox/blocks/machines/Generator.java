package me.desht.sensibletoolbox.blocks.machines;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public abstract class Generator extends AbstractProcessingMachine {
    public Generator(ConfigurationSection conf) {
        super(conf);
    }

    public Generator() {
        super();
        setChargeDirection(ChargeDirection.CELL);
    }

    @Override
    public boolean acceptsEnergy(BlockFace face) {
        return false;
    }

    @Override
    public boolean suppliesEnergy(BlockFace face) {
        return true;
    }
}
