package me.desht.sensibletoolbox.items.itemroutermodules;

import com.google.common.collect.Maps;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
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
        int dist = RADIUS * RADIUS;
        loc.add(0.5, 0.5, 0.5);
        ItemStack buffer = getItemRouter().getBufferItem();
        boolean acted = false;
        for (Item item : getItemEntities(loc.getWorld())) {
            if (!item.isValid()) {
                // important, since we're looking at entities cached in the last second
                continue;
            }
            double d = loc.distanceSquared(item.getLocation());
            if (d >= dist) {
                continue;
            }
            ItemStack onGround = item.getItemStack();
            if (item.getPickupDelay() > 0 || !getFilter().shouldPass(onGround) || !rightDirection(item, loc)) {
                continue;
            }
            if (d < 2.0) {
                // slurp?
                if (buffer == null) {
                    getItemRouter().setBufferItem(onGround);
                    buffer = getItemRouter().getBufferItem();
                    item.remove();
                } else if (buffer.isSimilar(onGround)) {
                    int toSlurp = Math.min(onGround.getAmount(), buffer.getType().getMaxStackSize() - buffer.getAmount());
                    getItemRouter().setBufferAmount(buffer.getAmount() + toSlurp);
                    buffer = getItemRouter().getBufferItem();
                    onGround.setAmount(onGround.getAmount() - toSlurp);
                    if (onGround.getAmount() == 0) {
                        item.remove();
                    } else {
                        item.setItemStack(onGround);
                    }
                }
                acted = true;
            } else {
                Vector vel = loc.subtract(item.getLocation()).toVector().normalize().multiply(Math.min(d * 0.06, 1.0));
                item.setVelocity(vel);
            }
        }
        return acted;
    }

    private boolean rightDirection(Item item, Location loc) {
        if (getDirection() == null || getDirection() == BlockFace.SELF) {
            return true;
        }
        Location itemLoc = item.getLocation();
        switch (getDirection()) {
            case NORTH:
                return itemLoc.getZ() < loc.getZ();
            case EAST:
                return itemLoc.getX() > loc.getX();
            case SOUTH:
                return itemLoc.getZ() > loc.getZ();
            case WEST:
                return itemLoc.getX() > loc.getX();
            case UP:
                return itemLoc.getY() > loc.getY();
            case DOWN:
                return itemLoc.getY() < loc.getY();
            default:
                return true;
        }
    }
}
