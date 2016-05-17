package me.mrCookieSlime.sensibletoolbox;

import me.desht.dhutils.LogUtils;
import me.mrCookieSlime.sensibletoolbox.api.AccessControl;
import me.mrCookieSlime.sensibletoolbox.api.RedstoneBehaviour;

/**
 * Cache some frequently-access config values to reduce config lookup overhead
 */
public class ConfigCache {
	
    private final SensibleToolboxPlugin plugin;
    private RedstoneBehaviour defaultRedstone;
    private AccessControl defaultAccess;
    private boolean noisyMachines;
    private int particleLevel;
    private boolean creativeEnderAccess;

    public ConfigCache(SensibleToolboxPlugin plugin) {
        this.plugin = plugin;
    }

    public void processConfig() {
        try {
            defaultRedstone = RedstoneBehaviour.valueOf(plugin.getConfig().getString("default_redstone").toUpperCase());
        } catch (IllegalArgumentException e) {
            LogUtils.warning("bad value for default_redstone in config.yml: must be one of ignore,high,low,pulsed (defaulting to ignore)");
            defaultRedstone = RedstoneBehaviour.IGNORE;
        }
        try {
            defaultAccess = AccessControl.valueOf(plugin.getConfig().getString("default_access").toUpperCase());
        } catch (IllegalArgumentException e) {
            LogUtils.warning("bad value for default_access in config.yml: must be one of public,private,restricted (defaulting to public)");
            defaultAccess = AccessControl.PUBLIC;
        }
        noisyMachines = plugin.getConfig().getBoolean("noisy_machines");
        particleLevel = plugin.getConfig().getInt("particle_effects");
        creativeEnderAccess = plugin.getConfig().getBoolean("creative_ender_access");
    }

    public RedstoneBehaviour getDefaultRedstone() {
        return defaultRedstone;
    }

    void setDefaultRedstone(RedstoneBehaviour defaultRedstone) {
        this.defaultRedstone = defaultRedstone;
    }

    public AccessControl getDefaultAccess() {
        return defaultAccess;
    }

    void setDefaultAccess(AccessControl defaultAccess) {
        this.defaultAccess = defaultAccess;
    }

    public boolean isNoisyMachines() {
        return noisyMachines;
    }

    void setNoisyMachines(boolean noisyMachines) {
        this.noisyMachines = noisyMachines;
    }

    public int getParticleLevel() {
        return particleLevel;
    }

    void setParticleLevel(int particleLevel) {
        this.particleLevel = particleLevel;
    }

    public boolean isCreativeEnderAccess() {
        return creativeEnderAccess;
    }

    public void setCreativeEnderAccess(boolean creativeEnderAccess) {
        this.creativeEnderAccess = creativeEnderAccess;
    }
}
