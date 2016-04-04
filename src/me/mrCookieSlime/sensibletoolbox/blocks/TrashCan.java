package me.mrCookieSlime.sensibletoolbox.blocks;

import java.util.UUID;

import me.desht.sensibletoolbox.dhutils.Debugger;
import me.mrCookieSlime.sensibletoolbox.api.STBInventoryHolder;
import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBBlock;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class TrashCan extends BaseSTBBlock implements STBInventoryHolder {

    public TrashCan() {
    }

    public TrashCan(ConfigurationSection conf) {
        super(conf);
    }

    public static TrashCan getTrashCan(Inventory inv) {
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof Dropper) {
            return SensibleToolbox.getBlockAt(((Dropper) holder).getLocation(), TrashCan.class, false);
        }
        return null;
    }

    @SuppressWarnings("deprecation")
	@Override
    public MaterialData getMaterialData() {
        return new MaterialData(Material.DROPPER, STBUtil.getDirectionData(getFacing()));
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
    public void onBlockRegistered(Location location, boolean isPlacing) {
        if (isPlacing) {
            // put a skull on top of the main block
            Block above = location.getBlock().getRelative(BlockFace.UP);
            Skull skull = STBUtil.setSkullHead(above, "MHF_Exclamation", getFacing());
            skull.update();
        }

        super.onBlockRegistered(location, isPlacing);
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
        if (l != null && l.getBlock().getType() == getMaterialData().getItemType()) {
            Dropper d = (Dropper) l.getBlock().getState();
            if (noisy) {
                for (ItemStack stack : d.getInventory()) {
                    if (stack != null) {
                        d.getLocation().getWorld().playSound(d.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
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
        if (l != null && l.getBlock().getType() == getMaterialData().getItemType()) {
            Dropper d = (Dropper) getLocation().getBlock().getState();
            return d.getInventory();
        } else {
            return null;
        }
    }
}
