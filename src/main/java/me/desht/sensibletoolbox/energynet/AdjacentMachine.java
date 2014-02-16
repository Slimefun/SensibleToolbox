package me.desht.sensibletoolbox.energynet;

import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import org.bukkit.block.BlockFace;

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
