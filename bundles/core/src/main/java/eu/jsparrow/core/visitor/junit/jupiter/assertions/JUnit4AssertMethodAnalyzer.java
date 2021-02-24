package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * 
 * @since 3.28.0
 *
 */
class JUnit4AssertMethodAnalyzer {

	Optional<AssertTransformationData> findAssertTransformationData(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (isSupportedJUnit4Method(methodBinding)) {
			return Optional.empty();
		}
		String newMethodName = isAssertEqualsComparingObjectArrays(methodBinding) ? "assertArrayEquals" : null;

		methodBinding.getParameterTypes();
		methodBinding.getMethodDeclaration()
			.getParameterTypes();

		List<Expression> invocationArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);
		Expression assertionMessage = null;
		if (invocationArguments.size() > 0) {
			Expression invocationArgument = invocationArguments.get(0);
			if (invocationArgument.resolveTypeBinding()
				.getQualifiedName()
				.equals("java.lang.String")) {
				assertionMessage = invocationArgument;
			}
			invocationArguments.remove(0);
		}

		if (assertionMessage != null) {
			if (newMethodName != null) {
				return Optional.of(new AssertTransformationData(newMethodName, invocationArguments, assertionMessage));
			}
			return Optional.of(new AssertTransformationData(invocationArguments, assertionMessage));
		}

		if (newMethodName != null) {
			return Optional.of(new AssertTransformationData(newMethodName, invocationArguments));
		}

		return Optional.of(new AssertTransformationData(invocationArguments));
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
}
