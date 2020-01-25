package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.AutoFarmingMachine;
import io.github.thebusybiscuit.sensibletoolbox.items.IronCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;
import me.mrCookieSlime.CSCoreLibPlugin.CSCoreLib;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;

public class AutoFarm extends AutoFarmingMachine {
	
    private static final Map<Material, Material> crops = new HashMap<>();
    private static final int RADIUS = 3;
    
    static {
    	crops.put(Material.WHEAT, Material.WHEAT);
    	crops.put(Material.POTATOES, Material.POTATO);
    	crops.put(Material.CARROTS, Material.CARROT);
    }
    
    private Set<Block> blocks;
    private Material buffer;

    public AutoFarm() {
    	super();
        blocks = new HashSet<>();
    }

    public AutoFarm(ConfigurationSection conf) {
        super(conf);
        blocks = new HashSet<>();
    }
    
    @Override
    public Material getMaterial() {
        return Material.BROWN_TERRACOTTA;
    }

    @Override
    public String getItemName() {
        return "Auto Farm";
    }

    @Override
    public String[] getLore() {
        return new String[] {
                "Automatically harvests and replants",
                "Wheat/Potato/Carrot Crops",
                "in a " + RADIUS + "x" + RADIUS + " Radius 2 Blocks above the Machine"
        };
    }

    @Override
    public Recipe getRecipe() {
    	MachineFrame frame = new MachineFrame();
    	IronCombineHoe hoe = new IronCombineHoe();
    	registerCustomIngredients(frame, hoe);
        ShapedRecipe res = new ShapedRecipe(getKey(), toItemStack());
        res.shape(" H ", "IFI", "RGR");
        res.setIngredient('R', Material.REDSTONE);
        res.setIngredient('G', Material.GOLD_INGOT);
        res.setIngredient('I', Material.IRON_INGOT);
        res.setIngredient('H', hoe.getMaterial());
        res.setIngredient('F', frame.getMaterial());
        return res;
    }
    
    @Override
    public void onBlockRegistered(Location location, boolean isPlacing) {
    	int i = RADIUS / 2;
    	for (int x = -i; x <= i; x++) {
    		for (int z = -i; z <= i; z++) {
        		blocks.add(new Location(location.getWorld(), location.getBlockX() + x, location.getBlockY() + 2, location.getBlockZ() + z).getBlock());
        	}
    	}
    	
    	super.onBlockRegistered(location, isPlacing);
    }
    
	@Override
    public void onServerTick() {
    	if (!isJammed()) {
    		if (getCharge() >= getScuPerCycle()) {
        		for (Block crop : blocks) {
            		if (crops.containsKey(crop.getType())) {
            			Ageable ageable = (Ageable) crop.getBlockData();
            			
            			if (ageable.getAge() >= ageable.getMaximumAge()) {
                			setCharge(getCharge() - getScuPerCycle());
                			
                			ageable.setAge(0);
                			crop.getWorld().playEffect(crop.getLocation(), Effect.STEP_SOUND, crop.getType());
                    		setJammed(!output(crops.get(crop.getType())));
                			break;
            			}
            		}
            	}
    		}
    	}
    	else if (buffer != null) {
    		setJammed(!output(buffer));
    	}
    	
        super.onServerTick();
    }
    
	private boolean output(Material m) {
		for (int slot: getOutputSlots()) {
			ItemStack stack = getInventoryItem(slot);
			if (stack == null || (stack.getType() == m && stack.getAmount() < stack.getMaxStackSize())) {
				if (stack == null) stack = new ItemStack(m);
				int amount = (stack.getMaxStackSize() - stack.getAmount()) > 3 ? (CSCoreLib.randomizer().nextInt(2) + 1): (stack.getMaxStackSize() - stack.getAmount());
				setInventoryItem(slot, new CustomItem(stack, stack.getAmount() + amount));
				buffer = null;
				return true;
			}
		}
		return false;
	}
    
    @Override
    public double getScuPerCycle() {
        return 25.0;
    }
}
