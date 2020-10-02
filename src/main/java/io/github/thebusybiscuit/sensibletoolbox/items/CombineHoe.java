package io.github.thebusybiscuit.sensibletoolbox.items;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.cscorelib2.protection.ProtectableAction;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.GUIUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.SlotType;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
import me.desht.dhutils.cuboid.Cuboid;

public abstract class CombineHoe extends BaseSTBItem {

    private Material seedType;
    private int seedAmount;
    private InventoryGUI gui;
    private int durability;

    @Nonnull
    public static String getInventoryTitle() {
        return ChatColor.DARK_GREEN + "Seed Bag";
    }

    public CombineHoe() {
        super();
        seedType = null;
        seedAmount = 0;
    }

    public CombineHoe(ConfigurationSection conf) {
        super(conf);
        setSeedAmount(conf.getInt("amount"));
        setSeedType(Material.getMaterial(conf.getString("seeds")));
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("amount", getSeedAmount());
        conf.set("seeds", getSeedType() == null ? "" : getSeedType().toString());
        return conf;
    }

    public Material getSeedType() {
        return seedType;
    }

    public void setSeedType(Material seedType) {
        this.seedType = seedType;
    }

    public int getSeedAmount() {
        return seedAmount;
    }

    public void setSeedAmount(int seedAmount) {
        this.seedAmount = seedAmount;
    }

    @Override
    public boolean isEnchantable() {
        return false;
    }

    @Override
    public String[] getLore() {
        int n = getWorkRadius() * 2 + 1;
        String s = n + "x" + n;
        return new String[] { "Right-click dirt/grass:" + ChatColor.WHITE + " till 3x3 area", "Right-click soil:" + ChatColor.WHITE + " sow 3x3 area", "Right-click other:" + ChatColor.WHITE + " open seed bag", "Left-click plants:" + ChatColor.WHITE + " harvest " + s + " area", "Left-click leaves:" + ChatColor.WHITE + " break 3x3x3 area", };
    }

