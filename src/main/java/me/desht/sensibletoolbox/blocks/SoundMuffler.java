package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.ParticleEffect;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;

public class SoundMuffler extends BaseSTBBlock {
	public static final int DISTANCE = 8;
	private int volume; // 0-100

	public SoundMuffler() {
		volume = 10;
	}

	public SoundMuffler(Configuration conf) {
		volume = conf.getInt("volume");
	}

	public Map<String,Object> serialize() {
		Map<String,Object> res = super.serialize();
		res.put("volume", volume);
		return res;
	}

	public static SoundMuffler deserialize(Map<String, Object> map) {
		SoundMuffler sm = new SoundMuffler(getConfigFromMap(map));
		return sm;
	}

	@Override
	public void setBaseLocation(Location loc) {
		if (loc == null && getBaseLocation() != null && SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
			SensibleToolboxPlugin.getInstance().getSoundMufflerListener().unregisterMuffler(this);
		}
		super.setBaseLocation(loc);
		if (loc != null && SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
			SensibleToolboxPlugin.getInstance().getSoundMufflerListener().registerMuffler(this);
		}
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	@Override
	public Material getBaseMaterial() {
		return Material.WOOL;
	}

	@Override
	public String getItemName() {
		return "Sound Muffler";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Reduces the volume of all", "sounds within a " + DISTANCE + "-block radius" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("WWW", "WNW", "WWW");
		recipe.setIngredient('W', Material.WOOL);
		recipe.setIngredient('N', Material.NOTE_BLOCK);
		return recipe;
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent event) {
		super.handleBlockPlace(event);
//		SensibleToolboxPlugin.getInstance().getSoundMufflerListener().registerMuffler(this);
	}

	@Override
	public void handleBlockBreak(BlockBreakEvent event) {
		super.handleBlockBreak(event);
		SensibleToolboxPlugin.getInstance().getSoundMufflerListener().unregisterMuffler(this);
	}

	@Override
	public void onServerTick(Location loc) {
		long time = loc.getWorld().getTime();
		if (time % 40 == 0) {
			if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
				ParticleEffect.NOTE.play(loc.add(0.5, 0.5, 0.5), 0.5f, 0.5f, 0.5f, 1.0f, 2);
			}
		}
	}

	@Override
	public void handleBlockInteraction(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == Material.SIGN) {
			// attach a label sign
			attachLabelSign(event);
		}
	}

	@Override
	public String[] getSignLabel() {
		return new String[] {
				getItemName(),
				ChatColor.DARK_RED + "Volume " + ChatColor.RESET + getVolume(),
				"",
				""
		};
	}
}
