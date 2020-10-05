package io.github.thebusybiscuit.sensibletoolbox.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.utils.SoilSaturation;

public class MoistureChecker extends BaseSTBItem {

    public MoistureChecker() {
        super();
    }

    public MoistureChecker(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.GHAST_TEAR;
    }

    @Override
    public String getItemName() {
        return "Moisture Checker";
    }

    @Override
    public String[] getLore() {
        int r = getRadius() * 2 + 1;
        return new String[] { "Tests the saturation level", " of a " + r + "x" + r + " area of farmland.", "R-click: " + ChatColor.WHITE + "use" };
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("S", "C", "I");
        recipe.setIngredient('S', Material.OAK_SIGN);
        recipe.setIngredient('C', sc.getMaterial());
        recipe.setIngredient('I', Material.GOLDEN_SWORD);
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    protected int getRadius() {
        return 1;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = event.getClickedBlock();

            if (STBUtil.isCrop(b.getType())) {
                b = b.getRelative(BlockFace.DOWN);
            }

            List<Location> l = new ArrayList<>();

            for (int i = -getRadius(); i <= getRadius(); i++) {
                for (int j = -getRadius(); j <= getRadius(); j++) {
                    Block b1 = b.getRelative(i, 0, j);

                    if (b1.getType() == Material.FARMLAND) {
                        l.add(b1.getLocation());
                    }
                }
            }

            if (!l.isEmpty()) {
                Bukkit.getScheduler().runTask(getProviderPlugin(), () -> {
                    for (Location loc : l) {
                        player.sendBlockChange(loc, getWoolFromSaturationlevel(loc.getBlock()));
                    }
                });

                Bukkit.getScheduler().runTaskLater(getProviderPlugin(), () -> {
                    for (Location loc : l) {
                        player.sendBlockChange(loc, loc.getBlock().getBlockData());
                    }
                }, 30L);

                event.setCancelled(true);
            }
        }
    }

    private BlockData getWoolFromSaturationlevel(Block b) {
        long now = System.currentTimeMillis();
        long delta = (now - SoilSaturation.getLastWatered(b)) / 1000;
        int saturation = SoilSaturation.getSaturationLevel(b);
        saturation = Math.max(0, saturation - (int) delta);

        if (saturation < 10) {
            return Material.YELLOW_WOOL.createBlockData();
        }
        else if (saturation < 30) {
            return Material.BROWN_WOOL.createBlockData();
        }
        else if (saturation < 50) {
            return Material.GREEN_WOOL.createBlockData();
        }
        else if (saturation < 70) {
            return Material.LIGHT_BLUE_WOOL.createBlockData();
        }
        else if (saturation < 90) {
            return Material.CYAN_WOOL.createBlockData();
        }
        else {
            return Material.BLUE_WOOL.createBlockData();
        }
    }
}
