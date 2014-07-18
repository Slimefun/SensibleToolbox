package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.api.ChargeDirection;
import me.desht.sensibletoolbox.api.RedstoneBehaviour;
import me.desht.sensibletoolbox.api.gui.EnergyFlowGadget;
import me.desht.sensibletoolbox.api.gui.InventoryGUI;
import me.desht.sensibletoolbox.api.items.BaseSTBMachine;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.core.energy.EnergyNet;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public abstract class BatteryBox extends BaseSTBMachine {
    private final Map<BlockFace, EnergyFlow> energyFlow = new HashMap<BlockFace, EnergyFlow>();

    protected BatteryBox() {
        for (BlockFace face : STBUtil.directFaces) {
            energyFlow.put(face, EnergyFlow.NONE);
        }
        setChargeDirection(ChargeDirection.CELL);
    }

    public BatteryBox(ConfigurationSection conf) {
        super(conf);
        for (BlockFace face : STBUtil.directFaces) {
            energyFlow.put(face, EnergyFlow.valueOf(conf.getString("flow." + face, "NONE")));
        }
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        for (BlockFace face : energyFlow.keySet()) {
            conf.set("flow." + face, energyFlow.get(face).toString());
        }
        return conf;
    }

    @Override
    public int[] getInputSlots() {
        return new int[0];  // no input
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];  // no output
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[0];  // no upgrades
    }

    @Override
    public int getUpgradeLabelSlot() {
        return -1;
    }

    @Override
    protected void playActiveParticleEffect() {
        // do nothing
    }

    @Override
    public int getEnergyCellSlot() {
        return 18;
    }

    @Override
    public int getChargeDirectionSlot() {
        return 19;
    }

    @Override
    public int getInventoryGUISize() {
        return 27;
    }

    @Override
    public String[] getLore() {
        return new String[]{"Stores up to \u2301 " + getMaxCharge() + " SCU"};
    }

    @Override
    public String[] getExtraLore() {
        return new String[]{STBUtil.getChargeString(this)};
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = super.createGUI();

        gui.addGadget(new EnergyFlowGadget(gui, 4,  BlockFace.NORTH));
        gui.addGadget(new EnergyFlowGadget(gui, 22, BlockFace.SOUTH));
        gui.addGadget(new EnergyFlowGadget(gui, 14, BlockFace.EAST));
        gui.addGadget(new EnergyFlowGadget(gui, 12, BlockFace.WEST));
        gui.addGadget(new EnergyFlowGadget(gui, 3,  BlockFace.UP));
        gui.addGadget(new EnergyFlowGadget(gui, 21, BlockFace.DOWN));
        gui.getInventory().setItem(5, null);
        gui.getInventory().setItem(23, null);
        gui.addLabel(" ", 13, getMaterialData().toItemStack());

        return gui;
    }

    public EnergyFlow getEnergyFlow(BlockFace face) {
        return energyFlow.get(face);
    }

    public void setFlow(BlockFace face, EnergyFlow flow) {
        energyFlow.put(face, flow);
        for (EnergyNet net : getAttachedEnergyNets()) {
            net.findSourcesAndSinks();
        }
        update(false);
    }

    @Override
    public boolean acceptsEnergy(BlockFace face) {
        return energyFlow.get(face) == EnergyFlow.IN;
    }

    @Override
    public boolean suppliesEnergy(BlockFace face) {
        return energyFlow.get(face) == EnergyFlow.OUT;
    }

    @Override
    public boolean supportsRedstoneBehaviour(RedstoneBehaviour behaviour) {
        return behaviour != RedstoneBehaviour.PULSED;
    }

    public enum EnergyFlow {
        IN,
        OUT,
        NONE;
    }
}
