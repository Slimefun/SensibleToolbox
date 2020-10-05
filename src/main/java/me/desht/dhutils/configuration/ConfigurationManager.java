package me.desht.dhutils.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.Debugger;

public class ConfigurationManager {

    private final Plugin plugin;
    private final Configuration config;

    private ConfigurationListener listener;
    private boolean validate = true;

    public ConfigurationManager(@Nonnull Plugin plugin, @Nullable ConfigurationListener listener) {
        this.plugin = plugin;
        this.listener = listener;

        this.config = plugin.getConfig();
        config.options().copyDefaults(true);

        plugin.saveConfig();
    }

    public ConfigurationManager(@Nonnull Plugin plugin) {
        this(plugin, null);
    }

    public ConfigurationManager(@Nonnull Configuration config) {
        this.plugin = null;
        this.listener = null;
        this.config = config;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    @Nonnull
    public Plugin getPlugin() {
        return plugin;
    }

    @Nonnull
    public Configuration getConfig() {
        return config;
    }

    public void setConfigurationListener(@Nullable ConfigurationListener listener) {
        this.listener = listener;
    }

    @Nonnull
    public Class<?> getType(@Nonnull String key) {
        if (config.getDefaults().contains(key)) {
            return config.getDefaults().get(key).getClass();
        }
        else if (config.contains(key)) {
            return config.get(key).getClass();
        }
        else {
            throw new IllegalArgumentException("can't determine type for unknown key '" + key + "'");
        }
    }

    @Nullable
    public Object get(@Nonnull String key) {
        if (!config.contains(key)) {
            throw new DHUtilsException("No such config item: " + key);
        }

        return config.get(key);
    }

    public void set(@Nonnull String key, @Nullable String val) {
        Object current = get(key);

        setItem(key, val);

        if (listener != null) {
            listener.onConfigurationChanged(this, key, current, get(key));
        }

        if (plugin != null) {
            plugin.saveConfig();
        }
    }

    public <T> void set(@Nonnull String key, @Nonnull List<T> val) {
        Object current = get(key);
        setItem(key, val);

        if (listener != null) {
            listener.onConfigurationChanged(this, key, current, get(key));
        }

        if (plugin != null) {
            plugin.saveConfig();
        }
    }

    @SuppressWarnings("unchecked")
    private void setItem(@Nonnull String key, @Nullable String val) {
        Class<?> c = getType(key);
        Debugger.getInstance().debug(2, "setItem: key = " + key + ", val = " + val + ", class = " + c.getName());

        Object processedVal = null;

        if (val != null) {
            if (List.class.isAssignableFrom(c)) {
                List<String> list = new ArrayList<>(1);
                list.add(val);
                processedVal = handleListValue(key, list);
            }
            else if (String.class.isAssignableFrom(c)) {
                // String config values are common, so this should be a little quicker than going
                // through the default case below (using reflection)
                processedVal = val;
            }
            else if (Enum.class.isAssignableFrom(c)) {
                // this really isn't very pretty, but as far as I can tell there's no way to
                // do this with a parameterised Enum type
                @SuppressWarnings("rawtypes")
                Class<? extends Enum> cSub = c.asSubclass(Enum.class);
                try {
                    processedVal = Enum.valueOf(cSub, val.toUpperCase());
                }
                catch (IllegalArgumentException e) {
                    throw new DHUtilsException("'" + val + "' is not a valid value for '" + key + "'");
                }
            }
            else {
                // the class we're converting to must have a constructor taking a single String argument
                try {
                    Constructor<?> ctor = c.getDeclaredConstructor(String.class);
                    processedVal = ctor.newInstance(val);
                }
                catch (NoSuchMethodException e) {
                    throw new DHUtilsException("Cannot convert '" + val + "' into a " + c.getName());
                }
                catch (IllegalArgumentException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                catch (InvocationTargetException e) {
                    if (e.getCause() instanceof NumberFormatException) {
                        throw new DHUtilsException("Invalid numeric value: " + val);
                    }
                    else if (e.getCause() instanceof IllegalArgumentException) {
                        throw new DHUtilsException("Invalid argument: " + val);
                    }
                    else {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (processedVal != null || val == null) {
            if (listener != null && validate) {
                processedVal = listener.onConfigurationValidate(this, key, get(key), processedVal);
            }

            config.set(key, processedVal);
        }
        else {
            throw new DHUtilsException("Don't know what to do with " + key + " = " + val);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void setItem(@Nonnull String key, @Nonnull List<T> list) {
        if (config.getDefaults().get(key) == null) {
            throw new DHUtilsException("No such key '" + key + "'");
        }

        if (!(config.getDefaults().get(key) instanceof List<?>)) {
            throw new DHUtilsException("Key '" + key + "' does not accept a list of values");
        }

        if (listener != null && validate) {
            // noinspection unchecked
            list = (List<T>) listener.onConfigurationValidate(this, key, get(key), list);
        }

        config.set(key, handleListValue(key, list));
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> handleListValue(@Nonnull String key, @Nonnull List<T> list) {
        HashSet<T> current = new HashSet<>((List<T>) config.getList(key));

        if (list.get(0).equals("-")) {
            // remove specified item from list
            list.remove(0);
            current.removeAll(list);
        }
        else if (list.get(0).equals("=")) {
            // replace list
            list.remove(0);
            current = new HashSet<>(list);
        }
        else if (list.get(0).equals("+")) {
            // append to list
            list.remove(0);
            current.addAll(list);
        }
        else {
            // append to list
            current.addAll(list);
        }

        return new ArrayList<>(current);
    }
}
