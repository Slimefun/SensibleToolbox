package io.github.thebusybiscuit.sensibletoolbox.core.energy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.metadata.FixedMetadataValue;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.ChargeableBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.EnergyNet;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.BlockPosition;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import me.desht.dhutils.Debugger;

public class STBEnergyNet implements EnergyNet {
	
    public static final String STB_ENET_ID = "STB_ENet_ID";
    public static final int MAX_BLOCKS_IN_CABLE = 512;
    private static int freeID = 1;
    private final int netID;
    private final String worldName;
    private final List<BlockPosition> cables = new ArrayList<BlockPosition>();
    private final Set<ChargeableBlock> machines = new HashSet<ChargeableBlock>();
    private double totalDemand;
    private double totalSupply;
    private final Set<ChargeableBlock> energySinks = new HashSet<ChargeableBlock>();
    private final Set<ChargeableBlock> energySources = new HashSet<ChargeableBlock>();
    private final EnergyNetManager enetManager;

    private STBEnergyNet(String worldName, EnergyNetManager manager) {
        this.worldName = worldName;
        this.netID = getNextFreeID();
        this.enetManager = manager;
    }

    private synchronized int getNextFreeID() {
        return freeID++;
    }

    static STBEnergyNet buildNet(Block b, EnergyNetManager manager) {
        STBEnergyNet enet = new STBEnergyNet(b.getWorld().getName(), manager);

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
        enet.findSourcesAndSinks();
        Debugger.getInstance().debug("built new net #" + enet.getNetID() + " with " + enet.cables.size() + " cables & " + enet.machines.size() + " machines");
        return enet;
    }

    /**
     * Recursively scan blocks attached to this block for cables and machines.  Machines are added to the
     * list of discovered blocks, but scanning stops where a machine is encountered.
     *
     * @param b          the block being checked
     * @param discovered set of discovered blocks so far
     */
    private static void recursiveScan(Block b, Set<Object> discovered, BlockFace fromDir) {
        if (discovered.size() > MAX_BLOCKS_IN_CABLE || discovered.contains(b)) {
            return;
        }

        if (!STBUtil.isCable(b)) {
            BaseSTBMachine machine = LocationManager.getManager().get(b.getLocation(), BaseSTBMachine.class);
            if (machine != null) {
                discovered.add(new AdjacentMachine(machine, fromDir));
            }
        } else {
            discovered.add(b);
            for (BlockFace face : STBUtil.directFaces) {
                recursiveScan(b.getRelative(face), discovered, face.getOppositeFace());
            }
        }
    }

    /**
     * Determine which machines on this net can supply energy, and which consume it.
     */
    @Override
    public void findSourcesAndSinks() {
        energySinks.clear();
        energySources.clear();

        for (ChargeableBlock machine : machines) {
            for (BlockFace face : machine.getFacesForNet(this)) {
                if (machine.acceptsEnergy(face)) {
                    energySinks.add(machine);
                } else if (machine.suppliesEnergy(face)) {
                    energySources.add(machine);
                }
            }
        }
        Debugger.getInstance().debug("Energy net #" + getNetID() + ": found "
                + energySources.size() + " sources and " + energySinks.size() + " sinks");
    }

    @Override
    public int getNetID() {
        return netID;
    }

    void addMachine(ChargeableBlock machine, BlockFace face) {
        machine.attachToEnergyNet(this, face);
        machines.add(machine);
        findSourcesAndSinks();
        Debugger.getInstance().debug("Enet #" + getNetID() + ": added machine " + machine + " on face " + face);
    }

    void removeMachine(ChargeableBlock machine) {
        machine.detachFromEnergyNet(this);
        machines.remove(machine);
        findSourcesAndSinks();
        Debugger.getInstance().debug("Enet #" + getNetID() + ": removed machine " + machine);
    }

    void addCable(Block cable) {
        cable.setMetadata(STB_ENET_ID, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), getNetID()));
        cables.add(new BlockPosition(cable.getLocation()));
        Debugger.getInstance().debug("Enet #" + getNetID() + ": added cable @ " + cable);
    }

    void removeCable(Block cable) {
        cable.removeMetadata(STB_ENET_ID, SensibleToolboxPlugin.getInstance());
        cables.remove(new BlockPosition(cable.getLocation()));
        Debugger.getInstance().debug("Enet #" + getNetID() + ": removed cable @ " + cable);
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
        for (ChargeableBlock machine : machines) {
            machine.detachFromEnergyNet(this);
        }
        machines.clear();
        Debugger.getInstance().debug("Enet #" + getNetID() + " shutdown complete");
    }

    @Override
    public int getCableCount() {
        return cables.size();
    }

    @Override
    public int getSourceCount() {
        return energySources.size();
    }

    @Override
    public int getSinkCount() {
        return energySinks.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        STBEnergyNet energyNet = (STBEnergyNet) o;

        return netID == energyNet.netID;
    }

    @Override
    public int hashCode() {
        return netID;
    }

    void tick() {
        totalDemand = totalSupply = 0;

        long tickRate = enetManager.getTickRate();

        for (ChargeableBlock machine : energySources) {
            if (machine.getCharge() > 0) {
                totalSupply += Math.min(machine.getCharge(), machine.getChargeRate() * tickRate);
            }
        }
        for (ChargeableBlock machine : energySinks) {
            if (machine.getCharge() < machine.getMaxCharge()) {
                double needed = machine.getMaxCharge() - machine.getCharge();
                needed = Math.min(needed, machine.getChargeRate() * tickRate);
                totalDemand += needed;
            }
        }

        if (totalDemand == 0.0 || totalSupply == 0.0) {
            return;
        }

        double ratio = totalDemand / totalSupply;
        if (ratio <= 1.0) {
            // there's enough power to supply all sinks
            for (ChargeableBlock source : energySources) {
                double toTake = Math.min(source.getCharge(), source.getChargeRate() * tickRate);
                source.setCharge(source.getCharge() - toTake * ratio);
            }
            for (ChargeableBlock sink : energySinks) {
                double toGive = Math.min(sink.getMaxCharge() - sink.getCharge(), sink.getChargeRate() * tickRate);
                sink.setCharge(sink.getCharge() + toGive);
            }
        } else {
            // more demand than supply!
            for (ChargeableBlock source : energySources) {
                double toTake = Math.min(source.getCharge(), source.getChargeRate() * tickRate);
                source.setCharge(source.getCharge() - toTake);
            }
            for (ChargeableBlock sink : energySinks) {
                double toGive = Math.min(sink.getMaxCharge() - sink.getCharge(), sink.getChargeRate() * tickRate);
                sink.setCharge(sink.getCharge() + toGive / ratio);
            }
        }
    }

    @Override
    public double getDemand() {
        return totalDemand / enetManager.getTickRate();
    }

    @Override
    public double getSupply() {
        return totalSupply / enetManager.getTickRate();
    }
}
