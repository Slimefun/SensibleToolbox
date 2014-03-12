package me.desht.sensibletoolbox.energynet;

import com.google.common.base.Joiner;
import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.ChargeableBlock;
import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public class EnergyNetManager {
	public static final long ENERGY_TICK_RATE = 10; // TODO make configurable

	private static final Map<Integer,EnergyNet> allNets = new HashMap<Integer, EnergyNet>();

	/**
	 * Get the energy net this block is in, if any.
	 *
	 * @param b the block to check
	 * @return the block's energy net, or null if none
	 */
	public static EnergyNet getEnergyNet(Block b) {
		Integer netId = (Integer) STBUtil.getMetadataValue(b, EnergyNet.STB_ENET_ID);
		return netId == null ? null : allNets.get(netId);
	}

	/**
	 * Given a cable which has just been placed, check what energy nets and machines, if any,
	 * are adjacent to it, and act accordingly.
	 *
	 * @param cable the newly placed cable
	 */
	public static void onCablePlaced(Block cable) {
		Set<Integer> netIds = getAdjacentNets(cable);
//		Debugger.getInstance().debug("new cable " + cable + " has " + netIds.size() + " adjacent nets [" + Joiner.on(",").join(netIds) + "]");
		List<AdjacentMachine> adjacentMachines;
		switch (netIds.size()) {
			case 0:
				// not connected to any net, start a new one IFF there is one or more adjacent machines
				adjacentMachines = getAdjacentMachines(cable);
				if (!adjacentMachines.isEmpty()) {
					EnergyNet newNet = EnergyNet.buildNet(cable);
					allNets.put(newNet.getNetID(), newNet);
					addConnectedCables(cable, newNet);
				}
				break;
			case 1:
				// connected to a single net; just add this cable to that net
				Integer[] id = netIds.toArray(new Integer[1]);
				EnergyNet net = allNets.get(id[0]);
				net.addCable(cable);
				// attach any adjacent machines
				adjacentMachines = getAdjacentMachines(cable);
				for (AdjacentMachine record : adjacentMachines) {
					net.addMachine(record.getMachine(), record.getDirection().getOppositeFace());
				}
				addConnectedCables(cable, net);
				break;
			default:
				// connected to more than one different net!
				// delete those nets, then re-scan and build a new single unified net
				for (int netId : netIds) {
					deleteEnergyNet(netId);
				}
				EnergyNet newNet = EnergyNet.buildNet(cable);
				allNets.put(newNet.getNetID(), newNet);
		}
	}

	public static void onCableRemoved(Block cable) {
		EnergyNet thisNet = EnergyNetManager.getEnergyNet(cable);
		if (thisNet == null) {
			// cable with no net at all?
			return;
		}

		System.out.println("removing cable " + cable + " from enet #" + thisNet.getNetID());

		// scan this cable's neighbours to see what it was attached to
		final List<Block> attachedCables = new ArrayList<Block>();
		final List<BaseSTBMachine> attachedMachines = new ArrayList<BaseSTBMachine>();
		for (BlockFace face : STBUtil.directFaces) {
			Block b = cable.getRelative(face);
			if (isCable(b)) {
				attachedCables.add(b);
			} else {
				BaseSTBMachine machine = LocationManager.getManager().get(b.getLocation(), BaseSTBMachine.class);
				if (machine != null) {
					attachedMachines.add(machine);
				}
			}
		}

		if (attachedCables.size() == 1 && attachedMachines.isEmpty()) {
			// simple case; cable attached to only one other cable - no need to delete the net
			thisNet.removeCable(cable);
		} else {
			// delete the energy net for the removed cable; this will also detach any machines
			deleteEnergyNet(thisNet.getNetID());
			if (attachedCables.size() > 0) {
				// need a delayed task here, since the block for the cable being removed isn't
				// actually updated to air yet...
				Bukkit.getScheduler().runTask(SensibleToolboxPlugin.getInstance(), new Runnable() {
					@Override
					public void run() {
						// rebuild energy nets for the deleted cable's neighbours
						for (Block b : attachedCables) {
							// those neighbours could have another path to each other
							EnergyNet net1 = EnergyNetManager.getEnergyNet(b);
							if (net1 == null) {
								EnergyNet newNet1 = EnergyNet.buildNet(b);
								allNets.put(newNet1.getNetID(), newNet1);
							}
						}
					}
				});
			}
		}
	}

	public static void onMachinePlaced(ChargeableBlock machine) {
		Block b = machine.getLocation().getBlock();
		// scan adjacent blocks for cables
		for (BlockFace face : STBUtil.directFaces) {
			Block cable = b.getRelative(face);
			if (isCable(cable)) {
				EnergyNet net = EnergyNetManager.getEnergyNet(cable);
				if (net == null) {
//					System.out.println("found cable with no enet " + face + " from " + machine);
					// cable with no net - create one!
					EnergyNet newNet = EnergyNet.buildNet(cable);
					newNet.addMachine(machine, face);
					allNets.put(newNet.getNetID(), newNet);
				} else {
					// cable on a net - add machine to it
//					System.out.println("found cable on enet #" + net.getNetID() + ", " + face + " from " + machine);
					net.addMachine(machine, face);
				}
			}
		}
	}

	public static void onMachineRemoved(ChargeableBlock machine) {
		for (EnergyNet net : machine.getAttachedEnergyNets()) {
			net.removeMachine(machine);
		}
	}

	/**
	 * Scan for any cable which is not currently part of an energy net, and add it to the
	 * given net.
	 *
	 * @param start block to scan from
	 * @param net net to add cabling to
	 */
	private static void addConnectedCables(Block start, EnergyNet net) {
		for (BlockFace face : STBUtil.directFaces) {
			Block b = start.getRelative(face);
			if (isCable(b)) {
				EnergyNet net2 = getEnergyNet(b);
				if (net2 == null) {
					net.addCable(b);
					addConnectedCables(b, net);
				}
			}
		}
	}

	private static List<AdjacentMachine> getAdjacentMachines(Block cable) {
		final List<AdjacentMachine> attachedMachines = new ArrayList<AdjacentMachine>();
		for (BlockFace face : STBUtil.directFaces) {
			Block b = cable.getRelative(face);
			BaseSTBMachine machine = LocationManager.getManager().get(b.getLocation(), BaseSTBMachine.class);
			if (machine != null) {
				attachedMachines.add(new AdjacentMachine(machine, face));
			}
		}
		return attachedMachines;
	}

	/**
	 * Get all the energy nets that the given block is attached to.
	 *
	 * @param startBlock the block to check
	 * @return set of up to 6 integers, representing energy net IDs
	 */
	private static Set<Integer> getAdjacentNets(Block startBlock) {
		Set<Integer> res = new HashSet<Integer>();
		for (BlockFace face : STBUtil.directFaces) {
			EnergyNet net = EnergyNetManager.getEnergyNet(startBlock.getRelative(face));
			if (net != null) {
				res.add(net.getNetID());
			}
		}
		return res;
	}

	public static void deleteEnergyNet(int netID) {
//		System.out.println("deleting energy net #" + netID);
		EnergyNet enet = allNets.get(netID);
		if (enet != null) {
			enet.shutdown();
			allNets.remove(netID);
		}
	}

	public static boolean isCable(Block b) {
		return b.getType() == Material.IRON_FENCE;
	}

	public static void tick() {
		for (EnergyNet net : allNets.values()) {
			net.tick();
		}
	}
}
