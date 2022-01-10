package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;

import io.github.thebusybiscuit.sensibletoolbox.api.STBInventoryHolder;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.filters.Filter;
import io.github.thebusybiscuit.sensibletoolbox.api.filters.FilterType;
import io.github.thebusybiscuit.sensibletoolbox.api.filters.Filtering;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.GUIUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.SlotType;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.DirectionGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.FilterTypeGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.ToggleButton;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.blocks.router.ItemRouter;
import io.github.thebusybiscuit.sensibletoolbox.utils.UnicodeSymbol;
import io.github.thebusybiscuit.sensibletoolbox.utils.VanillaInventoryUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;

public abstract class DirectionalItemRouterModule extends ItemRouterModule implements Filtering, Directional {

    private static final String LIST_ITEM = ChatColor.LIGHT_PURPLE + UnicodeSymbol.CENTERED_POINT.toUnicode() + " " + ChatColor.AQUA;

    private static final ItemStack WHITE_BUTTON = GUIUtil.makeTexture(Material.WHITE_WOOL, ChatColor.WHITE.toString() + ChatColor.UNDERLINE + "Whitelist", "Module will only process", "items which match the filter.");
    private static final ItemStack BLACK_BUTTON = GUIUtil.makeTexture(Material.BLACK_WOOL, ChatColor.WHITE.toString() + ChatColor.UNDERLINE + "Blacklist", "Module will NOT process", "items which match the filter.");
    private static final ItemStack OFF_BUTTON = GUIUtil.makeTexture(Material.LIGHT_BLUE_STAINED_GLASS, ChatColor.WHITE.toString() + ChatColor.UNDERLINE + "Termination OFF", "Subsequent modules in the", "Item Router will process items", "as normal.");
    private static final ItemStack ON_BUTTON = GUIUtil.makeTexture(Material.ORANGE_WOOL, ChatColor.WHITE.toString() + ChatColor.UNDERLINE + "Termination ON", "If this module processes an", "item, the Item Router will", "not process any more items", "on this tick.");

    public static final int FILTER_LABEL_SLOT = 0;
    public static final int DIRECTION_LABEL_SLOT = 5;
    private final Filter filter;
    private BlockFace direction;
    private boolean terminator;
    private InventoryGUI gui;
    private final int[] filterSlots = { 1, 2, 3, 10, 11, 12, 19, 20, 21 };

    /**
     * Run this module's action.
     *
     * @param loc
     *            the location of the module's owning item router
     * @return true if the module did some work on this tick
     */
    public abstract boolean execute(Location loc);

    public DirectionalItemRouterModule() {
        // default filter: blacklist, no items
        filter = new Filter();
        setFacingDirection(BlockFace.SELF);
    }

    public DirectionalItemRouterModule(ConfigurationSection conf) {
        super(conf);
        setFacingDirection(BlockFace.valueOf(conf.getString("direction")));
        setTerminator(conf.getBoolean("terminator", false));

        if (conf.contains("filtered")) {
            boolean isWhite = conf.getBoolean("filterWhitelist", true);
            FilterType filterType = FilterType.valueOf(conf.getString("filterType", "MATERIAL"));
            @SuppressWarnings("unchecked")
            List<ItemStack> l = (List<ItemStack>) conf.getList("filtered");
            filter = Filter.fromItemList(isWhite, l, filterType);
        } else {
            filter = new Filter();
        }
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("direction", getFacing().toString());
        conf.set("terminator", isTerminator());

        if (filter != null) {
            conf.set("filtered", filter.getFilterList());
            conf.set("filterWhitelist", filter.isWhiteList());
            conf.set("filterType", filter.getFilterType().toString());
        }
        return conf;
    }

    @Override
    public String[] getExtraLore() {
        if (filter == null) {
            return new String[0];
        } else {
            String[] lore = new String[(filter.size() + 1) / 2 + 2];
            String what = filter.isWhiteList() ? "white-listed" : "black-listed";
            String s = filter.size() == 1 ? "" : "s";
            lore[0] = ChatColor.GOLD.toString() + filter.size() + " item" + s + " " + what;

            if (isTerminator()) {
                lore[0] += ", " + ChatColor.BOLD + "Terminating";
            }

            lore[1] = ChatColor.GOLD + filter.getFilterType().getLabel();
            int i = 2;

            for (ItemStack stack : filter.getFilterList()) {
                int n = i / 2 + 1;
                String name = ItemUtils.getItemName(stack);
                lore[n] = lore[n] == null ? LIST_ITEM + name : lore[n] + " " + LIST_ITEM + name;
                i++;
            }

            return lore;
        }
    }

    @Override
    public String getDisplaySuffix() {
        return direction != BlockFace.SELF ? direction.toString() : null;
    }

    @Override
    public void setFacingDirection(BlockFace blockFace) {
        direction = blockFace;
    }

    @Override
    public BlockFace getFacing() {
        return direction;
    }

    public Filter getFilter() {
        return filter;
    }

    public boolean isTerminator() {
        return terminator;
    }

