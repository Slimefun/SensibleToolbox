package me.mrCookieSlime.sensibletoolbox.blocks;

import me.desht.sensibletoolbox.dhutils.ParticleEffect;
import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.api.RedstoneBehaviour;
import me.mrCookieSlime.sensibletoolbox.api.gui.AccessControlGadget;
import me.mrCookieSlime.sensibletoolbox.api.gui.GUIUtil;
import me.mrCookieSlime.sensibletoolbox.api.gui.InventoryGUI;
import me.mrCookieSlime.sensibletoolbox.api.gui.NumericGadget;
import me.mrCookieSlime.sensibletoolbox.api.gui.RedstoneBehaviourGadget;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBBlock;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class RedstoneClock extends BaseSTBBlock {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.RED);
    private static final MaterialData md2 = new MaterialData(Material.REDSTONE_BLOCK);
    private int interval;
    private int onDuration;
    private boolean active = false;

    public RedstoneClock() {
        interval = 20;
        onDuration = 5;
    }

    public RedstoneClock(ConfigurationSection conf) {
        super(conf);
        setInterval(conf.contains("interval") ? conf.getInt("interval") : conf.getInt("frequency"));
        setOnDuration(conf.getInt("onDuration"));
        active = conf.getBoolean("active", false);
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = GUIUtil.createGUI(this, 9, ChatColor.DARK_RED + getItemName());
        gui.addGadget(new NumericGadget(gui, 0, "Pulse Interval", new IntRange(1, Integer.MAX_VALUE), getInterval(), 10, 1, new NumericGadget.NumericListener() {
            @Override
            public boolean run(int newValue) {
                if (newValue > getOnDuration()) {
                    setInterval(newValue);
                    return true;
                } else {
                    return false;
                }
            }
        }));
        gui.addGadget(new NumericGadget(gui, 1, "Pulse Duration", new IntRange(1, Integer.MAX_VALUE), getOnDuration(), 10, 1, new NumericGadget.NumericListener() {
            @Override
            public boolean run(int newValue) {
                if (newValue < getInterval()) {
                    setOnDuration(newValue);
                    return true;
                } else {
                    return false;
                }
            }
        }));
        gui.addGadget(new RedstoneBehaviourGadget(gui, 8));
        gui.addGadget(new AccessControlGadget(gui, 7));
        return gui;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
        update(false);
    }

    public int getOnDuration() {
        return onDuration;
    }

    public void setOnDuration(int onDuration) {
        this.onDuration = onDuration;
        update(false);
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("interval", interval);
        conf.set("onDuration", onDuration);
        conf.set("active", active);
        return conf;
    }

    @Override
    public MaterialData getMaterialData() {
        return active ? md2 : md;
    }

    @Override
    public String getItemName() {
        return "Redstone Clock";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Clock-in-a-block",
                "Emits a redstone signal with",
                "configurable interval & duration",
                "R-click block: " + ChatColor.RESET + " configure clock"
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe res = new ShapedRecipe(toItemStack());
        res.shape("RSR", "STS", "RSR");
        res.setIngredient('R', Material.REDSTONE);
        res.setIngredient('S', Material.STONE);
        res.setIngredient('T', Material.REDSTONE_TORCH_ON);
        return res;
    }

    @Override
    public String[] getExtraLore() {
        String l = BaseSTBItem.LORE_COLOR + "Interval: " + ChatColor.GOLD + getInterval() +
                LORE_COLOR + "t, Duration: " + ChatColor.GOLD + getOnDuration() + LORE_COLOR + "t";
        return new String[]{l};
    }

    @Override
    public int getTickRate() {
        return 1;
    }

    @Override
    public void onServerTick() {
        Location loc = getLocation();
        Block b = loc.getBlock();
        long time = getTicksLived();
        if (time % getInterval() == 0 && isRedstoneActive()) {
            // power up
            active = true;
            repaint(b);
        } else if (time % getInterval() == getOnDuration()) {
            // power down
            active = false;
            repaint(b);
        }

        if (time % 50 == 10) {
            if (((SensibleToolboxPlugin) getProviderPlugin()).isProtocolLibEnabled()) {
                ParticleEffect.RED_DUST.play(loc.add(0.5, 0.5, 0.5), 0.7f, 0.7f, 0.7f, 0.0f, 10);
            } else {
                loc.getWorld().playEffect(loc.add(0, 0.5, 0), Effect.SMOKE, BlockFace.UP);
            }
        }
        super.onServerTick();
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
            getGUI().show(event.getPlayer());
        }
        super.onInteractBlock(event);
    }

    @Override
    public void onBlockUnregistered(Location location) {
        // ensure the non-active form of the item is always dropped
        active = false;
        super.onBlockUnregistered(location);
    }

    @Override
    public boolean supportsRedstoneBehaviour(RedstoneBehaviour behaviour) {
        return behaviour != RedstoneBehaviour.PULSED;
    }
}
