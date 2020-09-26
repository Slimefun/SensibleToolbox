package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.EnergyNet;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBMachine;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;
import io.github.thebusybiscuit.sensibletoolbox.util.HoloMessage;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;

public class Multimeter extends BaseSTBItem {

    public Multimeter() {
        super();
    }

    public Multimeter(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.CLOCK;
    }

    @Override
    public String getItemName() {
        return "Multimeter";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Use on cabling and machines", "to check energy net connections", "and power usage", "R-Click: " + ChatColor.WHITE + "use" };
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("IGI", "CSC", " T ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('G', Material.GLOWSTONE_DUST);
        recipe.setIngredient('C', sc.getMaterial());
        recipe.setIngredient('S', Material.OAK_SIGN);
        recipe.setIngredient('T', Material.STICK);
        return recipe;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            EnergyNet net = SensibleToolbox.getEnergyNet(event.getClickedBlock());
            Player player = event.getPlayer();
            if (net != null) {
                showNetInfo(player, net, event.getClickedBlock());
            }
            else {
                BaseSTBMachine machine = SensibleToolbox.getBlockAt(event.getClickedBlock().getLocation(), BaseSTBMachine.class, true);
                if (machine != null && machine.getMaxCharge() > 0) {
                    showMachineInfo(player, machine, event.getClickedBlock());
                }
                else {
                    // nothing to examine here
                    STBUtil.complain(player);
                }
            }
            event.setCancelled(true);
        }
    }

    private void showNetInfo(final Player player, EnergyNet net, Block clicked) {
        String s1 = net.getCableCount() == 1 ? "" : "s";
        String s2 = net.getSourceCount() == 1 ? "" : "s";
        String s3 = net.getSinkCount() == 1 ? "" : "s";
        String[] lines = new String[] { net.getSourceCount() + ChatColor.GOLD.toString() + " source" + s2 + ChatColor.WHITE + ", " + net.getSinkCount() + ChatColor.GOLD.toString() + " sink" + s3, net.getCableCount() + ChatColor.GOLD.toString() + " cable" + s1, String.format("Demand: " + ChatColor.GOLD + "%5.2f SCU/t", net.getDemand()), String.format("Supply: " + ChatColor.GOLD + "%5.2f SCU/t", net.getSupply()), };
        HoloMessage.popup(player, clicked.getLocation(), lines);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }

    private void showMachineInfo(Player player, BaseSTBMachine machine, Block clicked) {
        int n = machine.getAttachedEnergyNets().length;
        String s = n == 1 ? "" : "s";
        String[] lines = new String[] { ChatColor.GOLD + machine.getItemName() + ChatColor.WHITE + ": on " + n + " energy net" + s, "Charge: " + STBUtil.getChargeString(machine), "Max Charge Rate: " + ChatColor.GOLD + machine.getChargeRate() + " SCU/t", };
        HoloMessage.popup(player, clicked.getLocation(), lines);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }
}
