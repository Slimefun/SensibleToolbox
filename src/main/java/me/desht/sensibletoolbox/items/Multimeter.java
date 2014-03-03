package me.desht.sensibletoolbox.items;

import com.google.common.base.Joiner;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.energynet.EnergyNet;
import me.desht.sensibletoolbox.energynet.EnergyNetManager;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import me.desht.sensibletoolbox.nametags.NameTagSpawner;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sign;

import java.util.ArrayList;
import java.util.List;

public class Multimeter extends BaseSTBItem {
	private static final MaterialData md = new MaterialData(Material.WATCH);

	public Multimeter() {
	}

	public Multimeter(ConfigurationSection conf) {
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Multimeter";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Use on cabling and machines",
				"to check energy net connections",
				"and power usage",
				"R-Click: " + ChatColor.RESET + "use"
		};
	}

	@Override
	public boolean hasGlow() {
		return true;
	}

	@Override
	public Recipe getRecipe() {
		SimpleCircuit sc = new SimpleCircuit();
		registerCustomIngredients(sc);
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("IGI", "CSC", " T ");
		recipe.setIngredient('I', Material.IRON_INGOT);
		recipe.setIngredient('G', Material.GLOWSTONE_DUST);
		recipe.setIngredient('C', sc.getMaterialData());
		recipe.setIngredient('S', Material.SIGN);
		recipe.setIngredient('T' ,Material.STICK);
		return recipe;
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			EnergyNet net = EnergyNetManager.getEnergyNet(event.getClickedBlock());
			Player player = event.getPlayer();
			if (net != null) {
				showNetInfo(player, net, event.getClickedBlock());
			} else {
				Block b;
				if (event.getClickedBlock().getType() == Material.WALL_SIGN) {
					Sign sign = (Sign)event.getClickedBlock().getState().getData();
					b = event.getClickedBlock().getRelative(sign.getAttachedFace());
				} else {
					b = event.getClickedBlock();
				}
				BaseSTBMachine machine = LocationManager.getManager().get(b.getLocation(), BaseSTBMachine.class);
				if (machine != null) {
					showMachineInfo(player, machine);
				} else {
					// nothing to examine here
					player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
				}
			}
			event.setCancelled(true);
		}
	}

	private void showNetInfo(final Player player, EnergyNet net, Block clicked) {
		String s1 = net.getCableCount() == 1 ? "" : "s";
		String s2 = net.getSourceCount() == 1 ? "" : "s";
		String s3 = net.getSinkCount() == 1 ? "" : "s";
		String line1 = String.format("Net &f#%d&-, %d cable" + s1 + ", %d source" + s2 + ", %d sink" + s3,
				net.getNetID(), net.getCableCount(), net.getSourceCount(), net.getSinkCount());
		String line2 = String.format("▶ Demand: &6%5.2f/t&-, Supply: &6%5.2f/t&-",
				net.getDemand(), net.getSupply());
//		if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
//			final NameTagSpawner spawner = new NameTagSpawner(2);
//			Location loc = clicked.getLocation();
//			spawner.setNameTag(0, player, loc, 1.0, ChatColor.translateAlternateColorCodes('&', line1));
//			spawner.setNameTag(1, player, loc, 0.75, ChatColor.translateAlternateColorCodes('&', line2));
//			Bukkit.getScheduler().runTaskLater(SensibleToolboxPlugin.getInstance(), new Runnable() {
//				@Override
//				public void run() {
//					spawner.clearNameTags(player);
//				}
//			}, 50L);
//		} else {
			MiscUtil.statusMessage(player, line1);
			MiscUtil.statusMessage(player, line2);
//		}
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);
	}

	private void showMachineInfo(Player player, BaseSTBMachine machine) {
		List<Integer> ids = new ArrayList<Integer>();
		for (EnergyNet net2 : machine.getAttachedEnergyNets()) {
			ids.add(net2.getNetID());
		}
		if (ids.isEmpty()) {
			MiscUtil.statusMessage(player,
					ChatColor.GOLD + machine.getItemName() + ChatColor.AQUA + " is not attached to any energy net.");
		} else {
			String s = ids.size() == 1 ? "" : "s";
			String nets = "[#" + Joiner.on(" #").join(ids) + "]";
			MiscUtil.statusMessage(player, String.format("&6%s&- is attached to %d energy net%s: &f%s",
					machine.getItemName(), ids.size(), s, nets));
		}
		MiscUtil.statusMessage(player, "▶ Charge: " + STBUtil.getChargeString(machine) + "&-, charge rate: &e" + machine.getChargeRate() + " SCU/t");
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);
	}
}
