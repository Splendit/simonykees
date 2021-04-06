package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;
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
	static final String ASSERT_THROWS = "assertThrows"; //$NON-NLS-1$

	private final JUnitJupiterTestMethodsStore jUnitJupiterTestMethodsStore;

	JUnit4AssertMethodInvocationAnalyzer(CompilationUnit compilationUnit) {
		jUnitJupiterTestMethodsStore = new JUnitJupiterTestMethodsStore(compilationUnit);
	}

	List<JUnit4AssertMethodInvocationAnalysisResult> collectJUnit4AssertionAnalysisResults(
			CompilationUnit compilationUnit) {

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		List<MethodInvocation> allMethodInvocations = invocationCollectorVisitor.getMethodInvocations();

		return allMethodInvocations
			.stream()
			.map(this::findAnalysisResult)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	private Optional<JUnit4AssertMethodInvocationAnalysisResult> findAnalysisResult(
			MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding != null && isSupportedJUnit4AssertMethod(methodBinding)) {
			return Optional.of(createAnalysisResult(methodInvocation, methodBinding));
		}
		return Optional.empty();
	}

	boolean isSupportedJUnit4AssertMethod(IMethodBinding methodBinding) {
		return isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assert") //$NON-NLS-1$
				&& !methodBinding.getName()
					.equals("assertThat"); //$NON-NLS-1$
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
		if (parameterType.isArray() && parameterType.getDimensions() == 1) {
			return isContentOfType(parameterType.getComponentType(), "java.lang.Object"); //$NON-NLS-1$
		}
		return false;
	}

	private boolean isParameterTypeString(ITypeBinding parameterType) {
		return isContentOfType(parameterType, "java.lang.String"); //$NON-NLS-1$
	}

	private boolean isArgumentWithUnambiguousType(Expression expression) {
		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			return methodBinding != null && !(methodBinding.isParameterizedMethod() && methodInvocation.typeArguments()
				.isEmpty());
		}
		if (expression.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) expression;
			IMethodBinding superMethodBinding = superMethodInvocation.resolveMethodBinding();
			return superMethodBinding != null
					&& !(superMethodBinding.isParameterizedMethod() && superMethodInvocation.typeArguments()
						.isEmpty());
		}
		return true;
	}

	private JUnit4AssertMethodInvocationAnalysisResult createAnalysisResult(
			MethodInvocation methodInvocation, IMethodBinding methodBinding) {

		MethodDeclaration surroundingJUnitJupiterTest = jUnitJupiterTestMethodsStore
			.findSurroundingJUnitJupiterTest(methodInvocation)
			.orElse(null);
		if (surroundingJUnitJupiterTest == null) {
			return createNotTransformableResult(methodInvocation);
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		boolean unambiguousArgumentTypes = arguments
			.stream()
			.allMatch(this::isArgumentWithUnambiguousType);
		if (!unambiguousArgumentTypes) {
			return createNotTransformableResult(methodInvocation);
		}

		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();
		Type throwingRunnableTypeToReplace;
		if (methodIdentifier.equals(ASSERT_THROWS)) {
			ThrowingRunnableArgumentAnalyzer throwingRunnableArgumentAnalyser = new ThrowingRunnableArgumentAnalyzer();
			if (!throwingRunnableArgumentAnalyser.analyze(surroundingJUnitJupiterTest, arguments)) {
				return createNotTransformableResult(methodInvocation);
			}
			throwingRunnableTypeToReplace = throwingRunnableArgumentAnalyser.getLocalVariableTypeToReplace()
				.orElse(null);
		} else {
			throwingRunnableTypeToReplace = null;
		}

		ITypeBinding[] declaredParameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		boolean messageMovingToLastPosition = declaredParameterTypes.length > 0
				&& isParameterTypeString(declaredParameterTypes[0]);

		String methodName = methodBinding.getName();
		String deprecatedMethodNameReplacement = null;
		if (isDeprecatedAssertEqualsComparingObjectArrays(methodName, declaredParameterTypes)) {
			deprecatedMethodNameReplacement = "assertArrayEquals"; //$NON-NLS-1$
		}

		return createTransformableResult(methodInvocation, throwingRunnableTypeToReplace, messageMovingToLastPosition,
				deprecatedMethodNameReplacement);
	}

	private JUnit4AssertMethodInvocationAnalysisResult createTransformableResult(MethodInvocation methodInvocation,
			Type throwingRunnableTypeToReplace, boolean messageMovingToLastPosition,
			String deprecatedMethodNameReplacement) {
		if (throwingRunnableTypeToReplace != null) {
			return new JUnit4AssertMethodInvocationAnalysisResult(methodInvocation,
					messageMovingToLastPosition, true, throwingRunnableTypeToReplace);
		} else if (deprecatedMethodNameReplacement != null) {
			return new JUnit4AssertMethodInvocationAnalysisResult(methodInvocation,
					messageMovingToLastPosition, true, deprecatedMethodNameReplacement);
		} else {
			return (new JUnit4AssertMethodInvocationAnalysisResult(methodInvocation,
					messageMovingToLastPosition, true));
		}
	}

	private JUnit4AssertMethodInvocationAnalysisResult createNotTransformableResult(
			MethodInvocation methodInvocation) {
		return new JUnit4AssertMethodInvocationAnalysisResult(methodInvocation, false, false);
	}
}
