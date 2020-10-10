package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.items.GoldCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;

public class InfernalFarm extends AutoFarm {

    private static final int RADIUS = 5;

    private Set<Block> blocks;
    private Material buffer;

    public InfernalFarm() {
        blocks = new HashSet<>();
    }

    public InfernalFarm(ConfigurationSection conf) {
        super(conf);
        blocks = new HashSet<>();
    }

    @Override
    public Material getMaterial() {
        return Material.NETHER_BRICKS;
    }

    @Override
    public String getItemName() {
        return "Infernal Farm";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Automatically harvests and replants", "Nether Warts", "in a " + RADIUS + "x" + RADIUS + " Radius 2 Blocks above the Machine" };
    }

    @Override
    public Recipe getRecipe() {
        MachineFrame frame = new MachineFrame();
        GoldCombineHoe hoe = new GoldCombineHoe();
        registerCustomIngredients(frame, hoe);
        ShapedRecipe res = new ShapedRecipe(getKey(), toItemStack());
        res.shape("NHN", "IFI", "RGR");
        res.setIngredient('R', Material.REDSTONE);
        res.setIngredient('G', Material.GOLD_INGOT);
        res.setIngredient('I', Material.IRON_INGOT);
        res.setIngredient('H', hoe.getMaterial());
        res.setIngredient('F', frame.getMaterial());
        res.setIngredient('N', Material.NETHER_BRICK);
        return res;
    }

    @Override
    public void onServerTick() {
        if (!isJammed()) {
            if (getCharge() >= getScuPerCycle()) {
                for (Block crop : blocks) {
                    if (crop.getType() == Material.NETHER_WART) {
                        Ageable ageable = (Ageable) crop.getBlockData();

                        if (ageable.getAge() >= ageable.getMaximumAge()) {
                            setCharge(getCharge() - getScuPerCycle());

                            ageable.setAge(0);
                            crop.getWorld().playEffect(crop.getLocation(), Effect.STEP_SOUND, crop.getType());
                            setJammed(!output(Material.NETHER_WART));
                            break;
                        }
                    }
                }
            }
        } else if (buffer != null) {
            setJammed(!output(buffer));
        }

        super.onServerTick();
    }

    @Override
    public double getScuPerCycle() {
        return 50.0;
    }
}
