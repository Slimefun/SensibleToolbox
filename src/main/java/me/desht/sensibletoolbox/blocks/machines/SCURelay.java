package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.core.IDTracker;
import me.desht.sensibletoolbox.items.components.UnlinkedSCURelay;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import java.util.Arrays;

public class SCURelay extends BatteryBox {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.CYAN);

    private int relayId;

    public SCURelay() {
        super();
    }

    public SCURelay(ConfigurationSection conf) {
        super(conf);
        relayId = conf.getInt("relayId");
        IDTracker tracker = getTracker();
        if (!tracker.contains(relayId)) {
            RelayData relayData = new RelayData();
            relayData.chargeLevel = super.getCharge();
            tracker.add(relayId, relayData);
        }
    }

    private IDTracker getTracker() {
        return ((SensibleToolboxPlugin) getProviderPlugin()).getScuRelayIDTracker();
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("relayId", relayId);
        return conf;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "SCU Relay";
    }

    @Override
    public String[] getExtraLore() {
        String[] lore = super.getExtraLore();
        if (relayId == 0) {
            return lore;
        }
        String[] res = Arrays.copyOf(lore, lore.length + 3);
        res[lore.length] = String.format("ID: " + ChatColor.DARK_AQUA + "%08x", relayId);
        res[lore.length + 1] = "Displayed charge may be out of date";
        res[lore.length + 2] = "L-Click to refresh";
        return res;
    }

    @Override
    public Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(2));
        UnlinkedSCURelay usr = new UnlinkedSCURelay();
        registerCustomIngredients(usr);
        recipe.addIngredient(usr.getMaterialData());
        recipe.addIngredient(usr.getMaterialData());
        return recipe;
    }

    @Override
    public int getMaxCharge() {
        // takes two 50k battery boxes to make, so...
        return 100000;
    }

    @Override
    public int getChargeRate() {
        return isRedstoneActive() ? 500 : 0;
    }

    @Override
    public double getCharge() {
        RelayData relayData = (RelayData) getTracker().get(relayId);
        return relayData == null ? 0 : relayData.chargeLevel;
    }

    @Override
    public void setCharge(double charge) {
        RelayData relayData = (RelayData) getTracker().get(relayId);
        if (relayData != null) {
            relayData.chargeLevel = charge;
            if (relayData.block1 != null) {
                relayData.block1.notifyCharge();
            }
            if (relayData.block2 != null) {
                relayData.block2.notifyCharge();
            }
        }
    }

    private void notifyCharge() {
        super.setCharge(getCharge());
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            event.getPlayer().setItemInHand(toItemStack(event.getPlayer().getItemInHand().getAmount()));
            event.setCancelled(true);
        } else {
            super.onInteractItem(event);
        }
    }

//    @Override
//    public void onInteractBlock(PlayerInteractEvent event) {
//        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
//            for (Map.Entry<Integer, RelayData> e : relayMap.entrySet()) {
//                System.out.println(e.getKey() + ": charge = " + e.getValue().chargeLevel);
//                System.out.println("  " + e.getValue().block1);
//                System.out.println("  " + e.getValue().block2);
//            }
//        }
//        super.onInteractBlock(event);
//    }

    @Override
    public boolean onCrafted() {
        relayId = getTracker().add(new RelayData());
        return true;
    }

    @Override
    protected String[] getSignLabel(BlockFace face) {
        String[] label = super.getSignLabel(face);
        label[1] = String.format(ChatColor.DARK_RED + "%08x", relayId);
        return label;
    }

    @Override
    public void onBlockRegistered(Location location, boolean isPlacing) {
        super.onBlockRegistered(location, isPlacing);
        RelayData relayData = (RelayData) getTracker().get(relayId);
        if (relayData.block1 == null) {
            relayData.block1 = this;
        } else if (relayData.block2 == null) {
            relayData.block2 = this;
        } else {
            // shouldn't happen!
            LogUtils.warning("trying to register more than 2 SCU relays of ID " + relayId);
        }
    }

    @Override
    public void onBlockUnregistered(Location loc) {
        super.onBlockUnregistered(loc);
        RelayData relayData = (RelayData) getTracker().get(relayId);
        if (relayData != null) {
            if (this.equals(relayData.block1)) {
                relayData.block1 = null;
            } else if (this.equals(relayData.block2)) {
                relayData.block2 = null;
            } else {
                // shouldn't happen!
                LogUtils.warning("relay loc for ID " + relayId + " doesn't match placed relays");
            }
        } else {
            // shouldn't happen!
            LogUtils.warning("can't find any register SCU relay of ID " + relayId);
        }
    }

    private class RelayData {
        SCURelay block1;
        SCURelay block2;
        double chargeLevel;
    }
}
