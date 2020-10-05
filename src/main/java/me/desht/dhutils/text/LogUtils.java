package me.desht.dhutils.text;

import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

/**
 * A utility class for logging.
 * 
 * @author desht
 * @author TheBusyBiscuit
 *
 */
public final class LogUtils {

    private LogUtils() {}

    private static Logger logger;

    public static void init(@Nonnull String name) {
        logger = Logger.getLogger(name);
    }

    public static void init(@Nonnull Plugin plugin) {
        logger = plugin.getLogger();
    }

    @Nonnull
    public static Level getLogLevel() {
        return logger.getLevel();
    }

    public static void setLogLevel(@Nonnull Level level) {
        logger.setLevel(level);

        for (Handler h : logger.getHandlers()) {
            h.setLevel(level);
        }
    }

    /**
     * Set the new log level
     *
     * @param val
     * @throws IllegalArgumentException
     *             if the value does not represent a valid log level
     */
    public static void setLogLevel(@Nonnull String val) {
        setLogLevel(Level.parse(val.toUpperCase(Locale.ROOT)));
    }

    public static void log(@Nonnull Level level, @Nonnull String message) {
        logger.log(level, message);
    }

    public static void fine(@Nonnull String message) {
        logger.fine(message);
    }

    public static void finer(@Nonnull String message) {
        logger.finer(message);
    }

    public static void finest(@Nonnull String message) {
        logger.finest(message);
    }

    public static void info(@Nonnull String message) {
        logger.info(message);
    }

    public static void warning(@Nonnull String message) {
        logger.warning(message);
    }

    public static void severe(@Nonnull String message) {
        logger.severe(message);
    }

    public static void warning(@Nonnull String message, @Nullable Exception err) {
        if (err == null) {
            warning(message);
        }
        else {
            logger.log(Level.WARNING, getMsg(message, err));
        }
    }

    public static void severe(@Nonnull String message, @Nullable Exception err) {
        if (err == null) {
            severe(message);
        }
        else {
            logger.log(Level.SEVERE, getMsg(message, err));
        }
    }

    @Nonnull
    private static String getMsg(@Nullable String message, @Nonnull Exception e) {
        return message == null ? e.getMessage() : ChatColor.stripColor(message);
    }

}
