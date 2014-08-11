package me.desht.sensibletoolbox.items.itemroutermodules;

import com.google.common.collect.Maps;
import me.desht.sensibletoolbox.api.util.STBUtil;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VacuumModule extends DirectionalItemRouterModule {
    private static final Dye md = makeDye(DyeColor.BLACK);
    private static final int RADIUS = 6;
    private static final Map<UUID,List<Item>> recentItemCache = Maps.newHashMap();
    private static final Map<UUID,Long> cacheTime = Maps.newHashMap();
    public static final int CACHE_TIME = 1000;
    private static final String STB_VACUUMED = "STB_Vacuumed";

    public VacuumModule() {
    }

    public VacuumModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Vacuum";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Insert into an Item Router", "Sucks up items within a " + RADIUS + "-block radius"};
    }

    @Override
    public Recipe getRecipe() {
        registerCustomIngredients(new BlankModule());
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.HOPPER);
        recipe.addIngredient(Material.EYE_OF_ENDER);
        return recipe;
    }

    private static List<Item> getItemEntities(World w) {
        // Caching the list of item entities per-world can avoid the overhead of
        // repeated getEntities() calls if there are many vacuum modules in operation.
        List<Item> list = recentItemCache.get(w.getUID());
        if (list == null) {
            list = new ArrayList<Item>();
            recentItemCache.put(w.getUID(), list);
            cacheTime.put(w.getUID(), 0L);
        }
        if (System.currentTimeMillis() - cacheTime.get(w.getUID()) > CACHE_TIME) {
            list.clear();
            list.addAll(w.getEntitiesByClass(Item.class));
            cacheTime.put(w.getUID(), System.currentTimeMillis());
        }
        return list;
    }

    @Override
    public boolean execute(Location loc) {
        int thresholdDist = RADIUS * RADIUS;
        loc.add(0.5, 0.5, 0.5);
        for (final Item item : getItemEntities(loc.getWorld())) {
            if (!item.isValid()) {
                // important, since we're looking at entities cached in the last second
                continue;
            }
            double dist = loc.distanceSquared(item.getLocation());
            if (dist >= thresholdDist) {
                continue;
            }
            final ItemStack onGround = item.getItemStack();
            ItemStack buffer = getItemRouter().getBufferItem();
            Location itemLoc = item.getLocation();
            if (item.getPickupDelay() <= 0
                    && getFilter().shouldPass(onGround)
                    && rightDirection(itemLoc, loc)
                    && (buffer == null || buffer.isSimilar(onGround))
                    && STBUtil.getMetadataValue(item, STB_VACUUMED) == null) {
                double rtrY = loc.getY();
                Vector vel = loc.subtract(itemLoc).toVector().normalize().multiply(Math.min(dist * 0.06, 0.7));
                if (itemLoc.getY() < rtrY) {
                    vel.setY(vel.getY() + (rtrY - itemLoc.getY()) / 10);
                }
                item.setMetadata(STB_VACUUMED, new FixedMetadataValue(getProviderPlugin(), getItemRouter()));
                item.setVelocity(vel);
                Bukkit.getScheduler().runTaskLater(getProviderPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        if (item.isValid()) {
                            ItemStack newBuffer = getItemRouter().getBufferItem();
                            int toSlurp = 0;
                            if (newBuffer == null) {
                                toSlurp = onGround.getAmount();
                                getItemRouter().setBufferItem(onGround);
                                item.remove();
                            } else if (newBuffer.isSimilar(onGround)) {
                                toSlurp = Math.min(onGround.getAmount(), newBuffer.getType().getMaxStackSize() - newBuffer.getAmount());
                                getItemRouter().setBufferAmount(newBuffer.getAmount() + toSlurp);
                                onGround.setAmount(onGround.getAmount() - toSlurp);
                                if (onGround.getAmount() == 0) {
                                    item.remove();
                                } else {
                                    item.setItemStack(onGround);
                                }
                            }
                            if (toSlurp > 0) {
                                getItemRouter().playParticles();
                                getItemRouter().update(false);
                            }
                        }
                    }
                }, (long) (dist / 3));
            }
        }
        return false; // any work done is deferred
    }

    private boolean rightDirection(Location itemLoc, Location rtrLoc) {
        if (getFacing() == null || getFacing() == BlockFace.SELF) {
            return true;
        }
        switch (getFacing()) {
            case NORTH:
                return itemLoc.getZ() < rtrLoc.getZ();
            case EAST:
                return itemLoc.getX() > rtrLoc.getX();
            case SOUTH:
                return itemLoc.getZ() > rtrLoc.getZ();
            case WEST:
                return itemLoc.getX() > rtrLoc.getX();
            case UP:
                return itemLoc.getY() > rtrLoc.getY();
            case DOWN:
                return itemLoc.getY() < rtrLoc.getY();
            default:
                return true;
        }
    }
}
