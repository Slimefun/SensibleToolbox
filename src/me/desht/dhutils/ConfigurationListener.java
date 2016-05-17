package me.desht.dhutils;

public interface ConfigurationListener {
    /**
     * Validate and possibly modify or veto a potential change to this configuration.
     * <p>
     * Simply return newVal to proceed with the proposed configuration change.
     *
     * @param configurationManager the ConfigurationManager object
     * @param key the configuration key
     * @param oldVal the previous configuration value
     * @param newVal the proposed new configuration value
     * @return the new configuration value, possibly different from {@code newVal}
     * @throws DHUtilsException to prevent the change from occurring
     */
	public Object onConfigurationValidate(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal);

    /**
     * Called when a change has been made to this configuration.
     *
     * @param configurationManager the ConfigurationManager object
     * @param key the configuration key
     * @param oldVal the previous configuration value
     * @param newVal the new configuration value
     */
	public void onConfigurationChanged(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal);
}
