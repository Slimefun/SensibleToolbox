package me.mrCookieSlime.sensibletoolbox.blocks.machines;

import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;
import me.mrCookieSlime.sensibletoolbox.api.energy.EnergyNet;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBBlock;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class HolographicMonitor extends BaseSTBBlock {
	
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.LIGHT_BLUE);
    private Hologram hologram;

    public HolographicMonitor() {
    }

    public HolographicMonitor(ConfigurationSection conf) {
        super(conf);
    }
    
	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Holographic Monitor";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Displays the Net Gain/Loss",
				"using Holograms"
		};
	}

	@Override
	public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("GGG", "LPL", "GGG");
        PowerMonitor monitor = new PowerMonitor();
        registerCustomIngredients(monitor);
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('P', monitor.getMaterialData());
        recipe.setIngredient('L', STBUtil.makeColouredMaterial(Material.INK_SACK, DyeColor.BLUE));
        return recipe;
	}
	
	@Override
	public int getTickRate() {
        return 120;
    }
	
	@Override
	public void onServerTick() {
		super.onServerTick();
		if (hologram == null) return;
		this.hologram.clearLines();
		
		for (BlockFace f: STBUtil.mainHorizontalFaces) {
            EnergyNet net = SensibleToolbox.getEnergyNet(getRelativeLocation(f).getBlock());
            if (net != null) {
            	double stat = net.getSupply() - net.getDemand();
            	String prefix;
            	if (stat > 0) prefix = "§2§l+";
            	else prefix = "§4§l-";
            	this.hologram.appendTextLine(prefix + " §7" + DoubleHandler.getFancyDouble(Double.valueOf(String.valueOf(stat).replace("-", ""))) + " SCU/t");
            	break;
            }
        }
    }
	
	@Override
	public void onBlockRegistered(Location location, boolean isPlacing) {
		super.onBlockRegistered(location, isPlacing);
		
		onServerTick();
		this.hologram = HologramsAPI.createHologram(SensibleToolboxPlugin.getInstance(), getLocation().add(0.5, 1.4, 0.5));
	}
	
	@Override
	public void onBlockUnregistered(Location location) {
		super.onBlockUnregistered(location);
		
		this.hologram.delete();
	}
}