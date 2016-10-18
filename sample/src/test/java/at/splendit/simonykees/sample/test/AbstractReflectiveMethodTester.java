package at.splendit.simonykees.sample.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This class makes it easy to take two classes with the same methods and assert
 * that every corresponding return value (of methods with the same name),
 * returns the same value.
 * 
 * Only methods with return values are taken into account. Only methods where
 * the parameter types match the types of parameterizedValues, are taken into
 * account.
 */
@SuppressWarnings("nls")
public abstract class AbstractReflectiveMethodTester {

	private static Logger log = LogManager.getLogger(AbstractReflectiveMethodTester.class);

	private ParameterType parameterType;

	// parameterized value
	private Object parameterizedValue;

	public AbstractReflectiveMethodTester(ParameterType parameterType, Object parameterizedValue) {

		assertTrue(
				String.format("ParameterType [%s] does not allow parameter values of type [%s]", parameterType.name(),
						parameterizedValue.getClass().getSimpleName()),
				parameterType.allowsType(parameterizedValue.getClass()));

		getHolder().incrementValueCounter(parameterType);

		this.parameterType = parameterType;
		this.parameterizedValue = parameterizedValue;
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

	@Test
	public void test() throws Exception {
		log.debug(String.format("Class: [%s], Type: [%s], Value: [%s]",
				getHolder().getPreObject().getClass().getSimpleName(), this.parameterType, this.parameterizedValue));

		for (Method m : getHolder().getPreMethods(this.parameterType).values()) {

			boolean isArrayRetVal = m.getReturnType().isArray();

			Method postMethod = getHolder().getPostMethod(this.parameterType, m.getName());

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
	private void testArrayReturnValue(Method m1, Method m2)
			throws IllegalAccessException, InvocationTargetException, ArrayComparisonFailure {
		Object[] preRetVal = (Object[]) m1.invoke(getHolder().getPreObject(), this.parameterizedValue);

		Object[] postRetVal = (Object[]) m2.invoke(getHolder().getPostObject(), parameterizedValue);

		log.debug(String.format("Type: [%s], Method: [%s], isArrayRetVal: [%b], preRetVal: [%s], postRetVal: [%s]",
				this.parameterType, m1.getName(), true, Arrays.toString(preRetVal), Arrays.toString(postRetVal)));

		assertArrayEquals(String.format("Return value mismatch for parameter [%s]. [%s.%s] expected [%s] but was [%s]",
				parameterizedValue, getHolder().preObject.getClass().getSimpleName(), m1.getName(),
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
	private void testSingleReturnValue(Method m1, Method m2)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object preRetVal = m1.invoke(getHolder().getPreObject(), this.parameterizedValue);

		Object postRetVal = m2.invoke(getHolder().getPostObject(), parameterizedValue);

		log.debug(String.format("Type: [%s], Method: [%s], isArrayRetVal: [%b], preRetVal: [%s], postRetVal: [%s]",
				this.parameterType, m1.getName(), false, preRetVal, postRetVal));

		assertEquals(String.format("Return value mismatch for parameter [%s]. [%s.%s]", parameterizedValue,
				getHolder().preObject.getClass().getSimpleName(), m1.getName()), preRetVal, postRetVal);
	}

	/**
	 * Singleton holder class to avoid having to create the method map and class
	 * instance of pre- and post-classes for every parameterized value.
	 */
	protected static class PreAndPostClassHolder {

		private Object preObject;
		private Object postObject;

		private Table<ParameterType, String, Method> preMethods;
		private Table<ParameterType, String, Method> postMethods;

		private Map<ParameterType, MethodAndValueCounter> counter = new LinkedHashMap<>();

		public void incrementValueCounter(ParameterType parameterType) {
			counter.putIfAbsent(parameterType, new MethodAndValueCounter(0));
			counter.get(parameterType).incrementValueCounter();
		}

		public String getCounterToString() {
			return counter.toString();
		}

		public Object getPreObject() {
			return preObject;
		}

		public Object getPostObject() {
			return postObject;
		}

		public Map<String, Method> getPreMethods(ParameterType parameterType) {
			return preMethods.row(parameterType);
		}

		public Method getPostMethod(ParameterType parameterType, String methodName) {
			return postMethods.get(parameterType, methodName);
		}

		public PreAndPostClassHolder(Class<?> preClass, Class<?> postClass) {
			try {
				this.preObject = preClass.newInstance();
				this.postObject = postClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				// this should never happen
				e.printStackTrace();
			}

			this.preMethods = initMethodTable(preClass);
			this.postMethods = initMethodTable(postClass);

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
				for (ParameterType parameterType : this.postMethods.rowKeySet()) {
					for (Method m : this.postMethods.row(parameterType).values()) {

						Method preMethod = this.preMethods.get(parameterType, m.getName());

						assertNotNull(
								String.format("Expected method [%s] for parameter type [%s] not present in class [%s]",
										m.getName(), parameterType.name(), this.preObject.getClass().getName()),
								preMethod);
					}
				}
			}

			this.preMethods.rowKeySet().stream()
					.forEach(p -> counter.putIfAbsent(p, new MethodAndValueCounter(this.preMethods.row(p).size())));

		}

		/**
		 * We make the assumption, that this specific parameterizedValues array
		 * has the same types for all values as all the following
		 * parameterizedValues arrays.
		 * 
		 * @param clazz
		 * @param parameterizedValue
		 *            // TODO
		 * @return
		 */
		private static Table<ParameterType, String, Method> initMethodTable(Class<?> clazz) {
			List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
					// only take methods with a return value
					.filter(m -> !m.getReturnType().equals(Void.TYPE))
					// only methods with exactly one parameter
					.filter(m2 -> m2.getParameterCount() == 1)
					.collect(Collectors.toList());

			Table<ParameterType, String, Method> retVal = HashBasedTable.create();
			for (Method method : methods) {
				addMethodToTable(method, retVal);
			}

			return retVal;
		}

		private static void addMethodToTable(Method method, Table<ParameterType, String, Method> tableToInsert) {

			assertEquals(String.format(
					"At this point, only methods with one parameter should be present. Method [%s], parameters: [%s]", //$NON-NLS-1$
					method.getName(), Arrays.toString(method.getParameters())), method.getParameterCount(), 1);

			Class<?> parameterClass = method.getParameterTypes()[0];

			for (ParameterType parameterType : ParameterType.values()) {
				// we already know that there is only one parameter
				if (parameterType.allowsType(parameterClass)) {
					tableToInsert.put(parameterType, method.getName(), method);
				}
			}
		}

	}

	private static class MethodAndValueCounter {
		private int methodCounter = 0;
		private int valueCounter = 0;

		public MethodAndValueCounter(int methodCounter) {
			this.methodCounter = methodCounter;
		}

		public void incrementValueCounter() {
			valueCounter++;
		}

		@Override
		public String toString() {
			return "[methods: [" + methodCounter + "], values: [" + valueCounter + "]]";
		}

	}

}
