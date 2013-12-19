package me.desht.sensibletoolbox.items;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.ItemGlow;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.attributes.AttributeStorage;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EnderLeash extends BaseSTBItem {
	private EntityType captured;
	private String entityDetails;

	@Override
	public Material getBaseMaterial() {
		return Material.LEASH;
	}

	@Override
	public String getDisplayName() {
		return "Ender Leash";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Right-click a passive creature to capture it", "Right-click again to place the mob" };
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

	public void processItemMeta(ItemStack stack) {
		List<String> lore = stack.getItemMeta().getLore();
		for (String s : lore) {
			if (s.contains("Captured: ")) {
				String[] fields = ChatColor.stripColor(s).split(": ");
				EntityType type = EntityType.valueOf(fields[1]);
				setCaptured(type);
			}
		}
	}

	@Override
	public void handleEntityInteraction(PlayerInteractEntityEvent event) {
		Entity target = event.getRightClicked();
		System.out.println("ender leash interact entity " + target);
		Player p = event.getPlayer();
		if (target instanceof Animals && isPassive(target) && p.getItemInHand().getAmount() == 1) {
			capture((Animals) target);
			p.setItemInHand(this.toItemStack(p.getItemInHand().getAmount()));
		}
		event.setCancelled(true);
	}

	private boolean isPassive(Entity entity) {
		return !(entity instanceof Wolf) || !((Wolf) entity).isAngry();
	}


	@Override
	public void handleInteraction(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !isInteractive(event.getClickedBlock().getType())) {
			processItemMeta(event.getPlayer().getItemInHand());
			if (getCaptured() != null) {
				Block where = event.getClickedBlock().getRelative(event.getBlockFace());
				Entity e = where.getWorld().spawnEntity(where.getLocation().add(0.5, 0.0, 0.5), getCaptured());
				AttributeStorage storage = AttributeStorage.newTarget(event.getPlayer().getItemInHand(), SensibleToolboxPlugin.UNIQUE_ID);
				thaw((Animals) e, storage.getData(""));
				setCaptured(null);
				event.getPlayer().setItemInHand(this.toItemStack(1));
				event.setCancelled(true);
			}
		}
	}

	private void thaw(Animals entity, String data) {
		System.out.println("thaw entity: " + entity + ", data:" + data);
		YamlConfiguration conf = new YamlConfiguration();
		try {
			conf.loadFromString(data);
		} catch (InvalidConfigurationException e1) {
			e1.printStackTrace();
			return;
		}

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

	private void capture(Animals target) {
		entityDetails = freeze(target);
		System.out.println("froze " + target + " data = " + entityDetails);
		setCaptured(target.getType());
		target.remove();
	}

	private String freeze(Animals target) {
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
		return conf.saveToString();
	}

	@Override
	public ItemStack toItemStack(int amount) {
		ItemStack stack = super.toItemStack(amount);
		if (captured != null) {
			ItemMeta im = stack.getItemMeta();
			List<String> lore = im.getLore();
			List<String> newLore = new ArrayList<String>(lore);
			newLore.add(ChatColor.WHITE  + "Captured: " + ChatColor.GOLD + captured.toString());
			im.setLore(newLore);
			stack.setItemMeta(im);
			ItemGlow.setGlowing(stack, true);
			AttributeStorage storage = AttributeStorage.newTarget(stack, SensibleToolboxPlugin.UNIQUE_ID);
			storage.setData(entityDetails);
			return storage.getTarget();
		} else {
			return stack;
		}
	}

	public static ItemStack setAnimalName(ItemStack stack, String newName) {
		AttributeStorage storage = AttributeStorage.newTarget(stack, SensibleToolboxPlugin.UNIQUE_ID);
		YamlConfiguration conf = new YamlConfiguration();
		try {
			conf.loadFromString(storage.getData(""));
			conf.set("name", newName);
			storage.setData(conf.saveToString());
			return storage.getTarget();
		} catch (InvalidConfigurationException e) {
			throw new DHUtilsException("can't modify this Ender Leash! (data corrupted?)");
		}
	}

	public void setCaptured(EntityType type) {
		this.captured = type;
	}

	public EntityType getCaptured() {
		return captured;
	}

	public boolean isInteractive(Material mat) {
		if (!mat.isBlock()) {
			return false;
		}
		switch (mat) {
			case DISPENSER:
			case NOTE_BLOCK:
			case BED_BLOCK:
			case CHEST:
			case WORKBENCH:
			case FURNACE:
			case BURNING_FURNACE:
			case WOODEN_DOOR:
			case LEVER:
			case REDSTONE_ORE:
			case STONE_BUTTON:
			case JUKEBOX:
			case CAKE_BLOCK:
			case DIODE_BLOCK_ON:
			case DIODE_BLOCK_OFF:
			case TRAP_DOOR:
			case FENCE_GATE:
			case ENCHANTMENT_TABLE:
			case BREWING_STAND:
			case DRAGON_EGG:
			case ENDER_CHEST:
			case COMMAND:
			case BEACON:
			case WOOD_BUTTON:
			case ANVIL:
			case TRAPPED_CHEST:
			case REDSTONE_COMPARATOR_ON:
			case REDSTONE_COMPARATOR_OFF:
			case HOPPER:
			case DROPPER:
				return true;
			default:
				return false;
		}
	}
}
