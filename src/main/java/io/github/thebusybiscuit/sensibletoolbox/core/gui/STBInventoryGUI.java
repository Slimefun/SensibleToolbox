package io.github.thebusybiscuit.sensibletoolbox.core.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.GUIUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUIListener;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.SlotType;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.ClickableGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.MonitorGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.utils.BukkitSerialization;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.utils.IntRange;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.text.LogUtils;

public class STBInventoryGUI implements InventoryGUI {

    // some handy stock textures
    public static ItemStack INPUT_TEXTURE;
    public static ItemStack OUTPUT_TEXTURE;
    public static ItemStack BG_TEXTURE;
    public static ItemStack LABEL_TEXTURE;
    public static ItemStack BUTTON_TEXTURE;

    static {
        buildStockTextures();
    }

    public static void buildStockTextures() {
        Configuration config = SensibleToolboxPlugin.getInstance().getConfig();
        INPUT_TEXTURE = STBUtil.parseMaterialSpec(config.getString("gui.texture.input"));
        OUTPUT_TEXTURE = STBUtil.parseMaterialSpec(config.getString("gui.texture.output"));
        BG_TEXTURE = STBUtil.parseMaterialSpec(config.getString("gui.texture.bg"));
        LABEL_TEXTURE = STBUtil.parseMaterialSpec(config.getString("gui.texture.label"));
        BUTTON_TEXTURE = STBUtil.parseMaterialSpec(config.getString("gui.texture.button"));
        GUIUtil.setDisplayName(INPUT_TEXTURE, ChatColor.AQUA + "Input");
        GUIUtil.setDisplayName(OUTPUT_TEXTURE, ChatColor.AQUA + "Output");
        GUIUtil.setDisplayName(BG_TEXTURE, " ");
    }

    private static final String STB_OPEN_GUI = "STB_Open_GUI";
    private final Inventory inventory;
    private final InventoryGUIListener listener;
    private final ClickableGadget[] gadgets;
    private final SlotType[] slotTypes;
    private final IntRange slotRange;
    private final List<MonitorGadget> monitors = new ArrayList<>();

    public STBInventoryGUI(InventoryGUIListener listener, int size, String title) {
        this(null, listener, size, title);
    }

    public STBInventoryGUI(Player player, InventoryGUIListener listener, int size, String title) {
        this.listener = listener;
        this.inventory = player == null ? Bukkit.createInventory(((BaseSTBBlock) listener).getGuiHolder(), size, title) : Bukkit.createInventory(player, size, title);
        this.gadgets = new ClickableGadget[size];
        this.slotRange = new IntRange(0, size - 1);
        this.slotTypes = new SlotType[size];

        for (int slot = 0; slot < size; slot++) {
            setSlotType(slot, SlotType.BACKGROUND);
        }
    }

