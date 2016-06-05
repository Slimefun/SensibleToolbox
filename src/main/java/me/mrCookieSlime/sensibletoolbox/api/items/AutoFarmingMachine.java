package me.mrCookieSlime.sensibletoolbox.api.items;

import me.mrCookieSlime.sensibletoolbox.api.STBInventoryHolder;
import me.mrCookieSlime.sensibletoolbox.api.util.VanillaInventoryUtils;
import me.mrCookieSlime.sensibletoolbox.core.storage.LocationManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public abstract class AutoFarmingMachine extends BaseSTBMachine {

	public AutoFarmingMachine() {
		super();
	}
	
	public AutoFarmingMachine(ConfigurationSection conf) {
		super(conf);
	}

	public abstract double getScuPerCycle();
	
	@Override
	public boolean acceptsEnergy(BlockFace face) {
		return true;
	}

	@Override
	public boolean suppliesEnergy(BlockFace face) {
		return false;
	}
	
	@Override
    public int getTickRate() {
        return 60;
    }

	@Override
	public int getMaxCharge() {
		return 2500;
	}

	@Override
	public int getChargeRate() {
		return 25;
	}

	@Override
    public int[] getInputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[]{10, 11, 12, 13, 14, 15};
    }
    
    @Override
    public int[] getUpgradeSlots() {
        return new int[]{43, 44};
    }

    @Override
    public int getUpgradeLabelSlot() {
        return 42;
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
    public void onServerTick() {
    	handleAutoEjection();
    	super.onServerTick();
    }
    
    protected void handleAutoEjection() {
        if (getAutoEjectDirection() != null && getAutoEjectDirection() != BlockFace.SELF) {
            for (int slot : getOutputSlots()) {
                ItemStack stack = getInventoryItem(slot);
                if (stack != null) {
                    if (autoEject(stack)) {
                    	int amount = stack.getAmount() > 3 ? stack.getAmount() - 4: 0;
                        stack.setAmount(amount);
                        setInventoryItem(slot, stack);
                        setJammed(false);
                    }
                    break;
                }
            }
        }
    }

    private boolean autoEject(ItemStack result) {
        Location loc = getRelativeLocation(getAutoEjectDirection());
        Block target = loc.getBlock();
        ItemStack item = result.clone();
        item.setAmount(1);
        if (!target.getType().isSolid() || target.getType() == Material.WALL_SIGN) {
            // no (solid) block there - just drop the item
            Item i = loc.getWorld().dropItem(loc.add(0.5, 0.5, 0.5), item);
            i.setVelocity(new Vector(0, 0, 0));
            return true;
        } 
        else {
            BaseSTBBlock stb = LocationManager.getManager().get(loc);
            int nInserted = stb instanceof STBInventoryHolder ?
                    ((STBInventoryHolder) stb).insertItems(item, getAutoEjectDirection().getOppositeFace(), false, getOwner()) :
                    VanillaInventoryUtils.vanillaInsertion(target, item, 1, getAutoEjectDirection().getOppositeFace(), false, getOwner());
            return nInserted > 0;
        }
    }
    
}
