package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class analyzing a {@link MethodInvocation}-node. If the
 * {@link MethodInvocation} represents the invocation of one of the supported
 * methods of the class {@code org.junit.Assert} or {@code org.junit.Assume},
 * then all necessary informations for a possible transformation are collected
 * in an instance of {@link JUnit4MethodInvocationAnalysisResult}.
 * 
 * @since 3.28.0
 *
 */
class JUnit4MethodInvocationAnalyzer {
	static final String ASSERT_THROWS = "assertThrows"; //$NON-NLS-1$

	private final JUnitJupiterTestMethodsStore jUnitJupiterTestMethodsStore;
	private final Predicate<IMethodBinding> supportedJUnit4MethodPredicate;

	JUnit4MethodInvocationAnalyzer(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate) {
		jUnitJupiterTestMethodsStore = new JUnitJupiterTestMethodsStore(compilationUnit);
		this.supportedJUnit4MethodPredicate = supportedJUnit4MethodPredicate;
	}

	List<JUnit4MethodInvocationAnalysisResult> collectJUnit4AssertionAnalysisResults(
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

	private Optional<JUnit4MethodInvocationAnalysisResult> findAnalysisResult(
			MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding != null && supportedJUnit4MethodPredicate.test(methodBinding)) {
			return Optional.of(createAnalysisResult(methodInvocation, methodBinding));
		}
		return Optional.empty();
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

	private JUnit4MethodInvocationAnalysisResult createAnalysisResult(
			MethodInvocation methodInvocation, IMethodBinding methodBinding) {

		MethodDeclaration surroundingJUnitJupiterTest = jUnitJupiterTestMethodsStore
			.findSurroundingJUnitJupiterTest(methodInvocation)
			.orElse(null);
		if (surroundingJUnitJupiterTest == null) {
			return createNotTransformableResult(methodInvocation, methodBinding);
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		boolean unambiguousArgumentTypes = arguments
			.stream()
			.allMatch(this::isArgumentWithUnambiguousType);
		if (!unambiguousArgumentTypes) {
			return createNotTransformableResult(methodInvocation, methodBinding);
		}

		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();
		Type throwingRunnableTypeToReplace;
		if (methodIdentifier.equals(ASSERT_THROWS)) {
			ThrowingRunnableArgumentAnalyzer throwingRunnableArgumentAnalyser = new ThrowingRunnableArgumentAnalyzer();
			if (!throwingRunnableArgumentAnalyser.analyze(surroundingJUnitJupiterTest, arguments)) {
				return createNotTransformableResult(methodInvocation, methodBinding);
			}
			throwingRunnableTypeToReplace = throwingRunnableArgumentAnalyser.getLocalVariableTypeToReplace()
				.orElse(null);
		} else {
			throwingRunnableTypeToReplace = null;
		}

		if (throwingRunnableTypeToReplace != null) {
			return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding,
					throwingRunnableTypeToReplace, true);
		}
		return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, true);
	}

	private JUnit4MethodInvocationAnalysisResult createNotTransformableResult(
			MethodInvocation methodInvocation, IMethodBinding methodBinding) {
		boolean transformableInvocation = false;
		return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding,
				transformableInvocation);
	}
}
