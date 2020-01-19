package io.github.thebusybiscuit.sensibletoolbox.util;

public enum UnicodeSymbol {
	
	ARROW_LEFT("\u21E6"),
	ARROW_UP("\u21E7"),
	ARROW_RIGHT("\u21E8"),
	ARROW_DOWN("\u21E9"),
	
	ELECTRICITY("\u2301"),
	NUMBER("\u2116"),
	
	CENTERED_POINT("\u2022"),
	SQUARE("\u2588");
	
	String code;
	
	UnicodeSymbol(String code) {
		this.code = code;
	}

	public String toUnicode() {
		return this.code;
	}
}
