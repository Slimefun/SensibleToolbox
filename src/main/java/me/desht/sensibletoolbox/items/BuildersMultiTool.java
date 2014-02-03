package me.desht.sensibletoolbox.items;

import com.google.common.collect.Lists;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.ItemNames;
import me.desht.dhutils.block.MaterialWithData;
import me.desht.dhutils.cost.ItemCost;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.items.energycells.TenKEnergyCell;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BuildersMultiTool extends BaseSTBItem implements Chargeable {
	private static final int MAX_REPLACED = 21;
	public static final int MAX_BUILD_BLOCKS = 9;
	public static final int CHARGE_PER_OPERATION = 40;
	private Mode mode;
	private double charge;
	private MaterialWithData mat;
	public BuildersMultiTool() {
		mode = Mode.BUILD;
		charge = 0;
	}

	public BuildersMultiTool(ConfigurationSection conf) {
		mode = Mode.valueOf(conf.getString("mode"));
		charge = conf.getDouble("charge");
		String s = conf.getString("material");
		mat = s.isEmpty() ? null : MaterialWithData.get(s);
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public double getCharge() {
		return charge;
	}

	public void setCharge(double charge) {
		this.charge = charge;
	}

	public int getMaxCharge() {
		return 10000;
	}

	@Override
	public int getChargeRate() {
		return 100;
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration map = super.freeze();
		map.set("mode", mode.toString());
		map.set("charge", charge);
		map.set("material", mat == null ? "" : mat.toString());
		return map;
	}

	@Override
	public Material getBaseMaterial() {
		return Material.GOLD_AXE;
	}

	@Override
	public String getItemName() {
		return "Builder's Multitool";
	}

	@Override
	public String[] getLore() {
		switch (getMode()) {
			case BUILD:
				return new String[] {
						"L-click block: preview",
						"R-click block: build",
						"\u21e7 + R-click block: build one",
						"\u21e7 + mouse-wheel: EXCHANGE mode"
				};
			case EXCHANGE:
				return new String[] {
						"L-click block: exchange one block",
						"R-click block: exchange many blocks",
						"\u21e7 + R-click block: set target block",
						"\u21e7 + mouse-wheel: BUILD mode"
				};
			default:
				return new String[0];
		}
	}

	@Override
	public String[] getExtraLore() {
		return new String[] { STBUtil.getChargeString(this) };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		ItemStack cell = new ItemStack(Material.LEATHER_HELMET, 1, Material.LEATHER_HELMET.getMaxDurability());
		recipe.shape("DPD", "BEB", " I ");
		recipe.setIngredient('D', Material.DIAMOND);
		recipe.setIngredient('P', Material.DIAMOND_AXE);
		recipe.setIngredient('I', Material.IRON_INGOT);
		recipe.setIngredient('E', cell.getData()); // an empty 10k energy cell
		recipe.setIngredient('B', Material.IRON_FENCE);
		return recipe;
	}

	@Override
	public Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
		return mat == Material.LEATHER_HELMET ? TenKEnergyCell.class : null;
	}

	@Override
	public String getDisplaySuffix() {
		switch (getMode()) {
			case BUILD:
				return "Build";
			case EXCHANGE:
				String s;
				if (mat == null) {
					s = "";
				} else {
					s = " [" + ItemNames.lookup(new ItemStack(mat.getBukkitMaterial(), 1, mat.getData())) + "]";
				}
				return "Exchange " + s;
			default: return null;
		}
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		switch (getMode()) {
			case BUILD:
				handleBuildMode(event);
				break;
			case EXCHANGE:
				handleExchangeMode(event);
				break;
		}
	}

	@Override
	public void onItemHeld(PlayerItemHeldEvent event) {
		int delta = event.getNewSlot() - event.getPreviousSlot();
		if (delta == 0) {
			return;
		} else if (delta >= 6) {
			delta -= 9;
		} else if (delta <= -6) {
			delta += 9;
		}
		delta = (delta > 0) ? 1 : -1;
		int o = getMode().ordinal() + delta;
		if (o < 0) {
			o = Mode.values().length - 1;
		} else if (o >= Mode.values().length) {
			o = 0;
		}
		setMode(Mode.values()[o]);
		event.getPlayer().setItemInHand(toItemStack(1));
	}

	private void handleExchangeMode(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		boolean done = false;

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (player.isSneaking()) {
				// set the target material
				mat = MaterialWithData.get(event.getClickedBlock());
				done = true;
			} else if (getCharge() > 0) {
				// replace multiple blocks
				int sharpness = player.getItemInHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
				int max = (int) (MAX_REPLACED * Math.pow(1.2, sharpness));
				Block[] blocks = getReplacementCandidates(player, event.getClickedBlock(), max);
				Debugger.getInstance().debug(this + ": replacing " + blocks.length + " blocks");
				done = doExchange(player, blocks) > 0;
			}
			event.setCancelled(true);
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK && getCharge() > 0) {
			// replace one block
			Block[] blocks = getReplacementCandidates(player, event.getClickedBlock(), 1);
			done = doExchange(player, blocks) > 0;
			event.setCancelled(true);
		} else {
			return;
		}

		if (done) {
			player.setItemInHand(toItemStack(1));
		} else {
			player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 0.5f);
		}
	}

	private Block[] getReplacementCandidates(Player player, Block b, int max) {
		if (!canReplace(player, b) || mat == null || mat.getBukkitMaterial() == b.getType() && mat.getData() == b.getData()) {
			return new Block[0];
		}

		if (max <= 1) {
			return new Block[] { b };
		} else {
			Set<Block> res = new HashSet<Block>(max * 4 / 3, 0.75f);
			recursiveExchangeScan(player, b, b.getType(), b.getData(), res, max, BlockFace.SELF);
//			floodFillScan(player, b, new MaterialData(b.getType(), b.getData()), res, max);
			return res.toArray(new Block[res.size()]);
		}
	}

