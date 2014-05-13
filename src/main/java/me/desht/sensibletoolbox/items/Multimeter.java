package me.desht.sensibletoolbox.items;

import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.energynet.EnergyNet;
import me.desht.sensibletoolbox.energynet.EnergyNetManager;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.PopupMessage;
import me.desht.sensibletoolbox.util.STBUtil;
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
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sign;

public class Multimeter extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.WATCH);

    public Multimeter() {
        super();
    }

    public Multimeter(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Multimeter";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Use on cabling and machines",
                "to check energy net connections",
                "and power usage",
                "R-Click: " + ChatColor.RESET + "use"
        };
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("IGI", "CSC", " T ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('G', Material.GLOWSTONE_DUST);
        recipe.setIngredient('C', sc.getMaterialData());
        recipe.setIngredient('S', Material.SIGN);
        recipe.setIngredient('T', Material.STICK);
        return recipe;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            EnergyNet net = EnergyNetManager.getEnergyNet(event.getClickedBlock());
            Player player = event.getPlayer();
            if (net != null) {
                showNetInfo(player, net, event.getClickedBlock());
            } else {
                Block b;
                if (event.getClickedBlock().getType() == Material.WALL_SIGN) {
                    Sign sign = (Sign) event.getClickedBlock().getState().getData();
                    b = event.getClickedBlock().getRelative(sign.getAttachedFace());
                } else {
                    b = event.getClickedBlock();
                }
                BaseSTBMachine machine = LocationManager.getManager().get(b.getLocation(), BaseSTBMachine.class);
                if (machine != null && machine.getMaxCharge() > 0) {
                    showMachineInfo(player, machine, event.getClickedBlock());
                } else {
                    // nothing to examine here
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
                }
            }
            event.setCancelled(true);
        }
    }

    private void showNetInfo(final Player player, EnergyNet net, Block clicked) {
        String s1 = net.getCableCount() == 1 ? "" : "s";
        String s2 = net.getSourceCount() == 1 ? "" : "s";
        String s3 = net.getSinkCount() == 1 ? "" : "s";
        String[] lines = new String[] {
                net.getSourceCount() + ChatColor.GOLD.toString() + " source" + s2 + ChatColor.RESET + ", " +
                        net.getSinkCount() + ChatColor.GOLD.toString() + " sink" + s3,
                net.getCableCount() + ChatColor.GOLD.toString() + " cable" + s1,
                String.format("Demand: " + ChatColor.GOLD + "%5.2f SCU/t", net.getDemand()),
                String.format("Supply: " + ChatColor.GOLD + "%5.2f SCU/t", net.getSupply()),
        };
        PopupMessage.quickMessage(player, clicked.getLocation(), lines);
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);
    }

    private void showMachineInfo(Player player, BaseSTBMachine machine, Block clicked) {
        int n = machine.getAttachedEnergyNets().length;
        String s = n == 1 ? "" : "s";
        String[] lines = new String[] {
                ChatColor.GOLD + machine.getItemName() + ChatColor.RESET + ": on " + n + " energy net" + s,
                "Charge: " + STBUtil.getChargeString(machine),
                "Max Charge Rate: " + ChatColor.GOLD + machine.getChargeRate() + " SCU/t",
        };
        PopupMessage.quickMessage(player, clicked.getLocation(), lines);
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);
    }
}
