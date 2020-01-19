package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import me.mrCookieSlime.CSCoreLibPlugin.CSCoreLib;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.AbstractProcessingMachine;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.items.components.FishBait;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;

public class FishingNet extends AbstractProcessingMachine {
	
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.WHITE);
    private static final int FISHING_TIME = 600; // 600 ticks (30 Seconds) to catch a Fish
    @SuppressWarnings("deprecation")
	private static final ItemStack[] fish = 
	{
    	new MaterialData(Material.RAW_FISH, (byte) 0).toItemStack(1), 
    	new MaterialData(Material.RAW_FISH, (byte) 1).toItemStack(1),
    	new MaterialData(Material.RAW_FISH, (byte) 2).toItemStack(1), 
    	new MaterialData(Material.RAW_FISH, (byte) 3).toItemStack(1)
    }; // Catchable Fish

    public FishingNet() {
        super();
    }

    public FishingNet(ConfigurationSection conf) {
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
        return new ItemStack(Material.FISHING_ROD);
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
        getLocation().getWorld().playEffect(getLocation(), Effect.STEP_SOUND, Material.WEB);
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
        return 2000;
    }

    @Override
    public int getChargeRate() {
        return 25;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Fishing Net";
    }

    @Override
    public String[] getLore() {
        return new String[] {
            "Catches Fish"
        };
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        MachineFrame mf = new MachineFrame();
        registerCustomIngredients(sc, mf);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape(" F ", "SMS", "RGR");
        recipe.setIngredient('F', Material.FISHING_ROD);
        recipe.setIngredient('S', sc.getMaterialData());
        recipe.setIngredient('M', mf.getMaterialData());
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public double getScuPerTick() {
        return 2.0;
    }

    @Override
    public void onServerTick() {
    	int inputSlot = getInputSlots()[0];
        ItemStack input = getInventoryItem(inputSlot);

        if (getProcessing() == null && input != null && isRedstoneActive() && getRelativeLocation(BlockFace.DOWN).getBlock().getType() == Material.STATIONARY_WATER) {
            // pull a bucket from the input stack into processing
            ItemStack toProcess = fish[CSCoreLib.randomizer().nextInt(fish.length)];
            setProcessing(toProcess);
            if (toProcess != null) {
                getProgressMeter().setMaxProgress(FISHING_TIME);
                setProgress(FISHING_TIME);
                input.setAmount(input.getAmount() - 1);
                setInventoryItem(inputSlot, input);
            }
        }

        if (getProgress() > 0 && getCharge() > 0) {
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
            } 
            else setJammed(true);
        }

        handleAutoEjection();

        super.onServerTick();
    }

    @Override
    public boolean acceptsItemType(ItemStack stack) {
    	ItemMeta bait = new FishBait().toItemStack().getItemMeta();
        if (!stack.hasItemMeta()) return false;
        if (!stack.getItemMeta().hasLore()) return false;
        if (!stack.getItemMeta().hasDisplayName()) return false;
        if (stack.getItemMeta().getDisplayName().equals(bait.getDisplayName()) && stack.getItemMeta().getLore().toString().equals(bait.getLore().toString())) return true;
        else return false;
    }
}
