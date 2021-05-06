package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

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
	private final CompilationUnit compilationUnit;
	private final JUnitJupiterTestMethodsStore jUnitJupiterTestMethodsStore;
	private final Predicate<IMethodBinding> supportedJUnit4MethodPredicate;

	JUnit4MethodInvocationAnalyzer(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate) {
		this.compilationUnit = compilationUnit;
		this.jUnitJupiterTestMethodsStore = new JUnitJupiterTestMethodsStore(compilationUnit);
		this.supportedJUnit4MethodPredicate = supportedJUnit4MethodPredicate;
	}

	JUnit4MethodInvocationAnalysisResultStore collectAnalysisResults() {

		List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults = new ArrayList<>();

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		invocationCollectorVisitor.getMethodInvocations()
			.forEach(methodInvocation -> {
				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
				List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
						Expression.class);
				if (methodBinding != null && supportedJUnit4MethodPredicate.test(methodBinding)) {
					String methodIdentifier = methodInvocation.getName()
						.getIdentifier();
					JUnit4MethodInvocationAnalysisResult result;
					if (methodIdentifier.equals("assertThrows")) { //$NON-NLS-1$
						result = createAssertThrowsInvocationData(methodInvocation, methodBinding, arguments);
					} else if (methodIdentifier.equals("assumeNotNull")) { //$NON-NLS-1$
						result = createAssumeNotNullInvocationAnalysisResult(methodInvocation, methodBinding,
								arguments);
					} else {
						result = new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments,
								supportTransformation(methodInvocation, arguments));
					}
					methodInvocationAnalysisResults.add(result);
				}
			});

		List<MethodInvocation> notTransformedMethodInvocations = methodInvocationAnalysisResults.stream()
			.filter(result -> !result.isTransformable())
			.map(JUnit4MethodInvocationAnalysisResult::getMethodInvocation)
			.collect(Collectors.toList());

		return new JUnit4MethodInvocationAnalysisResultStore(methodInvocationAnalysisResults,
				notTransformedMethodInvocations);
	}

	private JUnit4MethodInvocationAnalysisResult createAssertThrowsInvocationData(
			MethodInvocation methodInvocation, IMethodBinding methodBinding, List<Expression> arguments) {

		ThrowingRunnableArgumentAnalyzer throwingRunnableArgumentAnalyser = new ThrowingRunnableArgumentAnalyzer();
		boolean transformationSupported = supportTransformation(methodInvocation, arguments)
				&& throwingRunnableArgumentAnalyser.analyze(arguments);

		Type throwingRunnableTypeToReplace = throwingRunnableArgumentAnalyser.getLocalVariableTypeToReplace()
			.orElse(null);

		if (transformationSupported && throwingRunnableTypeToReplace != null) {
			return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments,
					throwingRunnableTypeToReplace);
		}
		return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments,
				transformationSupported);
	}

	private JUnit4MethodInvocationAnalysisResult createAssumeNotNullInvocationAnalysisResult(
			MethodInvocation methodInvocation, IMethodBinding methodBinding, List<Expression> arguments) {

		if (supportTransformation(methodInvocation, arguments)) {
			if (arguments.size() == 1) {
				Expression onlyOneArgument = arguments.get(0);
				if (onlyOneArgument.getNodeType() == ASTNode.ARRAY_CREATION || !onlyOneArgument.resolveTypeBinding()
					.isArray()) {
					return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments, true);
				}
				AssertThatEveryItemNotNullAnalysisResult assumeNotNullInvocationAncestors = findAssumeNotNullInvocationAncestors(
						methodInvocation, onlyOneArgument).orElse(null);
				if (assumeNotNullInvocationAncestors != null) {
					return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments,
							assumeNotNullInvocationAncestors);
				}
			} else {
				return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments, true);
			}
		}
		return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments, false);
	}

	private Optional<AssertThatEveryItemNotNullAnalysisResult> findAssumeNotNullInvocationAncestors(
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
			.of(new AssertThatEveryItemNotNullAnalysisResult(arrayArgument, methodInvocationStatement, block));

	}

	private boolean supportTransformation(MethodInvocation methodInvocation, List<Expression> arguments) {
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