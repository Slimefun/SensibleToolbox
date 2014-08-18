package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.SensibleToolbox;
import me.desht.sensibletoolbox.api.gui.InventoryGUI;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.core.IDTracker;
import me.desht.sensibletoolbox.items.components.SubspaceTransponder;
import me.desht.sensibletoolbox.items.components.UnlinkedSCURelay;
import org.bukkit.*;
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
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.UUID;

public class SCURelay extends BatteryBox {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.CYAN);
    private static final int TRANSPONDER_LABEL_SLOT = 0;
    private static final int TRANSPONDER_SLOT = 1;
    private UUID worldID = null;
    private int relayId;
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
        if (!isRedstoneActive()) {
            return 0;
        }
        RelayData relayData = (RelayData) getTracker().get(relayId);
        if (relayData == null || relayData.block1 == null || relayData.block2 == null) {
            return 0;
        }
        if (relayData.block1.worldID != relayData.block2.worldID
                && (!relayData.block1.hasTransponder || !relayData.block2.hasTransponder)) {
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
    public int[] getInputSlots() {
        return new int[] { TRANSPONDER_SLOT };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] { TRANSPONDER_SLOT };
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
        } else {
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
        Bukkit.getScheduler().runTask(getProviderPlugin(), new Runnable() {
            @Override
            public void run() {
                SubspaceTransponder str = SensibleToolbox.getItemRegistry().fromItemStack(getGUI().getItem(1), SubspaceTransponder.class);
                hasTransponder = str != null;
            }
        });
    }

    @Override
    protected InventoryGUI createGUI() {
        InventoryGUI gui = super.createGUI();

        gui.addLabel("Subspace Transponder", TRANSPONDER_LABEL_SLOT, null, "Place a Subspace Transponder", "here if the relay partner will", "be on a different world");
        gui.setSlotType(TRANSPONDER_SLOT, InventoryGUI.SlotType.ITEM);

        drawTransponder(gui);

        return gui;
    }

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
        worldID = location.getWorld().getUID();
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
        worldID = null;
    }

    private class RelayData {
        SCURelay block1;
        SCURelay block2;
        double chargeLevel;
    }
}
