package me.mrCookieSlime.sensibletoolbox.slimefun.machines;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import me.mrCookieSlime.CSCoreLibPlugin.CSCoreLib;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.InvUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;
import me.mrCookieSlime.sensibletoolbox.api.recipes.FuelItems;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.Generator;
import me.mrCookieSlime.sensibletoolbox.items.machineupgrades.RegulatorUpgrade;
import me.mrCookieSlime.sensibletoolbox.slimefun.STBSlimefunMachine;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class NuclearReactor extends Generator implements STBSlimefunMachine {
	
	public static final ItemStack COOLANT_ITEM = new CustomItem(new MaterialData(Material.IRON_INGOT), "&bCoolant Cell", "", "&rUsed to cool Reactors");
	public static final ItemStack NEPTUNIUM = new CustomItem(new MaterialData(Material.SLIME_BALL), "&aNeptunium", "&4&oHazmat Suit highly recommended!");
	public static final ItemStack PLUTONIUM = new CustomItem(new MaterialData(Material.CLAY_BALL), "&7Plutonium", "&4&oHazmat Suit highly recommended!");
	
	private static final MaterialData md = new MaterialData(Material.IRON_BLOCK);
	private static final int TICK_FREQUENCY = 10;
	private static final FuelItems fuelItems = new FuelItems();
    private static final BlockFace[] cooling = 
    	{
    		BlockFace.NORTH,
    		BlockFace.NORTH_EAST,
    		BlockFace.EAST,
    		BlockFace.SOUTH_EAST,
    		BlockFace.SOUTH,
    		BlockFace.SOUTH_WEST,
    		BlockFace.WEST,
    		BlockFace.NORTH_WEST
    	};
    
    static {
    	SlimefunItem.setRadioactive(NEPTUNIUM);
    	SlimefunItem.setRadioactive(PLUTONIUM);
    }
    
    private final double slowBurnThreshold;
    private FuelItems.FuelValues currentFuel;

    static {
    	fuelItems.addFuel(SlimefunItems.URANIUM, true, 100, 5 * 60 * 20);
    	fuelItems.addFuel(NEPTUNIUM, true, 50, 2 * 60 * 20);
    }
    
    public NuclearReactor() {
        super();
        currentFuel = null;
        slowBurnThreshold = getMaxCharge() * 0.75;
    }

    public NuclearReactor(ConfigurationSection conf) {
        super(conf);
        if (getProgress() > 0) currentFuel = fuelItems.get(getInventory().getItem(getProgressItemSlot()));
        slowBurnThreshold = getMaxCharge() * 0.75;
    }
	
    @Override
    public int[] getInputSlots() {
        return new int[]{10};
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] {14, 15};
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
        if (getTicksLived() % 20 == 0) getLocation().getWorld().playEffect(getLocation(), Effect.STEP_SOUND, Material.PORTAL);
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
        return "Nuclear Reactor";
    }

    @Override
    public String[] getLore() {
        return new String[] {
                "- Runs on Uranium",
                "- Must be surrounded by Water",
                "  otherwise it will explode"
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
        return null;
    }

    @Override
    public int getMaxCharge() {
        return 50000;
    }

    @Override
    public int getChargeRate() {
        return 512;
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
        return SlimefunManager.isItemSimiliar(item, SlimefunItems.URANIUM, true);
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
            } 
            else if (getProgress() > 0) {
            	Block block = getRelativeLocation(cooling[CSCoreLib.randomizer().nextInt(cooling.length)]).getBlock();
            	if (!block.isLiquid()) {
            		explode();
            		return;
            	}
            	else {
            		boolean cool = true;
            		if (getTicksLived() % (TICK_FREQUENCY * 50) == 0) {
            			cool = false;
            			Block port = getRelativeLocation(BlockFace.DOWN).getBlock();
            			SlimefunItem slimefun = BlockStorage.check(port);
                		if (!(port.getState() instanceof Dispenser)) {
                    		explode();
                    		return;
                    	}
                		else if (slimefun == null) {
                    		explode();
                    		return;
                    	}
                		else if (!slimefun.getName().equalsIgnoreCase("REACTOR_COOLANT_PORT")) {
                    		explode();
                    		return;
                    	}
                		else {
                			Dispenser coolantPort = (Dispenser) port.getState();
                			for (int i = 0; i < 9; i++) {
                				ItemStack item = coolantPort.getInventory().getItem(i);
                				if (SlimefunManager.isItemSimiliar(item, COOLANT_ITEM, true)) {
                					coolantPort.getInventory().setItem(i, InvUtils.decreaseItem(item, 1));
                					cool = true;
                					break;
                				}
                			}
                		}
            		}
            		
            		if (cool) {
        				// currently processing....
                        // if charge is > 75%, burn rate reduces to conserve fuel
                        double burnRate = Math.max(getBurnRate() * Math.min(getProgress(), TICK_FREQUENCY), 1.0);
                        setProgress(getProgress() - burnRate);
                        setCharge(getCharge() + currentFuel.getCharge() * burnRate);
                        playActiveParticleEffect();
                        if (getProgress() <= 0) {
                            // fuel burnt
                        	int index = SlimefunManager.isItemSimiliar(getProcessing(), NEPTUNIUM, true) ? 1: 0;
                        	ItemStack result = getInventoryItem(getOutputSlots()[index]);
                        	if (result == null) result = index == 1 ? PLUTONIUM: NEPTUNIUM;
                        	else result.setAmount(result.getAmount() + 1);
                            setInventoryItem(getOutputSlots()[0], result);
                            setProcessing(null);
                            update(false);
                        }
        			}
        			else {
                		explode();
                		return;
                	}
            	}
            }
        }
        super.onServerTick();
    }

    protected void explode() {
    	breakBlock(false);
		getLocation().getWorld().createExplosion(getLocation(), 12.0F);
		BlockStorage.retrieve(getRelativeLocation(BlockFace.DOWN).getBlock());
	}

	private double getBurnRate() {
    	return getCharge() < slowBurnThreshold ? 1.0: 1.15 - (getCharge() / getMaxCharge());
    }

    private void pullItemIntoProcessing(int inputSlot) {
    	if (getInventoryItem(getOutputSlots()[0]) != null && getInventoryItem(getOutputSlots()[0]).getAmount() >= getInventoryItem(getOutputSlots()[0]).getMaxStackSize()) return;
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

	@Override
	public List<ItemStack> getSlimefunRecipe() {
		return Arrays.asList(
				SlimefunItems.REINFORCED_PLATE,
				SlimefunItems.REINFORCED_PLATE,
				SlimefunItems.REINFORCED_PLATE,
				SlimefunItems.REINFORCED_PLATE,
				SlimefunItems.ADVANCED_CIRCUIT_BOARD,
				SlimefunItems.REINFORCED_PLATE,
				SlimefunItems.REINFORCED_PLATE,
				SlimefunItems.REINFORCED_PLATE,
				SlimefunItems.REINFORCED_PLATE
		);
	}
}
