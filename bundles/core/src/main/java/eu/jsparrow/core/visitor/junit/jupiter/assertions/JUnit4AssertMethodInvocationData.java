package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Immutable class storing all necessary informations about the given invocation
 * of a static method of the class {@code org.junit.Assert} which may be
 * replaced by an invocation of the corresponding method of
 * {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.28.0
 *
 */
class JUnit4AssertMethodInvocationData {
	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test"; //$NON-NLS-1$
	private final MethodInvocation methodInvocation;
	private final boolean invocationWithinJUnitJupiterTest;
	private final String methodName;
	private final String deprecatedMethodNameReplacement;
	private final Expression assertionMessageAsFirstArgument;
	private final List<Expression> assertionArgumentsExceptForMessage;

	static Optional<JUnit4AssertMethodInvocationData> findJUnit4MethodInvocationData(
			MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (isSupportedJUnit4AssertMethod(methodBinding)) {
			return Optional.of(new JUnit4AssertMethodInvocationData(methodInvocation, methodBinding));
		}
		return Optional.empty();
	}

	static boolean isSupportedJUnit4AssertMethod(IMethodBinding methodBinding) {
		if (methodBinding.getDeclaringClass()
			.getQualifiedName()
			.equals("org.junit.Assert")) { //$NON-NLS-1$
			String methodName = methodBinding.getName();
			return !methodName.equals("assertThat") //$NON-NLS-1$
					&& !methodName.equals("assertThrows"); //$NON-NLS-1$
		}
		return false;
	}

	private static boolean isInvocationWithinJUnitJupiterTest(MethodInvocation methodInvocation) {
		ASTNode parent = methodInvocation.getParent();
		while (parent != null) {
			if (parent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				MethodDeclaration methodDeclaration = (MethodDeclaration) parent;
				if (methodDeclaration.getLocationInParent() != TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
					return false;
				}
				TypeDeclaration typeDeclaration = (TypeDeclaration) methodDeclaration.getParent();
				if (typeDeclaration.isLocalTypeDeclaration()) {
					return false;
				}
				return ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), Annotation.class)
					.stream()
					.map(Annotation::resolveAnnotationBinding)
					.map(IAnnotationBinding::getAnnotationType)
					.map(ITypeBinding::getQualifiedName)
					.anyMatch(ORG_JUNIT_JUPITER_API_TEST::equals);
			}
			if (parent.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
				return false;
			}
			parent = parent.getParent();
		}
		return false;
	}

	/**
	 * This applies to the following signatures:<br>
	 * {@code assertEquals(Object[], Object[])}
	 * {@code assertEquals(String, Object[], Object[])} where a corresponding
	 * method with the name "assertArrayEquals" is available
	 * 
	 * @return
	 */
	private static boolean isDeprecatedAssertEqualsComparingObjectArrays(String methodName,
			ITypeBinding[] declaredParameterTypes) {
		if (!methodName.equals("assertEquals")) { //$NON-NLS-1$
			return false;
		}

		if (declaredParameterTypes.length == 2) {
			return isParameterTypeObjectArray(declaredParameterTypes[0])
					&& isParameterTypeObjectArray(declaredParameterTypes[1]);
		}

		if (declaredParameterTypes.length == 3) {
			return isParameterTypeString(declaredParameterTypes[0])
					&& isParameterTypeObjectArray(declaredParameterTypes[1])
					&& isParameterTypeObjectArray(declaredParameterTypes[2]);
		}
		return false;
	}

	private static boolean isParameterTypeObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray()) {
			return parameterType.getComponentType()
				.getQualifiedName()
				.equals("java.lang.Object") && parameterType.getDimensions() == 1; //$NON-NLS-1$
		}
		return false;
	}

	private static boolean isParameterTypeString(ITypeBinding parameterType) {
		return parameterType.getQualifiedName()
			.equals("java.lang.String"); //$NON-NLS-1$
	}

	private JUnit4AssertMethodInvocationData(MethodInvocation methodInvocation, IMethodBinding methodBinding) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodBinding.getName();
		this.invocationWithinJUnitJupiterTest = isInvocationWithinJUnitJupiterTest(methodInvocation);
		ITypeBinding[] declaredParameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		if (isDeprecatedAssertEqualsComparingObjectArrays(methodName, declaredParameterTypes)) {
			deprecatedMethodNameReplacement = "assertArrayEquals"; //$NON-NLS-1$
		} else {
			deprecatedMethodNameReplacement = null;
		}
		List<Expression> invocationArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		if (!invocationArguments.isEmpty() && isParameterTypeString(declaredParameterTypes[0])) {
			assertionMessageAsFirstArgument = invocationArguments.remove(0);
		} else {
			assertionMessageAsFirstArgument = null;
		}
		assertionArgumentsExceptForMessage = Collections.unmodifiableList(invocationArguments);
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	String getMethodName() {
		return methodName;
	}

	Optional<String> getDeprecatedMethodNameReplacement() {
		return Optional.ofNullable(deprecatedMethodNameReplacement);
	}

	Optional<Expression> getMessageArgument() {
		return Optional.ofNullable(assertionMessageAsFirstArgument);
	}

	List<Expression> getArgumentsExceptForMessage() {
		return assertionArgumentsExceptForMessage;
	}

	boolean isInvocationWithinJUnitJupiterTest() {
		return invocationWithinJUnitJupiterTest;
	}
}