    public static InventoryGUI getOpenGUI(Player player) {
        for (MetadataValue mv : player.getMetadata(STB_OPEN_GUI)) {
            if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
                return (InventoryGUI) mv.value();
            }
        }
        return null;
    }

    private static void setOpenGUI(Player player, InventoryGUI gui) {
        if (gui != null) {
            player.setMetadata(STB_OPEN_GUI, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), gui));
        } else {
            player.removeMetadata(STB_OPEN_GUI, SensibleToolboxPlugin.getInstance());
        }
    }

    @Override
    public void addGadget(ClickableGadget gadget) {
        int slot = gadget.getSlot();
        if (containsSlot(slot)) {
            inventory.setItem(slot, gadget.getTexture());
            gadgets[slot] = gadget;
            setSlotType(slot, SlotType.GADGET);
        }
    }

    @Override
    public void addLabel(String label, int slot, ItemStack texture, String... lore) {
        ItemStack stack = texture == null ? LABEL_TEXTURE.clone() : texture;
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + label);
        if (lore.length > 0) {
            meta.setLore(GUIUtil.makeLore(lore));
        }
        stack.setItemMeta(meta);
        setSlotType(slot, SlotType.BACKGROUND);
        inventory.setItem(slot, stack);
    }

    @Override
    public int addMonitor(MonitorGadget gadget) {
        Preconditions.checkArgument(gadget.getSlots().length > 0, "Gadget has no slots!");
        monitors.add(gadget);
        for (int slot : gadget.getSlots()) {
            setSlotType(slot, SlotType.GADGET);
        }
        return monitors.size() - 1;
    }

    @Override
    public ItemStack getItem(int slot) {
        Preconditions.checkArgument(getSlotType(slot) == SlotType.ITEM, "Slot " + slot + " is not an item slot");
        return inventory.getItem(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        Preconditions.checkArgument(getSlotType(slot) == SlotType.ITEM, "Slot " + slot + " is not an item slot");
        inventory.setItem(slot, stack);
    }

    @Override
    public ClickableGadget getGadget(int slot) {
        Preconditions.checkArgument(getSlotType(slot) == SlotType.GADGET, "Slot " + slot + " is not a gadget slot");
        return gadgets[slot];
    }

    @Override
    public MonitorGadget getMonitor(int monitorId) {
        return monitors.get(monitorId);
    }

    @Override
    public BaseSTBBlock getOwningBlock() {
        if (listener instanceof BaseSTBBlock) {
            return (BaseSTBBlock) listener;
        }
        throw new IllegalStateException("attempt to get STB block for non-block listener");
    }

    @Override
    public BaseSTBItem getOwningItem() {
        if (listener instanceof BaseSTBItem) {
            return (BaseSTBItem) listener;
        }
        throw new IllegalStateException("attempt to get STB item for non-item listener");
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public boolean containsSlot(int slot) {
        return slotRange.containsInteger(slot);
    }

    @Override
    public void show(Player player) {
        if (getOwningItem() instanceof BaseSTBBlock && !getOwningBlock().hasAccessRights(player)) {
            STBUtil.complain(player, "That " + getOwningItem().getItemName() + " is private!");
            return;
        }
        if (inventory.getViewers().isEmpty()) {
            // no one's already looking at this inventory/gui, so ensure it's up to date
            Debugger.getInstance().debug("refreshing GUI inventory of " + getOwningItem());
            for (MonitorGadget monitor : monitors) {
                monitor.doRepaint();
            }
        }
        Debugger.getInstance().debug(player.getName() + " opened GUI for " + getOwningItem());
        setOpenGUI(player, this);
        listener.onGUIOpened(player);
        player.openInventory(inventory);
    }

    @Override
    public void hide(Player player) {
        Debugger.getInstance().debug(player.getName() + ": hide GUI");
        setOpenGUI(player, null);
        player.closeInventory();
    }

    @Override
    public List<HumanEntity> getViewers() {
        return inventory.getViewers();
    }

    public void receiveEvent(InventoryClickEvent event) {
        boolean shouldCancel = true;

        // try/finally here ensures the event always gets cancelled, even if
        // a listener's code throws an exception; we don't want bugs in
        // listeners allowing players to take items out of a GUI
        try {
            if (containsSlot(event.getRawSlot())) {
                // clicking inside the GUI
                switch (getSlotType(event.getRawSlot())) {
                    case GADGET:
                        if (gadgets[event.getRawSlot()] != null && gadgets[event.getRawSlot()].isEnabled()) {
                            gadgets[event.getRawSlot()].onClicked(event);
                        }
                        break;
                    case ITEM:
                        shouldCancel = !processGUIInventoryAction(event);
                        Debugger.getInstance().debug("handled click for " + event.getWhoClicked().getName() + " in item slot " + event.getRawSlot() + " of " + getOwningItem() + ": cancelled = " + shouldCancel);
                        break;
                    default:
                        break;
                }
            } else if (event.getRawSlot() > 0) {
                // clicking inside the player's inventory
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    int nInserted = listener.onShiftClickInsert(event.getWhoClicked(), event.getRawSlot(), event.getCurrentItem());
                    if (nInserted > 0) {
                        ItemStack stack = event.getCurrentItem();
                        stack.setAmount(stack.getAmount() - nInserted);
                        event.setCurrentItem(stack.getAmount() > 0 ? stack : null);
                    }
                } else {
                    shouldCancel = !listener.onPlayerInventoryClick(event.getWhoClicked(), event.getSlot(), event.getClick(), event.getCurrentItem(), event.getCursor());
                }
            } else {
                // clicking outside the inventory entirely
                shouldCancel = !listener.onClickOutside(event.getWhoClicked());
            }
        } finally {
            if (shouldCancel) {
                event.setCancelled(true);
            }
        }
    }

    public void receiveEvent(InventoryDragEvent event) {
        boolean inGUI = false;
        boolean shouldCancel = true;
        try {
            for (int slot : event.getRawSlots()) {
                if (containsSlot(slot)) {
                    inGUI = true;
                }
            }
            if (inGUI) {
                // we only allow drags with a single slot involved, and we fake that as a left-click on the slot
                if (event.getRawSlots().size() == 1) {
                    int slot = (event.getRawSlots().toArray(new Integer[1]))[0];
                    shouldCancel = !listener.onSlotClick(event.getWhoClicked(), slot, ClickType.LEFT, inventory.getItem(slot), event.getOldCursor());
                }
            } else {
                // drag is purely in the player's inventory; allow it
                shouldCancel = false;
            }
        } finally {
            if (shouldCancel) {
                event.setCancelled(true);
            }
        }
    }

    private boolean processGUIInventoryAction(InventoryClickEvent event) {
        switch (event.getAction()) {
            case MOVE_TO_OTHER_INVENTORY:
                return listener.onShiftClickExtract(event.getWhoClicked(), event.getRawSlot(), event.getCurrentItem());
            case PLACE_ONE:
            case PLACE_ALL:
            case PLACE_SOME:
            case SWAP_WITH_CURSOR:
            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
                return listener.onSlotClick(event.getWhoClicked(), event.getRawSlot(), event.getClick(), event.getCurrentItem(), event.getCursor());
            default:
                return false;
        }
    }

    public void receiveEvent(InventoryCloseEvent event) {
        Debugger.getInstance().debug("received GUI close event for " + event.getPlayer().getName());
        listener.onGUIClosed(event.getPlayer());
        if (event.getPlayer() instanceof Player) {
            setOpenGUI((Player) event.getPlayer(), null);
        }
        Debugger.getInstance().debug(event.getPlayer().getName() + " closed GUI for " + getOwningItem());
    }

    @Override
    public SlotType getSlotType(int slot) {
        return slotTypes[slot];
    }

    @Override
    public void setSlotType(int slot, SlotType type) {
        slotTypes[slot] = type;
        switch (type) {
            case BACKGROUND:
                paintSlot(slot, BG_TEXTURE, true);
                break;
            case ITEM:
                paintSlot(slot, null, true);
                break;
            default:
                break;
        }
    }

    @Override
    public void paintSlotSurround(int[] slots, ItemStack texture) {
        for (int slot : slots) {
            int row = slot / 9, col = slot % 9;
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col - 1; j <= col + 1; j++) {
                    paintSlot(i, j, texture, true);
                }
            }
        }
    }

    private void paintSlot(int row, int col, ItemStack texture, boolean overwrite) {
        paintSlot(row * 9 + col, texture, overwrite);
    }

    @Override
    public void paintSlot(int slot, ItemStack texture, boolean overwrite) {
        if (slotRange.containsInteger(slot) && (overwrite || inventory.getItem(slot) == null)) {
            inventory.setItem(slot, texture);
        }
    }

    @Override
    public String freezeSlots(int... slots) {
        int invSize = STBUtil.roundUp(slots.length, 9);
        Inventory tmpInv = Bukkit.createInventory(null, invSize);

        for (int i = 0; i < slots.length; i++) {
            tmpInv.setItem(i, inventory.getItem(slots[i]));
        }

        return BukkitSerialization.toBase64(tmpInv, slots.length);
    }

    @Override
    public void thawSlots(String frozen, int... slots) {
        if (frozen != null && !frozen.isEmpty() && slots.length > 0) {
            try {
                Inventory tmpInv = BukkitSerialization.fromBase64(frozen);
                for (int i = 0; i < slots.length; i++) {
                    inventory.setItem(slots[i], tmpInv.getItem(i));
                }
            } catch (IOException e) {
                LogUtils.severe("can't restore inventory for " + getOwningItem().getItemName());
            }
        }
    }

    @Override
    public void ejectItems(int... slots) {
        Location loc = getOwningBlock().getLocation();

        for (int slot : slots) {
            ItemStack stack = inventory.getItem(slot);

            if (stack != null) {
                loc.getWorld().dropItemNaturally(loc, stack);
                inventory.setItem(slot, null);
            }
        }
    }

}
