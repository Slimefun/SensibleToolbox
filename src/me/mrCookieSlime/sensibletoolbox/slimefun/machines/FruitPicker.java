package me.mrCookieSlime.sensibletoolbox.slimefun.machines;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.ExoticGarden.Berry;
import me.mrCookieSlime.ExoticGarden.ExoticGarden;
import me.mrCookieSlime.ExoticGarden.PlantType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.sensibletoolbox.api.items.AutoFarmingMachine;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.slimefun.STBSlimefunMachine;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

public class FruitPicker extends AutoFarmingMachine implements STBSlimefunMachine {
	
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.BROWN);
    private static final int radius = 5;
    
    private Set<Block> blocks;
    private ItemStack buffer;

    public FruitPicker() {
    	super();
        blocks = new HashSet<Block>();
    }

    public FruitPicker(ConfigurationSection conf) {
        super(conf);
        blocks = new HashSet<Block>();
    }
    
    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Fruit Picker";
    }

    @Override
    public String[] getLore() {
        return new String[] {
                "Automatically harvests and replants",
                "Exotic Garden Bushes and Plants",
                "in a " + radius + "x" + radius + " Radius 2 Blocks above the Machine"
        };
    }

    @Override
    public Recipe getRecipe() {
    	return null;
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
    
	@Override
    public void onServerTick() {
    	if (!isJammed()) {
    		for (Block crop: blocks) {
    			ItemStack item = harvest(crop);
        		if (item != null) {
        			if (getCharge() >= getScuPerCycle()) setCharge(getCharge() - getScuPerCycle());
        			else break;
            		setJammed(!output(item));
        			break;
        		}
        	}
    	}
    	else if (buffer != null) setJammed(!output(buffer));
    	
        super.onServerTick();
    }
    
	private boolean output(ItemStack item) {
		for (int slot: getOutputSlots()) {
			ItemStack stack = getInventoryItem(slot);
			if (stack == null || (SlimefunManager.isItemSimiliar(stack, item, true) && stack.getAmount() < stack.getMaxStackSize())) {
				if (stack == null) setInventoryItem(slot, item);
				else setInventoryItem(slot, new CustomItem(stack, stack.getAmount() + 1));
				buffer = null;
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private ItemStack harvest(Block block) {
		Berry berry = ExoticGarden.getBerry(block);
		if (berry == null) return null;
		block.setType(Material.SAPLING);
		if (berry.getType() == PlantType.DOUBLE_PLANT) {
			block.getWorld().playEffect(block.getRelative(BlockFace.UP).getLocation(), Effect.STEP_SOUND, Material.LEAVES);
			block.getRelative(BlockFace.UP).setType(Material.AIR);
			BlockStorage.retrieve(block.getRelative(BlockFace.UP));
		}
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.LEAVES);
		block.setData((byte) 0);
		BlockStorage.store(block, SlimefunItem.getByName(berry.toBush()).getItem());
		return berry.getItem();
	}
    
    @Override
    public double getScuPerCycle() {
        return 25.0;
    }

	@Override
	public List<ItemStack> getSlimefunRecipe() {
		return Arrays.asList(
				null,
				new ItemStack(Material.GOLD_HOE),
				null,
				SlimefunItems.GOLD_8K,
				SlimefunItems.BASIC_CIRCUIT_BOARD,
				SlimefunItems.GOLD_8K,
				SlimefunItems.BRONZE_INGOT,
				SlimefunItems.BRONZE_INGOT,
				SlimefunItems.BRONZE_INGOT
		);
	}
}
