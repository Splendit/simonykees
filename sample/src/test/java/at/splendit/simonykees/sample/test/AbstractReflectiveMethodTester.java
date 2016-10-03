package at.splendit.simonykees.sample.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

/**
 * TODO: discuss logging: use logger / leave as is / toggle logging / remove
 * logging
 * 
 * This class makes it easy to take two classes with the same methods and assert
 * that every corresponding return value (of methods with the same name),
 * returns the same value.
 * 
 * Only methods with return values are taken into account. Only methods where
 * the parameter types match the types of parameterizedValues, are taken into
 * account.
 */
public abstract class AbstractReflectiveMethodTester {

	private static Logger log = LogManager.getLogger(AbstractReflectiveMethodTester.class);

	// parameterized values
	private Object[] parameterizedValues;

	public AbstractReflectiveMethodTester(Object... parameterizedValues) {
		this.parameterizedValues = parameterizedValues;
	}

	/**
	 * A subclass needs to implement a method to return a holder. The subclass
	 * should have a static holder field like this:
	 * 
	 * <pre>
	 * <code>
	 * private static PreAndPostClassHolder holder;
	 * </code>
	 * </pre>
	 * 
	 * @return PreAndPostClassHolder instance
	 */
	protected abstract PreAndPostClassHolder getHolder();

	@SuppressWarnings("nls")
	@Test
	public void test() throws Exception {
		log.debug(String.format("Class: [%s], Values: [%s]", getHolder().getPreObject().getClass().getSimpleName(),
				Arrays.toString(this.parameterizedValues)));

		for (Method m : getHolder().getPreMethods().values()) {

			boolean isArrayRetVal = m.getReturnType().isArray();

			Method postMethod = getHolder().getPostMethod(m.getName());

			assertNotNull(String.format("Expected method [%s] not present in class [%s]", m.getName(),
					getHolder().getPostObject().getClass().getName()), postMethod);

			assertEquals(
					String.format("Return values mismatch for method [%s]: [%s] does not match [%s]", m.getName(),
							m.getReturnType(), postMethod.getReturnType()),
					m.getReturnType(), postMethod.getReturnType());

			if (isArrayRetVal) {
				testArrayReturnValue(m, postMethod);
			} else {
				testSingleReturnValue(m, postMethod);
			}
		}
	}

	/**
	 * Compares return values of two methods, where the return value is an
	 * array.
	 * 
	 * @param m1
	 *            method to invoke
	 * @param m2
	 *            method to invoke
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ArrayComparisonFailure
	 */
	@SuppressWarnings("nls")
	private void testArrayReturnValue(Method m1, Method m2)
			throws IllegalAccessException, InvocationTargetException, ArrayComparisonFailure {
		Object[] preRetVal = (Object[]) m1.invoke(getHolder().getPreObject(), this.parameterizedValues);

		Object[] postRetVal = (Object[]) m2.invoke(getHolder().getPostObject(), parameterizedValues);

		log.debug(String.format("Method: [%s], isArrayRetVal: [%b], preRetVal: [%s], postRetVal: [%s]", m1.getName(),
				true, Arrays.toString(preRetVal), Arrays.toString(postRetVal)));

		assertArrayEquals(String.format("Return value mismatch for parameter [%s]. [%s.%s] expected [%s] but was [%s]",
				Arrays.toString(parameterizedValues), getHolder().preObject.getClass().getSimpleName(), m1.getName(),
				Arrays.toString(preRetVal), Arrays.toString(postRetVal)), preRetVal, postRetVal);
	}

