package me.desht.sensibletoolbox.energynet;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.storage.BlockPosition;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnergyNet {
	public static final String STB_ENET_ID = "STB_ENet_ID";
	public static final int MAX_BLOCKS_IN_CABLE = 512;
	private static int freeID = 1;
	private final int netID;
	private final String worldName;
	private final List<BlockPosition> cables = new ArrayList<BlockPosition>();
	private final Set<BaseSTBMachine> machines = new HashSet<BaseSTBMachine>();
	private double totalDemand;
	private double totalSupply;

	private EnergyNet(String worldName) {
		this.worldName = worldName;
		this.netID = getNextFreeID();
	}

	private synchronized int getNextFreeID() {
		return freeID++;
	}


	static EnergyNet buildNet(Block b) {
		EnergyNet enet = new EnergyNet(b.getWorld().getName());

		Set<Object> blocks = new HashSet<Object>();
		recursiveScan(b, blocks, BlockFace.SELF);
		for (Object o : blocks) {
			if (o instanceof Block) {
				enet.addCable((Block) o);
			} else if (o instanceof AdjacentMachine) {
				AdjacentMachine rec = (AdjacentMachine) o;
				enet.addMachine(rec.getMachine(), rec.getDirection());
			}
		}
		System.out.println("built new net #" + enet.getNetID() + " with " + enet.cables.size() + " cables & " + enet.machines.size() + " machines");
		return enet;
	}

	/**
	 * Recursively scan blocks attached to this block for cables and machines.  Machines are added to the
	 * list of discovered blocks, but scanning stops where a machine is encountered.
	 *
	 * @param b the block being checked
	 * @param discovered set of discovered blocks so far
	 */
	private static void recursiveScan(Block b, Set<Object> discovered, BlockFace fromDir) {
		if (discovered.size() > MAX_BLOCKS_IN_CABLE || discovered.contains(b)) {
			return;
		}
		if (!EnergyNetManager.isCable(b)) {
			BaseSTBMachine machine = LocationManager.getManager().get(b.getLocation(), BaseSTBMachine.class);
			if (machine != null) {
				System.out.println("*** found machine " + b);
				discovered.add(new AdjacentMachine(machine, fromDir));
			}
		} else {
			System.out.println("*** found cable " + b);
			discovered.add(b);
			for (BlockFace face : STBUtil.directFaces) {
				recursiveScan(b.getRelative(face), discovered, face.getOppositeFace());
			}
		}
	}

	public int getNetID() {
		return netID;
	}

	void addMachine(BaseSTBMachine machine, BlockFace face) {
		machine.attachToEnergyNet(this, face);
		machines.add(machine);
		System.out.println("added machine " + machine + " to enet #" + getNetID() + " on face " + face);
	}

	public void removeMachine(BaseSTBMachine machine) {
		machine.detachFromEnergyNet(this);
		machines.remove(machine);
		System.out.println("removed machine " + machine + " from enet #" + getNetID());
	}

	void addCable(Block cable) {
		cable.setMetadata(STB_ENET_ID, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), getNetID()));
		cables.add(new BlockPosition(cable.getLocation()));
		System.out.println("added cable @ " + cable + " to enet #" + getNetID());
	}

	public void removeCable(Block cable) {
		cable.removeMetadata(STB_ENET_ID, SensibleToolboxPlugin.getInstance());
		cables.remove(new BlockPosition(cable.getLocation()));
		System.out.println("removed cable @ " + cable + " from enet #" + getNetID());
	}

	public void shutdown() {
		World w = Bukkit.getWorld(worldName);
		if (w != null) {
			for (BlockPosition pos : cables) {
				Block b = w.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
				b.removeMetadata(STB_ENET_ID, SensibleToolboxPlugin.getInstance());
			}
		}
		cables.clear();
		for (BaseSTBMachine machine : machines) {
			machine.detachFromEnergyNet(this);
		}
		machines.clear();
		System.out.println("enet #" + getNetID() + " shutdown complete");
	}

	public int getCableCount() {
		return cables.size();
	}

	public int getMachineCount() {
		return machines.size();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EnergyNet energyNet = (EnergyNet) o;

		if (netID != energyNet.netID) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return netID;
	}

	public void tick() {
		List<BaseSTBMachine> energySinks = new ArrayList<BaseSTBMachine>();
		List<BaseSTBMachine> energySources = new ArrayList<BaseSTBMachine>();

		totalDemand = totalSupply = 0;

		// TODO cache the list of potential sources & sinks (recalc when a machine added/removed/updated)

		// calculate who needs power, who can supply it, and the total supply/demand
		for (BaseSTBMachine machine : machines) {
			for (BlockFace face : machine.getFacesForNet(this)) {
				if (machine.acceptsEnergy(face)) {
					if (machine.getCharge() < machine.getMaxCharge()) {
						energySinks.add(machine);
						double needed = machine.getMaxCharge() - machine.getCharge();
						needed = Math.min(needed, machine.getChargeRate() * EnergyNetManager.ENERGY_TICK_RATE);
						totalDemand += needed;
					}
				} else if (machine.suppliesEnergy(face)) {
					if (machine.getCharge() > 0) {
						energySources.add(machine);
						totalSupply += Math.min(machine.getCharge(), machine.getChargeRate() * EnergyNetManager.ENERGY_TICK_RATE);
					}
				}
			}
		}

		if (totalDemand == 0.0 || totalSupply == 0.0) {
			return;
		}

		double ratio = totalDemand / totalSupply;
		if (ratio <= 1.0) {
			// there's enough power to supply all sinks
			for (BaseSTBMachine source : energySources) {
				double toTake = Math.min(source.getCharge(), source.getChargeRate() * EnergyNetManager.ENERGY_TICK_RATE);
				source.setCharge(source.getCharge() - toTake * ratio);
			}
			for (BaseSTBMachine sink : energySinks) {
				double needed = sink.getMaxCharge() - sink.getCharge();
				needed = Math.min(needed, sink.getChargeRate() * EnergyNetManager.ENERGY_TICK_RATE);
				sink.setCharge(sink.getCharge() + needed);
			}
		} else {
			// more demand than supply!
			for (BaseSTBMachine source : energySources) {
				double toTake = Math.min(source.getCharge(), source.getChargeRate() * EnergyNetManager.ENERGY_TICK_RATE);
				source.setCharge(source.getCharge() - toTake);
			}
			for (BaseSTBMachine sink : energySinks) {
				double needed = sink.getMaxCharge() - sink.getCharge();
				needed = Math.min(needed, sink.getChargeRate() * EnergyNetManager.ENERGY_TICK_RATE);
				sink.setCharge(sink.getCharge() + needed / ratio);
			}
		}
	}

	/**
	 * Get the instantaneous energy demand per tick.  This is requested energy, not necessarily supplied.
	 *
	 * @return the demand
	 */
	public double getDemand() {
		return totalDemand / EnergyNetManager.ENERGY_TICK_RATE;
	}

	/**
	 * Get the instantaneous energy supply per tick.  This is available energy, not necessarily used.
	 *
	 * @return the demand
	 */
	public double getSupply() {
		return totalSupply / EnergyNetManager.ENERGY_TICK_RATE;
	}
}
