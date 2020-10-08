package io.github.thebusybiscuit.sensibletoolbox.items;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Cat.Type;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.utils.BukkitSerialization;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.text.LogUtils;

public class EnderLeash extends BaseSTBItem {

    private YamlConfiguration capturedConf;
    private static final long MIN_THAW_DELAY = 50;

    public EnderLeash() {
        super();
        capturedConf = null;
    }

    public EnderLeash(ConfigurationSection conf) {
        super(conf);
        if (!conf.getKeys(false).isEmpty()) {
            capturedConf = new YamlConfiguration();
            for (String k : conf.getKeys(false)) {
                capturedConf.set(k, conf.get(k));
            }
        }
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration res = super.freeze();

        if (capturedConf != null) {
            for (String k : capturedConf.getKeys(false)) {
                res.set(k, capturedConf.get(k));
            }
        }
        return res;
    }

    @Override
    public Material getMaterial() {
        return Material.LEAD;
    }

    @Override
    public String getItemName() {
        return "Ender Leash";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Capture and store one peaceful animal", "Right-click: " + ChatColor.WHITE + "capture/release animal" };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), this.toItemStack());
        recipe.shape("GSG", "SPS", "GSG");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('P', Material.ENDER_PEARL);
        recipe.setIngredient('S', Material.STRING);
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return capturedConf != null;
    }

