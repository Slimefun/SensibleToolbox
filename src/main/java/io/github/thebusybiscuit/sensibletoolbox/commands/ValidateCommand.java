package io.github.thebusybiscuit.sensibletoolbox.commands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.block.RelativePosition;
import me.desht.dhutils.text.LogUtils;

public class ValidateCommand extends STBAbstractCommand {

    public ValidateCommand() {
        super("stb validate");
        setPermissionNode("stb.commands.validate");
        setUsage("/<command> validate");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        for (World world : Bukkit.getWorlds()) {
            int fixed = validate(plugin, world);
            String s = fixed == 1 ? "" : "s";
            MiscUtil.statusMessage(sender, "Fixed up &e" + fixed + "&- block" + s + " in world &6" + world.getName());
        }
        return true;
    }

    private int validate(Plugin plugin, World world) {
        Set<Block> fixed = new HashSet<>();

        for (BaseSTBBlock stb : LocationManager.getManager().listBlocks(world, false)) {
            Location loc = stb.getLocation();
            Block b = loc.getBlock();
            Material material = stb.getMaterial();

            Debugger.getInstance().debug("compare: block " + b + " vs. STB: " + stb + " - " + material);
            if (b.getType() != material) {
                // block's material doesn't match
                b.setType(material);
                LogUtils.info("restored type and data for STB block [" + stb + "], world block: " + b);
                fixed.add(b);
            }

            if (repairMeta(plugin, b, stb, BaseSTBBlock.STB_BLOCK)) {
                fixed.add(b);
            }

            for (RelativePosition rp : stb.getBlockStructure()) {
                Block b2 = stb.getAuxiliaryBlock(loc, rp);

                if (repairMeta(plugin, b2, stb, BaseSTBBlock.STB_MULTI_BLOCK)) {
                    fixed.add(b2);
                }
            }
        }
        return fixed.size();
    }

    private boolean repairMeta(Plugin plugin, Block b, BaseSTBBlock stb, String key) {
        BaseSTBBlock stb2 = (BaseSTBBlock) STBUtil.getMetadataValue(b, key);

        if (stb != stb2) {
            // block's bukkit metadata is wrong or missing
            b.removeMetadata(key, plugin);
            b.setMetadata(key, new FixedMetadataValue(plugin, stb));
            LogUtils.info("restored bukkit metadata for STB block " + stb + ", world block: " + b);
            return true;
        }

        return false;
    }
}
