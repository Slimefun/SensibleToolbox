package me.desht.sensibletoolbox.items;

import com.google.common.base.Joiner;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.energynet.EnergyNet;
import me.desht.sensibletoolbox.energynet.EnergyNetManager;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.material.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

public class Multimeter extends BaseSTBItem {
	private static final MaterialData md = new MaterialData(Material.BONE);

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
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("IGI", "RSR", " T ");
		recipe.setIngredient('I', Material.IRON_INGOT);
		recipe.setIngredient('G', Material.GLOWSTONE_DUST);
		recipe.setIngredient('R' ,Material.REDSTONE);
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
				showNetInfo(player, net);
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

	private void showNetInfo(Player player, EnergyNet net) {
		String s1 = net.getCableCount() == 1 ? "" : "s";
		String s2 = net.getMachineCount() == 1 ? "" : "s";
		String msg = String.format("Energy net &f#%d&-, %d cable" + s1 + ", %d machine" + s2,
				net.getNetID(), net.getCableCount(), net.getMachineCount());
		MiscUtil.statusMessage(player, msg);
		msg = String.format("▶ Instantaneous demand: &6%5.2f/t&-, supply available: &6%5.2f/t&-",
				net.getDemand(), net.getSupply());
		MiscUtil.statusMessage(player, msg);
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