    @Override
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Entity target = event.getRightClicked();
        Player player = event.getPlayer();
        if (event.getHand() == EquipmentSlot.HAND && target instanceof Animals && isPassive(target) && player.getInventory().getItemInMainHand().getAmount() == 1) {
            if (capturedConf == null || !capturedConf.contains("type")) {
                Animals animal = (Animals) target;
                if (!checkLeash(animal)) {
                    STBUtil.complain(player, "Can't capture a leashed animal!");
                } else if (!verifyOwner(player, animal)) {
                    STBUtil.complain(player, "This animal is owned by someone else!");
                } else {
                    capturedConf = freezeEntity(animal);
                    target.getWorld().playEffect(target.getLocation(), Effect.ENDER_SIGNAL, 0);
                    target.remove();
                    updateHeldItemStack(event.getPlayer(), event.getHand());
                }
            } else {
                // workaround CB bug to ensure client is updated properly
                STBUtil.complain(event.getPlayer(), "Ender Leash already has a captured animal");
                player.updateInventory();
            }
        }
        event.setCancelled(true);
    }

    private boolean verifyOwner(Player player, Animals animal) {
        if (animal instanceof Tameable) {
            AnimalTamer owner = ((Tameable) animal).getOwner();

            if (owner.getUniqueId() != player.getUniqueId()) {
                return player.hasPermission("stb.enderleash.captureany");
            }
        }

        return true;
    }

    private boolean checkLeash(Animals animal) {
        if (animal.isLeashed()) {
            Entity leashHolder = animal.getLeashHolder();
            if (leashHolder instanceof LeashHitch) {
                leashHolder.remove();
                animal.getWorld().dropItemNaturally(animal.getLocation(), new ItemStack(Material.LEAD));
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getClickedBlock().getType().isInteractable()) {
            if (capturedConf != null && capturedConf.contains("type")) {
                // Check enough time has passed since freezing entity
                if (System.currentTimeMillis() - capturedConf.getLong("captureTime") < MIN_THAW_DELAY) {
                    return;
                }

                Block where = event.getClickedBlock().getRelative(event.getBlockFace());
                EntityType type = EntityType.valueOf(capturedConf.getString("type"));
                Entity e = where.getWorld().spawnEntity(where.getLocation().add(0.5, 0.0, 0.5), type);
                thawEntity((Animals) e, capturedConf);
                capturedConf = null;
                updateHeldItemStack(event.getPlayer(), event.getHand());
                event.setCancelled(true);
            }
        }
    }

    private boolean isPassive(Entity entity) {
        return !(entity instanceof Wolf) || !((Wolf) entity).isAngry();
    }

    private YamlConfiguration freezeEntity(Animals target) {
        Debugger.getInstance().debug(this + ": freeze entity " + target);
        YamlConfiguration conf = new YamlConfiguration();

        conf.set("type", target.getType().toString());
        conf.set("age", target.getAge());
        conf.set("ageLock", target.getAgeLock());
        conf.set("name", target.getCustomName() == null ? "" : target.getCustomName());
        conf.set("nameVisible", target.isCustomNameVisible());
        conf.set("maxHealth", target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        conf.set("health", target.getHealth());
        conf.set("captureTime", System.currentTimeMillis());

        if (target instanceof Tameable) {
            conf.set("tamed", ((Tameable) target).isTamed());
            if (((Tameable) target).getOwner() != null) {
                conf.set("owner", ((Tameable) target).getOwner().getUniqueId().toString());
            }
        }

        switch (target.getType()) {
        case SHEEP:
            Sheep sheep = (Sheep) target;
            conf.set("sheared", sheep.isSheared());
            conf.set("color", sheep.getColor().toString());
            break;
        case CAT:
            Cat cat = (Cat) target;
            conf.set("catType", cat.getCatType().toString());
            conf.set("collarColor", cat.getCollarColor().toString());
            break;
        case PIG:
            conf.set("saddled", ((Pig) target).hasSaddle());
            break;
        case WOLF:
            Wolf wolf = (Wolf) target;
            conf.set("collarColor", wolf.getCollarColor().toString());
            conf.set("sitting", wolf.isSitting());
            break;
        case HORSE:
        case MULE:
        case DONKEY:
        case ZOMBIE_HORSE:
        case SKELETON_HORSE:
            if (target instanceof Horse) {
                Horse horse = (Horse) target;
                conf.set("horseStyle", horse.getStyle().toString());
                conf.set("horseColor", horse.getColor().toString());
            } else if (target instanceof ChestedHorse) {
                ChestedHorse chestedHorse = (ChestedHorse) target;
                conf.set("chest", chestedHorse.isCarryingChest());
            }

            AbstractHorse h = (AbstractHorse) target;
            conf.set("jumpStrength", h.getJumpStrength());
            conf.set("domestication", h.getDomestication());
            conf.set("maxDomestication", h.getMaxDomestication());
            conf.set("inventory", BukkitSerialization.toBase64(h.getInventory()));
            break;
        default:
            break;
        }
        return conf;
    }

    private void thawEntity(Animals entity, Configuration conf) {
        Debugger.getInstance().debug("thaw entity: " + entity + ", data:" + conf.getString("type"));

        entity.setAge(conf.getInt("age"));
        entity.setAgeLock(conf.getBoolean("ageLock"));
        entity.setCustomName(conf.getString("name"));
        entity.setCustomNameVisible(conf.getBoolean("nameVisible"));
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(conf.getDouble("maxHealth"));
        entity.setHealth(conf.getDouble("health"));

        if (entity instanceof Tameable) {
            ((Tameable) entity).setTamed(conf.getBoolean("tamed"));
            if (conf.contains("owner")) {
                UUID id = UUID.fromString(conf.getString("owner"));
                ((Tameable) entity).setOwner(Bukkit.getOfflinePlayer(id));
            }
        }

        switch (entity.getType()) {
        case PIG:
            ((Pig) entity).setSaddle(conf.getBoolean("saddled"));
            break;
        case SHEEP:
            Sheep sheep = (Sheep) entity;
            sheep.setSheared(conf.getBoolean("sheared"));
            sheep.setColor(DyeColor.valueOf(conf.getString("color")));
            break;
        case CAT:
            Cat cat = (Cat) entity;
            cat.setCatType(Type.valueOf(conf.getString("catType")));
            cat.setCollarColor(DyeColor.valueOf(conf.getString("collarColor")));
            break;
        case WOLF:
            Wolf wolf = (Wolf) entity;
            wolf.setSitting(conf.getBoolean("sitting"));
            wolf.setCollarColor(DyeColor.valueOf(conf.getString("collarColor")));
            break;
        case HORSE:
        case MULE:
        case DONKEY:
        case ZOMBIE_HORSE:
        case SKELETON_HORSE:
            if (entity instanceof Horse) {
                Horse h = (Horse) entity;
                h.setColor(Horse.Color.valueOf(conf.getString("horseColor")));
                h.setStyle(Horse.Style.valueOf(conf.getString("horseStyle")));
            } else if (entity instanceof ChestedHorse) {
                ChestedHorse chestedHorse = (ChestedHorse) entity;
                chestedHorse.setCarryingChest(conf.getBoolean("chest"));
            }

            AbstractHorse abstractHorse = (AbstractHorse) entity;
            abstractHorse.setJumpStrength(conf.getDouble("jumpStrength"));
            abstractHorse.setDomestication(conf.getInt("domestication"));
            abstractHorse.setMaxDomestication(conf.getInt("maxDomestication"));

            try {
                Inventory inv = BukkitSerialization.fromBase64(conf.getString("inventory"));
                for (int i = 0; i < abstractHorse.getInventory().getSize(); i++) {
                    abstractHorse.getInventory().setItem(i, inv.getItem(i));
                }
            } catch (IOException e) {
                LogUtils.warning("could not restore horse inventory!");
                e.printStackTrace();
            }
            break;
        default:
            break;
        }
    }

    @Override
    public String[] getExtraLore() {
        if (capturedConf != null) {
            String name = capturedConf.getString("name");
            if (!name.isEmpty()) {
                name = " (" + name + ")";
            }
            String l = ChatColor.WHITE + "Captured: " + ChatColor.GOLD + getCapturedEntityType().toString() + name;
            return new String[] { l };
        } else {
            return new String[0];
        }
    }

    public EntityType getCapturedEntityType() {
        return capturedConf == null ? null : EntityType.valueOf(capturedConf.getString("type"));
    }

    public void setAnimalName(String newName) {
        if (capturedConf != null) {
            capturedConf.set("name", newName);
        }
    }
}