	/**
	 * Compares return values of two methods, where the return value is a
	 * non-array type.
	 * 
	 * @param m1
	 *            method to invoke
	 * @param m2
	 *            method to invoke
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("nls")
	private void testSingleReturnValue(Method m1, Method m2)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object preRetVal = m1.invoke(getHolder().getPreObject(), this.parameterizedValues);

		Object postRetVal = m2.invoke(getHolder().getPostObject(), parameterizedValues);

		log.debug(String.format("Method: [%s], isArrayRetVal: [%b], preRetVal: [%s], postRetVal: [%s]", m1.getName(),
				false, preRetVal, postRetVal));

		assertEquals(String.format("Return value mismatch for parameter [%s]. [%s.%s]",
				Arrays.toString(parameterizedValues), getHolder().preObject.getClass().getSimpleName(), m1.getName()),
				preRetVal, postRetVal);
	}

	/**
	 * Singleton holder class to avoid having to create the method map and class
	 * instance of pre- and post-classes for every parameterized value.
	 */
	protected static class PreAndPostClassHolder {

		private Object preObject;
		private Object postObject;

		private Map<String, Method> preMethods;
		private Map<String, Method> postMethods;

		public Object getPreObject() {
			return preObject;
		}

		public Object getPostObject() {
			return postObject;
		}

		public Map<String, Method> getPreMethods() {
			return preMethods;
		}

		public Method getPostMethod(String name) {
			return postMethods.get(name);
		}

		@SuppressWarnings("nls")
		public PreAndPostClassHolder(Class<?> preClass, Class<?> postClass, Class<?>... parameterizedValues) {
			try {
				this.preObject = preClass.newInstance();
				this.postObject = postClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				// this should never happen
				e.printStackTrace();
			}

			this.preMethods = initMethodMap(preClass, parameterizedValues);
			this.postMethods = initMethodMap(postClass, parameterizedValues);

			/**
			 * We only need to check this case here, because the other case
			 * (where there are more methods in the pre-Class), already gets
			 * tested in the AbstractReflectiveMethodTester.test() method.
			 * 
			 * Additionally, we do not want to simply compare the number of
			 * methods, because that would not give us the information which
			 * method exactly is missing.
			 */
			if (this.postMethods.size() > this.preMethods.size()) {
				for (Method m : this.postMethods.values()) {

					Method preMethod = this.preMethods.get(m.getName());

					assertNotNull(String.format("Expected method [%s] not present in class [%s]", m.getName(),
							this.preObject.getClass().getName()), preMethod);
				}
			}
		}

		/**
		 * We make the assumption, that this specific parameterizedValues array
		 * has the same types for all values as all the following
		 * parameterizedValues arrays.
		 * 
		 * @param clazz
		 * @param parameterizedValues
		 * @return
		 */
		private static Map<String, Method> initMethodMap(Class<?> clazz, Class<?>... parameterizedValues) {
			Map<String, Method> retVal = Arrays.stream(clazz.getDeclaredMethods())
					// only take methods with a return value
					.filter(m -> !m.getReturnType().equals(Void.TYPE))
					// only take methods where the parameters fit
					.filter(m2 -> hasDesiredParameters(m2.getParameterTypes(), parameterizedValues))
					.collect(Collectors.toMap(Method::getName, Function.identity()));
			return retVal;
		}

		/*
		 * Too bad, Class.getDeclaredMethod(String name, Class<?>...
		 * parameterTypes) needs a method name and thus only returns one
		 * method..
		 */
		private static boolean hasDesiredParameters(Class<?>[] methodParameterClasses,
				Class<?>... parameterizedValues) {

			if (methodParameterClasses == null) {
				return parameterizedValues == null || parameterizedValues.length == 0;
			}

			if (methodParameterClasses.length != parameterizedValues.length) {
				return false;
			}

			for (int i = 0; i < methodParameterClasses.length; i++) {
				if (!isEquivalentClass(methodParameterClasses[i], parameterizedValues[i])) {
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
		private static boolean isEquivalentClass(Class<?> c1, Class<?> c2) {
			if (c1.equals(c2))
				return true;
			if (c1.equals(Integer.TYPE) && c2.equals(Integer.class))
				return true;
			if (c1.equals(Integer.class) && c2.equals(Integer.TYPE))
				return true;

			return false;
		}

	}

}
