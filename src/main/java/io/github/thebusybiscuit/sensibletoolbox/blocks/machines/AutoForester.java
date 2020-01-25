package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.AutoFarmingMachine;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;
import me.mrCookieSlime.CSCoreLibPlugin.general.Block.TreeCalculator;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;

public class AutoForester extends AutoFarmingMachine {
	
    private static final Set<Material> logs = new HashSet<>();
    private static final int radius = 5;
    
    static {
    	logs.add(Material.LOG);
    	logs.add(Material.LOG_2);
    }
    
    private Set<Block> blocks;
    private MaterialData buffer;

    public AutoForester() {
        blocks = new HashSet<>();
    }

    public AutoForester(ConfigurationSection conf) {
        super(conf);
        blocks = new HashSet<>();
    }
    
    @Override
    public Material getMaterial() {
        return Material.BROWN_TERRACOTTA;
    }

    @Override
    public String getItemName() {
        return "Auto Forester";
    }

    @Override
    public String[] getLore() {
        return new String[] {
                "Automatically harvests and replants",
                "Trees",
                "in a " + radius + "x" + radius + " Radius 2 Blocks above the Machine"
        };
    }

    @Override
    public Recipe getRecipe() {
    	MachineFrame frame = new MachineFrame();
    	registerCustomIngredients(frame);
        ShapedRecipe res = new ShapedRecipe(getKey(), toItemStack());
        res.shape("A A", "IFI", "RGR");
        res.setIngredient('R', Material.REDSTONE);
        res.setIngredient('G', Material.GOLD_INGOT);
        res.setIngredient('I', Material.IRON_INGOT);
        res.setIngredient('A', Material.IRON_AXE);
        res.setIngredient('F', frame.getMaterial());
        return res;
    }
    
    @Override
    public void onBlockRegistered(Location location, boolean isPlacing) {
    	int i = radius / 2;
    	for (int x = -i; x <= i; x++) {
    		for (int z = -i; z <= i; z++) {
        		blocks.add(new Location(location.getWorld(), location.getBlockX() + x, location.getBlockY() + 2, location.getBlockZ() + z).getBlock());
        	}
    	}
    	
    	super.onBlockRegistered(location, isPlacing);
    }

	@SuppressWarnings("deprecation")
	@Override
    public void onServerTick() {
    	if (!isJammed()) {
    		for (Block log: blocks) {
        		if (logs.contains(log.getType())) {
        			if (getCharge() >= getScuPerCycle()) setCharge(getCharge() - getScuPerCycle());
        			else break;
        			List<Location> list = new ArrayList<>();
        			TreeCalculator.getTree(log.getLocation(), log.getLocation(), list);
        			
        			for (Location l: list) {
        				buffer = new MaterialData(l.getBlock().getType(), l.getBlock().getData());
                		setJammed(!output(buffer));
        				log.getWorld().playEffect(l, Effect.STEP_SOUND, l.getBlock().getType());
        				if (blocks.contains(l.getBlock())) {
							byte data = l.getBlock().getData();
							if (l.getBlock().getType() == Material.LOG_2) data = (byte) (data + 4);
        					l.getBlock().setType(Material.SAPLING);
        					l.getBlock().setData(data);
        				}
        				else l.getBlock().setType(Material.AIR);
        			}
        			break;
        		}
        	}
    	}
    	else if (buffer != null) setJammed(!output(buffer));
    	
        super.onServerTick();
    }

	@SuppressWarnings("deprecation")
	private boolean output(MaterialData m) {
		for (int slot: getOutputSlots()) {
			ItemStack stack = getInventoryItem(slot);
			if (stack == null || (stack.getType() == m.getItemType() && stack.getData().getData() == m.getData() && stack.getAmount() < stack.getMaxStackSize())) {
				if (stack == null) stack = m.toItemStack(1);
				setInventoryItem(slot, new CustomItem(stack, stack.getAmount() + 1));
				buffer = null;
				return true;
			}
		}
		return false;
	}
	
	@Override
    public double getScuPerCycle() {
        return 250.0;
    }
}
