package io.github.thebusybiscuit.sensibletoolbox.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.ParticleEffect;
import me.desht.dhutils.commands.AbstractCommand;

public class ParticleCommand extends AbstractCommand {
	
    public ParticleCommand() {
        super("stb particle", 6, 6);
        setUsage("/<command> particle <type> <x> <y> <z> <speed> <amount>");
    }

	@Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        notFromConsole(sender);
        Player player = (Player) sender;

        String effect = args[0];
        try {
            ParticleEffect p = ParticleEffect.valueOf(effect.toUpperCase());

            List<Block> b = player.getLastTwoTargetBlocks((Set<Material>) null, 50);
            Location loc = b.get(0).getLocation().add(0.5, 0.5, 0.5);
            float sx = Float.parseFloat(args[1]);
            float sy = Float.parseFloat(args[2]);
            float sz = Float.parseFloat(args[3]);
            float speed = Float.parseFloat(args[4]);
            int amount = Integer.parseInt(args[5]);
            p.play(player, loc, sx, sy, sz, speed, amount);
        } catch (IllegalArgumentException e) {
            throw new DHUtilsException(e.getMessage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(Plugin plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return getEnumCompletions(sender, ParticleEffect.class, args[0]);
        } else {
            return noCompletions(sender);
        }
    }
}
