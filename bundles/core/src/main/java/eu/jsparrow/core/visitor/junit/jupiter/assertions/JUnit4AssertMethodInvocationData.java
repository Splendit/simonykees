package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Stores a {@link MethodInvocation} together with its corresponding
 * {@link IMethodBinding}. This helps to avoid resolving the same method binding
 * repeatedly.
 * 
 * @since 3.28.0
 *
 */
class JUnit4AssertMethodInvocationData {
	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test"; //$NON-NLS-1$
	private final MethodInvocation methodInvocation;
	private final boolean invocationWithinJUnitJupiterTest;
	private final String methodName;
	private final ITypeBinding[] declaredParameterTypes;

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

	private JUnit4AssertMethodInvocationData(MethodInvocation methodInvocation, IMethodBinding methodBinding) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodBinding.getName();
		this.declaredParameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		this.invocationWithinJUnitJupiterTest = isInvocationWithinJUnitJupiterTest(methodInvocation);
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public String getMethodName() {
		return methodName;
	}

	public ITypeBinding[] getDeclaredParameterTypes() {
		return declaredParameterTypes;
	}

	public boolean isInvocationWithinJUnitJupiterTest() {
		return invocationWithinJUnitJupiterTest;
	}
}
