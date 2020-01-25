package me.desht.dhutils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigurationManager {

	private final Plugin plugin;
	private final Configuration config;
	private final Configuration descConfig;
	private final Map<String,Class<?>> forceTypes = new HashMap<>();

	private ConfigurationListener listener;
	private String prefix;
	private boolean validate = true;

	public ConfigurationManager(Plugin plugin, ConfigurationListener listener) {
		this.plugin = plugin;
		this.prefix = null;
		this.listener = listener;

		this.config = plugin.getConfig();
		config.options().copyDefaults(true);

		this.descConfig = new MemoryConfiguration();

		plugin.saveConfig();
	}

	public ConfigurationManager(Plugin plugin) {
		this(plugin, null);
	}

	public ConfigurationManager(Configuration config) {
		this.plugin = null;
		this.prefix = null;
		this.listener = null;
		this.config = config;
		this.descConfig = new MemoryConfiguration();
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfigurationListener(ConfigurationListener listener) {
		this.listener = listener;
	}

	public void forceType(String key, Class<?> c) {
		forceTypes.put(key, c);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void forceType(String key, String className) {
		try {
			Class<?> c = Class.forName(className);
			forceTypes.put(key, c);
		} catch (ClassNotFoundException e) {
			throw new DHUtilsException("Unknown type " + className);
		}
	}

	public Class<?> getType(String key) {
		if (forceTypes.containsKey(key)) {
			return forceTypes.get(key);
		} 
		else {
			key = addPrefix(key);
			
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
	}

	public void insert(String key, Object def) {
		String keyPrefixed = addPrefix(key);
		
		if (config.contains(keyPrefixed)) {
			throw new DHUtilsException("Config item already exists: " + keyPrefixed);
		}
		
		config.addDefault(keyPrefixed, def);
		config.getDefaults().set(key, def);
	}

	public Object get(String key) {
		String keyPrefixed = addPrefix(key);

		if (!config.contains(keyPrefixed)) {
			throw new DHUtilsException("No such config item: " + keyPrefixed);
		}

		return config.get(keyPrefixed);
	}

	public Object check(String key) {
		String keyPrefixed = addPrefix(key);

		return config.get(keyPrefixed);
	}

	public void set(String key, String val) {
		Object current = get(key);

		setItem(key, val);

		if (listener != null) {
			listener.onConfigurationChanged(this, key, current, get(key));
		}

		if (plugin != null) {
			plugin.saveConfig();
		}
	}

	public <T> void set(String key, List<T> val) {
		Object current = get(key);

		setItem(key, val);

		if (listener != null) {
			listener.onConfigurationChanged(this, key, current, get(key));
		}

		if (plugin != null) {
			plugin.saveConfig();
		}
	}

	public String addPrefix(String key) {
		return prefix == null ? key : prefix + "."	 + key;
	}

	public String removePrefix(String k) {
		return k.replaceAll("^" + prefix + "\\.", "");
	}

	public void setDescription(String key, String desc) {
		String keyPrefixed = addPrefix(key);
		
		if (!config.contains(keyPrefixed)) {
			throw new DHUtilsException("No such config item: " + keyPrefixed);
		}
		
		descConfig.set(keyPrefixed, desc);
	}

	public String getDescription(String key) {
		String keyPrefixed = addPrefix(key);
		
		if (!config.contains(keyPrefixed)) {
			throw new DHUtilsException("No such config item: " + keyPrefixed);
		}
		
		return descConfig.getString(keyPrefixed, "");
	}

	@SuppressWarnings("unchecked")
	private void setItem(String key, String val) {
		Class<?> c = getType(key);
		Debugger.getInstance().debug(2, "setItem: key = " + key + ", val = " + val + ", class = " + c.getName());

		Object processedVal = null;

		if (val == null) {
			processedVal = null;
		} 
		else if (List.class.isAssignableFrom(c)) {
			List<String>list = new ArrayList<>(1);
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
			} catch (IllegalArgumentException e) {
				throw new DHUtilsException("'" + val + "' is not a valid value for '" + key + "'");
			}
		} 
		else {
			// the class we're converting to must either have a static method annotated with @FactoryMethod,
			// or have a constructor taking a single String argument
			try {
				for (Method method : c.getDeclaredMethods()) {
					Class<?>[] params = method.getParameterTypes();
					
					if (params.length != 1 || !String.class.isAssignableFrom(params[0]))
						continue;
					
					if (method.isAnnotationPresent(FactoryMethod.class) && Modifier.isStatic(method.getModifiers())) {
						processedVal = method.invoke(null, val);
						break;
					}
				}
				
				if (processedVal == null) {
					Constructor<?> ctor = c.getDeclaredConstructor(String.class);
					processedVal = ctor.newInstance(val);
				}
			} catch (NoSuchMethodException e) {
				throw new DHUtilsException("Cannot convert '" + val + "' into a " + c.getName());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
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

		if (processedVal != null || val == null) {
			if (listener != null && validate) {
				processedVal = listener.onConfigurationValidate(this, key, get(key), processedVal);
			}
			
			config.set(addPrefix(key), processedVal);
		} 
		else {
			throw new DHUtilsException("Don't know what to do with " + key + " = " + val);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void setItem(String key, List<T> list) {
		String keyPrefixed = addPrefix(key);
		
		if (config.getDefaults().get(keyPrefixed) == null) {
			throw new DHUtilsException("No such key '" + key + "'");
		}
		
		if (!(config.getDefaults().get(keyPrefixed) instanceof List<?>)) {
			throw new DHUtilsException("Key '" + key + "' does not accept a list of values");
		}
		
		if (listener != null && validate) {
            //noinspection unchecked
            list = (List<T>) listener.onConfigurationValidate(this, key, get(key), list);
		}
		
		config.set(addPrefix(key), handleListValue(key, list));
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> handleListValue(String key, List<T> list) {
		HashSet<T> current = new HashSet<>((List<T>) config.getList(addPrefix(key)));

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
