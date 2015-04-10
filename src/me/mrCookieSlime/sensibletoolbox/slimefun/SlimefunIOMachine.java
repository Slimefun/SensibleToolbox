package me.mrCookieSlime.sensibletoolbox.slimefun;

import java.util.List;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunMachine;
import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.api.items.AbstractIOMachine;
import me.mrCookieSlime.sensibletoolbox.api.recipes.CustomRecipeManager;
import me.mrCookieSlime.sensibletoolbox.api.recipes.SimpleCustomRecipe;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

public abstract class SlimefunIOMachine extends AbstractIOMachine {
	
	public SlimefunIOMachine() {
    }

    public SlimefunIOMachine(ConfigurationSection conf) {
        super(conf);
    }
	
	protected abstract String getSlimefunMachine();
	public abstract List<ItemStack> getSlimefunRecipe();
	
	@Override
    public void addCustomRecipes(final CustomRecipeManager crm) {
		final SlimefunIOMachine instance = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(SensibleToolboxPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				SlimefunMachine machine = (SlimefunMachine) SlimefunItem.getByName(getSlimefunMachine());
				if (machine != null) {
					List<ItemStack[]> recipes = machine.getRecipes();
					for (int i = 0; i < recipes.size(); i++) {
						if (i % 2 == 0) {
					        crm.addCustomRecipe(new SimpleCustomRecipe(instance, recipes.get(i)[0], recipes.get(i + 1)[0], 140));
						}
					}
				}
			}
		}, 0L);
	}
	
	@Override
    protected abstract void onMachineStartup();
	
	@Override
	public int getMaxCharge() {
		return 3000;
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
	public abstract ItemStack getProgressIcon();
	
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
	public abstract MaterialData getMaterialData();
	
	@Override
	public abstract String getItemName();
	
	@Override
	public abstract String[] getLore();
	
	@Override
	public Recipe getRecipe() {
		return null;
	}

}
