package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.sensibletoolbox.api.RedstoneBehaviour;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.ChargeDirection;
import io.github.thebusybiscuit.sensibletoolbox.api.items.AbstractProcessingMachine;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.FuelItems;

public abstract class Generator extends AbstractProcessingMachine {

    protected Generator() {
        super();
        setChargeDirection(ChargeDirection.CELL);
    }

    protected Generator(ConfigurationSection conf) {
        super(conf);
    }

    public abstract FuelItems getFuelItems();

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

    public List<ItemStack> getFuelInformation() {
        List<ItemStack> list = new ArrayList<>();
        list.addAll(getFuelItems().getFuelInfos());
        return list;
    }
}
