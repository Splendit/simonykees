package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

class JUnit4AssertMethodAnalyzerAlternative1 {

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
		return Optional.empty();
	}

	private boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		String declaringClassQualifiedName = methodBinding.getDeclaringClass()
			.getQualifiedName();
		if (declaringClassQualifiedName.equals("org.junit.Assert")) { //$NON-NLS-1$
			String methodName = methodBinding.getName();
			return !methodName.equals("assertThat") //$NON-NLS-1$
					&& !methodName.equals("assertThrows"); //$NON-NLS-1$
		}
		return false;
	}

	private Optional<AssertTransformationData> findAssertionOnTwoObjects(IMethodBinding methodBinding,
			List<Expression> arguments) {
		String methodName = methodBinding.getName();
		if (!methodName.equals("assertArrayEquals")
				&& !methodName.equals("assertEquals")
				&& !methodName.equals("assertNotEquals")
				&& !methodName.equals("assertSame")
				&& !methodName.equals("assertNotSame")) {
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
				.equals("java.lang.string")) {
				assertionMessage = arguments.get(0);
				expected = arguments.get(1);
				actual = arguments.get(2);
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
			return Optional.of(new AssertTransformationData(Arrays.asList(expected, actual)));
		}

		return Optional.empty();
	}

	private Optional<AssertTransformationData> findAssertionOnSingleObject(IMethodBinding methodBinding,
			List<Expression> arguments) {
		String methodName = methodBinding.getName();
		if (!methodName.equals("assertTrue")
				&& !methodName.equals("assertFalse")
				&& !methodName.equals("assertNull")
				&& !methodName.equals("assertNotNull")) {
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

}
