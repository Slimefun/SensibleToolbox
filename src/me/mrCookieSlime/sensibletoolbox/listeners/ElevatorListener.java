package me.mrCookieSlime.sensibletoolbox.listeners;

import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.blocks.Elevator;
import me.mrCookieSlime.sensibletoolbox.core.storage.LocationManager;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class ElevatorListener extends STBBaseListener {
    public ElevatorListener(SensibleToolboxPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        Location from = event.getFrom();
        if (to.getY() != from.getY() && ((Entity) event.getPlayer()).isOnGround()) {
            // player appears to be jumping from the ground...
            Block b = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
            Elevator e1 = LocationManager.getManager().get(b.getLocation(), Elevator.class);
            if (e1 != null) {
                Elevator e2 = e1.findOtherElevator(BlockFace.UP);
                if (e2 != null) {
                    Block b1 = e2.getLocation().getBlock().getRelative(BlockFace.UP);
                    if (!b1.getType().isSolid() && !b1.getRelative(BlockFace.UP).getType().isSolid()) {
                        Location dest = b1.getLocation().add(0.5, 0, 0.5);
                        dest.setPitch(event.getPlayer().getLocation().getPitch());
                        dest.setYaw(event.getPlayer().getLocation().getYaw());
                        event.setTo(dest);
                        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.8f);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            Block b = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
            Elevator e1 = LocationManager.getManager().get(b.getLocation(), Elevator.class);
            if (e1 != null) {
                Elevator e2 = e1.findOtherElevator(BlockFace.DOWN);
                if (e2 != null) {
                    Block b1 = e2.getLocation().getBlock().getRelative(BlockFace.UP);
                    if (!b1.getType().isSolid() && !b1.getRelative(BlockFace.UP).getType().isSolid()) {
                        Location dest = b1.getLocation().add(0.5, 0, 0.5);
                        dest.setPitch(event.getPlayer().getLocation().getPitch());
                        dest.setYaw(event.getPlayer().getLocation().getYaw());
                        event.getPlayer().teleport(dest);
                        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_PISTON_CONTRACT, 1.0f, 1.8f);
                    }
                }
            }

        }
    }

}
