package me.desht.sensibletoolbox.items.machineupgrades;

import me.desht.sensibletoolbox.api.SensibleToolbox;
import me.desht.sensibletoolbox.api.gui.DirectionGadget;
import me.desht.sensibletoolbox.api.gui.GUIUtil;
import me.desht.sensibletoolbox.api.gui.InventoryGUI;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.api.items.BaseSTBMachine;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

public class EjectorUpgrade extends MachineUpgrade implements Directional {
    private static final MaterialData md = new MaterialData(Material.QUARTZ);
    public static final int DIRECTION_LABEL_SLOT = 2;
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
            Block b = event.getClickedBlock();
            BaseSTBMachine machine = b == null ? null : SensibleToolbox.getBlockAt(b.getLocation(), BaseSTBMachine.class, true);
            if (b == null || machine == null && !STBUtil.isInteractive(b.getType())) {
                InventoryGUI gui = createGUI(event.getPlayer());
                gui.show(event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

    private InventoryGUI createGUI(Player player) {
        final InventoryGUI theGUI = GUIUtil.createGUI(player, this, 27, ChatColor.DARK_RED + "Ejector Configuration");
        theGUI.addLabel("Module Direction", DIRECTION_LABEL_SLOT, null,
                "Set the direction in which the", "machine should eject finished items");

        ItemStack texture = GUIUtil.makeTexture(getMaterialData(), "Ejection Direction");
        DirectionGadget dg = new DirectionGadget(theGUI, 13, texture);
        dg.setAllowSelf(false);
        theGUI.addGadget(dg);

        return theGUI;
    }

    @Override
    public void setFacingDirection(BlockFace blockFace) {
        direction = blockFace;
    }

    @Override
    public BlockFace getFacing() {
        return direction;
    }

    @Override
    public void onGUIClosed(HumanEntity player) {
        player.setItemInHand(toItemStack(player.getItemInHand().getAmount()));
    }
}
