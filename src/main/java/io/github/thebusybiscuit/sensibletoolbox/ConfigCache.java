package io.github.thebusybiscuit.sensibletoolbox;

import java.util.Locale;

import javax.annotation.Nonnull;

import io.github.thebusybiscuit.sensibletoolbox.api.AccessControl;
import io.github.thebusybiscuit.sensibletoolbox.api.RedstoneBehaviour;
import me.desht.dhutils.text.LogUtils;

/**
 * Cache some frequently-access config values to reduce config lookup overhead
 * 
 * @author desht
 */
public class ConfigCache {

    private final SensibleToolboxPlugin plugin;
    private RedstoneBehaviour defaultRedstone;
    private AccessControl defaultAccess;
    private boolean noisyMachines;
    private int particleLevel;
    private boolean creativeEnderAccess;

    public ConfigCache(@Nonnull SensibleToolboxPlugin plugin) {
        this.plugin = plugin;
    }

    public void processConfig() {
        try {
            defaultRedstone = RedstoneBehaviour.valueOf(plugin.getConfig().getString("default_redstone").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            LogUtils.warning("bad value for default_redstone in config.yml: must be one of ignore,high,low,pulsed (defaulting to ignore)");
            defaultRedstone = RedstoneBehaviour.IGNORE;
        }

        try {
            defaultAccess = AccessControl.valueOf(plugin.getConfig().getString("default_access").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            LogUtils.warning("bad value for default_access in config.yml: must be one of public,private,restricted (defaulting to public)");
            defaultAccess = AccessControl.PUBLIC;
        }

        noisyMachines = plugin.getConfig().getBoolean("noisy_machines");
        particleLevel = plugin.getConfig().getInt("particle_effects");
        creativeEnderAccess = plugin.getConfig().getBoolean("creative_ender_access");
    }

    @Nonnull
    public RedstoneBehaviour getDefaultRedstone() {
        return defaultRedstone;
    }

    void setDefaultRedstone(@Nonnull RedstoneBehaviour defaultRedstone) {
        this.defaultRedstone = defaultRedstone;
    }

    @Nonnull
    public AccessControl getDefaultAccess() {
        return defaultAccess;
    }

    void setDefaultAccess(@Nonnull AccessControl defaultAccess) {
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
