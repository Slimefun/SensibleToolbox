package io.github.thebusybiscuit.sensibletoolbox.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.items.components.IntegratedCircuit;
import io.github.thebusybiscuit.sensibletoolbox.items.energycells.TenKEnergyCell;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.util.UnicodeSymbol;
import io.github.thebusybiscuit.sensibletoolbox.util.VanillaInventoryUtils;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.block.BlockAndPosition;
import me.desht.dhutils.block.BlockUtil;
import me.desht.dhutils.cost.ItemCost;

public class MultiBuilder extends BaseSTBItem implements Chargeable {

    public static final int MAX_BUILD_BLOCKS = 9;
    public static final int DEF_SCU_PER_OPERATION = 40;
    private static final Map<UUID, LinkedBlockingQueue<SwapRecord>> swapQueues = new HashMap<>();
    private Mode mode;
    private double charge;
    private Material material;

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
        material = s.isEmpty() ? null : Material.matchMaterial(s);
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
        map.set("material", material == null ? "" : material.name());
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
            return new String[] { "L-click block: " + ChatColor.WHITE + "preview", "R-click block: " + ChatColor.WHITE + "build", UnicodeSymbol.ARROW_UP.toUnicode() + " + R-click block: " + ChatColor.WHITE + "build one", UnicodeSymbol.ARROW_UP.toUnicode() + " + mouse-wheel: " + ChatColor.WHITE + "EXCHANGE mode" };
        case EXCHANGE:
            return new String[] { "L-click block: " + ChatColor.WHITE + "exchange one block", "R-click block: " + ChatColor.WHITE + "exchange many blocks", UnicodeSymbol.ARROW_UP.toUnicode() + " + R-click block: " + ChatColor.WHITE + "set target block", UnicodeSymbol.ARROW_UP.toUnicode() + " + mouse-wheel: " + ChatColor.WHITE + "BUILD mode" };
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
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        TenKEnergyCell cell = new TenKEnergyCell();
        cell.setCharge(0.0);
        IntegratedCircuit sc = new IntegratedCircuit();
        registerCustomIngredients(cell, sc);
        recipe.shape(" DP", "CED", "I  ");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('P', Material.DIAMOND_AXE);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('E', cell.getMaterial());
        recipe.setIngredient('C', sc.getMaterial());
        return recipe;
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_AXE;
    }

    @Override
    public String getDisplaySuffix() {
        switch (getMode()) {
        case BUILD:
            return "Build";
        case EXCHANGE:
            String s = material == null ? "" : " [" + ItemUtils.getItemName(new ItemStack(material)) + "]";
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
        default:
            break;
        }
    }

    @Override
    public void onItemHeld(PlayerItemHeldEvent event) {
        int delta = event.getNewSlot() - event.getPreviousSlot();

        if (delta == 0) {
            return;
        }
        else if (delta >= 6) {
            delta -= 9;
        }
        else if (delta <= -6) {
            delta += 9;
        }

        delta = (delta > 0) ? 1 : -1;
        int o = getMode().ordinal() + delta;

        if (o < 0) {
            o = Mode.values().length - 1;
        }
        else if (o >= Mode.values().length) {
            o = 0;
        }

        setMode(Mode.values()[o]);
        event.getPlayer().setItemInHand(toItemStack());
    }

    private void handleExchangeMode(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // set the target material
                material = clicked.getType();
                player.setItemInHand(toItemStack());
            }
            else if (material != null) {
                // replace multiple blocks
                int sharpness = player.getItemInHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                int layers = 3 + sharpness;
                startSwap(event.getPlayer(), this, clicked, material, layers);
                Debugger.getInstance().debug(this + ": replacing " + layers + " layers of blocks");
            }

            event.setCancelled(true);
        }
        else if (event.getAction() == Action.LEFT_CLICK_BLOCK && material != null) {
            // replace a single block
            startSwap(event.getPlayer(), this, clicked, material, 0);
            event.setCancelled(true);
        }
    }

    private void startSwap(Player player, MultiBuilder builder, Block origin, Material target, int maxBlocks) {
        LinkedBlockingQueue<SwapRecord> queue = swapQueues.get(player.getWorld().getUID());

        if (queue == null) {
            queue = new LinkedBlockingQueue<>();
            swapQueues.put(player.getWorld().getUID(), queue);
        }

        if (queue.isEmpty()) {
            new QueueSwapper(queue).runTaskTimer(SensibleToolbox.getPluginInstance(), 1L, 1L);
        }

        int chargePerOp = getItemConfig().getInt("scu_per_op", DEF_SCU_PER_OPERATION);
        double chargeNeeded = chargePerOp * Math.pow(0.8, player.getItemInHand().getEnchantmentLevel(Enchantment.DIG_SPEED));
        queue.offer(new SwapRecord(player, origin, origin.getType(), target, maxBlocks, builder, -1, chargeNeeded));
    }

    private int howMuchDoesPlayerHave(Player p, Material mat) {
        int amount = 0;

        for (ItemStack stack : p.getInventory()) {
            if (stack != null && !stack.hasItemMeta() && stack.getType() == mat) {
                amount += stack.getAmount();
            }
        }

        return amount;
    }

    private boolean canReplace(Player player, Block b) {
        // we won't replace any block which can hold items, or any STB block, or any unbreakable block
        if (SensibleToolbox.getBlockAt(b.getLocation(), true) != null) {
            return false;
        }
        else if (VanillaInventoryUtils.isVanillaInventory(b)) {
            return false;
        }
        else if (b.getType().getHardness() >= 3600000) {
            return false;
        }
        else {
            return SensibleToolbox.getProtectionManager().hasPermission(player, b, ProtectableAction.BREAK_BLOCK);
        }
    }

    private void handleBuildMode(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Set<Block> blocks = getBuildCandidates(player, event.getClickedBlock(), event.getBlockFace());

            if (!blocks.isEmpty()) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    doBuild(player, event.getClickedBlock(), blocks);
                }
                else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    showBuildPreview(player, blocks);
                }
            }

            event.setCancelled(true);
        }
    }

    private void showBuildPreview(Player player, Set<Block> blocks) {
        Bukkit.getScheduler().runTask(getProviderPlugin(), () -> {
            for (Block b : blocks) {
                player.sendBlockChange(b.getLocation(), Material.WHITE_STAINED_GLASS.createBlockData());
            }
        });

        Bukkit.getScheduler().runTaskLater(getProviderPlugin(), () -> {
            for (Block b : blocks) {
                player.sendBlockChange(b.getLocation(), b.getBlockData());
            }
        }, 20L);
    }

    private void doBuild(Player player, Block source, Set<Block> actualBlocks) {
        ItemStack inHand = player.getItemInHand();
        int chargePerOp = getItemConfig().getInt("scu_per_op", DEF_SCU_PER_OPERATION);
        double chargeNeeded = chargePerOp * actualBlocks.size() * Math.pow(0.8, inHand.getEnchantmentLevel(Enchantment.DIG_SPEED));
        // we know at this point that the tool has sufficient charge and that the player has sufficient material
        setCharge(getCharge() - chargeNeeded);
        ItemCost cost = new ItemCost(source.getType(), actualBlocks.size());
        cost.apply(player);

        for (Block b : actualBlocks) {
            b.setType(source.getType(), true);
        }

        player.setItemInHand(toItemStack());
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
    }

    private Set<Block> getBuildCandidates(Player player, Block clickedBlock, BlockFace blockFace) {
        int sharpness = player.getItemInHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
        int max = MAX_BUILD_BLOCKS + sharpness * 3;
        Material clickedType = clickedBlock.getType();
        double chargePerOp = getItemConfig().getInt("scu_per_op", DEF_SCU_PER_OPERATION) * Math.pow(0.8, player.getItemInHand().getEnchantmentLevel(Enchantment.DIG_SPEED));
        int ch = (int) (getCharge() / chargePerOp);

        if (ch == 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.5f);
            return Collections.emptySet();
        }

        max = Math.min(Math.min(max, howMuchDoesPlayerHave(player, clickedType)), ch);
        return floodFill(player, clickedBlock.getRelative(blockFace), blockFace.getOppositeFace(), getBuildFaces(blockFace), max);
    }

    private Set<Block> floodFill(Player player, Block origin, BlockFace face, BlockFace[] faces, int max) {
        Block b = origin.getRelative(face);
        LinkedBlockingQueue<Block> queue = new LinkedBlockingQueue<>();
        queue.add(origin);
        Set<Block> result = new HashSet<>();

        while (!queue.isEmpty()) {
            Block b0 = queue.poll();
            Block b1 = b0.getRelative(face);
            if (result.size() >= max) {
                break;
            }
            if ((b0.isEmpty() || b0.isLiquid() || b0.getType() == Material.TALL_GRASS) && b1.getType() == b.getType() && !result.contains(b0) && canReplace(player, b0)) {
                result.add(b0);

                for (BlockFace f : faces) {
                    if (!player.isSneaking() || filterFace(player, face, f)) {
                        queue.add(b0.getRelative(f));
                    }
                }
            }
        }

        return result;
    }

    private boolean filterFace(Player player, BlockFace clickedFace, BlockFace face) {
        switch (clickedFace) {
        case NORTH:
        case SOUTH:
        case EAST:
        case WEST:
            BlockAndPosition pos = BlockUtil.getTargetPoint(player, null, 5);
            double frac = pos.point.getY() % 1;

            if (frac > 0.85 || frac < 0.15) {
                return face.getModY() != 0;
            }
            else {
                return face.getModY() == 0;
            }
        case UP:
        case DOWN:
            BlockFace playerFace = getRotation(player.getLocation());
            switch (playerFace) {
            case EAST:
            case WEST:
                return face.getModZ() == 0;
            case NORTH:
            case SOUTH:
                return face.getModX() == 0;
            default:
                break;
            }
        default:
            break;
        }
        return true;
    }

    private BlockFace getRotation(Location loc) {
        double rot = (loc.getYaw() - 90) % 360;
        if (rot < 0) {
            rot += 360;
        }

        if ((0 <= rot && rot < 45) || (315 <= rot && rot < 360.0)) {
            return BlockFace.NORTH;
        }
        else if (45 <= rot && rot < 135) {
            return BlockFace.EAST;
        }
        else if (135 <= rot && rot < 225) {
            return BlockFace.SOUTH;
        }
        else if (225 <= rot && rot < 315) {
            return BlockFace.WEST;
        }
        else {
            throw new IllegalArgumentException("impossible rotation: " + rot);
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
        default:
            break;
        }
        throw new IllegalArgumentException("invalid face: " + face);
    }

    private enum Mode {
        BUILD,
        EXCHANGE
    }

    private static class BuildFaces {

        private static final BlockFace[] ns = { BlockFace.EAST, BlockFace.DOWN, BlockFace.WEST, BlockFace.UP };
        private static final BlockFace[] ew = { BlockFace.NORTH, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.UP };
        private static final BlockFace[] ud = { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };

    }

    private class QueueSwapper extends BukkitRunnable {

        private final LinkedBlockingQueue<SwapRecord> queue;
        private final ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE); // ensure we can mine anything

        public QueueSwapper(LinkedBlockingQueue<SwapRecord> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            boolean didWork = false;

            while (!didWork) {
                // first, some validation & sanity checking...
                SwapRecord rec = queue.poll();

                if (rec == null) {
                    cancel();
                    return;
                }

                if (!rec.player.isOnline()) {
                    continue;
                }

                Block b = rec.block;

                if (b.getType() == rec.target || rec.builder.getCharge() < rec.chargeNeeded || !canReplace(rec.player, rec.block)) {
                    continue;
                }

                // (hopefully) take materials from the player...
                int slot = rec.slot;
                PlayerInventory inventory = rec.player.getInventory();

                if (slot < 0 || inventory.getItem(slot) == null) {
                    slot = getSlotForItem(rec.player, rec.target);

                    if (slot == -1) {
                        // player is out of materials to swap: scan the queue and remove any other
                        // records for this player & material, to avoid constant inventory rescanning
                        Iterator<SwapRecord> iter = queue.iterator();

                        while (iter.hasNext()) {
                            SwapRecord r = iter.next();

                            if (r.player.equals(rec.player) && r.target.equals(rec.target)) {
                                iter.remove();
                            }
                        }
                        continue;
                    }
                }

                ItemStack item = inventory.getItem(slot);
                item.setAmount(item.getAmount() - 1);
                inventory.setItem(slot, item.getAmount() > 0 ? item : null);

                // take SCU from the multibuilder...
                rec.builder.setCharge(rec.builder.getCharge() - rec.chargeNeeded);
                ItemStack builderItem = rec.builder.toItemStack();
                rec.player.setItemInHand(builderItem);

                // give materials to the player...
                if (builderItem.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 1) {
                    tool.addEnchantment(Enchantment.SILK_TOUCH, 1);
                }
                else {
                    tool.removeEnchantment(Enchantment.SILK_TOUCH);
                }

                for (ItemStack stack : b.getDrops(tool)) {
                    STBUtil.giveItems(rec.player, stack);
                }

                // make the actual in-world swap
                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
                b.setType(rec.target, true);

                // queue up the next set of blocks
                if (rec.layersLeft > 0) {
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                Block b1 = b.getRelative(x, y, z);

                                if ((x != 0 || y != 0 || z != 0) && b1.getType() == rec.source && STBUtil.isExposed(b1)) {
                                    queue.offer(new SwapRecord(rec.player, b1, rec.source, rec.target, rec.layersLeft - 1, rec.builder, slot, rec.chargeNeeded));
                                }
                            }
                        }
                    }
                }

                didWork = true;
            }
        }

        private int getSlotForItem(Player player, Material from) {
            for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);

                if (stack != null && stack.getType() == from && !stack.hasItemMeta()) {
                    return slot;
                }
            }

            return -1;
        }
    }

    private class SwapRecord {

        private final Player player;
        private final Block block;
        private final Material source;
        private final Material target;
        private final int layersLeft;
        private final MultiBuilder builder;
        private final int slot;
        private final double chargeNeeded;

        private SwapRecord(Player player, Block block, Material source, Material target, int layersLeft, MultiBuilder builder, int slot, double chargeNeeded) {
            this.player = player;
            this.block = block;
            this.source = source;
            this.target = target;
            this.layersLeft = layersLeft;
            this.builder = builder;
            this.slot = slot;
            this.chargeNeeded = chargeNeeded;
        }
    }
}
