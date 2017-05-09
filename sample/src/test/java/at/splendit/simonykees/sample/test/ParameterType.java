package at.splendit.simonykees.sample.test;

import java.util.Arrays;
import java.util.List;

/**
 * The enum is used by {@link AbstractReflectiveMethodTester} to define datatype
 * "groups" that are equivalent.
 * 
 * For example: A method with an int parameter could be called with either an
 * int or an Integer.
 * 
 * @author Ludwig Werzowa
 * @since 0.9.2
 */
public enum ParameterType {

	STRING(String.class),

	/*
	 * TODO which classes exactly?
	 */
	CHARSEQUENCE(CharSequence.class),
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
