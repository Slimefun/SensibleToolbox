package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import me.desht.sensibletoolbox.items.energycells.TenKEnergyCell;
import me.desht.sensibletoolbox.recipes.FuelItems;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.CoalType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Coal;
import org.bukkit.material.MaterialData;

import java.util.Arrays;

public class HeatEngine extends Generator {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.ORANGE);
    private static final FuelItems fuelItems = new FuelItems();
    private static final int TICK_FREQUENCY = 10;
    private final double slowBurnThreshold;

    static {
        fuelItems.addFuel(new Coal(CoalType.CHARCOAL).toItemStack(), false, 15, 80);
        fuelItems.addFuel(new ItemStack(Material.COAL), false, 15, 120);
        fuelItems.addFuel(new ItemStack(Material.COAL_BLOCK), true, 15, 1440);
        fuelItems.addFuel(new ItemStack(Material.BLAZE_ROD), true, 15, 180);
        fuelItems.addFuel(new ItemStack(Material.BLAZE_POWDER), true, 22.5, 30);
        fuelItems.addFuel(new ItemStack(Material.LOG), true, 10, 40);
        fuelItems.addFuel(new ItemStack(Material.LOG_2), true, 10, 40);
        fuelItems.addFuel(new ItemStack(Material.WOOD), true, 5, 20);
        fuelItems.addFuel(new ItemStack(Material.STICK), true, 2.5, 20);
        fuelItems.addFuel(new ItemStack(Material.FIREBALL), true, 50, 20);
    }

    private FuelItems.FuelValues currentFuel;

    public HeatEngine() {
        super();
        currentFuel = null;
        slowBurnThreshold = getMaxCharge() * 0.75;
    }

    public HeatEngine(ConfigurationSection conf) {
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
        return new int[0];  // no output slot
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[0]; // maybe in future
    }

    @Override
    public int getUpgradeLabelSlot() {
        return -1;
    }

    @Override
    protected void playActiveParticleEffect() {
        if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled() && getTicksLived() % 20 == 0) {
            ParticleEffect.FLAME.play(getLocation().add(0.5, 0.5, 0.5), 0.5f, 0.5f, 0.5f, 0.001f, 15);
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
        return "Heat Engine";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Converts burnable items to power",
        };
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        TenKEnergyCell cell = new TenKEnergyCell();
        registerCustomIngredients(sc, cell);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("III", "SCE", "RGR");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', sc.getMaterialData());
        recipe.setIngredient('E', STBUtil.makeWildCardMaterialData(cell));
        recipe.setIngredient('C', Material.CAULDRON_ITEM);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        return recipe;
    }

    @Override
    public int getMaxCharge() {
        return 5000;
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
    public Material getProgressIcon() {
        return Material.FLINT_AND_STEEL;
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
                    updateBlock(false);
                }
            }
        }
        super.onServerTick();
    }

    private double getBurnRate() {
        if (getCharge() < slowBurnThreshold) {
            return 1.0;
        } else {
            return 1.15 - (getCharge() / getMaxCharge());
        }
    }

    private void pullItemIntoProcessing(int inputSlot) {
        ItemStack stack = getInventoryItem(inputSlot);
        currentFuel = fuelItems.get(stack);
        setProcessing(makeProcessingItem(currentFuel, stack));
        getProgressMeter().setMaxProgress(currentFuel.getBurnTime());
        setProgress(currentFuel.getBurnTime());
        stack.setAmount(stack.getAmount() - 1);
        setInventoryItem(inputSlot, stack);
        updateBlock(false);
    }

    private ItemStack makeProcessingItem(FuelItems.FuelValues fuel, ItemStack input) {
        ItemStack toProcess = input.clone();
        toProcess.setAmount(1);
        ItemMeta meta = toProcess.getItemMeta();
        meta.setLore(Arrays.asList(ChatColor.GRAY.toString() + ChatColor.ITALIC + fuel.toString()));
        toProcess.setItemMeta(meta);
        return toProcess;
    }


}
