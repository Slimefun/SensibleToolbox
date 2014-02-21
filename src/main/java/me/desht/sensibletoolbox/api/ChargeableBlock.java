package me.desht.sensibletoolbox.api;

import me.desht.sensibletoolbox.energynet.EnergyNet;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.List;

/**
 * Represents a block that my be connected to an energy network.
 */
public interface ChargeableBlock extends Chargeable {
	/**
	 * Get this block's location.
	 *
	 * @return the block's location
	 */
	public Location getLocation();

	/**
	 * Check if this block accepts energy from an energy net on the given face.
	 *
	 * @param face the block face to check
	 * @return true if energy is accepted, false otherwise.
	 */
	public boolean acceptsEnergy(BlockFace face);

	/**
	 * Check if this block can supply energy to an energy net on the given face.
	 *
	 * @param face the block face to check
	 * @return true if energy can be supplied, false otherwise.
	 */
	public boolean suppliesEnergy(BlockFace face);

	/**
	 * Attach this machine to the given energy net, on the given face.
	 *
	 * @param energyNet the energy net to attach to
	 * @param face the face of the machine block to attach to
	 */
	public void attachToEnergyNet(EnergyNet energyNet, BlockFace face);

	/**
	 * Detach this machine from the given energy net.
	 *
	 * @param energyNet the net to detach from
	 */
	public void detachFromEnergyNet(EnergyNet energyNet);

	/**
	 * Get the faces on which the given energy net is connected.
	 *
	 * @param energyNet the net to look for
	 * @return a list of block faces where this net is connected
	 */
	List<BlockFace> getFacesForNet(EnergyNet energyNet);

	/**
	 * Get an array of all energy nets to which this machine is attached.  Note that a machine
	 * can be attached to the same energy net on more than one face.
	 *
	 * @return an array of the attached energy nets.
	 */
	public EnergyNet[] getAttachedEnergyNets();
}
