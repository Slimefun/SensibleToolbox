package me.desht.sensibletoolbox.items.machineupgrades;

import com.google.common.collect.Maps;
import me.desht.sensibletoolbox.api.gui.ButtonGadget;
import me.desht.sensibletoolbox.api.gui.GUIUtil;
import me.desht.sensibletoolbox.api.gui.InventoryGUI;
import me.desht.sensibletoolbox.api.gui.ToggleButton;
import me.desht.sensibletoolbox.blocks.ItemRouter;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

import java.util.Map;

public class EjectorUpgrade extends MachineUpgrade implements Directional {
    private static final MaterialData md = new MaterialData(Material.QUARTZ);
    private static final Map<Integer,BlockFace> directionSlots = Maps.newHashMap();
    public static final int DIRECTION_LABEL_SLOT = 5;
    static {
        directionSlots.put(6, BlockFace.UP);
        directionSlots.put(7, BlockFace.NORTH);
        directionSlots.put(15, BlockFace.WEST);
        directionSlots.put(17, BlockFace.EAST);
        directionSlots.put(24, BlockFace.DOWN);
        directionSlots.put(25, BlockFace.SOUTH);
    }
    private InventoryGUI gui;
    private BlockFace direction;

    public EjectorUpgrade() {
        direction = BlockFace.SELF;
    }

    public EjectorUpgrade(ConfigurationSection conf) {
        super(conf);
        direction = BlockFace.valueOf(conf.getString("direction"));
    }

    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("direction", getFacing().toString());
        return conf;
    }

//    public BlockFace getDirection() {
//        return direction;
//    }
//
//    public void setDirection(BlockFace direction) {
//        this.direction = direction;
//    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Ejector Upgrade";
    }

    @Override
    public String getDisplaySuffix() {
        return direction != null && direction != BlockFace.SELF ? direction.toString() : null;
    }

    @Override
    public String[] getLore() {
        return new String[]{"Place in a machine block ", "Auto-ejects finished items", "L-Click block: set ejection direction"};
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        recipe.shape("ISI", "IBI", "IGI");
        recipe.setIngredient('I', Material.IRON_FENCE);
        recipe.setIngredient('S', sc.getMaterialData());
        recipe.setIngredient('B', Material.PISTON_BASE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            setFacingDirection(event.getBlockFace().getOppositeFace());
            event.getPlayer().setItemInHand(toItemStack(event.getPlayer().getItemInHand().getAmount()));
            event.setCancelled(true);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // open ejector configuration GUI
            gui = createGUI(event.getPlayer());
            gui.show(event.getPlayer());
            event.setCancelled(true);
        }
    }

    private InventoryGUI createGUI(Player player) {
        final InventoryGUI theGUI = GUIUtil.createGUI(player, this, 36, ChatColor.DARK_RED + "Ejector Configuration");
        theGUI.addLabel("Module Direction", DIRECTION_LABEL_SLOT, null,
                "Set the direction that", "to eject finished items");
        for (Map.Entry<Integer,BlockFace> e : directionSlots.entrySet()) {
            theGUI.addGadget(makeDirectionButton(theGUI, e.getKey(), e.getValue()));
        }
        theGUI.addGadget(new ButtonGadget(theGUI, 16, "No Direction", null, new ItemRouter().getMaterialData().toItemStack(), new Runnable() {
            @Override
            public void run() {
                setFacingDirection(BlockFace.SELF);
                for (int slot : directionSlots.keySet()) {
                    ((ToggleButton) gui.getGadget(slot)).setValue(false);
                }
            }
        }));
        return theGUI;
    }

    private ToggleButton makeDirectionButton(final InventoryGUI gui, final int slot, final BlockFace face) {
        ItemStack trueStack = GUIUtil.makeTexture(new Wool(DyeColor.ORANGE), ChatColor.YELLOW + face.toString());
        ItemStack falseStack = GUIUtil.makeTexture(new Wool(DyeColor.SILVER), ChatColor.YELLOW + face.toString());
        return new ToggleButton(gui, slot, getFacing() == face, trueStack, falseStack, new ToggleButton.ToggleListener() {
            @Override
            public boolean run(boolean newValue) {
                // acts sort of like a radio button - switching one on switches all other
                // off, but switching one off leaves all switch off
                if (newValue) {
                    setFacingDirection(face);
                    for (int otherSlot : directionSlots.keySet()) {
                        if (slot != otherSlot) {
                            ((ToggleButton) gui.getGadget(otherSlot)).setValue(false);
                        }
                    }
                } else {
                    setFacingDirection(BlockFace.SELF);
                }
                return true;
            }
        });
    }

    @Override
    public void setFacingDirection(BlockFace blockFace) {
        direction = blockFace;
    }

    @Override
    public BlockFace getFacing() {
        return direction;
    }
}
