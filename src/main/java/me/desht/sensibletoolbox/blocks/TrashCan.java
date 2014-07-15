package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.RelativePosition;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.*;
import org.bukkit.material.MaterialData;

import java.util.UUID;

public class TrashCan extends BaseSTBBlock implements STBInventoryHolder {
    private static final MaterialData md = new MaterialData(Material.DROPPER);

    public TrashCan() {
    }

    public TrashCan(ConfigurationSection conf) {
        super(conf);
    }

    public static TrashCan getTrashCan(Inventory inv) {
        InventoryHolder h = inv.getHolder();
        if (h instanceof Dropper) {
            Dropper d = (Dropper) h;
            return LocationManager.getManager().get(d.getLocation(), TrashCan.class);
        }
        return null;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Trash Can";
    }

    @Override
    public String[] getLore() {
        return new String[]{"DESTROYS any items which are", "placed or piped into it.", "Beware!"};
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("SSS", "OCO", "OOO");
        recipe.setIngredient('S', Material.STONE);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('O', Material.COBBLESTONE);
        return recipe;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        setInventoryTitle(event, ChatColor.DARK_RED + "!!! " + getItemName() + " !!!");

        // put a skull on top of the main block
        Block above = event.getBlock().getRelative(BlockFace.UP);
        Skull skull = STBUtil.setSkullHead(above, "MHF_Exclamation", event.getPlayer());
        skull.update();
    }

    @Override
    public RelativePosition[] getBlockStructure() {
        return new RelativePosition[]{new RelativePosition(0, 1, 0)};
    }

    /**
     * Empty this trash can, permanently destroying its contents.
     *
     * @param noisy if true, play a sound effect if any items were destroyed
     */
    public void emptyTrash(boolean noisy) {
        Location l = getLocation();
        if (l != null && l.getBlock().getType() == getMaterial()) {
            Dropper d = (Dropper) l.getBlock().getState();
            if (noisy) {
                for (ItemStack stack : d.getInventory()) {
                    if (stack != null) {
                        d.getLocation().getWorld().playSound(d.getLocation(), Sound.EAT, 1.0f, 1.0f);
                        break;
                    }
                }
            }
            Debugger.getInstance().debug(this + ": trash emptied");
            d.getInventory().clear();
        }
    }

    @Override
    public int insertItems(ItemStack item, BlockFace face, boolean sorting, UUID uuid) {
        // just swallow whatever is offered
        return item.getAmount();
    }

    @Override
    public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid) {
        // can't ever extract anything from a trash can
        return null;
    }

    @Override
    public Inventory showOutputItems(UUID uuid) {
        // a trash can always appears to have an empty inventory
        return null;
    }

    @Override
    public void updateOutputItems(UUID uuid, Inventory inventory) {
        // do nothing
    }

    @Override
    public Inventory getInventory() {
        Location l = getLocation();
        if (l != null && l.getBlock().getType() == getMaterial()) {
            Dropper d = (Dropper) getLocation().getBlock().getState();
            return d.getInventory();
        } else {
            return null;
        }
    }
}
