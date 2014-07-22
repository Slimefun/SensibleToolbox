package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.gui.GUIUtil;
import me.desht.sensibletoolbox.api.gui.InventoryGUI;
import me.desht.sensibletoolbox.api.gui.NumericGadget;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

public class SoundMuffler extends BaseSTBBlock {
    private static final MaterialData md = new Wool(DyeColor.WHITE);
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
        gui.addGadget(new NumericGadget(gui, 0, "Volume", new IntRange(0, 100), getVolume(), 10, 1, new NumericGadget.NumericListener() {
            @Override
            public boolean run(int newValue) {
                setVolume(newValue);
                return true;
            }
        }));
        return gui;
    }

    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("volume", volume);
        return conf;
    }

    @Override
    public void setLocation(Location loc) {
        SensibleToolboxPlugin plugin = (SensibleToolboxPlugin) getProviderPlugin();
        if (plugin.isProtocolLibEnabled()) {
            if (loc == null && getLocation() != null) {
                plugin.getSoundMufflerListener().unregisterMuffler(this);
            }
            super.setLocation(loc);
            if (loc != null) {
                plugin.getSoundMufflerListener().registerMuffler(this);
            }
        } else {
            super.setLocation(loc);
        }
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
        update(false);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Sound Muffler";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Reduces the volume of all sounds",
                "within a " + DISTANCE + "-block radius",
                "R-click: " + ChatColor.RESET + " open configuration"
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("WWW", "WNW", "WWW");
        recipe.setIngredient('W', Material.WOOL);
        recipe.setIngredient('N', Material.NOTE_BLOCK);
        return recipe;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        ((SensibleToolboxPlugin) getProviderPlugin()).getSoundMufflerListener().registerMuffler(this);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        ((SensibleToolboxPlugin) getProviderPlugin()).getSoundMufflerListener().unregisterMuffler(this);
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
    public void onServerTick() {
        if (((SensibleToolboxPlugin) getProviderPlugin()).isProtocolLibEnabled()) {
            Location loc = getLocation();
            ParticleEffect.NOTE.play(loc.add(0.5, 1.0, 0.5), 0.2f, 0.5f, 0.2f, 0f, 3);
        }
        super.onServerTick();
    }

    @Override
    public String[] getSignLabel(BlockFace face) {
        return new String[]{
                makeItemLabel(face),
                ChatColor.DARK_RED + "Volume " + ChatColor.RESET + getVolume(),
                "",
                ""
        };
    }
}
