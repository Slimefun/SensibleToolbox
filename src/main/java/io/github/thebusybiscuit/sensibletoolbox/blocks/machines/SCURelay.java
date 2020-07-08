package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.core.IDTracker;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SubspaceTransponder;
import io.github.thebusybiscuit.sensibletoolbox.items.components.UnlinkedSCURelay;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;

public class SCURelay extends BatteryBox {

    private static final int TRANSPONDER_LABEL_SLOT = 43;
    private static final int TRANSPONDER_SLOT = 44;
    private UUID worldID = null;
    private int relayId = 0;
    private boolean hasTransponder;

    public SCURelay() {
        super();
    }

    public SCURelay(ConfigurationSection conf) {
        super(conf);
        relayId = conf.getInt("relayId");
        hasTransponder = conf.getBoolean("transponder", false);
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
        conf.set("transponder", hasTransponder);
        return conf;
    }

    @Override
    public Material getMaterial() {
        return Material.CYAN_STAINED_GLASS;
    }

    @Override
    public String getItemName() {
        return "SCU Relay";
    }

    @Override
    public String getDisplaySuffix() {
        return relayId > 0 ? "#" + relayId : null;
    }

    @Override
    public String[] getExtraLore() {
        String[] lore = super.getExtraLore();
        if (relayId == 0) {
            return lore;
        }
        String[] res = Arrays.copyOf(lore, lore.length + 4);
        res[lore.length] = "Comes in pairs: both partners";
        res[lore.length + 1] = "always have the same SCU level.";
        res[lore.length + 2] = "Displayed charge may be out of date";
        res[lore.length + 3] = "L-Click to refresh";
        return res;
    }

