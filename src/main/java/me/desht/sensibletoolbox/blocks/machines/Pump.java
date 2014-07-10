package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.items.components.MachineFrame;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class Pump extends AbstractProcessingMachine {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.CYAN);
    private static final int PUMP_FILL_TIME = 40; // 40 ticks to fill a bucket
    private static final double CHARGE_PER_TICK = 0.1 / PUMP_FILL_TIME; // 0.1 SCU to fill a bucket
    private BlockFace pumpFace = BlockFace.DOWN;  // will be configurable later

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
    public Material getProgressIcon() {
        return Material.DIAMOND_BOOTS;
    }

    @Override
    public int[] getInputSlots() {
        return new int[]{10};
    }

    @Override
    public int[] getOutputSlots() {
        return new int[]{14};
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[]{41, 42, 43, 44};
    }

    @Override
    public int getUpgradeLabelSlot() {
        return 40;
    }

    @Override
    protected void playActiveParticleEffect() {
        // nothing right now
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
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Pump";
    }

    @Override
    public String[] getLore() {
        return new String[] {
            "Pumps liquids into a bucket"
        };
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        MachineFrame mf = new MachineFrame();
        registerCustomIngredients(sc, mf);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("PB ", "SIS", "RGR");
        recipe.setIngredient('P', Material.PISTON_BASE);
        recipe.setIngredient('B', Material.BUCKET);
        recipe.setIngredient('S', sc.getMaterialData());
        recipe.setIngredient('I', mf.getMaterialData());
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public void onServerTick() {
        if (!isRedstoneActive()) {
            return;
        }

        int inputSlot = getInputSlots()[0];
        ItemStack stackIn = getInventoryItem(inputSlot);

        // TODO: for lava pumping, we need to seek the available lava source block
        Block toPump = findNextBlockToPump();

        if (getProcessing() == null && stackIn != null) {
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
            setCharge(getCharge() - getPowerMultiplier() * CHARGE_PER_TICK * getTickRate());
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
        // TODO: for lava pumping, we need to seek the next available lava source block
        return getRelativeLocation(pumpFace).getBlock();
    }

    private void replacePumpedBlock(Block block) {
        if (STBUtil.isInfiniteWaterSource(block)) {
            return;
        }
        switch (block.getType()) {
            case WATER: case STATIONARY_WATER:
                block.setType(Material.AIR);
                break;
            case LAVA: case STATIONARY_LAVA:
                block.setType(Material.COBBLESTONE);
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
                    case LAVA: case STATIONARY_LAVA:
                        res = Material.LAVA_BUCKET;
                        break;
                    case WATER: case STATIONARY_WATER:
                        res = Material.WATER_BUCKET;
                        break;
                    default:
                        res = null;
                }
                break;
            case GLASS_BOTTLE:
                switch (toPump.getType()) {
                    case WATER: case STATIONARY_WATER:
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
