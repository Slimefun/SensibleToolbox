package me.mrCookieSlime.sensibletoolbox.slimefun.machines;

import java.util.Arrays;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.CSCoreLib;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.sensibletoolbox.api.items.AbstractProcessingMachine;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.slimefun.STBSlimefunMachine;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

public class ElectricalPanningMachine extends AbstractProcessingMachine implements STBSlimefunMachine {
	
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.GRAY);
    private static final int TIME = 200; // 600 ticks (10 Seconds) to pan 1 Gravel
    private static final ItemStack[] drops =
    	{
    		SlimefunItems.SIFTED_ORE,
    		SlimefunItems.SIFTED_ORE,
    		new ItemStack(Material.FLINT),
    		new ItemStack(Material.CLAY_BALL)
    	};

    public ElectricalPanningMachine() {
        super();
    }

    public ElectricalPanningMachine(ConfigurationSection conf) {
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
        return new ItemStack(Material.IRON_SPADE);
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
        getLocation().getWorld().playEffect(getLocation(), Effect.STEP_SOUND, Material.GRAVEL);
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
        return "Electrical Panning Machine";
    }

    @Override
    public String[] getLore() {
        return new String[] {
            "An electrical Gold Pan",
            "§r+ §b17% Chance for Sifted Ore",
            ""
        };
    }

    @Override
    public Recipe getRecipe() {
        return null;
    }

    @Override
    public double getScuPerTick() {
        return 1.0;
    }

    @Override
    public void onServerTick() {
    	int inputSlot = getInputSlots()[0];
        ItemStack input = getInventoryItem(inputSlot);

        if (getProcessing() == null && input != null && isRedstoneActive()) {
            // pull a bucket from the input stack into processing
            ItemStack toProcess = drops[CSCoreLib.randomizer().nextInt(drops.length)];
            setProcessing(toProcess);
            if (toProcess != null) {
                getProgressMeter().setMaxProgress(TIME);
                setProgress(TIME);
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
    	return stack.getType() == Material.GRAVEL;
    }

	@Override
	public List<ItemStack> getSlimefunRecipe() {
		return Arrays.asList(
				null,
				SlimefunItems.GOLD_PAN,
				null,
				SlimefunItems.GOLD_4K,
				SlimefunItems.BASIC_CIRCUIT_BOARD,
				SlimefunItems.GOLD_4K,
				SlimefunItems.COPPER_INGOT,
				SlimefunItems.COPPER_INGOT,
				SlimefunItems.COPPER_INGOT
		);
	}
}