//	private void floodFillScan(Player player, Block b, MaterialData targetMat, Set<Block> blocks, int max) {
//		if (b.getType() != targetMat.getItemType() && b.getData() != targetMat.getData() || blocks.size() > max
//				|| blocks.contains(b) || !STBUtil.isExposed(b) || !canReplace(player, b)) {
//			return;
//		}
//		blocks.add(b);
//		for (int x = -1; x <= 1; x++) {
//			for (int y = -1; y <= 1; y++) {
//				for (int z = -1; z <= 1; z++) {
//					if (x != 0 || y != 0 || z != 0) {
//						floodFillScan(player, b.getRelative(x, y, z), targetMat, blocks, max);
//					}
//				}
//			}
//		}
//	}

	private void recursiveExchangeScan(Player player, Block b, Material mat, byte data, Set<Block> blocks, int max, BlockFace fromDirection) {
		if (b.getType() != mat || b.getData() != data || blocks.size() > max || blocks.contains(b)
				|| !STBUtil.isExposed(b) || !canReplace(player, b)) {
			return;
		}
		blocks.add(b);
		for (BlockFace toDirection : getExchangeDirections(fromDirection)) {
			recursiveExchangeScan(player, b.getRelative(toDirection), mat, data, blocks, max, toDirection);
		}
	}

	private BlockFace[] getExchangeDirections(BlockFace face) {
		switch (face) {
			case UP: return ExchangeFaces.up;
			case DOWN: return ExchangeFaces.down;
			case EAST: return ExchangeFaces.east;
			case WEST: return ExchangeFaces.west;
			case NORTH: return ExchangeFaces.north;
			case SOUTH: return ExchangeFaces.south;
			case SELF: return STBUtil.directFaces;
			default: throw new IllegalArgumentException("invalid direction " + face);
		}
	}

	private int doExchange(Player player, Block[] blocks) {
		// the blocks have already been validated as suitable for replacement at this point

		ItemStack inHand = player.getItemInHand();

		int nAffected = Math.min(blocks.length, howMuchDoesPlayerHave(player, mat));
		double chargeNeeded = CHARGE_PER_OPERATION * nAffected * Math.pow(0.8, inHand.getEnchantmentLevel(Enchantment.DIG_SPEED));
		if (nAffected > 0 && getCharge() >= chargeNeeded) {
			setCharge(getCharge() - chargeNeeded);
			ItemCost taken = new ItemCost(mat.getBukkitMaterial(), mat.getData(), nAffected);
			taken.apply(player);

			Block[] affectedBlocks = Arrays.copyOfRange(blocks, 0, nAffected);

			List<ItemStack> items = new ArrayList<ItemStack>();
			for (Block b : affectedBlocks) {
				items.addAll(STBUtil.calculateDrops(b, inHand));
			}
			HashMap<Integer,ItemStack> excess = player.getInventory().addItem(items.toArray(new ItemStack[items.size()]));
			for (ItemStack stack : excess.values()) {
				player.getWorld().dropItemNaturally(player.getLocation(), stack);
			}

			new SwapTask(player, affectedBlocks).runTaskTimer(SensibleToolboxPlugin.getInstance(), 1L, 1L);

			player.updateInventory();
		}
		return nAffected;
	}

	private int howMuchDoesPlayerHave(Player p, MaterialWithData mat) {
		int amount = 0;
		for (ItemStack stack : p.getInventory()) {
			if (stack != null && stack.getType() == mat.getBukkitMaterial() && stack.getData().getData() == mat.getData()) {
				amount += stack.getAmount();
			}
		}
		return amount;
	}

	private boolean canReplace(Player player, Block b) {
		// we won't replace any block which can hold items, or any STB block
		if (LocationManager.getManager().get(b.getLocation()) != null) {
			return false;
		} else if (b.getState() instanceof InventoryHolder) {
			return false;
		} else {
			BlockBreakEvent event = new BlockBreakEvent(b, player);
			Bukkit.getPluginManager().callEvent(event);
			return !event.isCancelled();
		}
	}

	private void handleBuildMode(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final List<Block> blocks = getBuildCandidates(player, event.getClickedBlock(), event.getBlockFace());
			MaterialWithData mwd = MaterialWithData.get(event.getClickedBlock());
			int nAffected = Math.min(blocks.size(), howMuchDoesPlayerHave(player, mwd));
			List<Block> actualBlocks = blocks.subList(0, nAffected);

			if (!actualBlocks.isEmpty()) {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					doBuild(player, event.getClickedBlock(), actualBlocks);
				} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					showBuildPreview(player, actualBlocks);
				}
			}
			event.setCancelled(true);
		}
	}

	private void showBuildPreview(final Player player, final List<Block> blocks) {
		Bukkit.getScheduler().runTask(SensibleToolboxPlugin.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Block b : blocks) {
					player.sendBlockChange(b.getLocation(), Material.STAINED_GLASS, DyeColor.WHITE.getWoolData());
				}
			}
		});
		Bukkit.getScheduler().runTaskLater(SensibleToolboxPlugin.getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Block b : blocks) {
					player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
				}
			}
		}, 20L);
	}

	private void doBuild(Player player, Block source, List<Block> actualBlocks) {
		ItemStack inHand = player.getItemInHand();
		double chargeNeeded = CHARGE_PER_OPERATION * actualBlocks.size() * Math.pow(0.8, inHand.getEnchantmentLevel(Enchantment.DIG_SPEED));
		if (getCharge() >= chargeNeeded) {
			setCharge(getCharge() - chargeNeeded);
			ItemCost cost = new ItemCost(source.getType(), source.getData(), actualBlocks.size());
			cost.apply(player);
			for (Block b : actualBlocks) {
				b.setTypeIdAndData(source.getType().getId(), source.getData(), true);
			}
			player.setItemInHand(toItemStack(1));
			player.playSound(player.getLocation(), Sound.DIG_STONE, 1.0f, 1.0f);
		} else {
			player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 0.5f);
		}
	}

	private List<Block> getBuildCandidates(Player player, Block clickedBlock, BlockFace blockFace) {
		int sharpness = player.getItemInHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
		int max = MAX_BUILD_BLOCKS + sharpness * 2;
		if (player.isSneaking()) {
			max = 1;
		}
		Set<Block> blocks = new HashSet<Block>(max * 4 / 3, 0.75f);
		floodFill2D(player, clickedBlock.getRelative(blockFace),
				new MaterialData(clickedBlock.getType(), clickedBlock.getData()),
				blockFace.getOppositeFace(), getBuildFaces(blockFace), max, blocks);
		return Lists.newArrayList(blocks);
	}

	private void floodFill2D(Player player, Block b, MaterialData target, BlockFace face, BlockFace[] faces, int max, Set<Block> blocks) {
		Block b0 = b.getRelative(face);
		if (!b.isEmpty() && !b.isLiquid() || b0.getType() != target.getItemType() || b0.getData() != target.getData()
				|| blocks.size() > max || blocks.contains(b) || !canReplace(player, b)) {
			return;
		}
		blocks.add(b);
		for (BlockFace dir : faces) {
			floodFill2D(player, b.getRelative(dir), target, face, faces, max, blocks);
		}
	}

	private BlockFace[] getBuildFaces(BlockFace face) {
		switch (face) {
			case NORTH:case SOUTH: return BuildFaces.ns;
			case EAST:case WEST: return BuildFaces.ew;
			case UP:case DOWN: return BuildFaces.ud;
		}
		throw new IllegalArgumentException("invalid face: " + face);
	}

