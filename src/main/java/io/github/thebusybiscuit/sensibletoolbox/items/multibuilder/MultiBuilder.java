package io.github.thebusybiscuit.sensibletoolbox.items.multibuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.items.components.IntegratedCircuit;
import io.github.thebusybiscuit.sensibletoolbox.items.energycells.TenKEnergyCell;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.utils.UnicodeSymbol;
import io.github.thebusybiscuit.sensibletoolbox.utils.VanillaInventoryUtils;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.blocks.BlockAndPosition;
import me.desht.dhutils.blocks.BlockUtil;
import me.desht.dhutils.cost.ItemCost;

public class MultiBuilder extends BaseSTBItem implements Chargeable {

    public static final int MAX_BUILD_BLOCKS = 9;
    public static final int DEF_SCU_PER_OPERATION = 40;
    private static final Map<UUID, LinkedBlockingQueue<SwapRecord>> swapQueues = new HashMap<>();
    private BuildingMode mode;
    private double charge;
    private Material material;

    public MultiBuilder() {
        super();
        mode = BuildingMode.BUILD;
        charge = 0;
    }

    public MultiBuilder(ConfigurationSection conf) {
        super(conf);
        mode = BuildingMode.valueOf(conf.getString("mode"));
        charge = conf.getDouble("charge");
        String s = conf.getString("material");
        material = s.isEmpty() ? null : Material.matchMaterial(s);
    }

    public BuildingMode getMode() {
        return mode;
    }

    public void setMode(BuildingMode mode) {
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
        } else if (delta >= 6) {
            delta -= 9;
        } else if (delta <= -6) {
            delta += 9;
        }

        delta = (delta > 0) ? 1 : -1;
        int o = getMode().ordinal() + delta;

        if (o < 0) {
            o = BuildingMode.values().length - 1;
        } else if (o >= BuildingMode.values().length) {
            o = 0;
        }

        setMode(BuildingMode.values()[o]);
        updateHeldItemStack(event.getPlayer(), EquipmentSlot.HAND);
    }

    private void handleExchangeMode(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // set the target material
                material = clicked.getType();
                updateHeldItemStack(player, event.getHand());
            } else if (material != null) {
                // replace multiple blocks
                int sharpness = event.getItem().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                int layers = 3 + sharpness;
                startSwap(event.getPlayer(), event.getItem(), this, clicked, material, layers);
                Debugger.getInstance().debug(this + ": replacing " + layers + " layers of blocks");
            }

            event.setCancelled(true);
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && material != null) {
            // replace a single block
            startSwap(event.getPlayer(), event.getItem(), this, clicked, material, 0);
            event.setCancelled(true);
        }
    }

    @ParametersAreNonnullByDefault
    private void startSwap(Player player, ItemStack item, MultiBuilder builder, Block origin, Material target, int maxBlocks) {
        LinkedBlockingQueue<SwapRecord> queue = swapQueues.get(player.getWorld().getUID());

        if (queue == null) {
            queue = new LinkedBlockingQueue<>();
            swapQueues.put(player.getWorld().getUID(), queue);
        }

        if (queue.isEmpty()) {
            new QueueSwapper(queue).runTaskTimer(SensibleToolbox.getPluginInstance(), 1L, 1L);
        }

        int chargePerOp = getItemConfig().getInt("scu_per_op", DEF_SCU_PER_OPERATION);
        double chargeNeeded = chargePerOp * Math.pow(0.8, item.getEnchantmentLevel(Enchantment.DIG_SPEED));
        queue.add(new SwapRecord(player, origin, origin.getType(), target, maxBlocks, builder, -1, chargeNeeded));
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

    protected boolean canReplace(Player player, Block b) {
        // we won't replace any block which can hold items, or any STB block, or any unbreakable block
        if (SensibleToolbox.getBlockAt(b.getLocation(), true) != null) {
            return false;
        } else if (VanillaInventoryUtils.isVanillaInventory(b)) {
            return false;
        } else if (b.getType().getHardness() >= 3600000) {
            return false;
        } else {
            return SensibleToolbox.getProtectionManager().hasPermission(player, b, ProtectableAction.BREAK_BLOCK);
        }
    }

    private void handleBuildMode(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Set<Block> blocks = getBuildCandidates(player, event.getItem(), event.getClickedBlock(), event.getBlockFace());

            if (!blocks.isEmpty()) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    doBuild(player, event.getHand(), event.getItem(), event.getClickedBlock(), blocks);
                } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
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

    private void doBuild(Player player, EquipmentSlot hand, ItemStack item, Block source, Set<Block> actualBlocks) {
        int chargePerOp = getItemConfig().getInt("scu_per_op", DEF_SCU_PER_OPERATION);
        double chargeNeeded = chargePerOp * actualBlocks.size() * Math.pow(0.8, item.getEnchantmentLevel(Enchantment.DIG_SPEED));
        // we know at this point that the tool has sufficient charge and that the player has sufficient material
        setCharge(getCharge() - chargeNeeded);
        ItemCost cost = new ItemCost(source.getType(), actualBlocks.size());
        cost.apply(player);

        for (Block b : actualBlocks) {
            b.setType(source.getType(), true);
        }

        updateHeldItemStack(player, hand);
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F);
    }

    @Nonnull
    private Set<Block> getBuildCandidates(Player player, ItemStack item, Block clickedBlock, BlockFace blockFace) {
        int sharpness = item.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
        double chargePerOp = getItemConfig().getInt("scu_per_op", DEF_SCU_PER_OPERATION) * Math.pow(0.8, item.getEnchantmentLevel(Enchantment.DIG_SPEED));
        int ch = (int) (getCharge() / chargePerOp);

        if (ch == 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 0.5F);
            return Collections.emptySet();
        }

        int max = MAX_BUILD_BLOCKS + sharpness * 3;
        Material clickedType = clickedBlock.getType();
        max = Math.min(Math.min(max, howMuchDoesPlayerHave(player, clickedType)), ch);
        return floodFill(player, clickedBlock.getRelative(blockFace), blockFace.getOppositeFace(), getBuildFaces(blockFace), max);
    }

    @Nonnull
    private Set<Block> floodFill(Player player, Block origin, BlockFace face, BuildFace buildFace, int max) {
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

                for (BlockFace f : buildFace.getFaces()) {
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
                } else {
                    return face.getModY() == 0;
                }
            case UP:
            case DOWN:
                BlockFace playerFace = getRotation(player.getLocation());

                if (playerFace == BlockFace.EAST || playerFace == BlockFace.WEST) {
                    return face.getModZ() == 0;
                } else if (playerFace == BlockFace.NORTH || playerFace == BlockFace.SOUTH) {
                    return face.getModX() == 0;
                }

                break;
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
        } else if (45 <= rot && rot < 135) {
            return BlockFace.EAST;
        } else if (135 <= rot && rot < 225) {
            return BlockFace.SOUTH;
        } else if (225 <= rot && rot < 315) {
            return BlockFace.WEST;
        } else {
            throw new IllegalArgumentException("impossible rotation: " + rot);
        }
    }

    @Nonnull
    private BuildFace getBuildFaces(@Nonnull BlockFace face) {
        switch (face) {
            case NORTH:
            case SOUTH:
                return BuildFace.NORTH_SOUTH;
            case EAST:
            case WEST:
                return BuildFace.EAST_WEST;
            case UP:
            case DOWN:
                return BuildFace.UP_DOWN;
            default:
                throw new IllegalArgumentException("invalid face: " + face);
        }
    }

}
