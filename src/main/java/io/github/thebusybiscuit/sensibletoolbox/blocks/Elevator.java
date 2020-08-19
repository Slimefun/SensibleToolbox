package io.github.thebusybiscuit.sensibletoolbox.blocks;

import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Colorable;

import io.github.thebusybiscuit.cscorelib2.materials.MaterialCollections;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;

public class Elevator extends BaseSTBBlock implements Colorable {

    private DyeColor color;

    public Elevator() {
        color = DyeColor.WHITE;
    }

    public Elevator(ConfigurationSection conf) {
        super(conf);
        color = DyeColor.valueOf(conf.getString("color"));
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("color", color.toString());
        return conf;
    }

    public DyeColor getColor() {
        return color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
        update(true);
    }

    @Override
    public Material getMaterial() {
        return MaterialCollections.getAllTerracottaColors().get(color.ordinal());
    }

    @Override
    public String getItemName() {
        return "Elevator";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Links to other elevators", " directly above or below", "Press Space to go up", "Press Shift to go down" };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("WWW", "WPW", "WWW");
        recipe.setIngredient('W', Material.WHITE_WOOL);
        recipe.setIngredient('P', Material.ENDER_PEARL);
        return recipe;
    }

    public Elevator findOtherElevator(BlockFace direction) {
        Validate.isTrue(direction == BlockFace.UP || direction == BlockFace.DOWN, "direction must be UP or DOWN");

        Block b = getLocation().getBlock();
        Elevator res = null;

        while (b.getY() > 0 && b.getY() < b.getWorld().getMaxHeight()) {
            b = b.getRelative(direction);
            if (b.getType().isSolid()) {
                res = SensibleToolbox.getBlockAt(b.getLocation(), Elevator.class, false);
                break;
            }
        }
        return (res != null && res.getColor() == getColor()) ? res : null;
    }
}