    @Override
    public Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack(2));
        UnlinkedSCURelay usr = new UnlinkedSCURelay();
        registerCustomIngredients(usr);
        recipe.addIngredient(usr.getMaterial());
        recipe.addIngredient(usr.getMaterial());
        return recipe;
    }

    @Override
    public int getMaxCharge() {
        // takes two 50k battery boxes to make, so...
        return 100000;
    }

    @Override
    public int getChargeRate() {
        if (!isRedstoneActive() || relayId == 0) {
            return 0;
        }
        RelayData relayData = (RelayData) getTracker().get(relayId);
        if (relayData == null || relayData.block1 == null || relayData.block2 == null) {
            return 0;
        }

        if (relayData.block1.worldID != relayData.block2.worldID && (!relayData.block1.hasTransponder || !relayData.block2.hasTransponder)) {
            return 0;
        }
        return 500;
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
        }
        else {
            super.onInteractItem(event);
        }
    }

    // @Override
    // public void onInteractBlock(PlayerInteractEvent event) {
    // if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
    // for (Map.Entry<Integer, RelayData> e : relayMap.entrySet()) {
    // System.out.println(e.getKey() + ": charge = " + e.getValue().chargeLevel);
    // System.out.println(" " + e.getValue().block1);
    // System.out.println(" " + e.getValue().block2);
    // }
    // }
    // super.onInteractBlock(event);
    // }

    @Override
    public int[] getInputSlots() {
        return new int[] { TRANSPONDER_SLOT };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] { TRANSPONDER_SLOT };
    }

    @Override
    public int getEnergyCellSlot() {
        return 36;
    }

    @Override
    public int getChargeDirectionSlot() {
        return 37;
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        boolean res = super.onSlotClick(player, slot, click, inSlot, onCursor);

        if (res) {
            rescanTransponder();
        }

        return res;
    }

    @Override
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        int inserted = super.onShiftClickInsert(player, slot, toInsert);

        if (inserted > 0) {
            rescanTransponder();
        }

        return inserted;
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        boolean res = super.onShiftClickExtract(player, slot, toExtract);

        if (res) {
            rescanTransponder();
        }

        return res;
    }

    @Override
    public void onGUIOpened(HumanEntity player) {
        drawTransponder(getGUI());
    }

    private void drawTransponder(InventoryGUI gui) {
        if (hasTransponder) {
            gui.setItem(TRANSPONDER_SLOT, new SubspaceTransponder().toItemStack());
        }
        else {
            gui.setItem(TRANSPONDER_SLOT, null);
        }
    }

    @Override
    public boolean acceptsItemType(ItemStack item) {
        return SensibleToolbox.getItemRegistry().isSTBItem(item, SubspaceTransponder.class);
    }

    @Override
    public int insertItems(ItemStack toInsert, BlockFace side, boolean sorting, UUID uuid) {
        int n = super.insertItems(toInsert, side, sorting, uuid);

        if (n > 0) {
            rescanTransponder();
        }

        return n;
    }

    @Override
    public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid) {
        ItemStack stack = super.extractItems(face, receiver, amount, uuid);

        if (stack != null) {
            rescanTransponder();
        }

        return stack;
    }

    private void rescanTransponder() {
        // defer this since we need to ensure the inventory slot is actually updated
        Bukkit.getScheduler().runTask(getProviderPlugin(), () -> {
            SubspaceTransponder str = SensibleToolbox.getItemRegistry().fromItemStack(getGUI().getItem(TRANSPONDER_SLOT), SubspaceTransponder.class);
            hasTransponder = str != null;
        });
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = super.createGUI();

        gui.addLabel("Subspace Transponder", TRANSPONDER_LABEL_SLOT, null, "Insert a Subspace Transponder", "here if the relay partner will", "be on a different world");
        gui.setSlotType(TRANSPONDER_SLOT, InventoryGUI.SlotType.ITEM);

        drawTransponder(gui);

        return gui;
    }

    @Override
    protected boolean shouldPaintSlotSurrounds() {
        return false;
    }

    private void updateInfoLabel(RelayData data) {
        String locStr = "(unknown)";

        if (this.equals(data.block1)) {
            locStr = data.block2 == null ? "(not placed)" : MiscUtil.formatLocation(data.block2.getLocation());
        }
        else if (this.equals(data.block2)) {
            locStr = data.block1 == null ? "(not placed)" : MiscUtil.formatLocation(data.block1.getLocation());
        }

        getGUI().addLabel("SCU Relay : #" + relayId, 0, null, ChatColor.DARK_AQUA + "Partner Location: " + locStr, "Relay will only accept/supply power", "when both partners are placed");
    }

    @Override
    public int getInventoryGUISize() {
        return 45;
    }

    @Override
    public boolean onCrafted() {
        relayId = getTracker().add(new RelayData());
        return true;
    }

    @Override
    protected String[] getSignLabel(BlockFace face) {
        String[] label = super.getSignLabel(face);
        label[1] = ChatColor.DARK_RED + "ID #" + relayId;
        return label;
    }

    @Override
    public void onBlockRegistered(Location location, boolean isPlacing) {
        super.onBlockRegistered(location, isPlacing);
        RelayData relayData = (RelayData) getTracker().get(relayId);

        if (relayData.block1 == null) {
            relayData.block1 = this;
        }
        else if (relayData.block2 == null) {
            relayData.block2 = this;
        }
        else {
            // shouldn't happen!
            LogUtils.warning("trying to register more than 2 SCU relays of ID " + relayId);
        }

        updateInfoLabels(relayData);
        worldID = location.getWorld().getUID();
    }

    @Override
    public void onBlockUnregistered(Location loc) {
        getGUI().setItem(TRANSPONDER_SLOT, null);

        RelayData relayData = (RelayData) getTracker().get(relayId);
        if (relayData != null) {
            if (this.equals(relayData.block1)) {
                relayData.block1 = null;
            }
            else if (this.equals(relayData.block2)) {
                relayData.block2 = null;
            }
            else {
                // shouldn't happen!
                LogUtils.warning("relay loc for ID " + relayId + " doesn't match placed relays");
            }

            updateInfoLabels(relayData);
        }
        else {
            // shouldn't happen!
            LogUtils.warning("can't find any register SCU relay of ID " + relayId);
        }

        worldID = null;

        super.onBlockUnregistered(loc);
    }

    private void updateInfoLabels(RelayData relayData) {
        if (relayData.block1 != null) {
            relayData.block1.updateInfoLabel(relayData);
        }

        if (relayData.block2 != null) {
            relayData.block2.updateInfoLabel(relayData);
        }
    }

    private class RelayData {

        SCURelay block1;
        SCURelay block2;
        double chargeLevel;

    }
}
