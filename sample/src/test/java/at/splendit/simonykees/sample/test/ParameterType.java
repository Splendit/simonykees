package at.splendit.simonykees.sample.test;

import java.util.Arrays;
import java.util.List;

public enum ParameterType {
	
	STRING(String.class),
	CHARSEQUENCE(CharSequence.class), // TODO which classes exactly?
	INTEGER(Integer.class, Integer.TYPE), 
	DOUBLE(Double.class, Double.TYPE),
	FLOAT(Float.class, Float.TYPE),
	LONG(Long.class, Long.TYPE), 
	SHORT(Short.class, Short.TYPE), 
	BYTE(Byte.class, Byte.TYPE), 
	CHARACTER(Character.class, Character.TYPE);

	private List<Class<?>> possibleTypes;

	ParameterType(Class<?>... possibleTypes) {
		this.possibleTypes = Arrays.asList(possibleTypes);
	}
	
	public List<Class<?>> getPossibleTypes() {
		return possibleTypes;
	}
	
	public boolean allowsType(Class<?> type) {
		return possibleTypes.contains(type);
	}

}
