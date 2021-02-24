package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

class JUnit4AssertMethodAnalyzer {

	Optional<AssertTransformationData> findAssertTransformationData(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		String declaringClassQualifiedName = methodBinding.getDeclaringClass()
			.getQualifiedName();

		if (!declaringClassQualifiedName.equals("org.junit.Assert")) { //$NON-NLS-1$
			return Optional.empty();
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);

		Optional<AssertTransformationData> data = findAssertionOnTwoObjects(methodBinding, arguments);
		if (data.isPresent()) {
			return data;
		}
		data = findAssertionOnSingleObject(methodBinding, arguments);
		if (data.isPresent()) {
			return data;
		}
		return findAssertFail(methodBinding, arguments);
	}

	private Optional<AssertTransformationData> findAssertionOnTwoObjects(IMethodBinding methodBinding,
			List<Expression> arguments) {
		String methodName = methodBinding.getName();
		if (!methodName.equals("assertArrayEquals") //$NON-NLS-1$
				&& !methodName.equals("assertEquals") //$NON-NLS-1$
				&& !methodName.equals("assertNotEquals") //$NON-NLS-1$
				&& !methodName.equals("assertSame") //$NON-NLS-1$
				&& !methodName.equals("assertNotSame")) { //$NON-NLS-1$
			return Optional.empty();
		}

		Expression assertionMessage;
		Expression expected;
		Expression actual;
		Expression delta;

		if (arguments.size() == 4) {
			assertionMessage = arguments.get(0);
			expected = arguments.get(1);
			actual = arguments.get(2);
			delta = arguments.get(3);
			return Optional.of(new AssertTransformationData(Arrays.asList(expected, actual, delta), assertionMessage));
		}

		if (arguments.size() == 3) {
			Expression firstArgument = arguments.get(1);
			if (firstArgument.resolveTypeBinding()
				.getQualifiedName()
				.equals("java.lang.string")) { //$NON-NLS-1$
				assertionMessage = arguments.get(0);
				expected = arguments.get(1);
				actual = arguments.get(2);
				if (isAssertEqualsComparingObjectArrays(methodBinding)) {
					/*
					 * {@code assertEquals(String, Object[], Object[])}
					 */
					return Optional.of(new AssertTransformationData("assertArrayEquals",
							Arrays.asList(expected, actual), assertionMessage));
				}
				return Optional.of(new AssertTransformationData(Arrays.asList(expected, actual), assertionMessage));
			} else {
				expected = arguments.get(0);
				actual = arguments.get(1);
				delta = arguments.get(2);
				return Optional.of(new AssertTransformationData(Arrays.asList(expected, actual, delta)));
			}
		}

		if (arguments.size() == 2) {
			expected = arguments.get(0);
			actual = arguments.get(1);
			if (isAssertEqualsComparingObjectArrays(methodBinding)) {
				/*
				 * {@code assertEquals(Object[], Object[])}
				 */
				return Optional.of(new AssertTransformationData("assertArrayEquals", Arrays.asList(expected, actual)));
			}
			return Optional.of(new AssertTransformationData(Arrays.asList(expected, actual)));
		}

		return Optional.empty();
	}

	/**
	 * This applies to the following signatures:<br>
	 * {@code assertEquals(Object[], Object[])}
	 * {@code assertEquals(String, Object[], Object[])} where a corresponding
	 * method with the name "assertArrayEquals" is available
	 * 
	 * @param methodBinding
	 * @return
	 */
	private boolean isAssertEqualsComparingObjectArrays(IMethodBinding methodBinding) {
		if (!methodBinding.getName()
			.equals("assertEquals")) {
			return false;
		}
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		/*
		 * applies to {@code assertEquals(Object[], Object[])}
		 */
		if (parameterTypes.length == 2) {
			return parameterTypes[0].getComponentType()
				.getQualifiedName()
				.equals("java.lang.Object")
					&& parameterTypes[0].getDimensions() == 1
					&& parameterTypes[1].getComponentType()
						.getQualifiedName()
						.equals("java.lang.Object")
					&& parameterTypes[1].getDimensions() == 1;
		}
		/*
		 * applies to {@code assertEquals(String, Object[], Object[])}
		 */
		if (parameterTypes.length == 3) {
			return parameterTypes[0].getComponentType()
				.getQualifiedName()
				.equals("java.lang.String")
					&& parameterTypes[1].getComponentType()
						.getQualifiedName()
						.equals("java.lang.Object")
					&& parameterTypes[1].getDimensions() == 1
					&& parameterTypes[2].getComponentType()
						.getQualifiedName()
						.equals("java.lang.Object")
					&& parameterTypes[2].getDimensions() == 1;
		}
		return false;
	}

	private Optional<AssertTransformationData> findAssertionOnSingleObject(IMethodBinding methodBinding,
			List<Expression> arguments) {
		String methodName = methodBinding.getName();
		if (!methodName.equals("assertTrue") //$NON-NLS-1$
				&& !methodName.equals("assertFalse") //$NON-NLS-1$
				&& !methodName.equals("assertNull") //$NON-NLS-1$
				&& !methodName.equals("assertNotNull")) { //$NON-NLS-1$
			return Optional.empty();
		}

		Expression assertionMessage;
		Expression assertionArgument;

		if (arguments.size() == 2) {
			assertionMessage = arguments.get(0);
			assertionArgument = arguments.get(1);
			return Optional.of(new AssertTransformationData(Arrays.asList(assertionArgument), assertionMessage));
		}
		if (arguments.size() == 1) {
			assertionArgument = arguments.get(0);
			return Optional.of(new AssertTransformationData(Arrays.asList(assertionArgument)));
		}

		return Optional.empty();
	}

	private Optional<AssertTransformationData> findAssertFail(IMethodBinding methodBinding,
			List<Expression> arguments) {
		String methodName = methodBinding.getName();
		if (!methodName.equals("fail")) { //$NON-NLS-1$
			return Optional.empty();
		}

		Expression message;

		if (arguments.size() == 1) {
			message = arguments.get(0);
			return Optional.of(new AssertTransformationData(Arrays.asList(), message));
		}
		if (arguments.size() == 0) {
			return Optional.of(new AssertTransformationData(Arrays.asList()));
		}

		return Optional.empty();
	}
}
