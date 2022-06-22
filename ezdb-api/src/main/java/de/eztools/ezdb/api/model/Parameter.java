package de.eztools.ezdb.api.model;

public interface Parameter {
	
	String getName();
	
	Class<?> getType();

	default String getKey() {
		return "${" + getName() + "}";
	}
}
