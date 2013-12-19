package me.desht.sensibletoolbox.items;

import me.desht.dhutils.LogUtils;
import me.desht.dhutils.ParticleEffect;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class AngelicBlock extends BaseSTBItem {

//	@Override
//	public Map<String, Object> serialize() {
//		return super.serialize();
//	}

	public static AngelicBlock deserialize() {
		// no specific config for this block
		return new AngelicBlock();
	}

	@Override
	public Material getBaseMaterial() {
		return Material.OBSIDIAN;
	}

	@Override
	public String getDisplayName() {
		return "Angelic Block";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Right-click to place this block in mid-air" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(this.toItemStack(1));
		recipe.shape(" G ", "FOF");
		recipe.setIngredient('G', Material.GOLD_INGOT);
		recipe.setIngredient('F', Material.FEATHER);
		recipe.setIngredient('O', Material.OBSIDIAN);
		return recipe;
	}

	@Override
	public void handleInteraction(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			// place the block in the air 2 blocks in the direction the player is looking at
			Player p = event.getPlayer();
			Vector v = p.getLocation().getDirection().normalize().multiply(2.0);
			Location loc = p.getEyeLocation().add(v);
			Block b = loc.getBlock();
			if (b.isEmpty()) {
				LogUtils.fine("placing angelic block...");
				ItemStack stack = p.getItemInHand();
				if (stack.getAmount() > 1) {
					stack.setAmount(stack.getAmount() - 1);
					p.setItemInHand(stack);
				} else {
					p.setItemInHand(new ItemStack(Material.AIR));
				}
				b.setType(getBaseMaterial());
				blockPlaced(b, this);
			}
		}
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent event) {
		// we don't allow normal placing of angelic blocks
		event.setCancelled(true);
	}

	@Override
	public void handleBlockDamage(BlockDamageEvent event) {
		// the angelic block has just been hit by a player
		Player p = event.getPlayer();
		if (p.hasPermission("stb.break_angelic_block")) {
			Block b = event.getBlock();
			b.setType(Material.AIR);
			p.getInventory().addItem(this.toItemStack(1));
			b.getWorld().playEffect(b.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
			blockRemoved(b, this);
			event.setCancelled(true);
		}
	}

	@Override
	public void onServerTick(PersistableLocation pLoc) {
		Location loc = pLoc.getLocation();
		long time = loc.getWorld().getTime();
		if (time % 40 == 0) {
			if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
				ParticleEffect.CLOUD.play(loc.add(0, 0.5, 0), 0f, 0f, 0f, 0.25f, 4);
			} else {
				loc.getWorld().playEffect(loc.add(0, 0.5, 0), Effect.SMOKE, BlockFace.UP);
			}
		}

	}
}
