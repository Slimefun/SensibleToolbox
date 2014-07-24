package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.SensibleToolbox;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.api.util.BlockProtection;
import me.desht.sensibletoolbox.api.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class AngelicBlock extends BaseSTBBlock {
    private static final MaterialData md = new MaterialData(Material.OBSIDIAN);

    public AngelicBlock() {
    }

    public AngelicBlock(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Angelic Block";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "R-click: " + ChatColor.RESET + " place block in the air",
                "L-click block: " + ChatColor.RESET + " insta-break"
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(this.toItemStack());
        recipe.shape(" G ", "FOF");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('F', Material.FEATHER);
        recipe.setIngredient('O', Material.OBSIDIAN);
        return recipe;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            // place the block in the air 2 blocks in the direction the player is looking at
            Player p = event.getPlayer();
            Vector v = p.getLocation().getDirection().normalize().multiply(2.0);
            Location loc = p.getEyeLocation().add(v);
            Block b = loc.getBlock();
            if (b.isEmpty() && SensibleToolbox.getBlockProtection().playerCanBuild(p, b, BlockProtection.Operation.PLACE)) {
                ItemStack stack = p.getItemInHand();
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                    p.setItemInHand(stack);
                } else {
                    p.setItemInHand(new ItemStack(Material.AIR));
                }
                b.setType(getMaterial());
                placeBlock(b, event.getPlayer(), STBUtil.getFaceFromYaw(p.getLocation().getYaw()).getOppositeFace());
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // no direct placing but we need to ensure the player's inventory looks OK
            hackyDelayedInvUpdate(event.getPlayer());
        }
        event.setCancelled(true);
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        // the angelic block has just been hit by a player - insta-break it
        Player p = event.getPlayer();
        Block b = event.getBlock();
        if (SensibleToolbox.getBlockProtection().playerCanBuild(p, b, BlockProtection.Operation.BREAK)) {
            b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
            breakBlock(false);
            STBUtil.giveItems(p, toItemStack());
        }
        event.setCancelled(true);
    }

    @Override
    public boolean onEntityExplode(EntityExplodeEvent event) {
        return false; // immune to explosions
    }

    @Override
    public int getTickRate() {
        return 40;
    }

    @Override
    public void onServerTick() {
        Location loc = getLocation();
        if (((SensibleToolboxPlugin) getProviderPlugin()).isProtocolLibEnabled()) {
            ParticleEffect.CLOUD.play(loc.add(0.5, 0.5, 0.5), 0f, 0f, 0f, 0.05f, 10);
        } else {
            loc.getWorld().playEffect(loc.add(0.5, 0.5, 0.5), Effect.SMOKE, BlockFace.UP);
        }
        super.onServerTick();
    }
}
