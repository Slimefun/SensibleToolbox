package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import io.github.thebusybiscuit.cscorelib2.math.DoubleHandler;
import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.EnergyNet;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;

public class HolographicMonitor extends BaseSTBBlock {

    private Hologram hologram;

    public HolographicMonitor() {}

    public HolographicMonitor(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.LIGHT_BLUE_STAINED_GLASS;
    }

    @Override
    public String getItemName() {
        return "Holographic Monitor";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Displays the Net Gain/Loss", "using Holograms" };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("GGG", "LPL", "GGG");
        PowerMonitor monitor = new PowerMonitor();
        registerCustomIngredients(monitor);
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('P', monitor.getMaterial());
        recipe.setIngredient('L', Material.LAPIS_LAZULI);
        return recipe;
    }

    @Override
    public int getTickRate() {
        return 120;
    }

    @Override
    public void onServerTick() {
        super.onServerTick();
        if (hologram == null) {
            return;
        }
        this.hologram.clearLines();

        for (BlockFace f : STBUtil.MAIN_HORIZONTAL_BLOCK_FACES) {
            EnergyNet net = SensibleToolbox.getEnergyNet(getRelativeLocation(f).getBlock());
            if (net != null) {
                double stat = net.getSupply() - net.getDemand();
                String prefix;

                if (stat > 0) {
                    prefix = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "+";
                } else {
                    prefix = ChatColor.DARK_RED + "" + ChatColor.BOLD + "-";
                }

                this.hologram.appendTextLine(prefix + " " + ChatColor.GRAY + DoubleHandler.getFancyDouble(Double.valueOf(String.valueOf(stat).replace("-", ""))) + " SCU/t");
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
