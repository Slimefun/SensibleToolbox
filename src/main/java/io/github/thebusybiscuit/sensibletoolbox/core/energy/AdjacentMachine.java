package io.github.thebusybiscuit.sensibletoolbox.core.energy;

import org.bukkit.block.BlockFace;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;

public class AdjacentMachine {

    private final BaseSTBMachine machine;
    private final BlockFace direction;

    AdjacentMachine(BaseSTBMachine machine, BlockFace direction) {
        this.machine = machine;
        this.direction = direction;
    }

    BlockFace getDirection() {
        return direction;
    }

    BaseSTBMachine getMachine() {
        return machine;
    }
}
