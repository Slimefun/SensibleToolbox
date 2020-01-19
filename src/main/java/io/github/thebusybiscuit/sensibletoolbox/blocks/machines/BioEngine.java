package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.Arrays;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.FuelItems;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;
import io.github.thebusybiscuit.sensibletoolbox.items.energycells.TenKEnergyCell;
import io.github.thebusybiscuit.sensibletoolbox.items.machineupgrades.RegulatorUpgrade;

public class BioEngine extends Generator {
	
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.LIME);
	private static final int TICK_FREQUENCY = 10;
	private static final FuelItems fuelItems = new FuelItems();
    private final double slowBurnThreshold;
    private FuelItems.FuelValues currentFuel;

    static {
        fuelItems .addFuel(new ItemStack(Material.ROTTEN_FLESH), true, 2, 60);
        fuelItems.addFuel(new ItemStack(Material.SPIDER_EYE), true, 2.5, 60);
        fuelItems.addFuel(new ItemStack(Material.BONE), true, 2, 60);
        fuelItems.addFuel(new ItemStack(Material.INK_SACK), true, 3, 60);
        fuelItems.addFuel(new ItemStack(Material.SLIME_BALL), true, 6, 80);
        for (TreeSpecies species : TreeSpecies.values()) {
        	fuelItems.addFuel(STBUtil.makeLeaves(species).toItemStack(1), true, 6, 40);
        	fuelItems.addFuel(STBUtil.makeSapling(species).toItemStack(1), true, 6, 60);
        }
        fuelItems.addFuel(new ItemStack(Material.LONG_GRASS), true, 4, 80);
        fuelItems.addFuel(new ItemStack(Material.APPLE), true, 10, 100);
        fuelItems.addFuel(new ItemStack(Material.MELON), true, 10, 100);
        fuelItems.addFuel(new ItemStack(Material.MELON_SEEDS), true, 10, 100);
        fuelItems.addFuel(new ItemStack(Material.MELON_BLOCK), true, 10, 900);
        fuelItems.addFuel(new ItemStack(Material.PUMPKIN), true, 10, 100);
        fuelItems.addFuel(new ItemStack(Material.PUMPKIN_SEEDS), true, 10, 100);
        fuelItems.addFuel(new ItemStack(Material.WHEAT), true, 10, 100);
        fuelItems.addFuel(new ItemStack(Material.SEEDS), true, 10, 100);
        fuelItems.addFuel(new ItemStack(Material.CARROT_ITEM), true, 10, 100);
        fuelItems.addFuel(new ItemStack(Material.POTATO_ITEM), true, 10, 100);
        fuelItems.addFuel(new ItemStack(Material.SUGAR_CANE), true, 8, 100);
        fuelItems.addFuel(new ItemStack(Material.NETHER_STALK), true, 12, 140);
        fuelItems.addFuel(new ItemStack(Material.DIRT), true, 0.5, 20);
        fuelItems.addFuel(new ItemStack(Material.GRASS), true, 0.5, 20);
        fuelItems.addFuel(new ItemStack(Material.YELLOW_FLOWER), true, 11, 80);
        fuelItems.addFuel(new ItemStack(Material.RED_ROSE), true, 11, 80);
        fuelItems.addFuel(new ItemStack(Material.RED_MUSHROOM), true, 11, 80);
        fuelItems.addFuel(new ItemStack(Material.BROWN_MUSHROOM), true, 11, 80);
        fuelItems.addFuel(new ItemStack(Material.VINE), true, 8, 80);
        fuelItems.addFuel(new ItemStack(Material.CACTUS), true, 8, 100);
        fuelItems.addFuel(new ItemStack(Material.WATER_LILY), true, 8, 80);
        fuelItems.addFuel(new ItemStack(Material.DOUBLE_PLANT), true, 11, 80);
    }
    
    public BioEngine() {
        super();
        currentFuel = null;
        slowBurnThreshold = getMaxCharge() * 0.75;
    }

    public BioEngine(ConfigurationSection conf) {
        super(conf);
        if (getProgress() > 0) {
            currentFuel = fuelItems.get(getInventory().getItem(getProgressItemSlot()));
        }
        slowBurnThreshold = getMaxCharge() * 0.75;
    }
	
    @Override
    public int[] getInputSlots() {
        return new int[]{10};
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[] {43, 44};
    }

    @Override
    public int getUpgradeLabelSlot() {
        return 42;
    }

    @Override
    protected void playActiveParticleEffect() {
        if (getTicksLived() % 20 == 0) {
            getLocation().getWorld().playEffect(getLocation(), Effect.STEP_SOUND, Material.LEAVES);
        }
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
    public int getInventoryGUISize() {
        return 45;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Bio Engine";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Converts organic Materials into power",
        };
    }

    @Override
    protected boolean isValidUpgrade(HumanEntity player, BaseSTBItem upgrade) {
        if (!super.isValidUpgrade(player, upgrade)) return false;
        if (!(upgrade instanceof RegulatorUpgrade)) {
            STBUtil.complain(player, upgrade.getItemName() + " is not accepted by a " + getItemName());
            return false;
        }
        return true;
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        TenKEnergyCell cell = new TenKEnergyCell();
        registerCustomIngredients(sc, cell);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("CCC", "SES", "RGR");
        recipe.setIngredient('S', sc.getMaterialData());
        recipe.setIngredient('E', STBUtil.makeWildCardMaterialData(cell));
        recipe.setIngredient('C', Material.CAULDRON_ITEM);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        return recipe;
    }

    @Override
    public int getMaxCharge() {
        return 2500;
    }

    @Override
    public int getChargeRate() {
        return 50;
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
        return new ItemStack(Material.FLINT_AND_STEEL);
    }

    @Override
    public boolean acceptsItemType(ItemStack item) {
        return fuelItems.has(item);
    }

    @Override
    public void onServerTick() {
        if (getTicksLived() % TICK_FREQUENCY == 0 && isRedstoneActive()) {
            if (getProcessing() == null && getCharge() < getMaxCharge()) {
                for (int slot : getInputSlots()) {
                    if (getInventoryItem(slot) != null) {
                        pullItemIntoProcessing(slot);
                        break;
                    }
                }
            } else if (getProgress() > 0) {
                // currently processing....
                // if charge is > 75%, burn rate reduces to conserve fuel
                double burnRate = Math.max(getBurnRate() * Math.min(getProgress(), TICK_FREQUENCY), 1.0);
                setProgress(getProgress() - burnRate);
                setCharge(getCharge() + currentFuel.getCharge() * burnRate);
                playActiveParticleEffect();
                if (getProgress() <= 0) {
                    // fuel burnt
                    setProcessing(null);
                    update(false);
                }
            }
        }
        super.onServerTick();
    }

    private double getBurnRate() {
    	return getCharge() < slowBurnThreshold ? 1.0: 1.15 - (getCharge() / getMaxCharge());
    }

    private void pullItemIntoProcessing(int inputSlot) {
        ItemStack stack = getInventoryItem(inputSlot);
        currentFuel = fuelItems.get(stack);
        if (getRegulatorAmount() > 0 && getCharge() + currentFuel.getTotalFuelValue() >= getMaxCharge() && getCharge() > 0) {
            // Regulator prevents pulling fuel in unless there's definitely
            // enough room to store the charge that would be generated
            return;
        }
        setProcessing(makeProcessingItem(currentFuel, stack));
        getProgressMeter().setMaxProgress(currentFuel.getBurnTime());
        setProgress(currentFuel.getBurnTime());
        stack.setAmount(stack.getAmount() - 1);
        setInventoryItem(inputSlot, stack);
        update(false);
    }

    private ItemStack makeProcessingItem(FuelItems.FuelValues fuel, ItemStack input) {
        ItemStack toProcess = input.clone();
        toProcess.setAmount(1);
        ItemMeta meta = toProcess.getItemMeta();
        meta.setLore(Arrays.asList(ChatColor.GRAY.toString() + ChatColor.ITALIC + fuel.toString()));
        toProcess.setItemMeta(meta);
        return toProcess;
    }
    
    public Set<ItemStack> getFuelInformation() {
    	return fuelItems.fuelItems;
    }
}
