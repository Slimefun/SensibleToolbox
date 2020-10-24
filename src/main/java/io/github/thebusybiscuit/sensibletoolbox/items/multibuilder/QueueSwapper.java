package io.github.thebusybiscuit.sensibletoolbox.items.multibuilder;

import java.util.Iterator;
import java.util.Queue;

import javax.annotation.Nonnull;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;

class QueueSwapper extends BukkitRunnable {

    // Ensure we can mine anything
    private final ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
    private final Queue<SwapRecord> queue;

    public QueueSwapper(@Nonnull Queue<SwapRecord> queue) {
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

            if (!rec.getPlayer().isOnline()) {
                continue;
            }

            Block b = rec.getBlock();

            if (b.getType() == rec.getTarget() || rec.getMultiBuilder().getCharge() < rec.getRequiredCharge() || !rec.getMultiBuilder().canReplace(rec.getPlayer(), rec.getBlock())) {
                continue;
            }

            // (hopefully) take materials from the player...
            int slot = rec.getSlot();
            PlayerInventory inventory = rec.getPlayer().getInventory();

            if (slot < 0 || inventory.getItem(slot) == null) {
                slot = getSlotForItem(rec.getPlayer(), rec.getTarget());

                if (slot == -1) {
                    // player is out of materials to swap: scan the queue and remove any other
                    // records for this player & material, to avoid constant inventory rescanning
                    Iterator<SwapRecord> iter = queue.iterator();

                    while (iter.hasNext()) {
                        SwapRecord r = iter.next();

                        if (r.getPlayer().equals(rec.getPlayer()) && r.getTarget() == rec.getTarget()) {
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
            rec.getMultiBuilder().setCharge(rec.getMultiBuilder().getCharge() - rec.getRequiredCharge());
            ItemStack builderItem = rec.getMultiBuilder().toItemStack();
            rec.getPlayer().setItemInHand(builderItem);

            // give materials to the player...
            if (builderItem.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 1) {
                tool.addEnchantment(Enchantment.SILK_TOUCH, 1);
            } else {
                tool.removeEnchantment(Enchantment.SILK_TOUCH);
            }

            for (ItemStack stack : b.getDrops(tool)) {
                STBUtil.giveItems(rec.getPlayer(), stack);
            }

            // make the actual in-world swap
            b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
            b.setType(rec.getTarget(), true);

            // queue up the next set of blocks
            queueNextSet(rec, b, slot);

            didWork = true;
        }
    }

    private void queueNextSet(@Nonnull SwapRecord rec, @Nonnull Block b, int slot) {
        if (rec.getRemainingLayers() > 0) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block block = b.getRelative(x, y, z);

                        if ((x != 0 || y != 0 || z != 0) && block.getType() == rec.getSource() && isExposed(block)) {
                            SwapRecord record = new SwapRecord(rec.getPlayer(), block, rec.getSource(), rec.getTarget(), rec.getRemainingLayers() - 1, rec.getMultiBuilder(), slot, rec.getRequiredCharge());

                            if (queue.offer(record)) {
                                return;
                            }
                        }
                    }
                }
            }
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

    /**
     * Check if the given block is exposed to the world on the given face.
     * {@link org.bukkit.Material#isOccluding()} is used to check if a face is exposed.
     *
     * @param block
     *            the block to check
     * @param face
     *            the face to check
     * @return true if the given block face is exposed
     */
    private boolean isExposed(@Nonnull Block block, @Nonnull BlockFace face) {
        return !block.getRelative(face).getType().isOccluding();
    }

    /**
     * Check if the given block is exposed on <i>any</i> face.
     * {@link org.bukkit.Material#isOccluding()} is used to check if a face is exposed.
     *
     * @param block
     *            the block to check
     * @return true if any face of the block is exposed
     */
    private boolean isExposed(@Nonnull Block block) {
        for (BlockFace face : STBUtil.DIRECT_BLOCK_FACES) {
            if (isExposed(block, face)) {
                return true;
            }
        }

        return false;
    }
}