package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.blocks.ItemRouter;
import io.github.thebusybiscuit.sensibletoolbox.util.UnicodeSymbol;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;

public class AdvancedSenderModule extends DirectionalItemRouterModule {
	
    private static final int RANGE = 24;
    private static final int RANGE2 = RANGE * RANGE;
    private Location linkedLoc;

    public AdvancedSenderModule() {
        linkedLoc = null;
    }

    public AdvancedSenderModule(ConfigurationSection conf) {
        super(conf);
        if (conf.contains("linkedLoc")) {
            try {
                linkedLoc = MiscUtil.parseLocation(conf.getString("linkedLoc"));
            } catch (IllegalArgumentException e) {
                linkedLoc = null;
            }
        }
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        if (linkedLoc != null) {
            conf.set("linkedLoc", MiscUtil.formatLocation(linkedLoc));
        }
        return conf;
    }

    @Override
    public Material getMaterial() {
        return Material.LIGHT_BLUE_DYE;
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Adv. Sender";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Insert into an Item Router",
                "Sends items to a linked Receiver Module",
                " anywhere within a " + RANGE + "-block radius",
                " (line of sight is not needed)",
                "L-Click item router with installed",
                " Receiver Module: " + ChatColor.RESET + " Link Adv. Sender",
                UnicodeSymbol.ARROW_UP.toUnicode() + " + L-Click: " + ChatColor.RESET + " Unlink Adv. Sender"
        };
    }

    @Override
    public String getDisplaySuffix() {
        return linkedLoc == null ? "[Not Linked]" : "[" + MiscUtil.formatLocation(linkedLoc) + "]";
    }

    @Override
    public Recipe getRecipe() {
        SenderModule sm = new SenderModule();
        registerCustomIngredients(sm);
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(sm.getMaterial());
        recipe.addIngredient(Material.ENDER_EYE);
        recipe.addIngredient(Material.DIAMOND);
        return recipe;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
            // try to link up with a receiver module
            ItemRouter rtr = SensibleToolbox.getBlockAt(event.getClickedBlock().getLocation(), ItemRouter.class, true);
            if (rtr != null && rtr.getReceiver() != null) {
                linkToRouter(rtr);
                event.getPlayer().setItemInHand(toItemStack(event.getPlayer().getItemInHand().getAmount()));
            } 
            else {
                STBUtil.complain(event.getPlayer());
            }
            event.setCancelled(true);
        } 
        else if (event.getPlayer().isSneaking() && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            linkToRouter(null);
            event.getPlayer().setItemInHand(toItemStack(event.getPlayer().getItemInHand().getAmount()));
            event.setCancelled(true);
        } 
        else if (event.getPlayer().getItemInHand().getAmount() == 1 &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            super.onInteractItem(event);
        }
    }

    public void linkToRouter(ItemRouter rtr) {
        linkedLoc = rtr == null ? null : rtr.getLocation();
    }

    protected boolean inRange(Location ourLoc) {
        return ourLoc != null &&
                ourLoc.getWorld().equals(linkedLoc.getWorld()) &&
                ourLoc.distanceSquared(linkedLoc) <= RANGE2;
    }

    @Override
    public boolean execute(Location loc) {
        if (getItemRouter() != null && getItemRouter().getBufferItem() != null && linkedLoc != null) {
            if (getFilter() != null && !getFilter().shouldPass(getItemRouter().getBufferItem())) {
                return false;
            }

            ItemRouter otherRouter = SensibleToolbox.getBlockAt(linkedLoc, ItemRouter.class, false);
            if (otherRouter != null) {
                if (!inRange(loc)) {
                    return false;
                }
                ReceiverModule mod = otherRouter.getReceiver();
                if (mod != null) {
                    return sendItems(mod) > 0;
                }
            }
        }
        return false;
    }

    private int sendItems(ReceiverModule receiver) {
        Debugger.getInstance().debug(this.getItemRouter() + ": adv.sender sending items to receiver module in " + receiver.getItemRouter());
        int nToSend = getItemRouter().getStackSize();
        ItemStack toSend = getItemRouter().getBufferItem().clone();
        toSend.setAmount(Math.min(nToSend, toSend.getAmount()));
        int received = receiver.receiveItem(toSend, getItemRouter().getOwner());
        getItemRouter().reduceBuffer(received);
        return received;
    }
}
