package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.FuelItems;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.FuelValues;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;
import io.github.thebusybiscuit.sensibletoolbox.items.energycells.TenKEnergyCell;
import io.github.thebusybiscuit.sensibletoolbox.items.upgrades.RegulatorUpgrade;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;

public class HeatEngine extends Generator {

    private static final int TICK_FREQUENCY = 10;
    private final double slowBurnThreshold;
    private static final FuelItems fuelItems = new FuelItems();

    private FuelValues currentFuel;

    static {
        fuelItems.addFuel(new ItemStack(Material.CHARCOAL), false, 15, 80);
        fuelItems.addFuel(new ItemStack(Material.COAL), false, 15, 120);

        // 1 coal block is slightly more efficient than 9 coal
        fuelItems.addFuel(new ItemStack(Material.COAL_BLOCK), true, 15, 1120);
        fuelItems.addFuel(new ItemStack(Material.BLAZE_ROD), true, 15, 180);
        fuelItems.addFuel(new ItemStack(Material.BLAZE_POWDER), true, 22.5, 30);

        for (Material log : Tag.LOGS.getValues()) {
            fuelItems.addFuel(new ItemStack(log), true, 10, 40);
        }

        for (Material plank : Tag.PLANKS.getValues()) {
            fuelItems.addFuel(new ItemStack(plank), true, 5, 20);
        }

        fuelItems.addFuel(new ItemStack(Material.STICK), true, 2.5, 20);
        fuelItems.addFuel(new ItemStack(Material.FIRE_CHARGE), true, 50, 20);
    }

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
    public FuelItems getFuelItems() {
        return fuelItems;
    }

    @Override
    public int[] getInputSlots() {
        return new int[] { 10 };
    }

    @Override
    public int[] getOutputSlots() {
        // no output slot
        return new int[0];
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[] { 43, 44 };
    }

    @Override
    public int getUpgradeLabelSlot() {
        return 42;
    }

    @Override
    protected void playActiveParticleEffect() {
        if (getTicksLived() % 20 == 0) {
            getLocation().getWorld().playEffect(getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
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
    public Material getMaterial() {
        return Material.ORANGE_TERRACOTTA;
    }

    @Override
    public String getItemName() {
        return "Heat Engine";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Converts burnable items to power" };
    }

    @Override
    protected boolean isValidUpgrade(HumanEntity player, BaseSTBItem upgrade) {
        if (!super.isValidUpgrade(player, upgrade)) {
            return false;
        }

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
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("III", "SCE", "RGR");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', sc.getMaterial());
        recipe.setIngredient('E', cell.getMaterial());
        recipe.setIngredient('C', Material.CAULDRON);
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
        return getCharge() < slowBurnThreshold ? 1.0 : 1.15 - (getCharge() / getMaxCharge());
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

    private ItemStack makeProcessingItem(FuelValues fuel, ItemStack input) {
        ItemStack toProcess = input.clone();
        toProcess.setAmount(1);
        ItemMeta meta = toProcess.getItemMeta();
        meta.setLore(Arrays.asList(ChatColor.GRAY.toString() + ChatColor.ITALIC + fuel.toString()));
        toProcess.setItemMeta(meta);
        return toProcess;
    }

}
