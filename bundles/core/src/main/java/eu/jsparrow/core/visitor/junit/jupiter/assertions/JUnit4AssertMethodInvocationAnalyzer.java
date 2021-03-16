package eu.jsparrow.core.visitor.junit.jupiter.assertions;

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
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class analyzing a {@link MethodInvocation}-node . If the
 * {@link MethodInvocation} represents the invocation of one of the supported
 * methods of the class {@code org.junit.Assert}, then all necessary
 * informations for a possible transformation are collected in an instance of
 * {@link JUnit4AssertMethodInvocationAnalysisResult}.
 * 
 * @since 3.28.0
 *
 */
class JUnit4AssertMethodInvocationAnalyzer {
	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test"; //$NON-NLS-1$

	Optional<JUnit4AssertMethodInvocationAnalysisResult> findAnalysisResult(
			MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (!isSupportedJUnit4AssertMethod(methodBinding)) {
			return Optional.empty();
		}
		boolean invocationAbleToBeTransformed = canTransformInvocation(methodInvocation);

		ITypeBinding[] declaredParameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		boolean messageAsFirstParameter = declaredParameterTypes.length > 0
				&& isParameterTypeString(declaredParameterTypes[0]);

		String methodName = methodBinding.getName();
		if (isDeprecatedAssertEqualsComparingObjectArrays(methodName, declaredParameterTypes)) {
			return Optional.of(new JUnit4AssertMethodInvocationAnalysisResult(methodInvocation,
					"assertArrayEquals", messageAsFirstParameter, invocationAbleToBeTransformed)); //$NON-NLS-1$
		} else {
			return Optional.of(new JUnit4AssertMethodInvocationAnalysisResult(methodInvocation,
					messageAsFirstParameter, invocationAbleToBeTransformed));
		}
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

	private boolean isInvocationWithinJUnitJupiterTest(MethodInvocation methodInvocation) {
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

	private boolean isDeprecatedAssertEqualsComparingObjectArrays(String methodName,
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

	private boolean isParameterTypeObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray()) {
			return parameterType.getComponentType()
				.getQualifiedName()
				.equals("java.lang.Object") && parameterType.getDimensions() == 1; //$NON-NLS-1$
		}
		return false;
	}

	private boolean isParameterTypeString(ITypeBinding parameterType) {
		return parameterType.getQualifiedName()
			.equals("java.lang.String"); //$NON-NLS-1$
	}

	private boolean isArgumentWithUnambiguousType(Expression expression) {
		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			List<Type> typeArguments = ASTNodeUtil.convertToTypedList(methodInvocation.typeArguments(), Type.class);
			return !(methodInvocation.resolveMethodBinding()
				.isParameterizedMethod() && typeArguments.isEmpty());
		}
		if (expression.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) expression;
			List<Type> typeArguments = ASTNodeUtil.convertToTypedList(superMethodInvocation.typeArguments(),
					Type.class);
			return !(superMethodInvocation.resolveMethodBinding()
				.isParameterizedMethod() && typeArguments.isEmpty());
		}
		return true;
	}

	private boolean canTransformInvocation(MethodInvocation methodInvocation) {
		if (!isInvocationWithinJUnitJupiterTest(methodInvocation)) {
			return false;
		}
		return ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.stream()
			.allMatch(this::isArgumentWithUnambiguousType);
	}
}
