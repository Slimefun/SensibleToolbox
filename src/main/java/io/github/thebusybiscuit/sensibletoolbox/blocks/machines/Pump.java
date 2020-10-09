package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.AbstractProcessingMachine;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import me.mrCookieSlime.CSCoreLibPlugin.general.Block.Vein;

public class Pump extends AbstractProcessingMachine {

    private static final int PUMP_FILL_TIME = 40; // 40 ticks to fill a bucket
    private BlockFace pumpFace = BlockFace.DOWN; // will be configurable later

    public Pump() {
        super();
    }

    public Pump(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public int getTickRate() {
        return 5;
    }

    @Override
    public int getInventoryGUISize() {
        return 45;
    }

    @Override
    public int getProgressItemSlot() {
        return 12;
    }

    @Override
    public int getProgressCounterSlot() {
        return 3;
    }

    @Override
    public ItemStack getProgressIcon() {
        return new ItemStack(Material.DIAMOND_BOOTS);
    }

    @Override
    public int[] getInputSlots() {
        return new int[] { 10 };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] { 14 };
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[] { 41, 42, 43, 44 };
    }

    @Override
    public int getUpgradeLabelSlot() {
        return 40;
    }

    @Override
    protected void playActiveParticleEffect() {
        getLocation().getWorld().playEffect(getLocation(), Effect.STEP_SOUND, getRelativeLocation(pumpFace).getBlock().getType());
    }

    @Override
    public boolean acceptsEnergy(BlockFace face) {
        return true;
    }

    @Override
    public boolean suppliesEnergy(BlockFace face) {
        return false;
    }

    @Override
    public int getEnergyCellSlot() {
        return 36;
    }

    @Override
    public int getChargeDirectionSlot() {
        return 37;
    }

    @Override
    public int getMaxCharge() {
        return 1000;
    }

    @Override
    public int getChargeRate() {
        return 20;
    }

    @Override
    public Material getMaterial() {
        return Material.CYAN_TERRACOTTA;
    }

    @Override
    public String getItemName() {
        return "Pump";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Pumps liquids into a bucket" };
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        MachineFrame mf = new MachineFrame();
        registerCustomIngredients(sc, mf);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("PB ", "SIS", "RGR");
        recipe.setIngredient('P', Material.PISTON);
        recipe.setIngredient('B', Material.BUCKET);
        recipe.setIngredient('S', sc.getMaterial());
        recipe.setIngredient('I', mf.getMaterial());
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public double getScuPerTick() {
        // 0.1 SCU to fill a bucket
        return 0.1 / PUMP_FILL_TIME;
    }

    @Override
    public void onServerTick() {
        int inputSlot = getInputSlots()[0];
        ItemStack stackIn = getInventoryItem(inputSlot);

        Block toPump = findNextBlockToPump();

        if (getProcessing() == null && stackIn != null && isRedstoneActive()) {
            // pull a bucket from the input stack into processing
            ItemStack toProcess = makeProcessingItem(toPump, stackIn.getType());
            setProcessing(toProcess);

            if (toProcess != null) {
                getProgressMeter().setMaxProgress(PUMP_FILL_TIME);
                setProgress(PUMP_FILL_TIME);
                stackIn.setAmount(stackIn.getAmount() - 1);
                setInventoryItem(inputSlot, stackIn);
            }
        }

        if (getProgress() > 0 && getCharge() > 0 && STBUtil.isLiquidSourceBlock(toPump)) {
            // currently processing....
            setProgress(getProgress() - getSpeedMultiplier() * getTickRate());
            setCharge(getCharge() - getPowerMultiplier() * getScuPerTick() * getTickRate());
            playActiveParticleEffect();
        }

        if (getProcessing() != null && getProgress() <= 0 && !isJammed()) {
            // done processing - try to move filled container into output
            ItemStack result = getProcessing();
            int slot = findOutputSlot(result);
            if (slot >= 0) {
                setInventoryItem(slot, result);
                setProcessing(null);
                update(false);
                replacePumpedBlock(toPump);
            } else {
                setJammed(true);
            }
        }

        handleAutoEjection();

        super.onServerTick();
    }

    private Block findNextBlockToPump() {
        switch (getRelativeLocation(pumpFace).getBlock().getType()) {
        case LAVA:
            List<Location> list = new ArrayList<>();
            list.add(getRelativeLocation(pumpFace));
            Vein.calculate(getRelativeLocation(pumpFace), getRelativeLocation(pumpFace), list, 128);
            return list.get(list.size() - 1).getBlock();
        default:
            return getRelativeLocation(pumpFace).getBlock();
        }
    }

    private void replacePumpedBlock(Block block) {
        if (STBUtil.isInfiniteWaterSource(block)) {
            return;
        }

        switch (block.getType()) {
        case WATER:
            block.setType(Material.AIR);
            break;
        case LAVA:
            block.setType(Material.STONE);
            break;
        default:
            break;
        }
    }

    private ItemStack makeProcessingItem(Block toPump, Material container) {
        if (!STBUtil.isLiquidSourceBlock(toPump)) {
            return null;
        }
        Material res;
        switch (container) {
        case BUCKET:
            switch (toPump.getType()) {
            case LAVA:
                res = Material.LAVA_BUCKET;
                break;
            case WATER:
                res = Material.WATER_BUCKET;
                break;
            default:
                res = null;
            }
            break;
        case GLASS_BOTTLE:
            switch (toPump.getType()) {
            case WATER:
                res = Material.POTION;
                break;
            default:
                res = null;
            }
            break;
        default:
            res = null;
        }

        return res == null ? null : new ItemStack(res);
    }

    @Override
    public boolean acceptsItemType(ItemStack stack) {
        return stack.getType() == Material.BUCKET || stack.getType() == Material.GLASS_BOTTLE;
    }
}