//	private List<Block> findSquare(Player player, Block centre, BlockFace face, int radius, int currentAmount, int max) {
//		Block b = centre.getRelative(face);
//		BlockFace oppositeFace = face.getOppositeFace();
//		List<Block> res = new ArrayList<Block>();
//		LOOP: for (int i = -radius; i <= radius; i++) {
//			for (int j = -radius; j <= radius; j++) {
//				if (i > -radius && i < radius && j == -radius + 1) {
//					j = radius;
//				}
//				Block toCheck;
//				switch (face) {
//					case NORTH: case SOUTH: toCheck = b.getRelative(i, j, 0); break;
//					case UP: case DOWN: toCheck = b.getRelative(i, 0, j); break;
//					case EAST: case WEST: toCheck = b.getRelative(0, i, j); break;
//					default: throw new IllegalArgumentException("invalid face " + face);
//				}
//				if (toCheck.isEmpty()) {
//					Block rel = toCheck.getRelative(oppositeFace);
//					if (rel.getType() == centre.getType() && rel.getData() == centre.getData() && canReplace(player, toCheck)) {
//						res.add(toCheck);
//					}
//				}
//				if (currentAmount + res.size() >= max) {
//					break LOOP;
//				}
//			}
//		}
//		return res;
//	}

	private enum Mode {
		BUILD, EXCHANGE
	}

	private static class BuildFaces {
		private static final BlockFace[] ns = {  BlockFace.EAST, BlockFace.DOWN, BlockFace.WEST, BlockFace.UP };
		private static final BlockFace[] ew = {  BlockFace.NORTH, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.UP };
		private static final BlockFace[] ud = {  BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };
	}

	private static class ExchangeFaces {
		private static final BlockFace[] north = { BlockFace.EAST, BlockFace.DOWN, BlockFace.WEST, BlockFace.UP, BlockFace.NORTH };
		private static final BlockFace[] east = { BlockFace.DOWN, BlockFace.SOUTH, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST };
		private static final BlockFace[] down = { BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.DOWN };
		private static final BlockFace[] south = { BlockFace.WEST, BlockFace.UP, BlockFace.EAST, BlockFace.DOWN, BlockFace.SOUTH };
		private static final BlockFace[] west = { BlockFace.UP, BlockFace.NORTH, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.WEST };
		private static final BlockFace[] up = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP };
	}

	private class SwapTask extends BukkitRunnable {
		private final Player player;
		private final Block[] blocks;
		private int n = 0;

		private SwapTask(Player player, Block[] blocks) {
			this.player = player;
			this.blocks = blocks;
		}

		@Override
		public void run() {
			Block b = blocks[n];
			player.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getTypeId());
			b.setTypeIdAndData(mat.getId(), (byte) mat.getData(), true);
			n++;
			if (n >= blocks.length) {
				cancel();
			}
		}
	}
}
