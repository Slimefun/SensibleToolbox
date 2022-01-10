package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.items.GoldCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;

public class AutoFarm2 extends AutoFarm {

    private static final Map<Material, Material> crops = new EnumMap<>(Material.class);
    private static final int RADIUS = 5;

    static {
        crops.put(Material.COCOA, Material.COCOA_BEANS);
        crops.put(Material.SWEET_BERRY_BUSH, Material.SWEET_BERRIES);
        crops.put(Material.SUGAR_CANE, Material.SUGAR_CANE);
        crops.put(Material.CACTUS, Material.CACTUS);
    }

    private Set<Block> blocks;
    private Material buffer;

    public AutoFarm2() {
        blocks = new HashSet<>();
    }

    public AutoFarm2(ConfigurationSection conf) {
        super(conf);
        blocks = new HashSet<>();
    }

    @Override
    public String getItemName() {
        return "Auto Farm MkII";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Automatically harvests and replants", "Cocoa Beans/Sugar Cane/Cactus", "in a " + RADIUS + "x" + RADIUS + " Radius 2 Blocks above the Machine" };
    }

    @Override
    public Recipe getMainRecipe() {
        MachineFrame frame = new MachineFrame();
        GoldCombineHoe hoe = new GoldCombineHoe();
        registerCustomIngredients(frame, hoe);
        ShapedRecipe res = new ShapedRecipe(getKey(), toItemStack());
        res.shape("LHL", "IFI", "RGR");
        res.setIngredient('R', Material.REDSTONE);
        res.setIngredient('G', Material.GOLD_INGOT);
        res.setIngredient('I', Material.IRON_INGOT);
        res.setIngredient('L', new MaterialChoice(Tag.LOGS));
        res.setIngredient('H', hoe.getMaterial());
        res.setIngredient('F', frame.getMaterial());
        return res;
    }

    @Override
    public void onServerTick() {
        if (!isJammed()) {
            for (Block crop : blocks) {
                if (crops.containsKey(crop.getType())) {
                    if (crop.getBlockData() instanceof Ageable) {
                        Ageable ageable = (Ageable) crop.getBlockData();

                        if (ageable.getAge() >= ageable.getMaximumAge()) {
                            if (getCharge() >= getScuPerCycle()) {
                                setCharge(getCharge() - getScuPerCycle());
                            } else {
                                break;
                            }

                            ageable.setAge(0);
                            crop.getWorld().playEffect(crop.getLocation(), Effect.STEP_SOUND, crop.getType());
                            setJammed(!output(crops.get(crop.getType())));
                            break;
                        }
                    } else {
                        Block block = crop.getRelative(BlockFace.UP);

                        if (crops.containsKey(block.getType()) && block.getType() != Material.COCOA) {
                            if (getCharge() >= getScuPerCycle()) {
                                setCharge(getCharge() - getScuPerCycle());
                            } else {
                                break;
                            }

                            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
                            setJammed(!output(crops.get(block.getType())));
                            block.setType(Material.AIR);
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
        return 30.0;
    }
}