    @Override
    public String[] getExtraLore() {
        if (getSeedType() != null && getSeedAmount() > 0) {
            String s = ItemUtils.getItemName(new ItemStack(getSeedType()));
            return new String[] { ChatColor.WHITE + "Seed bag: " + ChatColor.GOLD + getSeedAmount() + " x " + s };
        }
        else {
            return new String[0];
        }
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        durability = ((Damageable) event.getItem().getItemMeta()).getDamage();
        Block b = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (b.getType() == Material.FARMLAND) {
                plantSeeds(event.getPlayer(), b);
                event.setCancelled(true);
                return;
            }
            else if (b.getType() == Material.DIRT || b.getType() == Material.GRASS) {
                tillSoil(event.getPlayer(), b);
                event.setCancelled(true);
                return;
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() == null || !event.getClickedBlock().getType().isInteractable()) {
                gui = GUIUtil.createGUI(event.getPlayer(), this, 9, getInventoryTitle());

                for (int i = 0; i < gui.getInventory().getSize(); i++) {
                    gui.setSlotType(i, SlotType.ITEM);
                }

                populateSeedBag(gui);
                gui.show(event.getPlayer());
            }
        }
    }

    @Override
    public void onBreakBlockWithItem(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking()) {
            return;
        }

        Block b = event.getBlock();

        if (Tag.LEAVES.isTagged(b.getType())) {
            harvestLayer(player, b);

            if (!player.isSneaking()) {
                harvestLayer(player, b.getRelative(BlockFace.UP));
                harvestLayer(player, b.getRelative(BlockFace.DOWN));
            }

            ItemUtils.damageItem(player.getInventory().getItemInMainHand(), false);
        }
        else if (STBUtil.isPlant(b.getType())) {
            harvestLayer(player, b);
            ItemUtils.damageItem(player.getInventory().getItemInMainHand(), false);
        }
    }

    private boolean verifyUnique(Inventory inv, ItemStack stack, int exclude) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (i != exclude && inv.getItem(i) != null && inv.getItem(i).getType() != stack.getType()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return onCursor.getType() == Material.AIR || STBUtil.getCropType(onCursor.getType()) != null && verifyUnique(gui.getInventory(), onCursor, slot);
    }

    @Override
    public boolean onPlayerInventoryClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return true;
    }

    @Override
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        if (STBUtil.getCropType(toInsert.getType()) == null) {
            return 0;
        }
        else if (!verifyUnique(gui.getInventory(), toInsert, slot)) {
            return 0;
        }
        else {
            Map<Integer, ItemStack> excess = gui.getInventory().addItem(toInsert);
            int inserted = toInsert.getAmount();

            for (ItemStack stack : excess.values()) {
                inserted -= stack.getAmount();
            }

            return inserted;
        }
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        return true;
    }

    @Override
    public boolean onClickOutside(HumanEntity player) {
        return false;
    }

    @Override
    public void onGUIClosed(HumanEntity player) {
        Material seedType = null;
        int count = 0;
        String err = null;

        for (int i = 0; i < gui.getInventory().getSize(); i++) {
            ItemStack stack = gui.getInventory().getItem(i);
            if (stack != null) {
                if (seedType != null && seedType != stack.getType()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), stack);
                    err = "Mixed items in the seed bag??";
                }
                else if (STBUtil.getCropType(stack.getType()) == null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), stack);
                    err = "Non-seed items in the seed bag??";
                }
                else {
                    seedType = stack.getType();
                    count += stack.getAmount();
                }
            }
        }

        if (err != null) {
            STBUtil.complain((Player) player, err);
        }

        setSeedAmount(count);
        setSeedType(seedType);
    }

    private void populateSeedBag(InventoryGUI gui) {
        Inventory inv = gui.getInventory();

        if (getSeedType() != null && getSeedAmount() > 0) {
            int nFullStacks = getSeedAmount() / getSeedType().getMaxStackSize();
            int remainder = getSeedAmount() % getSeedType().getMaxStackSize();

            for (int i = 0; i < nFullStacks && i < inv.getSize(); i++) {
                inv.setItem(i, new ItemStack(getSeedType(), getSeedType().getMaxStackSize()));
            }

            if (remainder > 0 && nFullStacks < inv.getSize()) {
                inv.setItem(nFullStacks, new ItemStack(getSeedType(), remainder));
            }
        }
    }

    private void plantSeeds(Player player, Block b) {
        if (getSeedType() == null || getSeedAmount() == 0) {
            return;
        }

        int amountLeft = getSeedAmount();
        for (Block neighbour : STBUtil.getSurroundingBlocks(b)) {
            Block above = neighbour.getRelative(BlockFace.UP);

            if (!SensibleToolbox.getProtectionManager().hasPermission(player, above, ProtectableAction.PLACE_BLOCK)) {
                continue;
            }

            if (neighbour.getType() == Material.FARMLAND && above.isEmpty()) {
                // candidate for sowing
                above.setType(STBUtil.getCropType(getSeedType()));
                amountLeft--;

                if (amountLeft == 0) {
                    break;
                }
            }
        }

        if (amountLeft < getSeedAmount()) {
            setSeedAmount(amountLeft);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
        }
    }

    @ParametersAreNonnullByDefault
    private void harvestLayer(Player player, Block b) {
        Cuboid cuboid = new Cuboid(b.getLocation());
        cuboid = cuboid.outset(Cuboid.CuboidDirection.HORIZONTAL, Tag.LEAVES.isTagged(b.getType()) ? 1 : getWorkRadius());

        for (Block block : cuboid) {
            if (!block.equals(b) && (STBUtil.isPlant(block.getType()) || Tag.LEAVES.isTagged(block.getType()))) {
                if (!SensibleToolbox.getProtectionManager().hasPermission(player, b, ProtectableAction.BREAK_BLOCK)) {
                    block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
                    block.breakNaturally();
                }
            }
        }
    }

    @ParametersAreNonnullByDefault
    private void tillSoil(Player player, Block b) {
        ItemStack stack = player.getItemInHand();
        short count = 0;

        for (Block b1 : STBUtil.getSurroundingBlocks(b)) {
            if (!SensibleToolbox.getProtectionManager().hasPermission(player, b1, ProtectableAction.BREAK_BLOCK)) {
                continue;
            }

            Block above = b1.getRelative(BlockFace.UP);

            if ((b1.getType() == Material.DIRT || b1.getType() == Material.GRASS) && !above.getType().isSolid() && !above.isLiquid()) {
                b1.setType(Material.FARMLAND);
                count++;

                if (!above.isEmpty()) {
                    above.breakNaturally();
                }

                if (stack.getDurability() + count >= stack.getType().getMaxDurability()) {
                    break;
                }
            }

            if (player.isSneaking()) {
                break;
            }
        }
        if (count > 0) {
            player.playSound(b.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f);
        }

        ItemUtils.damageItem(stack, false);
    }

    public int getWorkRadius() {
        return 1;
    }
}
