package io.github.thebusybiscuit.sensibletoolbox.blocks;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.GUIUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.NumericGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;

public class SoundMuffler extends BaseSTBBlock {

    public static final int DISTANCE = 8;
    private int volume; // 0-100

    public SoundMuffler() {
        volume = 10;
        createGUI();
    }

    public SoundMuffler(ConfigurationSection conf) {
        super(conf);
        volume = conf.getInt("volume");
        createGUI();
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = GUIUtil.createGUI(this, 9, ChatColor.DARK_AQUA + getItemName());

        gui.addGadget(new NumericGadget(gui, 0, "Volume", new IntRange(0, 100), getVolume(), 10, 1, newValue -> {
            setVolume(newValue);
            return true;
        }));

        return gui;
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("volume", volume);
        return conf;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
        update(false);
    }

    @Override
    public Material getMaterial() {
        return Material.WHITE_WOOL;
    }

    @Override
    public String getItemName() {
        return "Sound Muffler";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Reduces the volume of all sounds", "within a " + DISTANCE + "-block radius", "R-click: " + ChatColor.WHITE + " open configuration" };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("WWW", "WNW", "WWW");
        recipe.setIngredient('W', Material.WHITE_WOOL);
        recipe.setIngredient('N', Material.NOTE_BLOCK);
        return recipe;
    }

    @Override
    public void onBlockRegistered(Location loc, boolean isPlacing) {
        ((SensibleToolboxPlugin) getProviderPlugin()).getSoundMufflerListener().registerMuffler(this);

        super.onBlockRegistered(loc, isPlacing);
    }

    @Override
    public void onBlockUnregistered(Location loc) {
        ((SensibleToolboxPlugin) getProviderPlugin()).getSoundMufflerListener().unregisterMuffler(this);

        super.onBlockUnregistered(loc);
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
            getGUI().show(event.getPlayer());
        }
        super.onInteractBlock(event);
    }

    @Override
    public int getTickRate() {
        return 40;
    }

    @Override
    public String[] getSignLabel(BlockFace face) {
        String[] label = super.getSignLabel(face);
        label[1] = ChatColor.DARK_RED + "Volume " + ChatColor.WHITE + getVolume();
        return label;
    }
}
