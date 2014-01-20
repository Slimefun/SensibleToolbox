package me.desht.sensibletoolbox.items;

import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;

public class EnderLeash extends BaseSTBItem {
	private YamlConfiguration capturedConf;

	public EnderLeash(ConfigurationSection conf) {
		if (!conf.getKeys(false).isEmpty()) {
			capturedConf = new YamlConfiguration();
			for (String k : conf.getKeys(false)) {
				capturedConf.set(k, conf.get(k));
			}
		}
	}

	public EnderLeash() {
		capturedConf = null;
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
	public Material getBaseMaterial() {
		return Material.LEASH;
	}

	@Override
	public String getItemName() {
		return "Ender Leash";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Right-click: capture/release animal" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(this.toItemStack(1));
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

	public EntityType getCapturedFromItemMeta(ItemStack stack) {
		List<String> lore = stack.getItemMeta().getLore();
		for (String s : lore) {
			if (s.contains("Captured: ")) {
				String[] fields = ChatColor.stripColor(s).split(": ");
				return EntityType.valueOf((fields[1].split(" "))[0]);
			}
		}
		return null;
	}

	@Override
	public void handleEntityInteraction(PlayerInteractEntityEvent event) {
		Entity target = event.getRightClicked();
		Player p = event.getPlayer();
		if (target instanceof Animals && isPassive(target) && p.getItemInHand().getAmount() == 1) {
			EntityType type = getCapturedFromItemMeta(p.getItemInHand());
			if (type == null) {
				capturedConf = freezeEntity((Animals) target);
				target.getWorld().playEffect(target.getLocation(), Effect.ENDER_SIGNAL, 0);
				target.remove();
				p.setItemInHand(this.toItemStack(1));
			} else {
				// workaround CB bug to ensure client is updated properly
				p.updateInventory();
			}
		}
		event.setCancelled(true);
	}

	@Override
	public void handleItemInteraction(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !STBUtil.isInteractive(event.getClickedBlock().getType())) {
			if (capturedConf != null) {
				Block where = event.getClickedBlock().getRelative(event.getBlockFace());
				EntityType type = EntityType.valueOf(capturedConf.getString("type"));
				Entity e = where.getWorld().spawnEntity(where.getLocation().add(0.5, 0.0, 0.5), type);
				thawEntity((Animals) e, capturedConf);
				capturedConf = null;
				event.getPlayer().setItemInHand(this.toItemStack(1));
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
		conf.set("health", target.getHealth());

		if (target instanceof Tameable) {
			conf.set("tamed", ((Tameable) target).isTamed());
		}
		switch (target.getType()) {
			case SHEEP:
				conf.set("sheared", ((Sheep) target).isSheared());
				conf.set("color", ((Sheep) target).getColor().toString());
				break;
			case OCELOT:
				conf.set("sitting", ((Ocelot) target).isSitting());
				conf.set("catType", ((Ocelot) target).getCatType().toString());
				break;
			case PIG:
				conf.set("saddled", ((Pig) target).hasSaddle());
				break;
			case WOLF:
				conf.set("collar", ((Wolf) target).getCollarColor().toString());
				conf.set("sitting", ((Wolf) target).isSitting());
				break;
			case HORSE:
				Horse h = (Horse) target;
				conf.set("horseStyle", h.getStyle().toString());
				conf.set("horseVariant", h.getVariant().toString());
				conf.set("horseColor", h.getColor().toString());
				conf.set("chest", h.isCarryingChest());
				conf.set("jumpStrength", h.getJumpStrength());
				conf.set("domestication", h.getDomestication());
				conf.set("maxDomestication", h.getMaxDomestication());
				ItemStack saddle = h.getInventory().getSaddle();
				if (saddle != null) {
					conf.set("saddle", saddle.getType().toString());
				}
				ItemStack armor = h.getInventory().getArmor();
				if (armor != null) {
					conf.set("armor", armor.getType().toString());
				}
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

		if (entity instanceof Tameable) {
			((Tameable) entity).setTamed(conf.getBoolean("tamed"));
		}
		switch (entity.getType()) {
			case PIG:
				((Pig) entity).setSaddle(conf.getBoolean("saddled"));
				break;
			case SHEEP:
				((Sheep) entity).setSheared(conf.getBoolean("sheared"));
				((Sheep) entity).setColor(DyeColor.valueOf(conf.getString("color")));
				break;
			case OCELOT:
				((Ocelot) entity).setSitting(conf.getBoolean("sitting"));
				((Ocelot) entity).setCatType(Ocelot.Type.valueOf(conf.getString("catType")));
				break;
			case WOLF:
				((Wolf) entity).setSitting(conf.getBoolean("sitting"));
				((Wolf) entity).setCollarColor(DyeColor.valueOf(conf.getString("collar")));
				break;
			case HORSE:
				Horse h = (Horse) entity;
				h.setColor(Horse.Color.valueOf(conf.getString("horseColor")));
				h.setVariant(Horse.Variant.valueOf(conf.getString("horseVariant")));
				h.setStyle(Horse.Style.valueOf(conf.getString("horseStyle")));
				h.setCarryingChest(conf.getBoolean("chest"));
				h.setJumpStrength(conf.getDouble("jumpStrength"));
				h.setDomestication(conf.getInt("domestication"));
				h.setMaxDomestication(conf.getInt("maxDomestication"));
				if (conf.contains("saddle")) {
					h.getInventory().setSaddle(new ItemStack(Material.getMaterial(conf.getString("saddle"))));
				}
				if (conf.contains("armor")) {
					h.getInventory().setArmor(new ItemStack(Material.getMaterial(conf.getString("armor"))));
				}
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
			String l = ChatColor.WHITE  + "Captured: " + ChatColor.GOLD + getCapturedEntityType().toString() + name;
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
