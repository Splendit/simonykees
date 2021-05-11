package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;

/**
 * Helper class analyzing a {@link MethodInvocation}-node. If the
 * {@link MethodInvocation} represents the invocation of one of the supported
 * methods of the class {@code org.junit.Assert} or {@code org.junit.Assume},
 * then all necessary informations are stored in a corresponding wrapper object.
 * 
 * @since 3.28.0
 *
 */
class JUnit4MethodInvocationAnalyzer {
	private final JUnitJupiterTestMethodsStore jUnitJupiterTestMethodsStore;

	JUnit4MethodInvocationAnalyzer(CompilationUnit compilationUnit) {
		this.jUnitJupiterTestMethodsStore = new JUnitJupiterTestMethodsStore(compilationUnit);
	}

	Optional<JUnit4MethodInvocationAnalysisResult> analyzeAssertionToJupiter(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, List<Expression> arguments) {
		if (!supportTransformation(methodInvocation, arguments)) {
			return Optional.empty();
		}
		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();
		if (methodIdentifier.equals("assertThrows")) { //$NON-NLS-1$
			return createAssertThrowsInvocationData(methodInvocation, methodBinding, arguments);
		}
		return Optional.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
	}

	Optional<JUnit4MethodInvocationAnalysisResult> analyzeAssumptionToHamcrest(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, List<Expression> arguments) {
		if (!supportTransformation(methodInvocation, arguments)) {
			return Optional.empty();
		}
		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();
		if (methodIdentifier.equals("assumeNotNull")) { //$NON-NLS-1$
			return createAssumeNotNullInvocationAnalysisResult(methodInvocation, methodBinding,
					arguments);
		}
		return Optional.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
	}

	Optional<JUnit4MethodInvocationAnalysisResult> analyzeAssumptionToJupiter(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, List<Expression> arguments) {
		if (!supportTransformation(methodInvocation, arguments)) {
			return Optional.empty();
		}
		return Optional.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
	}

	private Optional<JUnit4MethodInvocationAnalysisResult> createAssertThrowsInvocationData(
			MethodInvocation methodInvocation, IMethodBinding methodBinding, List<Expression> arguments) {

		ThrowingRunnableArgumentAnalyzer throwingRunnableArgumentAnalyser = new ThrowingRunnableArgumentAnalyzer();
		boolean transformationSupported = supportTransformation(methodInvocation, arguments)
				&& throwingRunnableArgumentAnalyser.analyze(arguments);
		if (!transformationSupported) {
			return Optional.empty();
		}

		Type throwingRunnableTypeToReplace = throwingRunnableArgumentAnalyser.getLocalVariableTypeToReplace()
			.orElse(null);

		if (throwingRunnableTypeToReplace != null) {
			return Optional.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments,
					throwingRunnableTypeToReplace));
		}
		return Optional.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
	}

	private Optional<JUnit4MethodInvocationAnalysisResult> createAssumeNotNullInvocationAnalysisResult(
			MethodInvocation methodInvocation, IMethodBinding methodBinding, List<Expression> arguments) {

		if (supportTransformation(methodInvocation, arguments)) {
			if (arguments.size() == 1) {
				Expression onlyOneArgument = arguments.get(0);
				if (onlyOneArgument.getNodeType() == ASTNode.ARRAY_CREATION || !onlyOneArgument.resolveTypeBinding()
					.isArray()) {
					return Optional
						.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
				}
				AssumptionThatEveryItemNotNull assumptionThatEveryItemNotNull = findAssumptionThatEveryItemNotNull(
						methodInvocation, onlyOneArgument).orElse(null);
				if (assumptionThatEveryItemNotNull != null) {
					return Optional
						.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments,
								assumptionThatEveryItemNotNull));
				}
			} else {
				return Optional
					.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
			}
		}
		return Optional.empty();
	}

	private Optional<AssumptionThatEveryItemNotNull> findAssumptionThatEveryItemNotNull(
			MethodInvocation methodInvocation, Expression arrayArgument) {
		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		ExpressionStatement methodInvocationStatement = (ExpressionStatement) methodInvocation.getParent();
		if (methodInvocationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}

		Block block = (Block) methodInvocationStatement.getParent();
		return Optional
			.of(new AssumptionThatEveryItemNotNull(arrayArgument, methodInvocationStatement, block));

	}

	boolean supportTransformation(MethodInvocation methodInvocation, List<Expression> arguments) {
		return jUnitJupiterTestMethodsStore.isSurroundedWithJUnitJupiterTest(methodInvocation)
				&& arguments
					.stream()
					.allMatch(this::isArgumentWithUnambiguousType);
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

	static boolean isDeprecatedAssertEqualsComparingObjectArrays(String methodName,
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

	static boolean isParameterTypeString(ITypeBinding parameterType) {
		return isContentOfType(parameterType, "java.lang.String"); //$NON-NLS-1$
	}

	static boolean isParameterTypeObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray() && parameterType.getDimensions() == 1) {
			return isContentOfType(parameterType.getComponentType(), "java.lang.Object"); //$NON-NLS-1$
		}
		return false;
	}
}