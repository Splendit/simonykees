package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
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
		List<JUnit4AssertThrowsInvocationAnalysisResult> assertThrowsInvocationAnalysisResults = new ArrayList<>();
		List<JUnit4AssumeNotNullInvocationAnalysisResult> assumeNotNullInvocationAnalysisResults = new ArrayList<>();

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		invocationCollectorVisitor.getMethodInvocations()
			.forEach(methodInvocation -> {
				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
				if (methodBinding != null && supportedJUnit4MethodPredicate.test(methodBinding)) {
					String methodIdentifier = methodInvocation.getName()
						.getIdentifier();
					if (methodIdentifier.equals("assertThrows")) { //$NON-NLS-1$
						assertThrowsInvocationAnalysisResults
							.add(createAssertThrowsInvocationData(methodInvocation, methodBinding));
					} else if (methodIdentifier.equals("assumeNotNull")) { //$NON-NLS-1$
						assumeNotNullInvocationAnalysisResults
							.add(createAssumeNotNullInvocationAnalysisResult(methodInvocation));
					} else {
						methodInvocationAnalysisResults.add(createAnalysisResult(methodInvocation, methodBinding));
					}
				}
			});

		List<MethodInvocation> notTransformedMethodInvocations = collectNotTransformedResults(
				methodInvocationAnalysisResults, assertThrowsInvocationAnalysisResults,
				assumeNotNullInvocationAnalysisResults);

		return new JUnit4MethodInvocationAnalysisResultStore(methodInvocationAnalysisResults,
				assertThrowsInvocationAnalysisResults, assumeNotNullInvocationAnalysisResults,
				notTransformedMethodInvocations);
	}

	private List<MethodInvocation> collectNotTransformedResults(
			List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults,
			List<JUnit4AssertThrowsInvocationAnalysisResult> assertThrowsInvocationAnalysisResults,
			List<JUnit4AssumeNotNullInvocationAnalysisResult> assumeNotNullInvocationAnalysisResults) {
		List<MethodInvocation> notTransformedMethodInvocations = new ArrayList<>();
		methodInvocationAnalysisResults.stream()
			.filter(result -> !result.isTransformable())
			.map(JUnit4MethodInvocationAnalysisResult::getMethodInvocation)
			.forEach(notTransformedMethodInvocations::add);

		assertThrowsInvocationAnalysisResults.stream()
			.map(JUnit4AssertThrowsInvocationAnalysisResult::getJUnit4InvocationData)
			.filter(result -> !result.isTransformable())
			.map(JUnit4MethodInvocationAnalysisResult::getMethodInvocation)
			.forEach(notTransformedMethodInvocations::add);

		assumeNotNullInvocationAnalysisResults.stream()
			.filter(result -> !result.isTransformable())
			.map(JUnit4AssumeNotNullInvocationAnalysisResult::getMethodInvocation)
			.forEach(notTransformedMethodInvocations::add);
		return notTransformedMethodInvocations;
	}

	private JUnit4AssertThrowsInvocationAnalysisResult createAssertThrowsInvocationData(
			MethodInvocation methodInvocation,
			IMethodBinding methodBinding) {

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		ThrowingRunnableArgumentAnalyzer throwingRunnableArgumentAnalyser = new ThrowingRunnableArgumentAnalyzer();
		boolean transformable = jUnitJupiterTestMethodsStore.isSurroundedWithJUnitJupiterTest(methodInvocation)
				&& arguments
					.stream()
					.allMatch(this::isArgumentWithUnambiguousType)
				&& throwingRunnableArgumentAnalyser.analyze(arguments);

		JUnit4MethodInvocationAnalysisResult jUnit4InvocationData = new JUnit4MethodInvocationAnalysisResult(
				methodInvocation, methodBinding, arguments, transformable);

		Type throwingRunnableTypeToReplace = transformable
				? throwingRunnableArgumentAnalyser.getLocalVariableTypeToReplace()
					.orElse(null)
				: null;

		if (throwingRunnableTypeToReplace != null) {
			return new JUnit4AssertThrowsInvocationAnalysisResult(jUnit4InvocationData,
					throwingRunnableTypeToReplace);
		}

		return new JUnit4AssertThrowsInvocationAnalysisResult(
				new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments, transformable));
	}

	private JUnit4AssumeNotNullInvocationAnalysisResult createAssumeNotNullInvocationAnalysisResult(
			MethodInvocation methodInvocation) {

		if (!jUnitJupiterTestMethodsStore.isSurroundedWithJUnitJupiterTest(methodInvocation)) {
			return new JUnit4AssumeNotNullInvocationAnalysisResult(methodInvocation, false);
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		boolean argumentsUnambiguous = arguments
			.stream()
			.allMatch(this::isArgumentWithUnambiguousType);
		if (!argumentsUnambiguous) {
			return new JUnit4AssumeNotNullInvocationAnalysisResult(methodInvocation, false);
		}

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return new JUnit4AssumeNotNullInvocationAnalysisResult(methodInvocation, false);
		}

		ExpressionStatement methodInvocationStatement = (ExpressionStatement) methodInvocation.getParent();
		if (methodInvocationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return new JUnit4AssumeNotNullInvocationAnalysisResult(methodInvocation, false);
		}

		Block block = (Block) methodInvocationStatement.getParent();

		return new JUnit4AssumeNotNullInvocationAnalysisResult(methodInvocation, methodInvocationStatement, block,
				true);
	}

	private JUnit4MethodInvocationAnalysisResult createAnalysisResult(MethodInvocation methodInvocation,
			IMethodBinding methodBinding) {
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		boolean transformable = jUnitJupiterTestMethodsStore.isSurroundedWithJUnitJupiterTest(methodInvocation)
				&& arguments
					.stream()
					.allMatch(this::isArgumentWithUnambiguousType);
		return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments,
				transformable);
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
}