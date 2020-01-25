package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.util.BlockProtection;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;

public class BreakerModule extends DirectionalItemRouterModule {
	
    private static final ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE, 1);

    public BreakerModule() {
    }

    public BreakerModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public boolean execute(Location loc) {
        Block b = getTargetLocation(loc).getBlock();
        if (b.isEmpty() || b.isLiquid() || STBUtil.getMaterialHardness(b.getType()) == Double.MAX_VALUE) {
            return false;
        }
        List<ItemStack> d = STBUtil.calculateDrops(b, getBreakerTool());
        if (d.isEmpty()) {
            return false;
        }
        ItemStack[] drops = d.toArray(new ItemStack[d.size()]);
        ItemStack mainDrop = drops[0];
        ItemStack inBuffer = getItemRouter().getBufferItem();
        if (inBuffer == null || inBuffer.isSimilar(mainDrop) && inBuffer.getAmount() < inBuffer.getMaxStackSize()) {
            if (getFilter().shouldPass(mainDrop) && SensibleToolbox.getBlockProtection().playerCanBuild(getItemRouter().getOwner(), b, BlockProtection.Operation.BREAK)) {
                if (inBuffer == null) {
                    getItemRouter().setBufferItem(mainDrop);
                } 
                else {
                    int toAdd = Math.min(mainDrop.getAmount(), inBuffer.getMaxStackSize() - inBuffer.getAmount());
                    getItemRouter().setBufferAmount(inBuffer.getAmount() + toAdd);
                    if (toAdd < mainDrop.getAmount()) {
                        ItemStack stack = mainDrop.clone();
                        stack.setAmount(mainDrop.getAmount() - toAdd);
                        b.getWorld().dropItemNaturally(b.getLocation(), stack);
                    }
                }
                
                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
                b.setType(Material.AIR);
                
                for (int i = 1; i < drops.length; i++) {
                    b.getWorld().dropItemNaturally(b.getLocation(), drops[i]);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Material getMaterial() {
        return Material.YELLOW_DYE;
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Breaker";
    }

    @Override
    public String[] getLore() {
        return new String[] { 
        		"Insert into an Item Router", 
        		"Breaks the block in its", 
        		"configured direction and", 
        		"pulls it into the item router"
        };
    }

    @Override
    public Recipe getRecipe() {
        BlankModule bm = new BlankModule();
        registerCustomIngredients(bm);
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(bm.getMaterial());
        recipe.addIngredient(Material.DIAMOND_PICKAXE);
        recipe.addIngredient(Material.HOPPER);
        return recipe;
    }

    protected ItemStack getBreakerTool() {
        return pick;
    }
}
