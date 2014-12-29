package me.mrCookieSlime.CSCoreLib.general.Reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

public class ReflectionUtils {
	
	private static final Map<Class<?>, Class<?>> conversion = new HashMap<Class<?>, Class<?>>();
	
	static {
		conversion.put(Byte.class, Byte.TYPE);
	    conversion.put(Short.class, Short.TYPE);
	    conversion.put(Integer.class, Integer.TYPE);
	    conversion.put(Long.class, Long.TYPE);
	    conversion.put(Character.class, Character.TYPE);
	    conversion.put(Float.class, Float.TYPE);
	    conversion.put(Double.class, Double.TYPE);
	    conversion.put(Boolean.class, Boolean.TYPE);
	}
	
	public static Method getMethod(Class<?> c, String method) {
		for (Method m : c.getMethods()) {
			if (m.getName().equals(method)) return m;
		}
        return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static Method getMethod(Class<?> c, String name, Class<?>[] paramTypes) {
	    Class[] t = toPrimitiveTypeArray(paramTypes);
	    for (Method m : c.getMethods()) {
	      Class[] types = toPrimitiveTypeArray(m.getParameterTypes());
	      if ((m.getName().equals(name)) && (equalsTypeArray(types, t)))
	        return m;
	    }
	    return null;
	}
	
	public static Field getField(Class<?> c, String field) throws Exception {
		return c.getDeclaredField(field);
	}
	
	public static void setValue(Object object, String key, Object value) throws Exception {
		Field f = getField(object.getClass(), key);
		f.setAccessible(true);
		f.set(object, value);
	}
	
	public static Object getValue(Object object, String key) throws Exception {
	    Field f = getField(object.getClass(), key);
	    f.setAccessible(true);
	    return f.get(object);
	}
	
	@SuppressWarnings("rawtypes")
	public static Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
		int a = classes != null ? classes.length : 0;
	    Class[] types = new Class[a];
	    for (int i = 0; i < a; i++) {
	    	types[i] = conversion.containsKey(classes[i]) ? conversion.get(classes[i]): classes[i];
	    }
	    return types;
	}
	
	@SuppressWarnings("rawtypes")
	public static Class<?>[] toPrimitiveTypeArray(Object[] objects) {
	    int a = objects != null ? objects.length : 0;
	    Class[] types = new Class[a];
	    for (int i = 0; i < a; i++)
	    	types[i] = conversion.containsKey(objects[i].getClass()) ? conversion.get(objects[i].getClass()): objects[i].getClass();
	    return types;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static Constructor<?> getConstructor(Class<?> clazz, Class<?>[] paramTypes) {
	    Class[] t = toPrimitiveTypeArray(paramTypes);
	    for (Constructor c : clazz.getConstructors()) {
	    	Class[] types = toPrimitiveTypeArray(c.getParameterTypes());
	    	if (equalsTypeArray(types, t)) return c;
	    }
	    return null;
	}
	
	public static Class<?> getClass(String name) throws Exception {
	    return Class.forName(new StringBuilder().append("net.minecraft.server.").append(Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(".") + 1)).append(".").append(name).toString());
	}
	
	private static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
	    if (a.length != o.length)
	      return false;
	    for (int i = 0; i < a.length; i++)
	      if ((!a[i].equals(o[i])) && (!a[i].isAssignableFrom(o[i])))
	        return false;
	    return true;
	}
}
