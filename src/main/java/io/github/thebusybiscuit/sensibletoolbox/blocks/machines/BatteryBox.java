package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.sensibletoolbox.api.RedstoneBehaviour;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.ChargeDirection;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.EnergyFlow;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.EnergyNet;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.EnergyFlowGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.utils.UnicodeSymbol;

public abstract class BatteryBox extends BaseSTBMachine {

    private final Map<BlockFace, EnergyFlow> energyFlow = new EnumMap<>(BlockFace.class);

    protected BatteryBox() {
        for (BlockFace face : STBUtil.getDirectBlockFaces()) {
            energyFlow.put(face, EnergyFlow.NONE);
        }

        setChargeDirection(ChargeDirection.CELL);
    }

    public BatteryBox(ConfigurationSection conf) {
        super(conf);

        for (BlockFace face : STBUtil.getDirectBlockFaces()) {
            energyFlow.put(face, EnergyFlow.valueOf(conf.getString("flow." + face, "NONE")));
        }
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();

        for (Map.Entry<BlockFace, EnergyFlow> entry : energyFlow.entrySet()) {
            conf.set("flow." + entry.getKey(), entry.getValue().toString());
        }

        return conf;
    }

    @Override
    public int[] getInputSlots() {
        // no input
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        // no output
        return new int[0];
    }

    @Override
    public int[] getUpgradeSlots() {
        // no upgrades
        return new int[0];
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
        return new String[] { "Stores up to " + UnicodeSymbol.ELECTRICITY.toUnicode() + " " + getMaxCharge() + " SCU" };
    }

    @Override
    public String[] getExtraLore() {
        return new String[] { STBUtil.getChargeString(this) };
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = super.createGUI();

        gui.addGadget(new EnergyFlowGadget(gui, 4, BlockFace.NORTH));
        gui.addGadget(new EnergyFlowGadget(gui, 22, BlockFace.SOUTH));
        gui.addGadget(new EnergyFlowGadget(gui, 14, BlockFace.EAST));
        gui.addGadget(new EnergyFlowGadget(gui, 12, BlockFace.WEST));
        gui.addGadget(new EnergyFlowGadget(gui, 3, BlockFace.UP));
        gui.addGadget(new EnergyFlowGadget(gui, 21, BlockFace.DOWN));
        gui.getInventory().setItem(5, null);
        gui.getInventory().setItem(23, null);
        gui.addLabel(" ", 13, new ItemStack(getMaterial()));

        return gui;
    }

    @Nullable
    public EnergyFlow getEnergyFlow(@Nonnull BlockFace face) {
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
}
