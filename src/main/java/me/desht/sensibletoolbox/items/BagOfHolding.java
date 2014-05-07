package me.desht.sensibletoolbox.items;

import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class BagOfHolding extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.ENDER_PORTAL_FRAME);

    public static final String BAG_SAVE_DIR = "bagofholding";
    public static final int BAG_SIZE = 54;

    public BagOfHolding() {
        super();
    }

    public BagOfHolding(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Bag Of Holding";
    }

    @Override
    public String[] getLore() {
        return new String[]{"R-click: open bag"};
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("WEW", "GCG", "WBW");
        recipe.setIngredient('W', Material.WOOL);
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('B', Material.GOLD_BLOCK);
        return recipe;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null && STBUtil.isInteractive(event.getClickedBlock().getType())) {
                return;
            }
            Player player = event.getPlayer();
            try {
                Inventory bagInv;
                if (getSaveFile(player).exists()) {
                    String encoded = new Scanner(getSaveFile(player)).useDelimiter("\\A").next();
                    Inventory savedInv = BukkitSerialization.fromBase64(encoded);
                    bagInv = Bukkit.createInventory(player, savedInv.getSize(), getInventoryTitle());
                    for (int i = 0; i < savedInv.getSize(); i++) {
                        bagInv.setItem(i, savedInv.getItem(i));
                    }
                } else {
                    // no saved inventory -  player must not have used the bag before
                    bagInv = Bukkit.createInventory(player, BAG_SIZE, getInventoryTitle());
                }
                player.openInventory(bagInv);
                event.setCancelled(true);
            } catch (IOException e) {
                MiscUtil.errorMessage(player, "Can't load bag of holding inventory! " + e.getMessage());
            }
        }
    }

    public String getInventoryTitle() {
        return ChatColor.GOLD + getItemName();
    }

    public static void createSaveDirectory(Plugin plugin) {
        File dir = new File(plugin.getDataFolder(), BAG_SAVE_DIR);
        if (dir.isDirectory()) {
            return;
        }
        if (!dir.mkdir()) {
            LogUtils.severe("Can't create " + dir);
        }
    }

    public static File getSaveFile(Player player) {
        File dir = new File(SensibleToolboxPlugin.getInstance().getDataFolder(), BAG_SAVE_DIR);
        return new File(dir, player.getUniqueId().toString());
    }
}
