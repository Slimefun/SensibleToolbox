package me.desht.dhutils;

public class DHValidate {
	public static void isTrue(boolean cond, String message) {
		if (!cond) {
			throw new DHUtilsException(message);
		}
	}

	public static void isFalse(boolean cond, String message) {
		if (cond) {
			throw new DHUtilsException(message);
		}
	}

	public static void notNull(Object o, String message) {
		if (o == null) {
			throw new DHUtilsException(message);
		}
	}
}
