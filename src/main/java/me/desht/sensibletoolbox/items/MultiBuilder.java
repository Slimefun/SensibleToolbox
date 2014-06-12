package me.desht.sensibletoolbox.items;

import com.google.common.collect.Lists;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.ItemNames;
import me.desht.dhutils.cost.ItemCost;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import me.desht.sensibletoolbox.items.energycells.TenKEnergyCell;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.PopupMessage;
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
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MultiBuilder extends BaseSTBItem implements Chargeable {
    private static final MaterialData md = new MaterialData(Material.GOLD_AXE);
    private static final int MAX_REPLACED = 21;
    public static final int MAX_BUILD_BLOCKS = 9;
    public static final int DEF_CHARGE_PER_OPERATION = 40;
    private Mode mode;
    private double charge;
    private MaterialData mat;

    public MultiBuilder() {
        super();
        mode = Mode.BUILD;
        charge = 0;
    }

    public MultiBuilder(ConfigurationSection conf) {
        super(conf);
        mode = Mode.valueOf(conf.getString("mode"));
        charge = conf.getDouble("charge");
        String s = conf.getString("material");
        mat = s.isEmpty() ? null : thawMaterialData(s);
    }

    private MaterialData thawMaterialData(String s) {
        String[] f = s.split(":");
        Material mat = Material.matchMaterial(f[0]);
        byte data = f.length > 1 ? Byte.parseByte(f[1]) : 0;
        return new MaterialData(mat, data);
    }

    private String freezeMaterialData(MaterialData mat) {
        return mat.getItemType().toString() + ":" + Byte.toString(mat.getData());
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
        map.set("material", mat == null ? "" : freezeMaterialData(mat));
        return map;
    }

    @Override
    public String getItemName() {
        return "Multibuilder";
    }

    @Override
    public String[] getLore() {
        switch (getMode()) {
            case BUILD:
                return new String[]{
                        "L-click block: preview",
                        "R-click block: build",
                        "\u21e7 + R-click block: build one",
                        "\u21e7 + mouse-wheel: EXCHANGE mode"
                };
            case EXCHANGE:
                return new String[]{
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
        return new String[]{STBUtil.getChargeString(this)};
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        TenKEnergyCell cell = new TenKEnergyCell();
        cell.setCharge(0.0);
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(cell, sc);
        recipe.shape(" DP", "CED", "I  ");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('P', Material.DIAMOND_AXE);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('E', STBUtil.makeWildCardMaterialData(cell));
        recipe.setIngredient('C', sc.toItemStack().getData());
        return recipe;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getDisplaySuffix() {
        switch (getMode()) {
            case BUILD:
                return "Build";
            case EXCHANGE:
                String s = mat == null ? "" : " [" + ItemNames.lookup(mat.toItemStack(1)) + "]";
                return "Swap " + s;
            default:
                return null;
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
        event.getPlayer().setItemInHand(toItemStack());
    }

    private void handleExchangeMode(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        boolean done = false;

        Block clicked = event.getClickedBlock();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // set the target material
                mat = new MaterialData(clicked.getType(), clicked.getData());
                done = true;
            } else if (mat != null) {
                // replace multiple blocks
                int sharpness = player.getItemInHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                int max = (int) (MAX_REPLACED * Math.pow(1.2, sharpness));
                Block[] blocks = getReplacementCandidates(player, event.getClickedBlock(), max);
                Debugger.getInstance().debug(this + ": replacing " + blocks.length + " blocks");
                done = doExchange(player, blocks, clicked) > 0;
            }
            event.setCancelled(true);
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && mat != null) {
            // replace one block
            Block[] blocks = getReplacementCandidates(player, event.getClickedBlock(), 1);
            done = doExchange(player, blocks, clicked) > 0;
            event.setCancelled(true);
        } else {
            return;
        }

        if (done) {
            player.setItemInHand(toItemStack());
        } else {
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 0.5f);
        }
    }

    private Block[] getReplacementCandidates(Player player, Block b, int max) {
        if (!canReplace(player, b) || mat == null || mat.getItemType() == b.getType() && mat.getData() == b.getData()) {
            return new Block[0];
        }

        if (max <= 1) {
            return new Block[]{b};
        } else {
            Set<Block> res = new HashSet<Block>(max * 4 / 3, 0.75f);
            recursiveExchangeScan(player, b, b.getType(), b.getData(), res, max, BlockFace.SELF);
            return res.toArray(new Block[res.size()]);
        }
    }


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
            case UP:
                return ExchangeFaces.up;
            case DOWN:
                return ExchangeFaces.down;
            case EAST:
                return ExchangeFaces.east;
            case WEST:
                return ExchangeFaces.west;
            case NORTH:
                return ExchangeFaces.north;
            case SOUTH:
                return ExchangeFaces.south;
            case SELF:
                return STBUtil.directFaces;
            default:
                throw new IllegalArgumentException("invalid direction " + face);
        }
    }

    private int doExchange(Player player, Block[] blocks, Block clicked) {
        // the blocks have already been validated as suitable for replacement at this point

        ItemStack inHand = player.getItemInHand();

        int howMuch = howMuchDoesPlayerHave(player, mat);
        if (howMuch == 0) {
            PopupMessage.quickMessage(player, clicked.getLocation(),
                    ChatColor.RED + "Out of " + ItemNames.lookup(mat.toItemStack(1)) + "!");
            return 0;
        }
        int nAffected = Math.min(blocks.length, howMuch);
        int chargePerOp = SensibleToolboxPlugin.getInstance().getConfig().getInt("multibuilder.charge_per_op", DEF_CHARGE_PER_OPERATION);
        double chargeNeeded = chargePerOp * nAffected * Math.pow(0.8, inHand.getEnchantmentLevel(Enchantment.DIG_SPEED));
        if (nAffected > 0 && getCharge() >= chargeNeeded) {
            setCharge(getCharge() - chargeNeeded);
            ItemCost taken = new ItemCost(mat.toItemStack(nAffected));
            taken.apply(player);

            Block[] affectedBlocks = Arrays.copyOfRange(blocks, 0, nAffected);

            List<ItemStack> items = new ArrayList<ItemStack>();
            for (Block b : affectedBlocks) {
                items.addAll(STBUtil.calculateDrops(b, inHand));
            }
            HashMap<Integer, ItemStack> excess = player.getInventory().addItem(items.toArray(new ItemStack[items.size()]));
            for (ItemStack stack : excess.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), stack);
            }

            new SwapTask(player, affectedBlocks).runTaskTimer(SensibleToolboxPlugin.getInstance(), 1L, 1L);

            player.updateInventory();
        } else if (getCharge() < chargeNeeded) {
            PopupMessage.quickMessage(player, clicked.getLocation(), ChatColor.RED + "Not enough power!");
        }
        return nAffected;
    }

    private int howMuchDoesPlayerHave(Player p, MaterialData mat) {
        int amount = 0;
        for (ItemStack stack : p.getInventory()) {
            if (stack != null && stack.getType() == mat.getItemType() &&
                    (losesDataWhenBroken(mat.getItemType()) || stack.getData().getData() == mat.getData())) {
                amount += stack.getAmount();
            }
        }
        return amount;
    }

    private boolean losesDataWhenBroken(Material mat) {
        MaterialData md = mat.getNewData((byte) 0);
        return md instanceof Directional;
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
            MaterialData m = new MaterialData(event.getClickedBlock().getType(), event.getClickedBlock().getData());
            int nAffected = Math.min(blocks.size(), howMuchDoesPlayerHave(player, m));
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
        int chargePerOp = SensibleToolboxPlugin.getInstance().getConfig().getInt("multibuilder.charge_per_op", DEF_CHARGE_PER_OPERATION);
        double chargeNeeded = chargePerOp * actualBlocks.size() * Math.pow(0.8, inHand.getEnchantmentLevel(Enchantment.DIG_SPEED));
        if (getCharge() >= chargeNeeded) {
            setCharge(getCharge() - chargeNeeded);
            ItemCost cost = new ItemCost(source.getType(), source.getData(), actualBlocks.size());
            cost.apply(player);
            for (Block b : actualBlocks) {
                b.setTypeIdAndData(source.getType().getId(), source.getData(), true);
            }
            player.setItemInHand(toItemStack());
            player.playSound(player.getLocation(), Sound.DIG_STONE, 1.0f, 1.0f);
        } else {
            PopupMessage.quickMessage(player, source.getLocation(), ChatColor.RED + "Not enough power!");
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
            case NORTH:
            case SOUTH:
                return BuildFaces.ns;
            case EAST:
            case WEST:
                return BuildFaces.ew;
            case UP:
            case DOWN:
                return BuildFaces.ud;
        }
        throw new IllegalArgumentException("invalid face: " + face);
    }

    private enum Mode {
        BUILD, EXCHANGE
    }

    private static class BuildFaces {
        private static final BlockFace[] ns = {BlockFace.EAST, BlockFace.DOWN, BlockFace.WEST, BlockFace.UP};
        private static final BlockFace[] ew = {BlockFace.NORTH, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.UP};
        private static final BlockFace[] ud = {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};
    }

    private static class ExchangeFaces {
        private static final BlockFace[] north = {BlockFace.EAST, BlockFace.DOWN, BlockFace.WEST, BlockFace.UP, BlockFace.NORTH};
        private static final BlockFace[] east = {BlockFace.DOWN, BlockFace.SOUTH, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST};
        private static final BlockFace[] down = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.DOWN};
        private static final BlockFace[] south = {BlockFace.WEST, BlockFace.UP, BlockFace.EAST, BlockFace.DOWN, BlockFace.SOUTH};
        private static final BlockFace[] west = {BlockFace.UP, BlockFace.NORTH, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.WEST};
        private static final BlockFace[] up = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP};
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
            player.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
            b.setTypeIdAndData(mat.getItemTypeId(), mat.getData(), true);
            n++;
            if (n >= blocks.length) {
                cancel();
            }
        }
    }
}
