package io.github.thebusybiscuit.sensibletoolbox.utils;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.bukkit.Material;

/**
 * A collection of miscellaneous material-related utility methods.
 */
public final class MaterialConverter {
    /**
     * Turn log into sapling preserving tree type
     *
     * @param log log you want to turn into sapling
     * @return sapling
     */
    public static Optional<Material> getSaplingFromLog(@Nonnull Material log) {
        if (!isLog(log))
            return Optional.empty();

        String type = log.name().substring(0, log.name().lastIndexOf('_'));
        type = type.replace("STRIPPED_", "");
        try {
            return Optional.ofNullable(Material.valueOf(type + "_SAPLING"));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Turn log into planks preserving tree type
     *
     * @param log log you want to turn into planks
     * @return planks
     */
    public static Optional<Material> getPlanksFromLog(@Nonnull Material log) {
        if (!isLog(log))
            return Optional.empty();

        String type = log.name().substring(0, log.name().lastIndexOf('_'));
        type = type.replace("STRIPPED_", "");
        try {
            return Optional.ofNullable(Material.valueOf(type + "_PLANKS"));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Check if material is log (any type)
     *
     * @param log the material to check
     * @return true if the stack is log; false otherwise
     */
    public static boolean isLog(@Nonnull Material log) {
        return log.name().endsWith("_LOG") || log.name().endsWith("_WOOD");
    }
}