    public void setTerminator(boolean terminator) {
        this.terminator = terminator;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // set module direction based on clicked block face
            setFacingDirection(event.getBlockFace().getOppositeFace());
            event.getPlayer().getInventory().setItem(event.getHand(), toItemStack(event.getItem().getAmount()));
            event.setCancelled(true);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR && event.getPlayer().isSneaking()) {
            // unset module direction
            setFacingDirection(BlockFace.SELF);
            event.getPlayer().getInventory().setItem(event.getHand(), toItemStack(event.getItem().getAmount()));
            event.setCancelled(true);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemRouter rtr = event.getClickedBlock() == null ? null : SensibleToolbox.getBlockAt(event.getClickedBlock().getLocation(), ItemRouter.class, true);
            if (event.getClickedBlock() == null || (rtr == null && !event.getClickedBlock().getType().isInteractable())) {
                // open module configuration GUI
                gui = createGUI(event.getPlayer());
                gui.show(event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

    private InventoryGUI createGUI(Player player) {
        InventoryGUI inventory = GUIUtil.createGUI(player, this, 36, ChatColor.DARK_RED + "Module Configuration");

        inventory.addGadget(new ToggleButton(inventory, 28, getFilter().isWhiteList(), WHITE_BUTTON, BLACK_BUTTON, newValue -> {
            if (getFilter() != null) {
                getFilter().setWhiteList(newValue);
                return true;
            } else {
                return false;
            }
        }));

        inventory.addGadget(new FilterTypeGadget(inventory, 29));
        inventory.addGadget(new ToggleButton(inventory, 30, isTerminator(), ON_BUTTON, OFF_BUTTON, newValue -> {
            setTerminator(newValue);
            return true;
        }));

        inventory.addLabel("Filtered Items", FILTER_LABEL_SLOT, null, "Place up to 9 items", "in the filter " + UnicodeSymbol.ARROW_RIGHT.toUnicode());
        for (int slot : filterSlots) {
            inventory.setSlotType(slot, SlotType.ITEM);
        }
        populateFilterInventory(inventory.getInventory());

        inventory.addLabel("Module Direction", DIRECTION_LABEL_SLOT, null, "Set the direction that", "the module works in", "once installed in an", "Item Router");
        ItemStack texture = new ItemStack(new ItemRouter().getMaterial());
        GUIUtil.setDisplayName(texture, "No Direction");
        inventory.addGadget(new DirectionGadget(inventory, 16, texture));

        return inventory;
    }

    private void populateFilterInventory(Inventory inv) {
        int n = 0;
        for (ItemStack stack : filter.getFilterList()) {
            inv.setItem(filterSlots[n], stack);
            n++;
            if (n >= filterSlots.length) {
                break;
            }
        }
    }

    protected String[] makeDirectionalLore(String... lore) {
        String[] newLore = Arrays.copyOf(lore, lore.length + 2);
        newLore[lore.length] = "L-click Block: " + ChatColor.WHITE + " Set direction";
        newLore[lore.length + 1] = UnicodeSymbol.ARROW_UP.toUnicode() + " + L-click Air: " + ChatColor.WHITE + " Unset direction";
        return newLore;
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        if (onCursor.getType() == Material.AIR) {
            gui.getInventory().setItem(slot, null);
        } else {
            ItemStack stack = onCursor.clone();
            stack.setAmount(1);
            gui.getInventory().setItem(slot, stack);
        }
        return false;
    }

    @Override
    public boolean onPlayerInventoryClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return true;
    }

    @Override
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        return 0;
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        return false;
    }

    @Override
    public boolean onClickOutside(HumanEntity player) {
        return false;
    }

    @Override
    public void onGUIClosed(HumanEntity player) {
        filter.clear();

        for (int slot : filterSlots) {
            ItemStack stack = gui.getInventory().getItem(slot);

            if (stack != null) {
                filter.addItem(stack);
            }
        }

        player.setItemInHand(toItemStack(player.getItemInHand().getAmount()));
    }

    protected boolean doPull(BlockFace from, Location loc) {
        ItemStack inBuffer = getItemRouter().getBufferItem();

        if (inBuffer != null && inBuffer.getAmount() >= inBuffer.getType().getMaxStackSize()) {
            return false;
        }

        int nToPull = getItemRouter().getStackSize();
        Location targetLoc = getTargetLocation(loc);
        ItemStack pulled;
        BaseSTBBlock stb = SensibleToolbox.getBlockAt(targetLoc, true);

        if (stb instanceof STBInventoryHolder) {
            pulled = ((STBInventoryHolder) stb).extractItems(from.getOppositeFace(), inBuffer, nToPull, getItemRouter().getOwner());
        } else {
            // possible vanilla inventory holder
            pulled = VanillaInventoryUtils.pullFromInventory(targetLoc.getBlock(), nToPull, inBuffer, getFilter(), getItemRouter().getOwner());
        }

        if (pulled != null) {
            if (stb != null) {
                stb.update(false);
            }

            getItemRouter().setBufferItem(inBuffer == null ? pulled : inBuffer);
            return true;
        }

        return false;
    }

    protected boolean vanillaInsertion(Block target, int amount, BlockFace side) {
        ItemStack buffer = getItemRouter().getBufferItem();
        int nInserted = VanillaInventoryUtils.vanillaInsertion(target, buffer, amount, side, false, getItemRouter().getOwner());

        if (nInserted == 0) {
            // no insertion happened
            return false;
        } else {
            // some or all items were inserted, buffer size has been adjusted accordingly
            getItemRouter().setBufferItem(buffer.getAmount() == 0 ? null : buffer);
            return true;
        }
    }

    protected Location getTargetLocation(Location loc) {
        BlockFace face = getFacing();
        return loc.clone().add(face.getModX(), face.getModY(), face.getModZ());
    }
}
