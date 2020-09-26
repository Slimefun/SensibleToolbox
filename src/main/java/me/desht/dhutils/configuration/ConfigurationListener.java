package me.desht.dhutils.configuration;

import org.bukkit.configuration.file.FileConfiguration;

import me.desht.dhutils.DHUtilsException;

/**
 * This interface can listen to changes in a {@link FileConfiguration}.
 * 
 * @author desht
 *
 */
public interface ConfigurationListener {

    /**
     * Validate and possibly modify or veto a potential change to this configuration.
     * <p>
     * Simply return newVal to proceed with the proposed configuration change.
     *
     * @param configurationManager
     *            the ConfigurationManager object
     * @param key
     *            the configuration key
     * @param oldVal
     *            the previous configuration value
     * @param newVal
     *            the proposed new configuration value
     * @return the new configuration value, possibly different from {@code newVal}
     * @throws DHUtilsException
     *             to prevent the change from occurring
     */
    <T> T onConfigurationValidate(ConfigurationManager configurationManager, String key, T oldVal, T newVal);

    /**
     * Called when a change has been made to this configuration.
     *
     * @param configurationManager
     *            the ConfigurationManager object
     * @param key
     *            the configuration key
     * @param oldVal
     *            the previous configuration value
     * @param newVal
     *            the new configuration value
     */
    <T> void onConfigurationChanged(ConfigurationManager configurationManager, String key, T oldVal, T newVal);
}
