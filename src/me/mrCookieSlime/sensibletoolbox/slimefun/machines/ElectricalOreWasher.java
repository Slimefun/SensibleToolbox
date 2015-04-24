package me.mrCookieSlime.sensibletoolbox.slimefun.machines;

import java.util.Arrays;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.CSCoreLib;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;
import me.mrCookieSlime.sensibletoolbox.api.items.AbstractProcessingMachine;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.slimefun.STBSlimefunMachine;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

public class ElectricalOreWasher extends AbstractProcessingMachine implements STBSlimefunMachine {
	
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.LIGHT_BLUE);
    private static final int TIME = 200; // 600 ticks (10 Seconds) to wash 1 Sifted ore
    private static final ItemStack[] drops =
    	{
    		SlimefunItems.ALUMINUM_DUST,
    		SlimefunItems.COPPER_DUST,
    		SlimefunItems.GOLD_DUST,
    		SlimefunItems.IRON_DUST,
    		SlimefunItems.LEAD_DUST,
    		SlimefunItems.MAGNESIUM_DUST,
    		SlimefunItems.SILVER_DUST,
    		SlimefunItems.TIN_DUST,
    		SlimefunItems.ZINC_DUST
    	};

    public ElectricalOreWasher() {
        super();
    }

    public ElectricalOreWasher(ConfigurationSection conf) {
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
        return "Electrical Ore Washer";
    }

    @Override
    public String[] getLore() {
        return new String[] {
            "An electrical Ore Washer"
        };
    }

    @Override
    public Recipe getRecipe() {
        return null;
    }

    @Override
    public double getScuPerTick() {
        return 2.0;
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
    	return SlimefunManager.isItemSimiliar(stack, SlimefunItems.SIFTED_ORE, true);
    }

	@Override
	public List<ItemStack> getSlimefunRecipe() {
		return Arrays.asList(
				null,
				new ItemStack(Material.WATER_BUCKET),
				null,
				SlimefunItems.GOLD_8K,
				SlimefunItems.BASIC_CIRCUIT_BOARD,
				SlimefunItems.GOLD_8K,
				SlimefunItems.DAMASCUS_STEEL_INGOT,
				SlimefunItems.DAMASCUS_STEEL_INGOT,
				SlimefunItems.DAMASCUS_STEEL_INGOT
		);
	}

	@Override
	protected void onMachineStartup() {
		if (SensibleToolbox.getPluginInstance().getConfigCache().isNoisyMachines()) getLocation().getWorld().playSound(getLocation(), Sound.SPLASH, 1.0f, 0.75f);
	}
}
