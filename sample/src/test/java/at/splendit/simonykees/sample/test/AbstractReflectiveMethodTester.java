package at.splendit.simonykees.sample.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

public abstract class AbstractReflectiveMethodTester {

	// parameterized
	private Object[] parameterizedValues;

	private Object preObject;
	private Object postObject;

	private Map<String, Method> preMethods;
	private Map<String, Method> postMethods;

	public AbstractReflectiveMethodTester(Class<?> preClass, Class<?> postClass, Object... parameterizedValues) {
		this.parameterizedValues = parameterizedValues;
		try {
			this.preObject = preClass.newInstance();
			this.postObject = postClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// this should never happen
			e.printStackTrace();
		}

		this.preMethods = initMethodMap(preClass);
		this.postMethods = initMethodMap(postClass);
	}

	private Map<String, Method> initMethodMap(Class<?> clazz) {
		Map<String, Method> retVal = Arrays.stream(clazz.getDeclaredMethods())
				.filter(m -> !m.getReturnType().equals(Void.TYPE))
				.filter(m2 -> hasDesiredParameters(m2.getParameterTypes()))
				.collect(Collectors.toMap(Method::getName, Function.identity()));
		return retVal;
	}

	@SuppressWarnings("nls")
	@Test
	public void test() throws Exception {
		for (Method m : preMethods.values()) {
			Object preRetVal = m.invoke(preObject, parameterizedValues);
			Object postRetVal = postMethods.get(m.getName()).invoke(postObject, parameterizedValues);
			System.out.println(String.format("Class: %s, Method: %s, values: %s, preRetVal: %s, postRetVal: %s",
					preObject.getClass().getSimpleName(), m.getName(), Arrays.toString(parameterizedValues), preRetVal,
					postRetVal));
			assertEquals(preRetVal, postRetVal);
		}
	}

	/*
	 * Too bad, Class.getDeclaredMethod(String name, Class<?>... parameterTypes)
	 * needs a method name and thus only returns one method..
	 */
	private boolean hasDesiredParameters(Class<?>[] methodParameterClasses) {

		if (methodParameterClasses == null) {
			return this.parameterizedValues == null || this.parameterizedValues.length == 0;
		}

		if (methodParameterClasses.length != this.parameterizedValues.length) {
			return false;
		}

		for (int i = 0; i < methodParameterClasses.length; i++) {
			if (!isEquivalentClass(methodParameterClasses[i], this.parameterizedValues[i].getClass())) {
				return false;
			}
		}

		return true;

	}

	/**
	 * To ignore mismatches if one is a primitive type and the other isn't
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	private boolean isEquivalentClass(Class<?> c1, Class<?> c2) {
		if (c1.equals(c2))
			return true;
		if (c1.equals(Integer.TYPE) && c2.equals(Integer.class))
			return true;
		if (c1.equals(Integer.class) && c2.equals(Integer.TYPE))
			return true;

		return false;
	}

}
