package at.splendit.simonykees.sample.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

/**
 * This class makes it easy to take two classes with the same methods and assert
 * that every corresponding return value (of methods with the same name),
 * returns the same value.
 * 
 * Only methods with return values are taken into account. Only methods where
 * the parameter types match the types of parameterizedValues, are taken into
 * account.
 */
public abstract class AbstractReflectiveMethodTester {

	// parameterized values
	private Object[] parameterizedValues;

	// we want this to be initialized only once for all parameterized values
	private PreAndPostClassHolder holder;

	public AbstractReflectiveMethodTester(Class<?> preClass, Class<?> postClass, Object... parameterizedValues) {
		this.parameterizedValues = parameterizedValues;
		this.holder = PreAndPostClassHolder.getInstance(preClass, postClass, parameterizedValues);
	}

	@SuppressWarnings("nls")
	@Test
	public void test() throws Exception {
		System.out.println(String.format("Class: [%s], Values: [%s]",
				this.holder.getPreObject().getClass().getSimpleName(), Arrays.toString(this.parameterizedValues)));

		for (Method m : this.holder.getPreMethods().values()) {

			boolean isArrayRetVal = m.getReturnType().isArray();

			System.out.print(String.format("\tMethod: [%s], isArrayRetVal: [%b]", m.getName(), isArrayRetVal));

			Method postMethod = this.holder.getPostMethod(m.getName());

			assertNotNull(String.format("Expected method [%s] not present in class [%s]", m.getName(),
					this.holder.getPostObject().getClass().getName()), postMethod);

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
		Object[] preRetVal = (Object[]) m1.invoke(this.holder.getPreObject(), this.parameterizedValues);

		System.out.print(String.format(", preRetVal: [%s]", Arrays.toString(preRetVal)));

		Object[] postRetVal = (Object[]) m2.invoke(this.holder.getPostObject(), parameterizedValues);

		System.out.println(String.format(", postRetVal: [%s]", Arrays.toString(postRetVal)));

		assertArrayEquals(String.format("Return value mismatch for parameter [%s]. [%s.%s] expected [%s] but was [%s]",
				Arrays.toString(parameterizedValues), holder.preObject.getClass().getSimpleName(), m1.getName(),
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
		Object preRetVal = m1.invoke(this.holder.getPreObject(), this.parameterizedValues);

		System.out.print(String.format(", preRetVal: [%s]", preRetVal));

		Object postRetVal = m2.invoke(this.holder.getPostObject(), parameterizedValues);

		System.out.println(String.format(", postRetVal: [%s]", postRetVal));

		assertEquals(String.format("Return value mismatch for parameter [%s]. [%s.%s]",
				Arrays.toString(parameterizedValues), holder.preObject.getClass().getSimpleName(), m1.getName()),
				preRetVal, postRetVal);
	}

	/**
	 * Singleton holder class to avoid having to create the method map and class
	 * instance of pre- and post-classes for every parameterized value.
	 */
	private static class PreAndPostClassHolder {

		private static PreAndPostClassHolder instance;

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
		static PreAndPostClassHolder getInstance(Class<?> preClass, Class<?> postClass, Object... parameterizedValues) {
			if (instance == null) {
				instance = new PreAndPostClassHolder();
				try {
					instance.preObject = preClass.newInstance();
					instance.postObject = postClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					// this should never happen
					e.printStackTrace();
				}

				instance.preMethods = initMethodMap(preClass, parameterizedValues);
				instance.postMethods = initMethodMap(postClass, parameterizedValues);

				/**
				 * We only need to check this case here, because the other case
				 * (where there are more methods in the pre-Class), already gets
				 * tested in the AbstractReflectiveMethodTester.test() method.
				 * 
				 * Additionally, we do not want to simply compare the number of
				 * methods, because that would not give us the information which
				 * method exactly is missing.
				 */
				if (instance.postMethods.size() > instance.preMethods.size()) {
					for (Method m : instance.postMethods.values()) {

						Method preMethod = instance.preMethods.get(m.getName());

						assertNotNull(String.format("Expected method [%s] not present in class [%s]", m.getName(),
								instance.preObject.getClass().getName()), preMethod);
					}
				}
			}
			return instance;
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
		private static Map<String, Method> initMethodMap(Class<?> clazz, Object... parameterizedValues) {
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
		private static boolean hasDesiredParameters(Class<?>[] methodParameterClasses, Object... parameterizedValues) {

			if (methodParameterClasses == null) {
				return parameterizedValues == null || parameterizedValues.length == 0;
			}

			if (methodParameterClasses.length != parameterizedValues.length) {
				return false;
			}

			for (int i = 0; i < methodParameterClasses.length; i++) {
				if (!isEquivalentClass(methodParameterClasses[i], parameterizedValues[i].getClass())) {
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
