package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.blocks.ItemRouter;
import me.desht.sensibletoolbox.storage.LocationManager;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

public class SenderModule extends DirectionalItemRouterModule {
    private static final Dye md = makeDye(DyeColor.BLUE);

    private static final int MAX_SENDER_DISTANCE = 10;

    public SenderModule() {
    }

    public SenderModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Sender";
    }

    @Override
    public String[] getLore() {
        return makeDirectionalLore(
                "Insert into an Item Router",
                "Sends items elsewhere:",
                " - An adjacent inventory OR",
                " - Item Router with Receiver Module:",
                "   within 10 blocks, with line of sight"
        );
    }

    @Override
    public Recipe getRecipe() {
        BlankModule bm = new BlankModule();
        registerCustomIngredients(bm);
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
        recipe.addIngredient(bm.getMaterialData());
        recipe.addIngredient(Material.ARROW);
        return recipe;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public boolean execute(Location loc) {
        if (getItemRouter() != null && getItemRouter().getBufferItem() != null) {
            if (getFilter() != null && !getFilter().shouldPass(getItemRouter().getBufferItem())) {
                return false;
            }
            Debugger.getInstance().debug(2, "sender in " + getItemRouter() + " has: " + getItemRouter().getBufferItem());
            Block b = loc.getBlock();
            Block target = b.getRelative(getDirection());
            int nToInsert = getItemRouter().getStackSize();
            if (allowsItemsThrough(target.getType())) {
                // search for a visible Item Router with an installed Receiver Module
                ReceiverModule receiver = findReceiver(b);
                if (receiver != null) {
                    Debugger.getInstance().debug(2, "sender found receiver module in " + receiver.getItemRouter());
                    ItemStack toSend = getItemRouter().getBufferItem().clone();
                    toSend.setAmount(Math.min(nToInsert, toSend.getAmount()));
                    int nReceived = receiver.receiveItem(toSend, getItemRouter().getOwner());
                    getItemRouter().reduceBuffer(nReceived);
                    if (nReceived > 0 && SensibleToolboxPlugin.getInstance().getConfig().getInt("particle_effects") >= 2) {
                        playSenderParticles(getItemRouter(), receiver.getItemRouter());

                    }
                    return nReceived > 0;
                }
            } else {
                STBBlock stb = LocationManager.getManager().get(target.getLocation());
                if (stb instanceof STBInventoryHolder) {
                    ItemStack toInsert = getItemRouter().getBufferItem().clone();
                    toInsert.setAmount(Math.min(nToInsert, toInsert.getAmount()));
                    int nInserted = ((STBInventoryHolder) stb).insertItems(toInsert, getDirection().getOppositeFace(), false, getItemRouter().getOwner());
                    getItemRouter().reduceBuffer(nInserted);
                    return nInserted > 0;
                } else {
                    // vanilla inventory holder?
                    return vanillaInsertion(target, nToInsert, getDirection().getOppositeFace());
                }
            }
        }
        return false;
    }

    private void playSenderParticles(ItemRouter src, ItemRouter dest) {
        if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
            Location s = src.getLocation();
            Location d = dest.getLocation();
            double xOff = (d.getX() - s.getX()) / 2;
            double zOff = (d.getZ() - s.getZ()) / 2;
            Location mid = s.add(xOff + 0.5, 0.5, zOff + 0.5);
            ParticleEffect.RED_DUST.play(mid, (float) xOff / 4, 0, (float) zOff / 4, 0.0f, 15);
        }
    }

    private ReceiverModule findReceiver(Block b) {
        for (int i = 0; i < MAX_SENDER_DISTANCE; i++) {
            b = b.getRelative(getDirection());
            if (!allowsItemsThrough(b.getType())) {
                break;
            }
        }
        ItemRouter rtr = LocationManager.getManager().get(b.getLocation(), ItemRouter.class);
        return rtr == null ? null : rtr.findModule(ReceiverModule.class);
    }

    private boolean allowsItemsThrough(Material mat) {
        if (mat.isTransparent()) {
            return true;
        }
        switch (mat) {
            case GLASS:
            case THIN_GLASS:
            case STAINED_GLASS:
            case STAINED_GLASS_PANE:
            case WATER:
            case ICE:
            case WALL_SIGN:
            case SIGN_POST:
                return true;
        }
        return false;
    }

}
